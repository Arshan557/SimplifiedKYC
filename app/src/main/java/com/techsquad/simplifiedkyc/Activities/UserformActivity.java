package com.techsquad.simplifiedkyc.Activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.button.MaterialButton;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.techsquad.simplifiedkyc.Network.HttpHandler;
import com.techsquad.simplifiedkyc.R;
import com.techsquad.simplifiedkyc.R2;
import com.techsquad.simplifiedkyc.Utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserformActivity extends AppCompatActivity {

    private static final String TAG = "UserformActivity";
    private static final int REQUEST_WRITE_STORAGE = 1;
    String userNameSP, mobileSP, aadharNumSP, nameSP, genderSP, dobSP, addressSP, fileName  = "";
    String panNum = "";
    public static final String DEFAULT = "N/A";
    private ProgressDialog pDialog;
    @BindView(R2.id.user_photo) ImageView user_photo;
    @BindView(R2.id.aadharNum) TextView aadharNum;
    @BindView(R2.id.user_dob) EditText user_dob;
    @BindView(R2.id.user_name) EditText user_name;
    @BindView(R2.id.gender) TextView gender;
    @BindView(R2.id.user_mobile)  EditText user_mobile;
    @BindView(R2.id.user_address) TextView user_address;
    @BindView(R2.id.user_pan) EditText user_pan;
    @BindView(R2.id.btn_submit) MaterialButton btn_submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userform);
        ButterKnife.bind(this);

        // Getting data from Shared preferences
        SharedPreferences sharedPreferencesUser = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        if (null != sharedPreferencesUser) {
            userNameSP = sharedPreferencesUser.getString("userNameSP", DEFAULT);
            mobileSP = sharedPreferencesUser.getString("mobileSP", DEFAULT);
            fileName = sharedPreferencesUser.getString("fileName", DEFAULT);
        }

        // Getting data from Shared preferences
        SharedPreferences sharedPreferencesAadhar = getSharedPreferences("AadharData", Context.MODE_PRIVATE);
        if (null != sharedPreferencesAadhar) {
            aadharNumSP = sharedPreferencesAadhar.getString("aadharNumSP", "XXXX-XXXX-XXXX");
            nameSP = sharedPreferencesAadhar.getString("nameSP", DEFAULT);
            genderSP = sharedPreferencesAadhar.getString("genderSP", DEFAULT);
            dobSP = sharedPreferencesAadhar.getString("dobSP", DEFAULT);
            addressSP = sharedPreferencesAadhar.getString("addressSP", DEFAULT);
        }

        /*@SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String fileName = userNameSP + sdf.format(new Date()) + ".jpg";*/
        Log.d("fileName: ", fileName);

        aadharNum.setText(aadharNumSP);
        user_name.setText(nameSP);
        gender.setText(genderSP);
        user_mobile.setText(mobileSP);
        user_dob.setText(dobSP);
        user_address.setText(addressSP);

        String outPicture = Constants.SCAN_IMAGE_LOCATION + File.separator + fileName;
        Log.d("outPicture: ", outPicture);
        /*File imgFile = new  File("outPicture");
        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        user_photo.setImageBitmap(myBitmap);*/
        /*if(imgFile.exists()){
            Log.d("imgFile: ", imgFile.getAbsolutePath());
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            user_photo.setImageBitmap(myBitmap);
        }*/

        //Disabling fields
        user_name.setEnabled(false);
        gender.setEnabled(false);
        user_mobile.setEnabled(false);
        user_dob.setEnabled(false);
        user_address.setEnabled(false);

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitDetails();
            }
        });
    }

    public void submitDetails() {
        panNum = user_pan.getText().toString();
        if (panNum.equals("") || panNum.isEmpty()) {
            Toast.makeText(getBaseContext(), "Please enter PAN no.", Toast.LENGTH_LONG).show();
        } else {
            String finalUrl = Constants.PAN_URL + panNum;
            new UserformActivity.submit().execute(finalUrl);
        }
    }

    private class submit extends AsyncTask<String, String, String> {
        boolean status = false;
        String pan, panstatus, lastname, firstname, middlename, pantitle, lastupdatedate, creditScore, filler1, filler2, filler3 = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(UserformActivity.this);
            pDialog.setMessage("Verifying...");
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
                        pan = c.getString("pan");
                        panstatus = c.getString("panstatus");
                        lastname = c.getString("lastname");
                        firstname = c.getString("firstname");
                        middlename = c.getString("middlename");
                        pantitle = c.getString("pantitle");
                        lastupdatedate = c.getString("lastupdatedate");
                        creditScore = c.getString("creditScore");
                        filler1 = c.getString("filler1");
                        filler2 = c.getString("filler2");
                        filler3 = c.getString("filler3");

                        //Shared preferences
                        SharedPreferences sharedPreferences = getSharedPreferences("PANData", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("panSP", pan);
                        editor.putString("panstatusSP", panstatus);
                        editor.putString("lastnameSP", lastname);
                        editor.putString("firstnameSP", firstname);
                        editor.putString("middlenameSP", middlename);
                        editor.putString("pantitleSP", pantitle);
                        editor.putString("lastupdatedateSP", lastupdatedate);
                        editor.putString("creditScoreSP", creditScore);
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
                if(Integer.valueOf(creditScore) < 750) {
                    Toast.makeText(getApplicationContext(),"Sorry your credit score is low", Toast.LENGTH_LONG).show();
                } else {
                    Intent i = new Intent(UserformActivity.this, KycdoneActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();

                    //Updating KYC flag
                    String finalUrl = Constants.PAN_URL + panNum;
                    //new UserformActivity.update().execute(finalUrl);
                }
            } else {
                Toast.makeText(getApplicationContext(),"Something went wrong. Please try again", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class update extends AsyncTask<String, String, String> {
        boolean status = false;
        String pan, panstatus, lastname, firstname, middlename, pantitle, lastupdatedate, creditScore, filler1, filler2, filler3 = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            /*pDialog = new ProgressDialog(UserformActivity.this);
            pDialog.setMessage("Verifying...");
            pDialog.setCancelable(false);
            pDialog.show();*/
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
                        pan = c.getString("pan");
                        panstatus = c.getString("panstatus");
                        lastname = c.getString("lastname");
                        firstname = c.getString("firstname");
                        middlename = c.getString("middlename");
                        pantitle = c.getString("pantitle");
                        lastupdatedate = c.getString("lastupdatedate");
                        creditScore = c.getString("creditScore");
                        filler1 = c.getString("filler1");
                        filler2 = c.getString("filler2");
                        filler3 = c.getString("filler3");

                        //Shared preferences
                        SharedPreferences sharedPreferences = getSharedPreferences("PANData", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("panSP", pan);
                        editor.putString("panstatusSP", panstatus);
                        editor.putString("lastnameSP", lastname);
                        editor.putString("firstnameSP", firstname);
                        editor.putString("middlenameSP", middlename);
                        editor.putString("pantitleSP", pantitle);
                        editor.putString("lastupdatedateSP", lastupdatedate);
                        editor.putString("creditScoreSP", creditScore);
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
            Intent i = new Intent(UserformActivity.this, KycdoneActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        }
    }

}
