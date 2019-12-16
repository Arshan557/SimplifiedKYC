package com.techsquad.simplifiedkyc.Activities;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.button.MaterialButton;
import com.techsquad.simplifiedkyc.R;
import com.techsquad.simplifiedkyc.R2;

import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.security.KeyStore;

import javax.crypto.Cipher;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;

public class HomeActivity extends AppCompatActivity {

    @BindView(R2.id.aadharNumberEditText) EditText aadharNumber;
    @BindView(R2.id.btn_submit) MaterialButton btn_submit;
    @BindView(R2.id.irisImage) ImageView irisImage;

    String aadharNum = "";
    String irisData = "";
    private KeyStore keyStore;
    // Variable used for storing the key in the Android Keystore container
    private static final String KEY_NAME = "kyc";
    private Cipher cipher;
    private TextView textView;

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
            String irisData = b.getString("irisData");
        }

        // Initializing both Android Keyguard Manager and Fingerprint Manager
        /*KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

        // Check whether the device has a Fingerprint sensor.
        if(!fingerprintManager.isHardwareDetected()){
            Toast.makeText(getBaseContext(),  " Your mobile doesn't have fingerprint sensor", Toast.LENGTH_LONG).show();
        } else {
            // Checks whether fingerprint permission is set on manifest
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getBaseContext(),  " Fingerprint permission not enabled", Toast.LENGTH_LONG).show();
            }else{
                // Check whether at least one fingerprint is registered
                if (!fingerprintManager.hasEnrolledFingerprints()) {
                    //textView.setText("Register at least one fingerprint in Settings");
                    Toast.makeText(getBaseContext(),  " Register at least one fingerprint in Settings", Toast.LENGTH_LONG).show();
                }else{
                    // Checks whether lock screen security is enabled or not
                    if (!keyguardManager.isKeyguardSecure()) {
                        //textView.setText("Lock screen security not enabled in Settings");
                        Toast.makeText(getBaseContext(),  " Lock screen security not enabled in Settings", Toast.LENGTH_LONG).show();
                    }else{
                        //generateKey();
                        //if (cipherInit()) {
                        if (true) {
                            FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
                            FingerPrintHandler helper = new FingerPrintHandler(this);
                            helper.startAuth(fingerprintManager, cryptoObject);
                        }
                    }
                }
            }
        }*/

        irisImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomeActivity.this, SplashActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                //finish();
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitDetails();
            }
        });
    }

    public void submitDetails() {
        aadharNum = aadharNumber.getText().toString();
        Toast.makeText(getBaseContext(), aadharNum + " and " + irisData + " will be submitted shortly", Toast.LENGTH_LONG).show();
        /*Intent i = new Intent(HomeActivity.this, SplashActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();*/
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
