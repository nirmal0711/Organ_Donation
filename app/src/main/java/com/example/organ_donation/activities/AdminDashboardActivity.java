package com.example.organ_donation.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;

import com.example.organ_donation.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminDashboardActivity extends AppCompatActivity {

    private LinearLayout cardManageDonors, cardManagePatients, cardManageHospitals;
    private TextView tvTotalDonors, tvTotalPatients, tvTotalHospitals, tvTotalUsers; // <-- Added
    private Button buttonLogout;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Cards
        cardManageDonors = findViewById(R.id.cardManageDonors);
        cardManagePatients = findViewById(R.id.cardManagePatients);
        cardManageHospitals = findViewById(R.id.cardManageHospitals);
        buttonLogout = findViewById(R.id.buttonLogout);

        // TextViews
        tvTotalDonors = findViewById(R.id.tvTotalDonors);
        tvTotalPatients = findViewById(R.id.tvTotalPatients);
        tvTotalHospitals = findViewById(R.id.tvTotalHospitals);
        tvTotalUsers = findViewById(R.id.tvTotalUsers); // <-- Added

        loadStats();

        // Navigation
        cardManageDonors.setOnClickListener(v -> openList("donor"));
        cardManagePatients.setOnClickListener(v -> openList("patient"));
        cardManageHospitals.setOnClickListener(v -> openList("hospital"));

        // Logout
        buttonLogout.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void openList(String role) {
        Intent i = new Intent(this, AdminUserListActivity.class);
        i.putExtra("role", role);
        startActivity(i);
    }

    private void loadStats() {

        // Count Donors
        db.collection("Users")
                .whereEqualTo("role", "donor")
                .addSnapshotListener((snap, e) -> {
                    tvTotalDonors.setText(String.valueOf(snap != null ? snap.size() : 0));
                });

        // Count Patients
        db.collection("Users")
                .whereEqualTo("role", "patient")
                .addSnapshotListener((snap, e) -> {
                    tvTotalPatients.setText(String.valueOf(snap != null ? snap.size() : 0));
                });

        // Count Hospitals
        db.collection("Users")
                .whereEqualTo("role", "hospital")
                .addSnapshotListener((snap, e) -> {
                    tvTotalHospitals.setText(String.valueOf(snap != null ? snap.size() : 0));
                });

        // ⭐ Count ALL Users
        db.collection("Users")
                .addSnapshotListener((snap, e) -> {
                    tvTotalUsers.setText(String.valueOf(snap != null ? snap.size() : 0));
                });
    }
}
