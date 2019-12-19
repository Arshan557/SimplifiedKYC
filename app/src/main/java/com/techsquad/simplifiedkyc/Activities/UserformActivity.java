package com.techsquad.simplifiedkyc.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.google.android.material.button.MaterialButton;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.techsquad.simplifiedkyc.R;
import com.techsquad.simplifiedkyc.R2;
import com.techsquad.simplifiedkyc.Utils.Constants;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserformActivity extends AppCompatActivity {

    private static final String TAG = "UserformActivity";
    private static final int REQUEST_WRITE_STORAGE = 1;
    String fname, genderString, mobile, dob, address, aadharNumString;
    public static final String DEFAULT = "N/A";
    @BindView(R2.id.user_photo) ImageView user_photo;
    @BindView(R2.id.aadharNum) TextView aadharNum;
    @BindView(R2.id.user_dob) EditText user_dob;
    @BindView(R2.id.user_name) EditText user_name;
    @BindView(R2.id.gender) TextView gender;
    @BindView(R2.id.user_mobile)  EditText user_mobile;
    @BindView(R2.id.user_address) TextView user_address;
    @BindView(R2.id.btn_submit) MaterialButton btn_submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userform);
        ButterKnife.bind(this);

        // Getting data from Shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        if (null != sharedPreferences) {
            fname = sharedPreferences.getString("fname", DEFAULT);
            genderString = sharedPreferences.getString("gender", DEFAULT);
            mobile = sharedPreferences.getString("mobile", DEFAULT);
            dob = sharedPreferences.getString("dob", DEFAULT);
            address = sharedPreferences.getString("address", DEFAULT);
            aadharNumString = sharedPreferences.getString("aadharNum", "XXXX-XXXX-XXXX");
        }

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String fileName = fname + sdf.format(new Date()) + ".jpg";
        Log.d("fileName: ", fileName);

        aadharNum.setText(aadharNumString);
        user_name.setText(fname);
        gender.setText(genderString);
        user_mobile.setText(mobile);
        user_dob.setText(dob);
        user_address.setText(address);
        String outPicture = Constants.SCAN_IMAGE_LOCATION + File.separator + fileName;
        Log.d("outPicture: ", outPicture);
        File imgFile = new  File("outPicture");
        if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            user_photo.setImageBitmap(myBitmap);
        };

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitDetails();
            }
        });

    }

    public void submitDetails() {
        Toast.makeText(getBaseContext(), "Submitting details ", Toast.LENGTH_LONG).show();
        Intent i = new Intent(UserformActivity.this, KycdoneActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

}
