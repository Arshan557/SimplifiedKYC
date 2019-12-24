package com.techsquad.simplifiedkyc.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.techsquad.simplifiedkyc.R;
import com.techsquad.simplifiedkyc.R2;

import butterknife.BindView;
import butterknife.ButterKnife;

public class KycdoneActivity extends AppCompatActivity {
    private static final String TAG = "KycdoneActivity";
    String userNameSP, aadharNumSP, panNumSP = "";
    @BindView(R2.id.user_name) TextView user_name;
    @BindView(R2.id.aadhar_num) TextView aadhar_num;
    @BindView(R2.id.pan_num) TextView pan_num;
    @BindView(R2.id.btn_logout)MaterialButton _logoutButton;
    public static final String DEFAULT = "N/A";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kycdone);
        ButterKnife.bind(this);

        // Getting data from Shared preferences
        SharedPreferences sharedPreferencesAadhar = getSharedPreferences("AadharData", Context.MODE_PRIVATE);
        if (null != sharedPreferencesAadhar) {
            aadharNumSP = sharedPreferencesAadhar.getString("aadharNumSP", "XXXX-XXXX-XXXX");
            userNameSP = sharedPreferencesAadhar.getString("nameSP", DEFAULT);
        }

        SharedPreferences sharedPreferencesPAN = getSharedPreferences("PANData", Context.MODE_PRIVATE);
        if (null != sharedPreferencesPAN) {
            panNumSP = sharedPreferencesPAN.getString("panSP", DEFAULT);
        }

        user_name.setText(userNameSP);
        aadhar_num.setText(aadharNumSP);
        pan_num.setText(panNumSP);

        _logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    public void logout() {
        SharedPreferences preferencesUserData = getApplicationContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorUserData = preferencesUserData.edit();
        editorUserData.clear();
        editorUserData.commit();

        SharedPreferences preferencesAadharData = getApplicationContext().getSharedPreferences("AadharData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorAadharData = preferencesAadharData.edit();
        editorAadharData.clear();
        editorAadharData.commit();

        SharedPreferences preferencesPANData = getApplicationContext().getSharedPreferences("PANData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorPANData = preferencesPANData.edit();
        editorPANData.clear();
        editorPANData.commit();

        Toast.makeText(getBaseContext(), "You'll be logging out ", Toast.LENGTH_LONG).show();
        Intent i = new Intent(KycdoneActivity.this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

}
