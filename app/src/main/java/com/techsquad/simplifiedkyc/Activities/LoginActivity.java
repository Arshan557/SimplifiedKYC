package com.techsquad.simplifiedkyc.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
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
import com.techsquad.simplifiedkyc.Network.HttpHandler;
import com.techsquad.simplifiedkyc.R;
import com.techsquad.simplifiedkyc.R2;
import com.techsquad.simplifiedkyc.Utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 555;
    private static final int REQUEST_WRITE_STORAGE = 1;
    private ProgressDialog pDialog;
    @BindView(R2.id.input_mobile) EditText mobileNum;
    @BindView(R2.id.input_password) EditText _passwordText;
    @BindView(R2.id.btn_login) MaterialButton _loginButton;
    @BindView(R2.id.forgot_pwd) TextView _forgotLink;
    @BindView(R2.id.link_signup) TextView _signupLink;
    @BindView(R2.id.parentRelative) RelativeLayout _parentRelative;
    @BindView(R2.id.rememberCheck) CheckBox _rememberChecked;

    boolean mobileNwInfo = false;

    String mobileNumber = null;
    String password = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        boolean hasNoCameraAccess = (ContextCompat.checkSelfPermission(LoginActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED);
        Log.d("hasPermission: " , "" + hasNoCameraAccess);
        if (hasNoCameraAccess) {
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{android.Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }

        boolean hasPermissionToWriteExternalStorage = (ContextCompat.checkSelfPermission(LoginActivity.this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        Log.d("hasWrteExternalStorage:" , "" + hasPermissionToWriteExternalStorage);
        if (!hasPermissionToWriteExternalStorage) {
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
        }

        boolean hasPermissionToReadExternalStorage = (ContextCompat.checkSelfPermission(LoginActivity.this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        Log.d("hasReadExternalStorage:" , "" + hasPermissionToWriteExternalStorage);
        if (!hasPermissionToWriteExternalStorage) {
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
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

        mobileNumber = mobileNum.getText().toString();
        password = _passwordText.getText().toString();

        if (!validate()) {
            onLoginFailed();
            return;
        }
        _loginButton.setEnabled(false);
        onLoginSuccess();
    }

    public boolean validate() {
        boolean valid = true;

        if (mobileNumber.isEmpty() || !Patterns.PHONE.matcher(mobileNumber).matches()) {
            mobileNum.setError("Enter a valid mobile number");
            valid = false;
        } else {
            mobileNum.setError(null);
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
                            startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                        }
                    });
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
            textView.setTextColor(Color.RED);

            snackbar.show();
        } else {
            String finalUrl = Constants.SERVICE_URL + mobileNumber + "/" + password;
            new Authenticate().execute(finalUrl);
        }
    }

    private class Authenticate extends AsyncTask<String, String, String> {
        boolean status = false;
        String userNameStr, passwordStr, userIdStr, emailStr, mobileStr, kyccompletedStr  = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("Authenticating...");
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected String doInBackground(String... f_url) {
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(f_url[0]);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {

                try {
                    // Getting JSON Array node
                    JSONObject c = new JSONObject(jsonStr);
                    // looping through All data
                    if(c != null) {
                        status = true;
                            userNameStr = c.getString("userName");
                            passwordStr = c.getString("password");
                            userIdStr = c.getString("userId");
                            emailStr = c.getString("email");
                            mobileStr = c.getString("mobile");
                            kyccompletedStr = c.getString("kyccompleted");

                            //Shared preferences
                            SharedPreferences sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("userNameSP", userNameStr);
                            editor.putString("passwordSP", passwordStr);
                            editor.putString("userIdSP", userIdStr);
                            editor.putString("emailSP", emailStr);
                            editor.putString("mobileSP", mobileStr);
                            editor.putString("kyccompletedSP", kyccompletedStr);
                            editor.commit();

                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Something went wrong. Try again" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "MalformedURLException " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Something went wrong. Try again" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"Something went wrong. Please try again", Toast.LENGTH_LONG).show();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            if(status) {
                if(kyccompletedStr.equalsIgnoreCase("true")) {
                    Toast.makeText(getApplicationContext(),"Your KYC has been completed already.", Toast.LENGTH_LONG).show();
                } else {
                    Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("fname", userNameStr);
                    i.putExtras(bundle);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                }
            } else {
                Toast.makeText(getApplicationContext(),"Something went wrong. Please try again", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Please try again", Toast.LENGTH_LONG).show();
        _loginButton.setEnabled(true);
    }

}
