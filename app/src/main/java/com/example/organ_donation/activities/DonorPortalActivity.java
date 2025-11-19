package com.example.organ_donation.activities;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.example.organ_donation.R;


public class DonorPortalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_portal);

        // Toolbar
        setSupportActionBar(findViewById(R.id.toolbar));

        MaterialButton btnMyDonations = findViewById(R.id.btnMyDonations);
        MaterialButton btnRequestPortal = findViewById(R.id.btnRequestPortal);
        MaterialButton btnProfile = findViewById(R.id.btnProfile);

        btnMyDonations.setOnClickListener(v ->
                startActivity(new Intent(this, DonorMyDonationActivity.class)));

        btnRequestPortal.setOnClickListener(v ->
                startActivity(new Intent(this, DonorRequestPortalActivity.class)));

        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, DonorFormActivity.class)));
    }
}