package com.example.organ_donation.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.organ_donation.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText name, email, password;
    private Spinner role;
    private Button signupBtn;
    private TextView loginRedirect;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private static final String TAG = "SignupActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        name = findViewById(R.id.editTextName);
        email = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPassword);
        role = findViewById(R.id.spinnerRole);
        signupBtn = findViewById(R.id.buttonSignup);
        loginRedirect = findViewById(R.id.textLoginRedirect);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        String[] roles = {"Donor", "Patient", "Hospital"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, roles);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        role.setAdapter(adapter);

        signupBtn.setOnClickListener(v -> registerUser());
        loginRedirect.setOnClickListener(v ->
                startActivity(new Intent(SignupActivity.this, LoginActivity.class)));
    }

    // -------------------------------------------------------------
    // VALIDATION FUNCTION
    // -------------------------------------------------------------
    private boolean validateForm(String nameTxt, String emailTxt, String passwordTxt) {

        // NAME VALIDATION
        if (nameTxt.isEmpty()) {
            name.setError("Name is required");
            name.requestFocus();
            return false;
        }

        if (nameTxt.length() < 2) {
            name.setError("Name must be at least 2 characters");
            name.requestFocus();
            return false;
        }

        // No digits allowed ➝ name must only contain letters + spaces
        if (!nameTxt.matches("^[a-zA-Z ]+$")) {
            name.setError("Name must contain only letters (no numbers or symbols)");
            name.requestFocus();
            return false;
        }

        // EMAIL VALIDATION
        if (emailTxt.isEmpty()) {
            email.setError("Email is required");
            email.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailTxt).matches()) {
            email.setError("Enter a valid email");
            email.requestFocus();
            return false;
        }

        // PASSWORD VALIDATION
        if (passwordTxt.isEmpty()) {
            password.setError("Password is required");
            password.requestFocus();
            return false;
        }

        if (passwordTxt.length() < 6) {
            password.setError("Password must be at least 6 characters");
            password.requestFocus();
            return false;
        }

        return true;
    }

    // -------------------------------------------------------------
    // REGISTER USER
    // -------------------------------------------------------------
    private void registerUser() {
        String nameTxt = name.getText().toString().trim();
        String emailTxt = email.getText().toString().trim();
        String passwordTxt = password.getText().toString().trim();
        String roleTxt = role.getSelectedItem().toString().trim().toLowerCase();

        // Validate form BEFORE calling Firebase
        if (!validateForm(nameTxt, emailTxt, passwordTxt)) {
            return;
        }

        auth.createUserWithEmailAndPassword(emailTxt, passwordTxt)
                .addOnSuccessListener(authResult -> {
                    String userId = authResult.getUser().getUid();
                    Log.d(TAG, "✅ User created: " + userId);

                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("name", nameTxt);
                    userMap.put("email", emailTxt);
                    userMap.put("role", roleTxt);

                    db.collection("Users").document(userId).set(userMap)
                            .addOnSuccessListener(unused -> {
                                Log.d(TAG, "✅ User data added to Firestore");
                                Toast.makeText(this, "Account created! Redirecting to Login...", Toast.LENGTH_SHORT).show();

                                auth.signOut();

                                new Handler(getMainLooper()).postDelayed(() -> {
                                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                    finish();
                                }, 1000);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "❌ Firestore error: " + e.getMessage());
                                Toast.makeText(this, "Error saving data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Signup failed: " + e.getMessage());
                    Toast.makeText(this, "Signup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
