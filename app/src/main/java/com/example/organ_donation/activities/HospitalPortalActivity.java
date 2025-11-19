package com.example.organ_donation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.example.organ_donation.R;


public class HospitalPortalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_portal);

        // Toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        MaterialButton btnAddDonation = findViewById(R.id.btnAddDonation);
        MaterialButton btnViewInventory = findViewById(R.id.btnViewInventory);
        MaterialButton btnPatientRequests = findViewById(R.id.btnPatientRequests);
        MaterialButton btnHospitalProfile = findViewById(R.id.btnHospitalProfile);

        btnAddDonation.setOnClickListener(v ->
                startActivity(new Intent(this, HospitalAddDonationActivity.class)));

        btnViewInventory.setOnClickListener(v ->
                startActivity(new Intent(this, AvailableDonationsActivity.class)));

        btnPatientRequests.setOnClickListener(v ->
                startActivity(new Intent(this, MyRequestsActivity.class)));

        btnHospitalProfile.setOnClickListener(v ->
                startActivity(new Intent(this, HospitalFormActivity.class)));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}