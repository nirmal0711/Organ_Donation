package com.example.organ_donation.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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

    private void registerUser() {
        String nameTxt = name.getText().toString().trim();
        String emailTxt = email.getText().toString().trim();
        String passwordTxt = password.getText().toString().trim();
        String roleTxt = role.getSelectedItem().toString().trim().toLowerCase(); // ✅ normalized lowercase

        if (nameTxt.isEmpty() || emailTxt.isEmpty() || passwordTxt.isEmpty()) {
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(emailTxt, passwordTxt)
                .addOnSuccessListener(authResult -> {
                    String userId = authResult.getUser().getUid();
                    Log.d(TAG, "✅ User created: " + userId);

                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("name", nameTxt);
                    userMap.put("email", emailTxt);
                    userMap.put("role", roleTxt); // ✅ store lowercase role

                    db.collection("Users").document(userId).set(userMap)
                            .addOnSuccessListener(unused -> {
                                Log.d(TAG, "✅ User data added to Firestore");
                                Toast.makeText(this, "Account created! Redirecting to Login...", Toast.LENGTH_SHORT).show();

                                auth.signOut();

                                // smooth guaranteed redirect
                                new Handler(getMainLooper()).postDelayed(() -> {
                                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                    finish();
                                    Log.d(TAG, "✅ Redirected to LoginActivity");
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
