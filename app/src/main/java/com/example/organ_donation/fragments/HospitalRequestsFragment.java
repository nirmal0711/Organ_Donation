package com.example.organ_donation.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.organ_donation.R;
import com.example.organ_donation.activities.HospitalMakeRequestActivity;
import com.example.organ_donation.adapters.HospitalUnifiedAdapter;
import com.example.organ_donation.models.DonationModel;
import com.example.organ_donation.models.RequestModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HospitalRequestsFragment extends Fragment {

    private RecyclerView rvRequests;
    private Button btnMakeRequest;
    private HospitalUnifiedAdapter adapter;
    private List<RequestModel> requestList = new ArrayList<>();
    private List<DonationModel> emptyDonations = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hospital_requests, container, false);

        rvRequests = view.findViewById(R.id.rvRequests);
        btnMakeRequest = view.findViewById(R.id.btnMakeRequest);

        rvRequests.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new HospitalUnifiedAdapter(getContext(), requestList, emptyDonations);
        rvRequests.setAdapter(adapter);

        // 🔥 Make Request button opens the form
        btnMakeRequest.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), HospitalMakeRequestActivity.class);
            startActivity(intent);
        });

        loadRequests();
        return view;
    }

    private void loadRequests() {
        db.collection("Requests")
                .whereEqualTo("status", "Pending")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Error loading requests", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    requestList.clear();

                    if (snapshot != null) {
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            RequestModel req = doc.toObject(RequestModel.class);

                            if (req != null) {
                                req.setRequestId(doc.getId());
                                requestList.add(req);
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}
