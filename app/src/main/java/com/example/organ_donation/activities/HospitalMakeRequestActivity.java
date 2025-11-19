package com.example.organ_donation.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.text.TextUtils;

import com.example.organ_donation.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class HospitalMakeRequestActivity extends AppCompatActivity {

    private EditText etOrganType, etBloodGroup, etPatientName, etContact, etReason;
    private CheckBox cbUrgent;
    private Button btnSubmitRequest;
    private ProgressBar progressBar;

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_make_request);

        initViews();
        setupSubmitButton();
    }

    private void initViews() {
        etOrganType = findViewById(R.id.etOrganType);
        etBloodGroup = findViewById(R.id.etBloodGroup);
        etPatientName = findViewById(R.id.etPatientName);
        etContact = findViewById(R.id.etContact);
        etReason = findViewById(R.id.etReason);
        cbUrgent = findViewById(R.id.cbUrgent);
        btnSubmitRequest = findViewById(R.id.btnSubmitRequest);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupSubmitButton() {
        btnSubmitRequest.setOnClickListener(v -> {

            String organ = etOrganType.getText().toString().trim();
            String blood = etBloodGroup.getText().toString().trim();
            String name = etPatientName.getText().toString().trim();
            String contact = etContact.getText().toString().trim();
            String reason = etReason.getText().toString().trim();

            if (TextUtils.isEmpty(organ) || TextUtils.isEmpty(blood) || TextUtils.isEmpty(name)) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            btnSubmitRequest.setEnabled(false);

            String hospitalId = auth.getCurrentUser().getUid();
            String requestId = db.collection("Requests").document().getId();

            Map<String, Object> request = new HashMap<>();
            request.put("requestId", requestId);
            request.put("hospitalId", hospitalId);
            request.put("organType", organ);
            request.put("bloodGroup", blood);

            // Patient details
            request.put("patientName", name);
            request.put("patientContact", contact);
            request.put("reason", reason);

            request.put("isUrgent", cbUrgent.isChecked());
            request.put("status", "Pending");
            request.put("requestedAt", FieldValue.serverTimestamp());

            db.collection("Requests")
                    .document(requestId)
                    .set(request)
                    .addOnSuccessListener(docRef -> {
                        Toast.makeText(this, "Request Submitted Successfully!", Toast.LENGTH_LONG).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                        btnSubmitRequest.setEnabled(true);
                    });
        });
    }
}
