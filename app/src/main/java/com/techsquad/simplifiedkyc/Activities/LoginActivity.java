package com.techsquad.simplifiedkyc.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.techsquad.simplifiedkyc.R;
import com.techsquad.simplifiedkyc.R2;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 555;
    private static final int REQUEST_WRITE_STORAGE = 1;
    @BindView(R2.id.input_email) EditText _emailText;
    @BindView(R2.id.input_password) EditText _passwordText;
    @BindView(R2.id.btn_login) MaterialButton _loginButton;
    @BindView(R2.id.forgot_pwd) TextView _forgotLink;
    @BindView(R2.id.link_signup) TextView _signupLink;
    @BindView(R2.id.parentRelative) RelativeLayout _parentRelative;
    @BindView(R2.id.rememberCheck) CheckBox _rememberChecked;

    boolean mobileNwInfo = false;

    String email = null;
    String password = null;
    boolean isRemembered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        boolean hasNoCameraAccess = (ContextCompat.checkSelfPermission(LoginActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED);
        //Log.d("hasPermission: " , "" + hasNoCameraAccess);
        if (hasNoCameraAccess) {
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{android.Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }

        boolean hasPermissionToWriteExternalStorage = (ContextCompat.checkSelfPermission(LoginActivity.this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermissionToWriteExternalStorage) {
            ActivityCompat.requestPermissions(LoginActivity.this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
        }

        _loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, SignupActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            }
        });

        _forgotLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "Under Construction", Toast.LENGTH_LONG).show();
            }
        });

    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        email = _emailText.getText().toString();
        password = _passwordText.getText().toString();
        isRemembered = _rememberChecked.isChecked();

        onLoginSuccess();

    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("Enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("Should be 4 to 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        boolean mobileNwInfo = false;
        try {
            ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            mobileNwInfo = conMgr.getActiveNetworkInfo().isConnected();
        } catch (NullPointerException e) {
            mobileNwInfo = false;
        }
        if (mobileNwInfo == false) {
            final boolean mnwI = mobileNwInfo;
            Snackbar snackbar = Snackbar
                    .make(_parentRelative, "Plz enable WiFi/Mobile data", Snackbar.LENGTH_LONG)
                    .setAction("SETTINGS", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            /*if(mnwI == true) {
                                Snackbar snackbar1 = Snackbar.make(_parentRelative, "Connected", Snackbar.LENGTH_SHORT);
                                snackbar1.show();
                            } else {
                                Snackbar snackbar = Snackbar.make(_parentRelative, "Sorry! Not yet connected", Snackbar.LENGTH_LONG);
                                snackbar.show();
                            }*/
                            startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                        }
                    });
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
            textView.setTextColor(Color.RED);

            snackbar.show();
        } else {
            //String finalUrl = Constants.AUTH_URL+"?uname="+email+"&password="+password;
            //new Authenticate().execute(finalUrl);
            if(email.equalsIgnoreCase("sudha@email.com") && password.equalsIgnoreCase("sudha123")) {
                Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            } else {
                onLoginFailed();
            }
        }
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Please try again", Toast.LENGTH_LONG).show();
        _loginButton.setEnabled(true);
    }

}
