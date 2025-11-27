package com.example.organ_donation.activities;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

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

        tvActivePatients = findViewById(R.id.tvActivePatients);
        tvPendingRequests = findViewById(R.id.tvPendingRequests);
        tvMatchesFound = findViewById(R.id.tvMatchesFound);
        tvUrgentCases = findViewById(R.id.tvUrgentCases);

        imgHospital = findViewById(R.id.imgHospital);

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
                        tvHospitalLicense.setText("License: " + doc.getString("license"));

                        boolean isVerified = "true".equals(doc.getString("verified"));
                        tvHospitalStatus.setText(isVerified ? "Verified Hospital" : "Verification Pending");

                        String imageUrl = doc.getString("imageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this).load(imageUrl).into(imgHospital);
                        }
                    }
                });
    }

    // 🔥 Animate numbers (0 ➜ value)
    private void animateTextView(TextView textView, int value, String suffix) {
        ValueAnimator animator = ValueAnimator.ofInt(0, value);
        animator.setDuration(800);
        animator.addUpdateListener(animation ->
                textView.setText(animation.getAnimatedValue().toString() + " " + suffix)
        );
        animator.start();
    }

    private void loadStats() {

        // Active Patients = Accepted
        db.collection("Requests")
                .whereEqualTo("status", "Accepted")
                .addSnapshotListener((query, e) -> {
                    if (query != null)
                        animateTextView(tvActivePatients, query.size(), "Active Patients");
                });

        // Pending Requests
        db.collection("Requests")
                .whereEqualTo("status", "Pending")
                .addSnapshotListener((query, e) -> {
                    if (query != null)
                        animateTextView(tvPendingRequests, query.size(), "Pending Requests");
                });

        // Matches Found = Matched
        db.collection("Requests")
                .whereEqualTo("status", "Matched")
                .addSnapshotListener((query, e) -> {
                    if (query != null)
                        animateTextView(tvMatchesFound, query.size(), "Matches Found");
                });

        // Urgent Cases
        db.collection("Requests")
                .whereEqualTo("urgency", "High")
                .addSnapshotListener((query, e) -> {
                    if (query != null)
                        animateTextView(tvUrgentCases, query.size(), "Urgent Cases");
                });
    }

    private void setupActions() {

        btnAddDonation.setOnClickListener(v ->
                startActivity(new Intent(this, HospitalAddDonationActivity.class)));

        btnMakeRequest.setOnClickListener(v ->
                startActivity(new Intent(this, HospitalMakeRequestActivity.class)));

        cardEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, HospitalFormActivity.class)));

        buttonLogout.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        });
    }
}
