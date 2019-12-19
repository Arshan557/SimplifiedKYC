package com.techsquad.simplifiedkyc.Activities;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Toast;

import com.techsquad.simplifiedkyc.R;
import com.techsquad.simplifiedkyc.R2;

import butterknife.BindView;
import butterknife.ButterKnife;

public class KycdoneActivity extends AppCompatActivity {
    private static final String TAG = "KycdoneActivity";
    @BindView(R2.id.btn_logout)MaterialButton _logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kycdone);
        ButterKnife.bind(this);

        _logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    public void logout() {
        Toast.makeText(getBaseContext(), "You'll be logging out ", Toast.LENGTH_LONG).show();
        Intent i = new Intent(KycdoneActivity.this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

}
