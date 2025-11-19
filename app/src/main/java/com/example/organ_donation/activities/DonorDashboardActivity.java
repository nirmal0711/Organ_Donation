package com.example.organ_donation.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.*;

import com.bumptech.glide.Glide;
import com.example.organ_donation.R;
import com.example.organ_donation.adapters.AvailableDonationAdapter;
import com.example.organ_donation.models.DonationModel;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class DonorDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome, tvName, tvEmail, tvBloodGroup;
    private Switch switchStatus;
    private ImageView imgProfile;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private MaterialCardView cardMyDonation, cardViewRequests, cardEditProfile;
    private Button btnLogout;

    // Available Donations Section
    private RecyclerView rvAvailableDonations;
    private AvailableDonationAdapter donationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_dashboard);

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupAvailableDonationsRecyclerView();
        loadDonorProfile();
        loadAvailableDonations(); // Real-time list
        setupClickListeners();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvBloodGroup = findViewById(R.id.tvBloodGroup);
        switchStatus = findViewById(R.id.switchStatus);
        imgProfile = findViewById(R.id.imgProfile);

        cardMyDonation = findViewById(R.id.cardMyDonation);
        cardViewRequests = findViewById(R.id.cardViewRequests);
        cardEditProfile = findViewById(R.id.cardEditProfile);
        btnLogout = findViewById(R.id.btnLogout);

        rvAvailableDonations = findViewById(R.id.rvAvailableDonations);
    }

    private void setupAvailableDonationsRecyclerView() {
        rvAvailableDonations.setLayoutManager(new LinearLayoutManager(this));
        donationAdapter = new AvailableDonationAdapter(this);
        rvAvailableDonations.setAdapter(donationAdapter);
    }

    private void loadDonorProfile() {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        db.collection("Donors").document(uid)
                .addSnapshotListener((doc, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Profile load failed", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (doc != null && doc.exists()) {
                        updateProfileUI(doc);
                    } else {
                        Toast.makeText(this, "Complete your profile first", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, DonorFormActivity.class));
                        finish();
                    }
                });
    }

    private void updateProfileUI(DocumentSnapshot doc) {
        String fullName = doc.getString("fullName");
        String bloodGroup = doc.getString("bloodGroup");
        Boolean available = doc.getBoolean("available");
        String imageUrl = doc.getString("profileImage");

        tvWelcome.setText("Welcome, " + (fullName != null ? fullName : "Donor"));
        tvName.setText("Name: " + (fullName != null ? fullName : "-"));
        tvEmail.setText("Email: " + auth.getCurrentUser().getEmail());
        tvBloodGroup.setText("Blood Group: " + (bloodGroup != null ? bloodGroup : "-"));
        switchStatus.setChecked(available != null && available);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).circleCrop().into(imgProfile);
        }

        // Availability toggle
        switchStatus.setOnCheckedChangeListener((v, isChecked) -> updateAvailability(isChecked));
    }

    private void loadAvailableDonations() {
        db.collection("Donations")
                .whereEqualTo("status", "available")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Failed to load donations", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<DonationModel> list = new ArrayList<>();
                    if (snapshot != null) {
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            DonationModel donation = doc.toObject(DonationModel.class);
                            if (donation != null) {
                                donation.setDonationId(doc.getId());  // Critical!
                                list.add(donation);
                            }
                        }
                    }
                    donationAdapter.updateData(list);
                });
    }

    private void setupClickListeners() {
        cardEditProfile.setOnClickListener(v -> startActivity(new Intent(this, DonorFormActivity.class)));
        cardMyDonation.setOnClickListener(v -> startActivity(new Intent(this, DonorMyDonationActivity.class)));
        cardViewRequests.setOnClickListener(v -> startActivity(new Intent(this, DonorRequestPortalActivity.class)));

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });
    }

    private void updateAvailability(boolean isActive) {
        if (auth.getCurrentUser() == null) return;

        db.collection("Donors").document(auth.getCurrentUser().getUid())
                .update("available", isActive)
                .addOnSuccessListener(a -> Toast.makeText(this,
                        isActive ? "Available for donation" : "Set to unavailable",
                        Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show());
    }
}