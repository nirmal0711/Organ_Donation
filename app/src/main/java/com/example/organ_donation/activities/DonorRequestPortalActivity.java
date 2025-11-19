package com.example.organ_donation.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.organ_donation.R;
import com.example.organ_donation.adapters.RequestAdapter;
import com.example.organ_donation.models.RequestModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DonorRequestPortalActivity extends AppCompatActivity {

    private RecyclerView recyclerRequests;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private RequestAdapter adapter;
    private final List<RequestModel> requestList = new ArrayList<>();
    private final List<String> documentIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_request_portal);

        recyclerRequests = findViewById(R.id.recyclerRequests);
        recyclerRequests.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Donor view → shows Accept/Decline buttons
        adapter = new RequestAdapter(requestList, documentIds, false);
        recyclerRequests.setAdapter(adapter);

        loadDonorInfoAndRequests();
    }

    private void loadDonorInfoAndRequests() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String donorId = auth.getCurrentUser().getUid();

        db.collection("Donors").document(donorId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String bloodGroup = doc.getString("bloodGroup");
                        String organType = null;

                        // Donor may have a list of organsDonated instead of a single field
                        if (doc.contains("organType")) {
                            organType = doc.getString("organType");
                        } else if (doc.contains("organsDonated")) {
                            List<String> organs = (List<String>) doc.get("organsDonated");
                            if (organs != null && !organs.isEmpty()) {
                                organType = organs.get(0); // use the first organ for now
                            }
                        }

                        if (bloodGroup != null && organType != null) {
                            fetchRequestsRealtime(bloodGroup, organType);
                        } else {
                            Toast.makeText(this, "Please complete your donor profile first.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Donor profile not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error fetching donor info: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void fetchRequestsRealtime(String bloodGroup, String organType) {
        if (bloodGroup == null) bloodGroup = "";
        if (organType == null) organType = "";

        final String finalBloodGroup = bloodGroup.trim().toUpperCase();
        final String finalOrganType = organType.trim().toLowerCase();
        final String donorId = auth.getCurrentUser().getUid();

        // ✅ include Declined along with Pending & Accepted
        db.collection("Requests")
                .whereIn("status", Arrays.asList("Pending", "Accepted", "Declined"))
                .addSnapshotListener((@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error loading requests: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    requestList.clear();
                    documentIds.clear();

                    if (snapshots != null && !snapshots.isEmpty()) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            RequestModel req = doc.toObject(RequestModel.class);
                            if (req == null) continue;

                            String reqBlood = req.getBloodGroup() != null ? req.getBloodGroup().toUpperCase() : "";
                            String reqOrgan = req.getOrganType() != null ? req.getOrganType().toLowerCase() : "";
                            String status = req.getStatus() != null ? req.getStatus() : "";
                            String reqDonorId = doc.getString("donorId");

                            // ✅ Core matching condition:
                            boolean match =
                                    // Pending requests that match donor’s organ & blood group
                                    (reqBlood.equals(finalBloodGroup)
                                            && reqOrgan.equals(finalOrganType)
                                            && status.equals("Pending"))
                                            ||
                                            // Show all Accepted or Declined requests by this donor
                                            ((status.equals("Accepted") || status.equals("Declined"))
                                                    && donorId.equals(reqDonorId));

                            // 🩸 Skip self-made requests
                            if (match && !donorId.equals(req.getRequestedBy())) {
                                requestList.add(req);
                                documentIds.add(doc.getId());
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (requestList.isEmpty()) {
                        Toast.makeText(this, "No matching, accepted, or declined requests found.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
