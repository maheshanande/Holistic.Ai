package com.example.holisticai;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;


public class Login extends AppCompatActivity {
    AppCompatButton appLoginBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        appLoginBtn = (AppCompatButton) findViewById(R.id.login_btn);
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

       appLoginBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Thread thread = new Thread(() -> {
                   try {
                       Thread.sleep(1);

                   }catch (Exception e){
                       e.printStackTrace();

                   }finally{
                       Intent intent2 = new Intent(Login.this , MainActivity.class);
                       startActivity(intent2);
                   }
               });thread.start();
           }
       });
    }
}