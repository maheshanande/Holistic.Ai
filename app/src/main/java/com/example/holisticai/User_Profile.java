package com.example.holisticai;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class User_Profile extends AppCompatActivity {
    private SessionManager session;

    Button logout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        logout = findViewById(R.id.logoutBtn);

        session = new SessionManager(getApplicationContext());

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });
    }
    private void logout() {
        session.logout(); // Clear session data

        // Redirect to the login screen
        Intent intent = new Intent(User_Profile.this, Login.class);
        startActivity(intent);
        finish();
    }
}