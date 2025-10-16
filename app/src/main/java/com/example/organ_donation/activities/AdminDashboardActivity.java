package com.example.organ_donation.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.Button;

import com.example.organ_donation.R;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    // Quick Action Cards
    private LinearLayout cardManageDonors, cardManagePatients, cardManageHospitals, cardReports;
    private Button buttonLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // 🔥 Firebase init
        auth = FirebaseAuth.getInstance();

        // 🎉 Welcome Toast
        Toast.makeText(this, "Welcome to Admin Dashboard", Toast.LENGTH_SHORT).show();

        // 🔗 Initialize Views
        cardManageDonors = findViewById(R.id.cardManageDonors);
        cardManagePatients = findViewById(R.id.cardManagePatients);
        cardManageHospitals = findViewById(R.id.cardManageHospitals);
        cardReports = findViewById(R.id.cardReports);
        buttonLogout = findViewById(R.id.buttonLogout);

        // 🚀 Quick Action Clicks
        cardManageDonors.setOnClickListener(v ->
                Toast.makeText(this, "Opening Donor Management...", Toast.LENGTH_SHORT).show()
        );

        cardManagePatients.setOnClickListener(v ->
                Toast.makeText(this, "Opening Patient Management...", Toast.LENGTH_SHORT).show()
        );

        cardManageHospitals.setOnClickListener(v ->
                Toast.makeText(this, "Opening Hospital Management...", Toast.LENGTH_SHORT).show()
        );

        cardReports.setOnClickListener(v ->
                Toast.makeText(this, "Opening Reports Section...", Toast.LENGTH_SHORT).show()
        );

        // 🚪 Logout Button
        buttonLogout.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
