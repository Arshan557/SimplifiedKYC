package com.techsquad.simplifiedkyc.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.Toast;

import com.techsquad.simplifiedkyc.R;

public class SplashActivity extends Activity {

    public static int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash_activity);

        runApp();
    }

    private void runApp() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences preferencesUserData = getApplicationContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
                SharedPreferences.Editor editorUserData = preferencesUserData.edit();
                editorUserData.clear();
                editorUserData.commit();
                int userInput = Integer.parseInt("5") * 1000;
                int minutes = (int) (userInput / 1000) / 60;
                int seconds = (int) (userInput / 1000) % 60;
                //Toast.makeText(getApplicationContext(), "It may ", Toast.LENGTH_SHORT).show();
                Intent introIntent = new Intent(SplashActivity.this, IrisActivity.class);
                String secs = String.valueOf(minutes);
                introIntent.putExtra("minutes", secs);
                startActivity(introIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}