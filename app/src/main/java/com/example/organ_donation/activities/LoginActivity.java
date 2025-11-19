package com.example.organ_donation.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.organ_donation.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText email, password;
    private Button loginBtn;
    private TextView forgotPassword, signupRedirect;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPassword);
        loginBtn = findViewById(R.id.buttonLogin);
        forgotPassword = findViewById(R.id.textForgotPassword);
        signupRedirect = findViewById(R.id.textSignup);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);

        loginBtn.setOnClickListener(v -> loginUser());

        forgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));

        signupRedirect.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class)));
    }

    private boolean validateForm(String emailTxt, String passwordTxt) {
        if (emailTxt.isEmpty()) {
            email.setError("Email is required");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailTxt).matches()) {
            email.setError("Enter a valid email address");
            return false;
        }
        if (passwordTxt.isEmpty()) {
            password.setError("Password is required");
            return false;
        }
        if (passwordTxt.length() < 6) {
            password.setError("Password must be at least 6 characters");
            return false;
        }
        return true;
    }

    private void loginUser() {
        String emailTxt = email.getText().toString().trim();
        String passwordTxt = password.getText().toString().trim();

        if (!validateForm(emailTxt, passwordTxt)) return;

        progressDialog.show();

        auth.signInWithEmailAndPassword(emailTxt, passwordTxt)
                .addOnSuccessListener(result -> {

                    String uid = auth.getCurrentUser().getUid();
                    Log.d(TAG, "User logged in: " + uid);

                    db.collection("Users").document(uid).get()
                            .addOnSuccessListener(doc -> {
                                progressDialog.dismiss();

                                if (!doc.exists()) {
                                    Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                String role = doc.getString("role");
                                if (role == null) {
                                    Toast.makeText(this, "Invalid role.", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // 🔥 AUTO CREATE DONOR PROFILE IF ROLE = DONOR
                                if (role.equalsIgnoreCase("donor")) {
                                    autoCreateDonorProfile(uid, doc.getString("name"));
                                }

                                redirectToDashboard(role.trim().toLowerCase());
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // --------------------------------------------------------------------
    // 🔥 AUTO-CREATE DONOR DOCUMENT (THIS FIXES YOUR ADMIN DONOR LIST)
    // --------------------------------------------------------------------
    private void autoCreateDonorProfile(String uid, String name) {

        db.collection("Donors").document(uid).get()
                .addOnSuccessListener(exists -> {

                    if (exists.exists()) {
                        Log.d(TAG, "Donor profile already exists");
                        return;
                    }

                    // Create default donor profile
                    Map<String, Object> donor = new HashMap<>();
                    donor.put("fullName", name != null ? name : "Donor");
                    donor.put("bloodGroup", "N/A");
                    donor.put("gender", "N/A");
                    donor.put("phone", "");
                    donor.put("address", "");
                    donor.put("age", "");
                    donor.put("available", false);
                    donor.put("healthConditions", "");
                    donor.put("emergencyContact", "");
                    donor.put("profileImage", "");
                    donor.put("organsDonated", new java.util.ArrayList<>());

                    db.collection("Donors").document(uid)
                            .set(donor)
                            .addOnSuccessListener(a ->
                                    Log.d(TAG, "Donor profile auto-created"))
                            .addOnFailureListener(e ->
                                    Log.e(TAG, "Failed to auto-create donor: " + e.getMessage()));
                });
    }

    private void redirectToDashboard(String role) {
        Intent intent;

        switch (role) {
            case "donor":
                intent = new Intent(this, DonorDashboardActivity.class);
                break;
            case "patient":
                intent = new Intent(this, PatientDashboardActivity.class);
                break;
            case "hospital":
                intent = new Intent(this, HospitalDashboardActivity.class);
                break;
            case "admin":
                intent = new Intent(this, AdminDashboardActivity.class);
                break;
            default:
                Toast.makeText(this, "Unknown role", Toast.LENGTH_SHORT).show();
                auth.signOut();
                intent = new Intent(this, LoginActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
