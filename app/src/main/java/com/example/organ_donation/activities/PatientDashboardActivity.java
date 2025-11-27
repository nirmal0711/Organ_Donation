package com.example.organ_donation.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.*;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.organ_donation.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

public class PatientDashboardActivity extends AppCompatActivity {

    private static final String TAG = "PatientDashboard";

    // 🔹 UI
    private TextView tvPatientName, tvPatientEmail, tvPatientRole, tvBloodGroup;
    private TextView tvPatientDesc;
    private ImageView imgPatient;
    private LinearLayout cardMakeRequest, cardMyRequests, cardAvailableDonations, cardEditProfile;
    private Button buttonLogout;

    // 🔹 Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_dashboard);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initUI();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 🔹 Load patient profile with slight delay (ensures Firebase ready)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String uid = auth.getCurrentUser().getUid();
            loadPatientProfile(uid);
        }, 800);

        // 🔹 Navigation setup
        setupNavigation();
    }

    // 🧩 Initialize all views
    private void    initUI() {
        imgPatient = findViewById(R.id.imgPatient);
        tvPatientName = findViewById(R.id.tvPatientName);
        tvPatientEmail = findViewById(R.id.tvPatientEmail);
        tvPatientRole = findViewById(R.id.tvPatientRole);
        tvBloodGroup = findViewById(R.id.tvBloodGroup);
        cardMakeRequest = findViewById(R.id.cardMakeRequest);
        cardMyRequests = findViewById(R.id.cardMyRequests);
        cardAvailableDonations = findViewById(R.id.cardAvailableDonations);
        cardEditProfile = findViewById(R.id.cardEditProfile);
        buttonLogout = findViewById(R.id.buttonLogout);
        tvPatientDesc = findViewById(R.id.tvPatientDescription);
        // Placeholders
        tvPatientName.setText("Loading...");
        tvBloodGroup.setText("Blood Group: --");
    }

    // 🧩 Navigation
    private void setupNavigation() {
        cardMakeRequest.setOnClickListener(v ->
                startActivity(new Intent(this, PatientRequestActivity.class)));

        cardMyRequests.setOnClickListener(v ->
                startActivity(new Intent(this, MyRequestsActivity.class)));

        cardAvailableDonations.setOnClickListener(v ->
                startActivity(new Intent(this, AvailableDonationsActivity.class)));

        cardEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, PatientFormActivity.class)));

        buttonLogout.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    // 🧩 Load profile (server first → cache fallback)
    private void loadPatientProfile(String uid) {
        db.collection("Patients").document(uid)
                .get(Source.SERVER)
                .addOnSuccessListener(this::updateUI)
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Server fetch failed: " + e.getMessage());
                    // Fallback to cached data
                    db.collection("Patients").document(uid)
                            .get(Source.CACHE)
                            .addOnSuccessListener(this::updateUI)
                            .addOnFailureListener(inner ->
                                    Toast.makeText(this, "Error loading profile: " + inner.getMessage(), Toast.LENGTH_SHORT).show());
                });
    }

    // 🧩 Update dashboard UI
    private void updateUI(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) {
            Log.w(TAG, "⚠️ No patient profile found — redirecting to form.");
            Toast.makeText(this, "Please complete your profile first.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, PatientFormActivity.class));
            finish();
            return;
        }

        String fullName = doc.getString("fullName");
        String bloodGroup = doc.getString("bloodGroup");
        String imageUrl = doc.getString("profileImage");
        String description = doc.getString("description");

        tvPatientName.setText(fullName != null ? fullName : "Patient");
        tvPatientEmail.setText(auth.getCurrentUser() != null
                ? auth.getCurrentUser().getEmail()
                : "Not Available");
        tvPatientRole.setText("Patient User");
        tvBloodGroup.setText("Blood Group: " + (bloodGroup != null ? bloodGroup : "--"));
        tvPatientDesc.setText(description != null && !description.isEmpty()
                ? description
                : "No description added.");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Log.d(TAG, "🖼️ Loading profile image: " + imageUrl);
            Glide.with(this)
                    .load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .placeholder(R.drawable.ic_user)
                    .error(R.drawable.ic_user)
                    .circleCrop()
                    .into(imgPatient);
        } else {
            imgPatient.setImageResource(R.drawable.ic_user);
        }
    }

    // 🧩 Refresh when returning
    @Override
    protected void onResume() {
        super.onResume();
        if (auth.getCurrentUser() != null)
            loadPatientProfile(auth.getCurrentUser().getUid());
    }

}
