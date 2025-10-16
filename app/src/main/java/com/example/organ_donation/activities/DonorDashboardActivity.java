package com.example.organ_donation.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.example.organ_donation.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DonorDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome, tvName, tvEmail, tvBloodGroup;
    private Switch switchStatus;
    private ImageView imgProfile;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private MaterialCardView cardMyDonation, cardViewRequests, cardEditProfile;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_dashboard);

        // 🔹 Firebase setup
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 🔹 UI references
        tvWelcome = findViewById(R.id.tvWelcome);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvBloodGroup = findViewById(R.id.tvBloodGroup);
        switchStatus = findViewById(R.id.switchStatus);
        cardMyDonation = findViewById(R.id.cardMyDonation);
        cardViewRequests = findViewById(R.id.cardViewRequests);
        cardEditProfile = findViewById(R.id.cardEditProfile);
        btnLogout = findViewById(R.id.btnLogout);
        imgProfile = findViewById(R.id.imgProfile);

        // 🔹 Load donor data
        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            loadDonorData(uid);
        }

        // 🔹 Navigation
        cardEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, DonorFormActivity.class)));

        cardMyDonation.setOnClickListener(v ->
                Toast.makeText(this, "Coming soon: Donation Info", Toast.LENGTH_SHORT).show());

        cardViewRequests.setOnClickListener(v ->
                Toast.makeText(this, "Coming soon: View Requests", Toast.LENGTH_SHORT).show());

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            finish();
        });

        // 🔹 Update availability in Firestore when toggled
        switchStatus.setOnCheckedChangeListener((buttonView, isChecked) -> updateAvailability(isChecked));
    }

    private void loadDonorData(String uid) {
        db.collection("Donors").document(uid)
                .get()
                .addOnSuccessListener(this::updateUI)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateUI(DocumentSnapshot doc) {
        if (!doc.exists()) {
            // 🚨 No profile yet → redirect to form
            startActivity(new Intent(this, DonorFormActivity.class));
            Toast.makeText(this, "Please complete your Donor Profile first.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String fullName = doc.getString("fullName");
        String bloodGroup = doc.getString("bloodGroup");
        Boolean available = doc.getBoolean("available");
        String imageUrl = doc.getString("profileImage");

        // 🧠 Set text safely
        tvWelcome.setText(fullName != null ? "Welcome, " + fullName : "Welcome, Donor");
        tvName.setText(fullName != null ? "Name: " + fullName : "Name: -");
        tvEmail.setText("Email: " + (auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : "-"));
        tvBloodGroup.setText("Blood Group: " + (bloodGroup != null ? bloodGroup : "-"));

        switchStatus.setChecked(available != null && available);

        // 🖼️ Load profile image (or fallback)
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_user)
                    .error(R.drawable.ic_user)
                    .circleCrop()
                    .into(imgProfile);
        } else {
            imgProfile.setImageResource(R.drawable.ic_user);
        }
    }

    // 🔁 Update availability toggle
    private void updateAvailability(boolean isActive) {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();
        db.collection("Donors").document(uid)
                .update("available", isActive)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this,
                                isActive ? "You are now available for donation" : "You are set to unavailable",
                                Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update status: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
