package com.example.organ_donation.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.example.organ_donation.R;
import com.google.firebase.auth.FirebaseAuth;

public class PatientDashboardActivity extends AppCompatActivity {
    private Button buttonLogout;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_dashboard);

        buttonLogout = findViewById(R.id.buttonLogout);
        auth = FirebaseAuth.getInstance();

        buttonLogout.setOnClickListener(v -> {
            auth.signOut(); // end Firebase session
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(PatientDashboardActivity.this, LoginActivity.class);
            // remove all previous screens from the stack
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
