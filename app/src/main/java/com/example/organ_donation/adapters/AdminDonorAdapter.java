package com.example.organ_donation.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.organ_donation.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDonorAdapter extends RecyclerView.Adapter<AdminDonorAdapter.ViewHolder> {

    private final Context context;
    private final List<String> donorIds;
    private final List<String> donorNames;
    private final List<String> donorEmails;

    private Map<String, String> bloodGroups;
    private Map<String, Integer> donationCounts;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // ⭐ EXISTING CONSTRUCTOR (KEEPING IT)
    public AdminDonorAdapter(Context context,
                             List<String> ids,
                             List<String> names,
                             List<String> emails,
                             Map<String, String> bloodGroups,
                             Map<String, Integer> donationCounts) {

        this.context = context;
        this.donorIds = ids;
        this.donorNames = names;
        this.donorEmails = emails;

        this.bloodGroups = (bloodGroups != null) ? bloodGroups : new HashMap<>();
        this.donationCounts = (donationCounts != null) ? donationCounts : new HashMap<>();
    }

    // ⭐ NEW CONSTRUCTOR (ADDED — fixes your error)
    public AdminDonorAdapter(Context context,
                             List<String> ids,
                             List<String> names,
                             List<String> emails,
                             List<String> bloods) {

        this.context = context;
        this.donorIds = ids;
        this.donorNames = names;
        this.donorEmails = emails;

        // convert List → Map
        this.bloodGroups = new HashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            bloodGroups.put(ids.get(i), bloods.get(i));
        }

        // default donation count = 0
        this.donationCounts = new HashMap<>();
        for (String id : ids) {
            donationCounts.put(id, 0);
        }
    }

    public void updateBloodGroup(String donorId, String bg) {
        bloodGroups.put(donorId, bg);
        notifyDataSetChanged();
    }

    public void updateDonationCount(String donorId, int count) {
        donationCounts.put(donorId, count);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminDonorAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_user, parent, false);
        return new AdminDonorAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminDonorAdapter.ViewHolder h, int position) {

        String donorId = donorIds.get(position);

        h.tvUserName.setText(donorNames.get(position));
        h.tvUserEmail.setText(donorEmails.get(position));
        h.tvUserRole.setText("Role: Donor");

        String bg = bloodGroups.getOrDefault(donorId, "N/A");
        h.tvBloodGroup.setText("Blood Group: " + bg);

        int count = donationCounts.getOrDefault(donorId, 0);
        h.tvDonationCount.setText("Donations Made: " + count);

        h.btnDelete.setOnClickListener(v ->
                db.collection("Users").document(donorId)
                        .delete()
                        .addOnSuccessListener(a ->
                                Toast.makeText(context, "Donor deleted!", Toast.LENGTH_SHORT).show()
                        )
                        .addOnFailureListener(e ->
                                Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        )
        );

        h.btnEdit.setOnClickListener(v ->
                Toast.makeText(context, "Edit feature coming soon", Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() {
        return donorIds.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvUserName, tvUserEmail, tvUserRole, tvBloodGroup, tvDonationCount;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
            tvBloodGroup = itemView.findViewById(R.id.tvBloodGroup);
            tvDonationCount = itemView.findViewById(R.id.tvDonationCount);

            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
