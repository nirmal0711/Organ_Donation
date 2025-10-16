package com.example.organ_donation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.organ_donation.R;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText email;
    private Button resetBtn;
    private ProgressBar progressBar;
    private TextView backToLogin;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize UI elements
        email = findViewById(R.id.editTextEmail);
        resetBtn = findViewById(R.id.buttonResetPassword);
        progressBar = findViewById(R.id.progressBar);
        backToLogin = findViewById(R.id.textBackToLogin);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();

        // Send password reset link
        resetBtn.setOnClickListener(v -> {
            String emailTxt = email.getText().toString().trim();

            if (emailTxt.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(emailTxt).matches()) {
                Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(ProgressBar.VISIBLE);
            resetBtn.setEnabled(false);

            auth.sendPasswordResetEmail(emailTxt)
                    .addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(ProgressBar.GONE);
                        resetBtn.setEnabled(true);

                        // Success dialog
                        new AlertDialog.Builder(this)
                                .setTitle("Email Sent ✅")
                                .setMessage("A password reset link has been sent to:\n" + emailTxt +
                                        "\n\nPlease check your inbox or spam folder.")
                                .setPositiveButton("Go to Login", (dialog, which) -> {
                                    startActivity(new Intent(this, LoginActivity.class));
                                    finish();
                                })
                                .setCancelable(false)
                                .show();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(ProgressBar.GONE);
                        resetBtn.setEnabled(true);
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        // Back to Login
        backToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
