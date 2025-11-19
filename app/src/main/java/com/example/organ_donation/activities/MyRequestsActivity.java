package com.example.organ_donation.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.organ_donation.R;
import com.example.organ_donation.adapters.RequestAdapter;
import com.example.organ_donation.models.RequestModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class MyRequestsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayout emptyStateLayout;
    private List<RequestModel> requestList = new ArrayList<>();
    private List<String> documentIds = new ArrayList<>();
    private RequestAdapter adapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_requests);

        recyclerView = findViewById(R.id.recyclerMyRequests);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        adapter = new RequestAdapter(requestList, documentIds);
        recyclerView.setAdapter(adapter);

        loadRequestsForCurrentUser();
    }

    private void loadRequestsForCurrentUser() {
        String uid = auth.getCurrentUser().getUid();

        // 🧩 Step 1: Determine user role
        db.collection("Users").document(uid).get().addOnSuccessListener(userDoc -> {
            String role = userDoc.getString("role");
            if (role == null) role = "patient";

            switch (role.toLowerCase()) {
                case "donor":
                    loadRequestsForDonor(uid);
                    break;
                case "hospital":
                    loadRequestsForHospital(uid);
                    break;
                default:
                    loadRequestsForPatient(uid);
                    break;
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error getting user role: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // 🧍‍♂️ For Patients → show only requests they made
    private void loadRequestsForPatient(String uid) {
        db.collection("Requests")
                .whereEqualTo("requestedBy", uid)
                .addSnapshotListener(requestListener);
    }

    // 🏥 For Hospitals → show requests this hospital created
    private void loadRequestsForHospital(String uid) {
        db.collection("Requests")
                .whereEqualTo("hospitalId", uid)
                .addSnapshotListener(requestListener);
    }

    // 🫀 For Donors → show requests that mention them or match their organs
    private void loadRequestsForDonor(String donorId) {
        db.collection("Requests")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error loading requests: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    requestList.clear();
                    documentIds.clear();

                    if (snapshots == null || snapshots.isEmpty()) {
                        adapter.notifyDataSetChanged();
                        emptyStateLayout.setVisibility(View.VISIBLE);
                        return;
                    }

                    for (DocumentSnapshot doc : snapshots) {
                        RequestModel req = doc.toObject(RequestModel.class);
                        if (req == null) continue;

                        // ✅ Condition 1: request directly addressed to donor
                        boolean isForThisDonor = donorId.equals(req.getDonorId());

                        // ✅ Condition 2: request created by hospital for available organ (no specific donor)
                        boolean isHospitalRequest = "hospital".equalsIgnoreCase(req.getFrom());

                        if (isForThisDonor || isHospitalRequest) {
                            requestList.add(req);
                            documentIds.add(doc.getId());
                        }
                    }

                    adapter.notifyDataSetChanged();
                    emptyStateLayout.setVisibility(requestList.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    // ♻️ Common listener for patient/hospital
    private final EventListener<QuerySnapshot> requestListener = new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
            if (error != null) {
                Toast.makeText(MyRequestsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            requestList.clear();
            documentIds.clear();

            if (value == null || value.isEmpty()) {
                adapter.notifyDataSetChanged();
                emptyStateLayout.setVisibility(View.VISIBLE);
                return;
            }

            for (DocumentSnapshot doc : value.getDocuments()) {
                RequestModel model = doc.toObject(RequestModel.class);
                if (model != null) {
                    requestList.add(model);
                    documentIds.add(doc.getId());
                }
            }

            adapter.notifyDataSetChanged();
            emptyStateLayout.setVisibility(requestList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    };
}
