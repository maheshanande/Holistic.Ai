package com.example.holisticai;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.time.DayOfWeek;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    private SessionManager session;
    TextView Name,Goal,userProfileView;
    String EmailHolder;
    ImageView startSession;
    String[] Userdata = new String[3];
    SQLiteDatabase sqLiteDatabaseObj;
    SQLiteHelper sqLiteHelper;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userProfileView = findViewById(R.id.ProfileViewBtn);
        Name = (TextView)findViewById(R.id.fnameTextbox);
        startSession =(ImageView)findViewById(R.id.curatedSessionImageView);
        Goal = findViewById(R.id.GoalTextbox);
        session = new SessionManager(getApplicationContext());



        EmailHolder = session.getEmail();//retrive email data
        get_user_name();
        Name.setText(Name.getText().toString()+Userdata[0]);
        if(Userdata[1].equalsIgnoreCase("Fitness")){
            Toast.makeText(this, "Hiiii", Toast.LENGTH_SHORT).show();
            startSession.setImageResource(R.drawable.fitness_icon);
        }
        else{
            startSession.setImageResource(R.drawable.session_img);
        }

        if (Userdata[1].equalsIgnoreCase("Target Disease")){
            Goal.setText(Goal.getText().toString()+Userdata[2]);
        }
        else{
            Goal.setText(Goal.getText().toString()+Userdata[1]);
        }

        //User Profile View Intent
        userProfileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(MainActivity.this,User_Profile.class);
                startActivity(profileIntent);

            }
        });

        startSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sessionView = new Intent(MainActivity.this,sessionView.class);
                sessionView.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(sessionView);

            }
        });



    }
    @SuppressLint("Range")
    void get_user_name(){
        //create an helper for db class
        sqLiteHelper = new SQLiteHelper(this);

        //get a readable database


        sqLiteDatabaseObj = sqLiteHelper.getReadableDatabase();
        // perform query
        Cursor cursor = sqLiteDatabaseObj.query(SQLiteHelper.TABLE_NAME, null, " " + SQLiteHelper.Table_Column_4_Email + "=?", new String[]{EmailHolder}, null, null, null);

        //iterate over the cursor to access the retrived data

        while (cursor.moveToNext()) {
            if (cursor.isFirst()) {
                cursor.moveToFirst();
                // Storing Password associated with entered email.
                Userdata[0] = cursor.getString(cursor.getColumnIndex(SQLiteHelper.Table_Column_1_FName));
                Userdata[1]= cursor.getString(cursor.getColumnIndex(SQLiteHelper.Table_Column_8_Goal));
                Userdata[2]= cursor.getString(cursor.getColumnIndex(SQLiteHelper.Table_Column_9_DiseaseTarget));
                // Closing cursor.
                cursor.close();
            }
        }



    }
}