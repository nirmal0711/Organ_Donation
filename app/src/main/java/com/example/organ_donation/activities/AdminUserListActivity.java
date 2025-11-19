package com.example.organ_donation.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.example.organ_donation.R;
import com.example.organ_donation.adapters.AdminUserAdapter;
import com.example.organ_donation.models.UserModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminUserListActivity extends AppCompatActivity {

    private RecyclerView recyclerUsers;
    private AdminUserAdapter adapter;
    private FirebaseFirestore db;

    private List<UserModel> users = new ArrayList<>();
    private String roleFilter = "";  // donor / patient / hospital

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_list);

        roleFilter = getIntent().getStringExtra("role");

        recyclerUsers = findViewById(R.id.recyclerUsers);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();

        adapter = new AdminUserAdapter(this, users);
        recyclerUsers.setAdapter(adapter);

        loadUsers();
    }

    private void loadUsers() {

        db.collection("Users")
                .whereEqualTo("role", roleFilter)
                .get()
                .addOnSuccessListener(query -> {

                    users.clear();

                    for (DocumentSnapshot snap : query.getDocuments()) {

                        UserModel model = snap.toObject(UserModel.class);

                        if (model != null) {

                            model.setDocumentId(snap.getId()); // keep your existing code
                            users.add(model);

                            // ⭐ ONLY FOR DONORS — fetch blood group + donations
                            if (roleFilter.equals("donor")) {

                                String uid = snap.getId();

                                // ⭐ FETCH BLOOD GROUP
                                db.collection("Donors").document(uid)
                                        .get()
                                        .addOnSuccessListener(donorSnap -> {
                                            if (donorSnap.exists()) {
                                                String bg = donorSnap.getString("bloodGroup");
                                                model.setBloodGroup(bg);   // ⭐ ADDED
                                            }
                                            adapter.notifyDataSetChanged();
                                        });

                                // ⭐ FETCH DONATION COUNT
                                db.collection("Donations")
                                        .whereEqualTo("donorId", uid)
                                        .get()
                                        .addOnSuccessListener(donationSnap -> {
                                            int count = donationSnap.size();
                                            model.setDonationCount(count);  // ⭐ ADDED
                                            adapter.notifyDataSetChanged();
                                        });
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load users: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
