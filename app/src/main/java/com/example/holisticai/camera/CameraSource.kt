/* Copyright 2021 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================
*/

package com.example.holisticai.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.RectF
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.SurfaceView
import kotlinx.coroutines.suspendCancellableCoroutine
import com.example.holisticai.VisualizationUtils
import com.example.holisticai.YuvToRgbConverter
import com.example.holisticai.data.Person
import com.example.holisticai.ml.MoveNetMultiPose
import com.example.holisticai.ml.PoseClassifier
import com.example.holisticai.ml.PoseDetector
import com.example.holisticai.ml.TrackerType
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


class CameraSource(
    private val surfaceView: SurfaceView,
    private val listener: CameraSourceListener? = null
) {

    companion object {
        private const val PREVIEW_WIDTH = 640
        private const val PREVIEW_HEIGHT = 480

        /** Threshold for confidence score. */
        private const val MIN_CONFIDENCE = .2f
        private const val TAG = "Camera Source"
    }

    private val lock = Any()
    private var detector: PoseDetector? = null
    private var classifier: PoseClassifier? = null
    private var isTrackerEnabled = false
    private var yuvConverter: YuvToRgbConverter = YuvToRgbConverter(surfaceView.context)
    private lateinit var imageBitmap: Bitmap

    /** Frame count that have been processed so far in an one second interval to calculate FPS. */
    private var fpsTimer: Timer? = null
    private var frameProcessedInOneSecondInterval = 0
    private var framesPerSecond = 0

    /** Detects, characterizes, and connects to a CameraDevice (used for all camera operations) */
    private val cameraManager: CameraManager by lazy {
        val context = surfaceView.context
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    /** Readers used as buffers for camera still shots */
    private var imageReader: ImageReader? = null

    /** The [CameraDevice] that will be opened in this fragment */
    private var camera: CameraDevice? = null

    /** Internal reference to the ongoing [CameraCaptureSession] configured with our parameters */
    private var session: CameraCaptureSession? = null

    /** [HandlerThread] where all buffer reading operations run */
    private var imageReaderThread: HandlerThread? = null

    /** [Handler] corresponding to [imageReaderThread] */
    private var imageReaderHandler: Handler? = null
    private var cameraId: String = ""

    suspend fun initCamera() {
        camera = openCamera(cameraManager, cameraId)
        imageReader = ImageReader.newInstance(PREVIEW_WIDTH, PREVIEW_HEIGHT, ImageFormat.YUV_420_888, 3)
        imageReader?.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            if (image != null) {
                if (!::imageBitmap.isInitialized) {
                    imageBitmap = Bitmap.createBitmap(PREVIEW_WIDTH, PREVIEW_HEIGHT, Bitmap.Config.ARGB_8888)
                }
                yuvConverter.yuvToRgb(image, imageBitmap)

                // Apply rotation for portrait display
                val rotateMatrix = Matrix()
                rotateMatrix.postRotate(270.0f) // Rotate image 270 degrees for portrait mode

                val rotatedBitmap = Bitmap.createBitmap(
                    imageBitmap, 0, 0, PREVIEW_WIDTH, PREVIEW_HEIGHT,
                    rotateMatrix, false
                )
                processImage(rotatedBitmap)
                image.close()
            }
        }, imageReaderHandler)

        imageReader?.surface?.let { surface ->
            session = createSession(listOf(surface))
            val cameraRequest = camera?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)?.apply {
                addTarget(surface)
            }
            cameraRequest?.build()?.let {
                session?.setRepeatingRequest(it, null, null)
            }
        }
    }

    private suspend fun createSession(targets: List<Surface>): CameraCaptureSession =
        suspendCancellableCoroutine { cont ->
            camera?.createCaptureSession(targets, object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(captureSession: CameraCaptureSession) =
                    cont.resume(captureSession)

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    cont.resumeWithException(Exception("Session error"))
                }
            }, null)
        }

    @SuppressLint("MissingPermission")
    private suspend fun openCamera(manager: CameraManager, cameraId: String): CameraDevice =
        suspendCancellableCoroutine { cont ->
            manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) = cont.resume(camera)

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    if (cont.isActive) cont.resumeWithException(Exception("Camera error"))
                }
            }, imageReaderHandler)
        }


    fun prepareCamera() {
        for (cameraId in cameraManager.cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)

            // Use front-facing camera
            val cameraDirection = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (cameraDirection != null && cameraDirection == CameraCharacteristics.LENS_FACING_FRONT) {
                this.cameraId = cameraId
                break
            }
        }
    }


    fun setDetector(detector: PoseDetector) {
        synchronized(lock) {
            if (this.detector != null) {
                this.detector?.close()
                this.detector = null
            }
            this.detector = detector
        }
    }

    fun setClassifier(classifier: PoseClassifier?) {
        synchronized(lock) {
            if (this.classifier != null) {
                this.classifier?.close()
                this.classifier = null
            }
            this.classifier = classifier
        }
    }

    /**
     * Set Tracker for Movenet MuiltiPose model.
     */
    fun setTracker(trackerType: TrackerType) {
        isTrackerEnabled = trackerType != TrackerType.OFF
        (this.detector as? MoveNetMultiPose)?.setTracker(trackerType)
    }

    fun resume() {
        imageReaderThread = HandlerThread("imageReaderThread").apply { start() }
        imageReaderHandler = Handler(imageReaderThread!!.looper)
        fpsTimer = Timer()
        fpsTimer?.scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    framesPerSecond = frameProcessedInOneSecondInterval
                    frameProcessedInOneSecondInterval = 0
                }
            },
            0,
            1000
        )
    }

    fun close() {
        session?.close()
        session = null
        camera?.close()
        camera = null
        imageReader?.close()
        imageReader = null
        stopImageReaderThread()
        detector?.close()
        detector = null
        classifier?.close()
        classifier = null
        fpsTimer?.cancel()
        fpsTimer = null
        frameProcessedInOneSecondInterval = 0
        framesPerSecond = 0
    }

    // process image
    private fun processImage(bitmap: Bitmap) {
        val persons = mutableListOf<Person>()
        var classificationResult: List<Pair<String, Float>>? = null

        synchronized(lock) {
            detector?.estimatePoses(bitmap)?.let {
                persons.addAll(it)

                // if the model only returns one item, allow running the Pose classifier.
                if (persons.isNotEmpty()) {
                    classifier?.run {
                        classificationResult = classify(persons[0])
                    }
                }
            }
        }
        frameProcessedInOneSecondInterval++
        if (frameProcessedInOneSecondInterval == 1) {
            // send fps to view
            listener?.onFPSListener(framesPerSecond)
        }

        // if the model returns only one item, show that item's score.
        if (persons.isNotEmpty()) {
            listener?.onDetectedInfo(persons[0].score, classificationResult)
        }
        visualize(persons, bitmap)
    }

    private fun visualize(persons: List<Person>, bitmap: Bitmap) {
        val outputBitmap = VisualizationUtils.drawBodyKeypoints(
            bitmap,
            persons.filter { it.score > MIN_CONFIDENCE },
            isTrackerEnabled
        )

        val flippedBitmap = flipBitmap(outputBitmap) // Apply horizontal flip transformation

        val holder = surfaceView.holder
        val surfaceCanvas = holder.lockCanvas()
        surfaceCanvas?.let { canvas ->
            val canvasWidth = canvas.width.toFloat()
            val canvasHeight = canvas.height.toFloat()

            val outputWidth = flippedBitmap.width.toFloat()
            val outputHeight = flippedBitmap.height.toFloat()

            val aspectRatio = outputWidth / outputHeight

            val targetWidth: Float
            val targetHeight: Float
            val left: Float
            val top: Float

            if (canvasWidth / canvasHeight > aspectRatio) {
                targetWidth = canvasHeight * aspectRatio
                targetHeight = canvasHeight
                left = (canvasWidth - targetWidth) / 2
                top = 0f
            } else {
                targetWidth = canvasWidth
                targetHeight = canvasWidth / aspectRatio
                left = 0f
                top = (canvasHeight - targetHeight) / 2
            }

            // Render the flipped bitmap on the canvas with adjusted dimensions
            val targetRect = RectF(left, top, left + targetWidth, top + targetHeight)
            canvas.drawBitmap(flippedBitmap, null, targetRect, null)
            surfaceView.holder.unlockCanvasAndPost(canvas)
        }
    }


    private fun flipBitmap(bitmap: Bitmap): Bitmap {
        val matrix = Matrix().apply {
            postScale(-1f, 1f) // Apply horizontal flip transformation
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun stopImageReaderThread() {
        imageReaderThread?.quitSafely()
        try {
            imageReaderThread?.join()
            imageReaderThread = null
            imageReaderHandler = null
        } catch (e: InterruptedException) {
            Log.d(TAG, e.message.toString())
        }
    }

    interface CameraSourceListener {


        fun onFPSListener(fps: Int)

        fun onDetectedInfo(personScore: Float?, poseLabels: List<Pair<String, Float>>?)
    }
}