package com.example.organ_donation.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.example.organ_donation.R;

public class PatientPortalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_portal);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        MaterialButton btnMakeRequest = findViewById(R.id.btnMakeRequest);
        MaterialButton btnMyRequests = findViewById(R.id.btnMyRequests);
        MaterialButton btnAvailableOrgans = findViewById(R.id.btnAvailableOrgans);
        MaterialButton btnPatientProfile = findViewById(R.id.btnPatientProfile);

        btnMakeRequest.setOnClickListener(v ->
                startActivity(new Intent(this, PatientRequestActivity.class)));

        btnMyRequests.setOnClickListener(v ->
                startActivity(new Intent(this, MyRequestsActivity.class)));

        btnAvailableOrgans.setOnClickListener(v ->
                startActivity(new Intent(this, AvailableDonationsActivity.class)));

        btnPatientProfile.setOnClickListener(v ->
                startActivity(new Intent(this, PatientFormActivity.class)));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}