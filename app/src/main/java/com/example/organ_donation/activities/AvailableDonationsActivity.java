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
import com.example.organ_donation.adapters.AvailableDonationAdapter;
import com.example.organ_donation.models.DonationModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AvailableDonationsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    AvailableDonationAdapter adapter;
    ArrayList<DonationModel> donationList = new ArrayList<>();
    FirebaseFirestore db;
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_donations);

        recyclerView = findViewById(R.id.rvDonations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadAvailableDonations();
    }

    private void loadAvailableDonations() {
        db.collection("Donations")
                .whereEqualTo("status", "available")   // ONLY available donations
                .get()
                .addOnSuccessListener(query -> {
                    donationList.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        DonationModel model = doc.toObject(DonationModel.class);
                        if (model != null) {
                            model.setDonationId(doc.getId());
                            donationList.add(model);
                        }
                    }

                    adapter = new AvailableDonationAdapter(AvailableDonationsActivity.this);
                    recyclerView.setAdapter(adapter);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
