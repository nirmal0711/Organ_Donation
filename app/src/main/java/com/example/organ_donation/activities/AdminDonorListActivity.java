package com.example.organ_donation.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.example.organ_donation.R;
import com.example.organ_donation.adapters.AdminDonorAdapter;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminDonorListActivity extends AppCompatActivity {

    private RecyclerView recyclerDonors;
    private FirebaseFirestore db;
    private AdminDonorAdapter adapter;

    private List<String> ids = new ArrayList<>();
    private List<String> names = new ArrayList<>();
    private List<String> emails = new ArrayList<>();
    private List<String> bloods = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_donors);

        recyclerDonors = findViewById(R.id.recyclerDonors);
        recyclerDonors.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();

        loadDonors();
    }

    private void loadDonors() {
        db.collection("Users")
                .whereEqualTo("role", "donor")
                .addSnapshotListener((snap, e) -> {

                    ids.clear();
                    names.clear();
                    emails.clear();
                    bloods.clear();

                    if (snap != null) {
                        snap.forEach(doc -> {
                            ids.add(doc.getId());
                            names.add(doc.getString("name"));
                            emails.add(doc.getString("email"));
                            bloods.add(doc.getString("bloodGroup"));
                        });
                    }

                    adapter = new AdminDonorAdapter(this, ids, names, emails, bloods);
                    recyclerDonors.setAdapter(adapter);

                    if (ids.isEmpty()) {
                        Toast.makeText(this, "No donors found", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
