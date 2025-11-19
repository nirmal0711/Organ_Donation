package com.example.organ_donation.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.example.organ_donation.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HospitalAddDonationActivity extends AppCompatActivity {

    private Spinner spinnerOrganType, spinnerBloodGroup;
    private EditText etDonorName, etNotes;
    private Switch switchStatus;
    private Button btnSubmitDonation;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_add_donation);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // ✅ Correct Views
        spinnerOrganType = findViewById(R.id.spinnerOrganType);
        spinnerBloodGroup = findViewById(R.id.spinnerBloodGroup);

        etDonorName = findViewById(R.id.etDonorName);
        etNotes = findViewById(R.id.etNotes);

        switchStatus = findViewById(R.id.switchStatus);
        btnSubmitDonation = findViewById(R.id.btnSubmitDonation);

        btnSubmitDonation.setOnClickListener(v -> saveDonation());
    }

    private void saveDonation() {

        String organType = spinnerOrganType.getSelectedItem().toString();
        String bloodGroup = spinnerBloodGroup.getSelectedItem().toString();
        String donorName = etDonorName.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        String status = switchStatus.isChecked() ? "Available" : "Unavailable";

        if (organType.equals("Select Organ")) {
            Toast.makeText(this, "Please select an organ type", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Adding donation...");
        dialog.setCancelable(false);
        dialog.show();

        String hospitalId = auth.getCurrentUser().getUid();
        String documentId = hospitalId + "_" + System.currentTimeMillis();

        Map<String, Object> donation = new HashMap<>();
        donation.put("donationId", documentId);
        donation.put("organType", organType);
        donation.put("bloodGroup", bloodGroup);
        donation.put("donorName", donorName.isEmpty() ? "Unknown Donor" : donorName);
        donation.put("notes", notes);
        donation.put("status", status);
        donation.put("hospitalId", hospitalId);
        donation.put("hospitalName", ""); // Update later if needed
        donation.put("date", DateFormat.getDateInstance().format(new Date()));

        db.collection("Donations")
                .document(documentId)
                .set(donation)
                .addOnSuccessListener(a -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Donation added successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
