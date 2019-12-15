package com.techsquad.simplifiedkyc.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.techsquad.simplifiedkyc.Constants.Constants;
import com.techsquad.simplifiedkyc.R;
import com.techsquad.simplifiedkyc.Views.CustomProgressDialog;

public class MainActivity extends AppCompatActivity {
    String rememberFlag, DEFAULT = "", email, password;
    SharedPreferences sharedPreferences = null;
    private CustomProgressDialog customProgressDialog;
    private static final String TAG = "MainActivity";

    RelativeLayout _parentRelative;
    private CheckBox _rememberChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean mobileNwInfo = false;
        // Getting data from Shared preferences
        sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        rememberFlag = sharedPreferences.getString("rememberFlag", DEFAULT);
        //Checking internet connection
        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            mobileNwInfo = conMgr.getActiveNetworkInfo().isConnected();
        } catch (NullPointerException e) {
            mobileNwInfo = false;
        }
        /*if (mobileNwInfo == false) {
            Toast.makeText(getApplicationContext(),"Check internet settings", Toast.LENGTH_LONG).show();
            if ("Y".equalsIgnoreCase(rememberFlag)) {
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }*/

        email = sharedPreferences.getString("email", DEFAULT);
        password = sharedPreferences.getString("password", DEFAULT);

        if ("Y".equalsIgnoreCase(rememberFlag)) {
            //loginAgain();
        } else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

    }

}
