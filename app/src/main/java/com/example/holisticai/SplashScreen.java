package com.example.holisticai;

import static com.example.holisticai.Login.UserEmail;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class SplashScreen extends AppCompatActivity {
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        session = new SessionManager(getApplicationContext());
        Thread thread =new Thread(){
            @Override
            public void run() {

                try {
                    sleep(2000);

                } catch (Exception e) {
                    e.printStackTrace();

                } finally {
                   /* Intent intent = new Intent(SplashScreen.this, Login.class);
                    startActivity(intent);
                    finish();*/


                    // Check if the user is already logged in
                    if (session.isLoggedIn()) {
                        redirectToHome();
                    } else {
                        redirectToLogin();
                    }
                }


            }

        };thread.start();
    }
    private void redirectToHome() {
        Intent intent = new Intent(SplashScreen.this, MainActivity.class);

        startActivity(intent);
        finish();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(SplashScreen.this, Login.class);
        startActivity(intent);
        finish();
    }
}