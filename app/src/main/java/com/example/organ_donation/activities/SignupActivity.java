package com.example.organ_donation.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.organ_donation.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
        loginRedirect.setOnClickListener(v -> startActivity(new Intent(SignupActivity.this, LoginActivity.class)));
    }

    private boolean validateForm(String nameTxt, String emailTxt, String passwordTxt) {
        if (nameTxt.isEmpty()) {
            name.setError("Name is required");
            return false;
        }
        if (!nameTxt.matches("^[a-zA-Z ]+$")) {
            name.setError("Only letters allowed");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailTxt).matches()) {
            email.setError("Invalid email");
            return false;
        }
        if (passwordTxt.length() < 6) {
            password.setError("Minimum 6 characters");
            return false;
        }
        return true;
    }

    private void registerUser() {
        String nameTxt = name.getText().toString().trim();
        String emailTxt = email.getText().toString().trim();
        String passwordTxt = password.getText().toString().trim();
        String roleTxt = role.getSelectedItem().toString().toLowerCase();

        if (!validateForm(nameTxt, emailTxt, passwordTxt)) return;

        auth.createUserWithEmailAndPassword(emailTxt, passwordTxt)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();

                    // 🔥 Send Verification Email
                    if (user != null) {
                        user.sendEmailVerification()
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this,
                                            "Verification email sent! Please check your inbox.",
                                            Toast.LENGTH_LONG).show();

                                    // Add user data to Firestore
                                    saveUserToFirestore(user.getUid(), nameTxt, emailTxt, roleTxt);

                                    // Logout until email verified
                                    auth.signOut();

                                    new Handler(getMainLooper()).postDelayed(() -> {
                                        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                        finish();
                                    }, 1500);
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Failed to send verification: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Signup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveUserToFirestore(String userId, String name, String email, String role) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("email", email);
        userMap.put("role", role);
        userMap.put("emailVerified", false); // Optional flag

        db.collection("Users").document(userId).set(userMap);
    }
}
