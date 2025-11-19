package com.example.organ_donation.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.organ_donation.R;
import com.example.organ_donation.adapters.HospitalUnifiedAdapter;
import com.example.organ_donation.models.DonationModel;
import com.example.organ_donation.models.RequestModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HospitalDonationsFragment extends Fragment {

    private RecyclerView rvDonations;
    private HospitalUnifiedAdapter adapter;
    private List<RequestModel> emptyRequests = new ArrayList<>();
    private List<DonationModel> donationList = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_hospital_donations, container, false);

        rvDonations = view.findViewById(R.id.rvDonations);
        rvDonations.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new HospitalUnifiedAdapter(getContext(), emptyRequests, donationList);
        rvDonations.setAdapter(adapter);

        loadDonations();

        return view;
    }

    private void loadDonations() {
        db.collection("Donations")
                .whereEqualTo("status", "Assigned")   // or "available" if you use that

                .addSnapshotListener((snapshot, error) -> {

                    donationList.clear();

                    if (snapshot != null) {
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {

                            DonationModel d = doc.toObject(DonationModel.class);
                            if (d != null) {
                                d.setDonationId(doc.getId());
                                donationList.add(d);
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}
