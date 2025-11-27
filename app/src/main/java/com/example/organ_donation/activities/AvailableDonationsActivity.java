package com.example.organ_donation.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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

public class AvailableDonationsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    AvailableDonationAdapter adapter;
    ArrayList<DonationModel> donationList = new ArrayList<>();

    FirebaseFirestore db;
    String currentUserId;
    String patientBloodGroup = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_donations);

        recyclerView = findViewById(R.id.rvDonations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        getPatientBloodGroup();
    }

    private void getPatientBloodGroup() {

        db.collection("Patients")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists()) {

                        patientBloodGroup = doc.getString("bloodGroup");

                        Log.d("DEBUG", "Patient Blood Group = " + patientBloodGroup);

                        if (patientBloodGroup != null && !patientBloodGroup.isEmpty()) {
                            loadMatchingDonations();
                        } else {
                            Toast.makeText(this, "Blood group missing in profile!", Toast.LENGTH_LONG).show();
                        }
                    }

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error fetching patient profile", Toast.LENGTH_LONG).show()
                );
    }

    private void loadMatchingDonations() {

        db.collection("Donations")
                .whereEqualTo("bloodGroup", patientBloodGroup)
                .whereEqualTo("status", "Pending")  // <-- Match your Firestore status
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

                    adapter = new AvailableDonationAdapter(this, donationList);
                    recyclerView.setAdapter(adapter);

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
