package com.example.holisticai;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class sessionView extends AppCompatActivity implements SurfaceHolder.Callback
{
    public static final int REQUEST_CAMERA = 100;
    List<String>ex;

    private NcnnBodypose ncnnbodypose = new NcnnBodypose();
    private int facing = 0;

    private Spinner spinnerModel;
    private Spinner spinnerCPUGPU;
    private int current_model = 0;
    private int current_cpugpu = 0;

    private SurfaceView cameraView;
    //get input form nandet.cpp

   /* private List<String> getLogMessages() {
        List<String> logMessages = new ArrayList<>();

        File logFile = new File("/assets/Log.txt");
        if (logFile.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(logFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    logMessages.add(line);
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return logMessages;
    }*/

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_view);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        cameraView = (SurfaceView) findViewById(R.id.cameraview);

        cameraView.getHolder().setFormat(PixelFormat.RGBA_8888);
        cameraView.getHolder().addCallback(this);

        Button buttonSwitchCamera = (Button) findViewById(R.id.buttonSwitchCamera);
        buttonSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                int new_facing = 1 - facing;

                ncnnbodypose.closeCamera();

                ncnnbodypose.openCamera(new_facing);

                facing = new_facing;
            }
        });

        spinnerModel = (Spinner) findViewById(R.id.spinnerModel);
        spinnerModel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id)
            {
                if (position != current_model)
                {
                    current_model = position;
                    reload();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
            }
        });

        spinnerCPUGPU = (Spinner) findViewById(R.id.spinnerCPUGPU);
        spinnerCPUGPU.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id)
            {
                if (position != current_cpugpu)
                {
                    current_cpugpu = position;
                    reload();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
            }
        });

        reload();

    }

    private void reload()
    {
        boolean ret_init = ncnnbodypose.loadModel(getAssets(), current_model, current_cpugpu);

        if (!ret_init)
        {
            Log.e("MainActivity", "ncnnbodypose loadModel failed");
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        ncnnbodypose.setOutputWindow(holder.getSurface());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }


      /*  ex =  getLogMessages();*//*
        Log.e("Hii iam getting logs",""+ex);*/
        ncnnbodypose.openCamera(facing);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        ncnnbodypose.closeCamera();
    }
}
