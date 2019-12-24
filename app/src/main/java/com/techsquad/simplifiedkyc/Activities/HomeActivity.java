package com.techsquad.simplifiedkyc.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.techsquad.simplifiedkyc.Network.HttpHandler;
import com.techsquad.simplifiedkyc.R;
import com.techsquad.simplifiedkyc.R2;
import com.techsquad.simplifiedkyc.Utils.Constants;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyStore;

import javax.crypto.Cipher;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;


public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    @BindView(R2.id.aadharNumberEditText) EditText aadharNumber;
    @BindView(R2.id.btn_proceed) MaterialButton btn_proceed;
    @BindView(R2.id.irisImage) ImageView irisImage;
    @BindView(R2.id.user_name) TextView user_name;
    private ProgressDialog pDialog;

    String userName = "";
    String aadharNum = "";
    //private KeyStore keyStore;
    // Variable used for storing the key in the Android Keystore container
    private static final String KEY_NAME = "kyc";
    private Cipher cipher;
    private TextView textView;
    public static int SPLASH_TIME_OUT = 2000;

    private static final int CARD_NUMBER_TOTAL_SYMBOLS = 14; // size of pattern 0000-0000-0000
    private static final int CARD_NUMBER_TOTAL_DIGITS = 12; // max numbers of digits in pattern: 0000 x 3
    private static final int CARD_NUMBER_DIVIDER_MODULO = 5; // means divider position is every 5th symbol beginning with 1
    private static final int CARD_NUMBER_DIVIDER_POSITION = CARD_NUMBER_DIVIDER_MODULO - 1; // means divider position is every 4th symbol beginning with 0
    private static final char CARD_NUMBER_DIVIDER = '-';

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        Bundle b = getIntent().getExtras();
        if(b != null && !b.isEmpty()) {
            userName = b.getString("fname");
        }

        user_name.setText(userName);

        irisImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomeActivity.this, SplashActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                //finish();
            }
        });

        btn_proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitDetails();
            }
        });
    }

    public void submitDetails() {
        aadharNum = aadharNumber.getText().toString();
        String aadharNumm = aadharNum.replaceAll("[-+.^:,]","");
        Log.d("aadharNum: ", aadharNumm);
        if(!aadharNum.isEmpty()) {
            String finalUrl = Constants.AADHAR_URL + aadharNumm;
            new HomeActivity.Verify().execute(finalUrl);
        } else {
            Toast.makeText(getBaseContext(),  "Something went wrong. Plz try again!", Toast.LENGTH_LONG).show();
        }
    }

    private class Verify extends AsyncTask<String, String, String> {
        boolean status = false;
        String uid, name, gender, dob, building, street, locality, district, state, pincode, address = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(HomeActivity.this);
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
                        uid = c.getString("uid");
                        name = c.getString("name");
                        gender = c.getString("gender");
                        dob = c.getString("dob");
                        building = c.getString("building");
                        street = c.getString("street");
                        locality = c.getString("locality");
                        district = c.getString("district");
                        state = c.getString("state");
                        pincode = c.getString("pincode");

                        address = building + ", " + street + ", " + locality + ", " + district + ", " + state + " - " + pincode;

                        //Shared preferences
                        SharedPreferences sharedPreferences = getSharedPreferences("AadharData", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("aadharNumSP", aadharNum);
                        editor.putString("uidSP", uid);
                        editor.putString("nameSP", name);
                        editor.putString("genderSP", gender);
                        editor.putString("dobSP", dob);
                        editor.putString("addressSP", address);
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
            if(name != null) {
                Intent i = new Intent(HomeActivity.this, UserformActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            } else {
                Toast.makeText(getApplicationContext(),"Something went wrong. Please try again", Toast.LENGTH_LONG).show();
            }
        }
    }

    @OnTextChanged(value = R.id.aadharNumberEditText, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    protected void onAdharNumberTextChanged(Editable s) {
        if (!isInputCorrect(s, CARD_NUMBER_TOTAL_SYMBOLS, CARD_NUMBER_DIVIDER_MODULO, CARD_NUMBER_DIVIDER)) {
            s.replace(0, s.length(), concatString(getDigitArray(s, CARD_NUMBER_TOTAL_DIGITS), CARD_NUMBER_DIVIDER_POSITION, CARD_NUMBER_DIVIDER));
        }
    }

    private boolean isInputCorrect(Editable s, int size, int dividerPosition, char divider) {
        boolean isCorrect = s.length() <= size;
        for (int i = 0; i < s.length(); i++) {
            if (i > 0 && (i + 1) % dividerPosition == 0) {
                isCorrect &= divider == s.charAt(i);
            } else {
                isCorrect &= Character.isDigit(s.charAt(i));
            }
        }
        return isCorrect;
    }

    private String concatString(char[] digits, int dividerPosition, char divider) {
        final StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < digits.length; i++) {
            if (digits[i] != 0) {
                formatted.append(digits[i]);
                if ((i > 0) && (i < (digits.length - 1)) && (((i + 1) % dividerPosition) == 0)) {
                    formatted.append(divider);
                }
            }
        }

        return formatted.toString();
    }

    private char[] getDigitArray(final Editable s, final int size) {
        char[] digits = new char[size];
        int index = 0;
        for (int i = 0; i < s.length() && index < size; i++) {
            char current = s.charAt(i);
            if (Character.isDigit(current)) {
                digits[index] = current;
                index++;
            }
        }
        return digits;
    }

}
