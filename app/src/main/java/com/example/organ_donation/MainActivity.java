package com.example.organ_donation;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.organ_donation.activities.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Simple splash animation
        ImageView logo = findViewById(R.id.logoImage);
        TextView title = findViewById(R.id.appTitle);
        TextView subtitle = findViewById(R.id.appSubtitle);

        logo.setAlpha(0f);
        title.setAlpha(0f);
        subtitle.setAlpha(0f);

        logo.animate().alpha(1f).setDuration(1200).setStartDelay(200).start();
        title.animate().alpha(1f).setDuration(1000).setStartDelay(600).start();
        subtitle.animate().alpha(1f).setDuration(1000).setStartDelay(900).start();

        // Wait before checking user
        new Handler().postDelayed(this::checkUserStatus, 2500);
    }

    private void checkUserStatus() {
        if (auth.getCurrentUser() == null) {
            // Not logged in
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            String userId = auth.getCurrentUser().getUid();

            // ✅ Fixed: Correct collection name (lowercase 'users')
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String role = documentSnapshot.getString("role");

                            if (role == null) {
                                Toast.makeText(this, "User role missing", Toast.LENGTH_SHORT).show();
                                auth.signOut();
                                startActivity(new Intent(this, LoginActivity.class));
                                finish();
                                return;
                            }

                            redirectToDashboard(role);
                        } else {
                            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, LoginActivity.class));
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    });
        }
    }

    private void redirectToDashboard(String role) {
        Intent intent;

        // ✅ To handle case differences safely
        switch (role.trim().toLowerCase()) {
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
                Toast.makeText(this, "Invalid role type: " + role, Toast.LENGTH_SHORT).show();
                intent = new Intent(this, LoginActivity.class);
                break;
        }

        startActivity(intent);
        finish();
    }
}
