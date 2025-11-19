package com.example.organ_donation.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.organ_donation.R;
import com.example.organ_donation.adapters.HospitalViewPagerAdapter;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class HospitalDashboardActivity extends AppCompatActivity {

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private TextView tvHospitalName, tvHospitalLicense, tvHospitalStatus;
    private TextView tvActivePatients, tvPendingRequests, tvMatchesFound, tvUrgentCases;
    private ImageView imgHospital;
    private MaterialCardView btnAddDonation, btnMakeRequest, cardEditProfile, buttonLogout;

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_dashboard);

        initViews();
        loadHospitalProfile();
        loadStats();
        setupTabs();
        setupActions();
    }

    private void initViews() {
        tvHospitalName = findViewById(R.id.tvHospitalName);
        tvHospitalLicense = findViewById(R.id.tvHospitalLicense);
        tvHospitalStatus = findViewById(R.id.tvHospitalStatus);

        imgHospital = findViewById(R.id.imgHospital);

        // Stats TextViews
        tvActivePatients = findViewById(R.id.tvActivePatients);
        tvPendingRequests = findViewById(R.id.tvPendingRequests);
        tvMatchesFound = findViewById(R.id.tvMatchesFound);
        tvUrgentCases = findViewById(R.id.tvUrgentCases);

        btnAddDonation = findViewById(R.id.btnAddDonation);
        btnMakeRequest = findViewById(R.id.btnMakeRequest);
        cardEditProfile = findViewById(R.id.cardEditProfile);
        buttonLogout = findViewById(R.id.buttonLogout);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
    }

    private void setupTabs() {
        HospitalViewPagerAdapter adapter = new HospitalViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Requests" : "Donations");
        }).attach();
    }

    private void loadHospitalProfile() {
        String uid = auth.getCurrentUser().getUid();

        db.collection("Hospitals").document(uid)
                .addSnapshotListener((doc, e) -> {
                    if (doc != null && doc.exists()) {

                        tvHospitalName.setText(doc.getString("hospitalName"));

                        tvHospitalLicense.setText(
                                "License: " + doc.getString("license")
                        );

                        tvHospitalStatus.setText(
                                "true".equals(doc.getString("verified"))
                                        ? "Verified Hospital"
                                        : "Verification Pending"
                        );

                        String imageUrl = doc.getString("imageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this).load(imageUrl).into(imgHospital);
                        }
                    }
                });
    }

    private void loadStats() {

        // 1️⃣ Active Patients (requests where status = active)
        db.collection("Requests")
                .whereEqualTo("status", "active")
                .addSnapshotListener((query, e) -> {
                    if (query != null)
                        tvActivePatients.setText(query.size() + " Active Patients");
                });

        // 2️⃣ Pending Requests
        db.collection("Requests")
                .whereEqualTo("status", "pending")
                .addSnapshotListener((query, e) -> {
                    if (query != null)
                        tvPendingRequests.setText(query.size() + " Pending Requests");
                });

        // 3️⃣ Matches Found
        db.collection("Requests")
                .whereEqualTo("status", "matched")
                .addSnapshotListener((query, e) -> {
                    if (query != null)
                        tvMatchesFound.setText(query.size() + " Matches Found");
                });

        // 4️⃣ Urgent Cases
        db.collection("Requests")
                .whereEqualTo("urgency", "High")
                .addSnapshotListener((query, e) -> {
                    if (query != null)
                        tvUrgentCases.setText(query.size() + " Urgent Cases");
                });
    }

    private void setupActions() {
        btnAddDonation.setOnClickListener(v ->
                startActivity(new Intent(this, HospitalAddDonationActivity.class))
        );

        btnMakeRequest.setOnClickListener(v ->
                startActivity(new Intent(this, HospitalMakeRequestActivity.class))
        );

        cardEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, HospitalFormActivity.class))
        );

        buttonLogout.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        });
    }
}
