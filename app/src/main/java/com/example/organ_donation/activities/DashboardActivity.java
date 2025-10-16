package com.example.organ_donation.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import com.example.organ_donation.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardActivity extends AppCompatActivity {

    private TextView textName, textEmail, textRole;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        textName = findViewById(R.id.textName);
        textEmail = findViewById(R.id.textEmail);
        textRole = findViewById(R.id.textRole);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadUserData();
    }

    private void loadUserData() {
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("Users").document(userId);

        userRef.get().addOnSuccessListener(document -> {
            if (document.exists()) {
                String name = document.getString("name");
                String email = document.getString("email");
                String role = document.getString("role");

                textName.setText("Welcome, " + name);
                textEmail.setText("Email: " + email);
                textRole.setText("Role: " + role);
            }
        });

        // Optional: show role passed from MainActivity
        String roleExtra = getIntent().getStringExtra("role");
        if (roleExtra != null) {
            textRole.setText("Role: " + roleExtra);
        }
    }
}
