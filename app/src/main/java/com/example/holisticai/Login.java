package com.example.holisticai;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;


public class Login extends AppCompatActivity {
    // Login activity code goes here
    private SessionManager session;
    EditText password,email;
    String EmailHolder, PasswordHolder;
    AppCompatButton appLoginBtn;
    Boolean EditTextEmptyHolder;
    SQLiteDatabase sqLiteDatabaseObj;
    SQLiteHelper sqLiteHelper;
    Cursor cursor;
    String TempPassword = "NOT_FOUND" ;
    public static final String UserEmail = "";
    ConstraintLayout regbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        appLoginBtn = (AppCompatButton) findViewById(R.id.login_btn);
        regbtn = findViewById(R.id.register_layout);
        email = findViewById(R.id.emailTextbox);
        password = findViewById(R.id.passwordTextBox);
        sqLiteHelper= new SQLiteHelper(this);

        //Session Variable

        session = new SessionManager(getApplicationContext());

        if (session.isLoggedIn()) {
            // User is already logged in, redirect to home activity
            redirectToHome();
        }

        // Get a writable database instance
        android.view.animation.Animation fade_in = AnimationUtils.loadAnimation(this,R.anim.fade_in);
        fade_in.setDuration(1000);
        android.view.animation.Animation bottom_down = AnimationUtils.loadAnimation(this,R.anim.bottom_down);
        LinearLayout toplinearLayout = (LinearLayout) findViewById(R.id.ToplinearLayout);
        toplinearLayout.setAnimation(bottom_down);
        CardView c1 = (CardView) findViewById(R.id.cardView1);
        CardView c2 = (CardView) findViewById(R.id.cardView2);
        CardView c3 = (CardView) findViewById(R.id.cardView3);
        CardView c4 = (CardView) findViewById(R.id.cardView4);
        CardView c5 = (CardView) findViewById(R.id.cardView);
        c1.setAnimation(fade_in);
        c2.setAnimation(fade_in);
        c3.setAnimation(fade_in);
        c4.setAnimation(fade_in);
        c5.setAnimation(fade_in);

        //Login Function


       appLoginBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               CheckEditTextStatus();
               // Calling login method.
               loginFunction();

           }
       });
       regbtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent intent2 = new Intent(Login.this , Register_form.class);
               startActivity(intent2);
           }
       });
    }



    @SuppressLint("Range")
    public void loginFunction() {
        if(EditTextEmptyHolder) {
            // Opening SQLite database write permission.
            sqLiteDatabaseObj = sqLiteHelper.getReadableDatabase();
            // Adding search email query to cursor.
            cursor = sqLiteDatabaseObj.query(SQLiteHelper.TABLE_NAME, null, " " + SQLiteHelper.Table_Column_4_Email + "=?", new String[]{EmailHolder}, null, null, null);

            while (cursor.moveToNext()) {
                if (cursor.isFirst()) {
                    cursor.moveToFirst();
                    // Storing Password associated with entered email.
                    TempPassword = cursor.getString(cursor.getColumnIndex(SQLiteHelper.Table_Column_3_Password));
                    // Closing cursor.
                    cursor.close();
                }
            }
            // Calling method to check final result ..
            CheckFinalResult();
        }
        else {
            //If any of login EditText empty then this block will be executed.
            Toast.makeText(Login.this,"Please Enter UserName or Password.",Toast.LENGTH_LONG).show();
        }
    }
    // Checking EditText is empty or not.
    public void CheckEditTextStatus(){
        // Getting value from All EditText and storing into String Variables.
        EmailHolder = email.getText().toString();
        PasswordHolder = password.getText().toString();
        // Checking EditText is empty or no using TextUtils.
        if( TextUtils.isEmpty(EmailHolder) || TextUtils.isEmpty(PasswordHolder)){
            EditTextEmptyHolder = false ;
        }
        else {
            EditTextEmptyHolder = true ;
        }
    }
    // Checking entered password from SQLite database email associated password.
    public void CheckFinalResult(){
        if(TempPassword.equalsIgnoreCase(PasswordHolder))
        {
            session.setLoggedIn(true); // Set the user as logged in
            session.setUserData(EmailHolder);
            redirectToHome();
            Toast.makeText(Login.this,"Login Successful",Toast.LENGTH_LONG).show();
            // Going to Dashboard activity after login success message.
          /*  Intent intent = new Intent(Login.this, MainActivity.class);*/

        }
        else {
            Toast.makeText(Login.this,"UserName or Password is Wrong, Please Try Again.",Toast.LENGTH_LONG).show();
            TempPassword = "NOT_FOUND" ;
        }

    }

    private void redirectToHome() {
        Intent intent = new Intent(Login.this, MainActivity.class);
        // Sending Email to Dashboard Activity using intent.

        startActivity(intent);
      /*  finish(); // Optional: finish the login activity so the user can't go back to it*/
    }


}