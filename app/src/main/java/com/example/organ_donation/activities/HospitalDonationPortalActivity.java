package com.example.organ_donation.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.organ_donation.R;
import com.example.organ_donation.adapters.HospitalUnifiedAdapter;
import com.example.organ_donation.models.DonationModel;
import com.example.organ_donation.models.RequestModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HospitalDonationPortalActivity extends AppCompatActivity {

    private RecyclerView recyclerUnified;
    private LinearLayout emptyStateLayout;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private List<RequestModel> requestList = new ArrayList<>();
    private List<DonationModel> donationList = new ArrayList<>();
    private HospitalUnifiedAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_donation_portal);  // ← FIXED THIS LINE

        recyclerUnified = findViewById(R.id.recyclerUnified);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        adapter = new HospitalUnifiedAdapter(this, requestList, donationList);
        recyclerUnified.setLayoutManager(new LinearLayoutManager(this));
        recyclerUnified.setAdapter(adapter);

        loadRequests();
        loadDonations();
    }
    // 🔹 Load all requests made by patients
    private void loadRequests() {
        db.collection("Requests")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error loading requests: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    requestList.clear();
                    if (snapshots == null || snapshots.isEmpty()) {
                        adapter.notifyDataSetChanged();
                        updateEmptyState();
                        return;
                    }

                    for (DocumentSnapshot doc : snapshots) {
                        RequestModel req = doc.toObject(RequestModel.class);
                        if (req != null) {
                            req.setRequestId(doc.getId());
                            requestList.add(req);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                });
    }

    // 🔹 Load available donations from donors
    private void loadDonations() {
        db.collection("Donations")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error loading donations: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    donationList.clear();
                    if (snapshots == null || snapshots.isEmpty()) {
                        adapter.notifyDataSetChanged();
                        updateEmptyState();
                        return;
                    }

                    for (DocumentSnapshot doc : snapshots) {
                        DonationModel d = doc.toObject(DonationModel.class);
                        if (d != null) {
                            donationList.add(d);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                });
    }

    private void updateEmptyState() {
        if (requestList.isEmpty() && donationList.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
        }
    }
}
