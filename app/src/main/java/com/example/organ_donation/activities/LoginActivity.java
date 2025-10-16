package com.example.organ_donation.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.organ_donation.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

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

        // Initialize UI components
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

    private void loginUser() {
        String emailTxt = email.getText().toString().trim();
        String passwordTxt = password.getText().toString().trim();

        if (emailTxt.isEmpty() || passwordTxt.isEmpty()) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        auth.signInWithEmailAndPassword(emailTxt, passwordTxt)
                .addOnSuccessListener(authResult -> {
                    String userId = auth.getCurrentUser().getUid();
                    Log.d(TAG, "✅ User logged in: " + userId);

                    db.collection("Users").document(userId).get()
                            .addOnSuccessListener(document -> {
                                progressDialog.dismiss();
                                if (document.exists()) {
                                    String role = document.getString("role");
                                    Log.d(TAG, "✅ Fetched role: " + role);

                                    if (role == null || role.trim().isEmpty()) {
                                        Toast.makeText(this, "User role not found.", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    redirectToDashboard(role.trim().toLowerCase());
                                } else {
                                    Toast.makeText(this, "No user record found in Firestore.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Log.e(TAG, "❌ Error fetching user: " + e.getMessage());
                                Toast.makeText(this, "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e(TAG, "❌ Login failed: " + e.getMessage());
                    Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void redirectToDashboard(String role) {
        Intent intent;

        switch (role) {
            case "donor":
                Toast.makeText(this, "Welcome Donor!", Toast.LENGTH_SHORT).show();
                intent = new Intent(this, DonorDashboardActivity.class);
                break;

            case "patient":
                Toast.makeText(this, "Welcome Patient!", Toast.LENGTH_SHORT).show();
                intent = new Intent(this, PatientDashboardActivity.class);
                break;

            case "hospital":
                Toast.makeText(this, "Welcome Hospital!", Toast.LENGTH_SHORT).show();
                intent = new Intent(this, HospitalDashboardActivity.class);
                break;

            case "admin":
                Toast.makeText(this, "Welcome Admin!", Toast.LENGTH_SHORT).show();
                intent = new Intent(this, AdminDashboardActivity.class);
                break;

            default:
                Toast.makeText(this, "Unknown role: " + role, Toast.LENGTH_SHORT).show();
                auth.signOut();
                intent = new Intent(this, LoginActivity.class);
                break;
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
