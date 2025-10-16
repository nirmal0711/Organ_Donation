package com.example.organ_donation.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.example.organ_donation.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HospitalDashboardActivity extends AppCompatActivity {

    private TextView tvHospitalName, tvHospitalLicense, tvHospitalStatus;
    private ImageView imgHospital;
    private LinearLayout cardManagePatients, cardSearchDonors, cardRequests, cardEditProfile;
    private Button buttonLogout;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_dashboard);

        // 🔹 Firebase setup
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 🔹 UI References
        tvHospitalName = findViewById(R.id.tvHospitalName);
        tvHospitalLicense = findViewById(R.id.tvHospitalLicense);
        tvHospitalStatus = findViewById(R.id.tvHospitalStatus);
        imgHospital = findViewById(R.id.imgHospital);

        cardManagePatients = findViewById(R.id.cardManagePatients);
        cardSearchDonors = findViewById(R.id.cardSearchDonors);
        cardRequests = findViewById(R.id.cardRequests);
        cardEditProfile = findViewById(R.id.cardEditProfile);
        buttonLogout = findViewById(R.id.buttonLogout);

        // 🔹 Fetch hospital data from Firestore
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid != null) {
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String license = documentSnapshot.getString("license");
                            String status = documentSnapshot.getString("verified");
                            String imageUrl = documentSnapshot.getString("imageUrl");

                            tvHospitalName.setText(name != null ? name : "Hospital");
                            tvHospitalLicense.setText("License: " + (license != null ? license : "N/A"));
                            tvHospitalStatus.setText(status != null && status.equals("true")
                                    ? "✅ Verified Hospital" : "⚠️ Not Verified");

                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Glide.with(this)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.ic_hospital)
                                        .into(imgHospital);
                            }
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to load data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }

//        // 🏥 Manage Patients
//        cardManagePatients.setOnClickListener(v ->
//                startActivity(new Intent(this, ManagePatientsActivity.class)));
//
//        // 🔍 Search Donors
//        cardSearchDonors.setOnClickListener(v ->
//                startActivity(new Intent(this, SearchDonorsActivity.class)));
//
//        // 📋 Requests Management
//        cardRequests.setOnClickListener(v ->
//                startActivity(new Intent(this, HospitalRequestsActivity.class)));
//
//        // 👤 Edit Profile
//        cardEditProfile.setOnClickListener(v ->
//                startActivity(new Intent(this, EditHospitalProfileActivity.class)));

        // 🚪 Logout
        buttonLogout.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HospitalDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
