package com.example.organ_donation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.organ_donation.R;
import com.example.organ_donation.adapters.DonationAdapter;
import com.example.organ_donation.models.DonationModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class DonorMyDonationActivity extends AppCompatActivity {

    private RecyclerView recyclerDonations;
    private DonationAdapter adapter;
    private List<DonationModel> donationList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FloatingActionButton fabAddDonation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_my_donation);

        recyclerDonations = findViewById(R.id.recyclerDonations);
        fabAddDonation = findViewById(R.id.fabAddDonation);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        donationList = new ArrayList<>();

        recyclerDonations.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DonationAdapter(donationList);
        recyclerDonations.setAdapter(adapter);

        loadDonationsRealtime();

        fabAddDonation.setOnClickListener(v ->
                startActivity(new Intent(this, DonorFormActivity.class))
        );
    }

    /**
     * ✅ Load donations in real-time and persist after login/logout
     */
    private void loadDonationsRealtime() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        // 🔹 Real-time listener: updates instantly when new donation added
        db.collection("Donations")
                .whereEqualTo("donorId", uid)
                .addSnapshotListener((@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) -> {
                    if (e != null) {
                        Log.e("DONATIONS_LOAD", "Error: " + e.getMessage());
                        Toast.makeText(this, "Failed to load donations.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    donationList.clear();

                    if (snapshots == null || snapshots.isEmpty()) {
                        Toast.makeText(this, "No donations found.", Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        try {
                            DonationModel donation = doc.toObject(DonationModel.class);
                            if (donation == null) continue;

                            // Fallback values to avoid null fields
                            donation.setOrganType(
                                    donation.getOrganType() != null
                                            ? donation.getOrganType()
                                            : doc.getString("organType") != null ? doc.getString("organType") : "Unknown"
                            );
                            donation.setHospitalName(
                                    donation.getHospitalName() != null
                                            ? donation.getHospitalName()
                                            : doc.getString("hospitalName") != null ? doc.getString("hospitalName") : "Not Assigned"
                            );
                            donation.setDate(
                                    (String) (donation.getDate() != null
                                                                                ? donation.getDate()
                                                                                : doc.getString("date") != null ? doc.getString("date") : "N/A")
                            );
                            donation.setStatus(
                                    donation.getStatus() != null
                                            ? donation.getStatus()
                                            : doc.getString("status") != null ? doc.getString("status") : "Pending"
                            );

                            donationList.add(donation);
                        } catch (Exception ex) {
                            Log.e("DONATIONS_PARSE", "Error parsing doc: " + ex.getMessage());
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}
