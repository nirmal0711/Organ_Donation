package com.example.organ_donation.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import com.bumptech.glide.Glide;
import com.example.organ_donation.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

public class DonorDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome, tvName, tvEmail, tvBloodGroup;
    private TextView tvDescription;
    private Switch switchStatus;
    private ImageView imgProfile;

    private TextView tvShortDesc, tvFullDesc, btnReadMore;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private MaterialCardView cardMyDonation, cardViewRequests, cardEditProfile;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_dashboard);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        loadDonorProfile();
        setupClickListeners();
        setupReadMoreFeature();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvBloodGroup = findViewById(R.id.tvBloodGroup);
        switchStatus = findViewById(R.id.switchStatus);
        imgProfile = findViewById(R.id.imgProfile);
        tvDescription = findViewById(R.id.tvDonorDescription);

        cardMyDonation = findViewById(R.id.cardMyDonation);
        cardViewRequests = findViewById(R.id.cardViewRequests);
        cardEditProfile = findViewById(R.id.cardEditProfile);

        btnLogout = findViewById(R.id.btnLogout);

        tvShortDesc = findViewById(R.id.tvShortDesc);
        tvFullDesc = findViewById(R.id.tvFullDesc);
        btnReadMore = findViewById(R.id.btnReadMore);
    }

    private void loadDonorProfile() {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        db.collection("Donors").document(uid)
                .addSnapshotListener((doc, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (doc != null && doc.exists()) {
                        updateProfileUI(doc);
                    } else {
                        Toast.makeText(this, "Please complete your donor profile", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, DonorFormActivity.class));
                        finish();
                    }
                });
    }

    private void updateProfileUI(DocumentSnapshot doc) {
        String fullName = doc.getString("fullName");
        String blood = doc.getString("bloodGroup");
        Boolean available = doc.getBoolean("available");
        String image = doc.getString("profileImage");
        String description = doc.getString("description");

        tvWelcome.setText("Welcome, " + (fullName != null ? fullName : "Donor"));
        tvName.setText("Name: " + (fullName != null ? fullName : "-"));
        tvEmail.setText("Email: " + auth.getCurrentUser().getEmail());
        tvBloodGroup.setText("Blood Group: " + (blood != null ? blood : "-"));

        switchStatus.setChecked(available != null && available);

        if (image != null && !image.isEmpty()) {
            Glide.with(this).load(image).circleCrop().into(imgProfile);
        }

        switchStatus.setOnCheckedChangeListener((view, isChecked) -> updateAvailability(isChecked));
        tvDescription.setText(description != null && !description.isEmpty()
                ? description
                : "No description added.");
    }

    private void setupClickListeners() {
        cardEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, DonorFormActivity.class)));

        cardMyDonation.setOnClickListener(v ->
                startActivity(new Intent(this, DonorMyDonationActivity.class)));

        cardViewRequests.setOnClickListener(v ->
                startActivity(new Intent(this, DonorRequestPortalActivity.class)));

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });
    }

    private void setupReadMoreFeature() {
        btnReadMore.setOnClickListener(v -> {

            if (tvFullDesc.getVisibility() == View.GONE) {
                tvFullDesc.setVisibility(View.VISIBLE);
                btnReadMore.setText("Read Less");

                tvFullDesc.animate()
                        .alpha(1f)
                        .setDuration(250)
                        .start();

            } else {

                tvFullDesc.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .withEndAction(() -> {
                            tvFullDesc.setVisibility(View.GONE);
                            btnReadMore.setText("Read More");
                        })
                        .start();
            }
        });
    }

    private void updateAvailability(boolean isActive) {
        if (auth.getCurrentUser() == null) return;

        db.collection("Donors")
                .document(auth.getCurrentUser().getUid())
                .update("available", isActive)
                .addOnSuccessListener(a ->
                        Toast.makeText(this,
                                isActive ? "You are now available to donate" :
                                        "You are now unavailable",
                                Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update availability", Toast.LENGTH_SHORT).show());
    }
}
