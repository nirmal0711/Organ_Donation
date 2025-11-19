package com.example.organ_donation.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.*;
import com.example.organ_donation.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import com.google.firebase.firestore.FieldValue;

public class PatientRequestActivity extends AppCompatActivity {

    private EditText etOrganType, etBloodGroup, etLocation;
    private Spinner spinnerUrgency;
    private Button btnSubmitRequest;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_request);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        etOrganType = findViewById(R.id.etOrganType);
        etBloodGroup = findViewById(R.id.etBloodGroup);
        etLocation = findViewById(R.id.etLocation);
        spinnerUrgency = findViewById(R.id.spinnerUrgency);
        btnSubmitRequest = findViewById(R.id.btnSubmitRequest);

        ArrayAdapter<CharSequence> urgencyAdapter = ArrayAdapter.createFromResource(
                this, R.array.urgency_levels, android.R.layout.simple_spinner_item);
        urgencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUrgency.setAdapter(urgencyAdapter);

        btnSubmitRequest.setOnClickListener(v -> saveRequest());
    }

    private void saveRequest() {
        String organType = etOrganType.getText().toString().trim();
        String bloodGroup = etBloodGroup.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String urgency = spinnerUrgency.getSelectedItem().toString();

        if (organType.isEmpty() || bloodGroup.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        String name = auth.getCurrentUser().getDisplayName(); // Optional

        Map<String, Object> request = new HashMap<>();
        request.put("requesterName", name == null ? "Unknown Patient" : name);
        request.put("requestedBy", uid);
        request.put("organType", organType);
        request.put("bloodGroup", bloodGroup);
        request.put("location", location);
        request.put("urgency", urgency);
        request.put("status", "Pending");
        request.put("createdAt", FieldValue.serverTimestamp()); // ✅ New field

        db.collection("Requests")
                .add(request)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Request Submitted ✅", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

}
