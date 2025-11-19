package com.example.organ_donation.adapters;

import android.app.AlertDialog;
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
import com.example.organ_donation.models.UserModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    private final Context context;
    private final List<UserModel> list;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AdminUserAdapter(Context context, List<UserModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        UserModel user = list.get(position);

        h.tvName.setText(user.getName());
        h.tvEmail.setText(user.getEmail());
        h.tvRole.setText("Role: " + user.getRole());

        // ⭐ ADDED → Show BLOOD GROUP for donors
        if (user.getRole().equals("donor")) {
            h.tvBloodGroup.setVisibility(View.VISIBLE);
            h.tvDonationCount.setVisibility(View.VISIBLE);

            String bg = user.getBloodGroup() == null ? "N/A" : user.getBloodGroup();
            h.tvBloodGroup.setText("Blood Group: " + bg);

            h.tvDonationCount.setText("Donations: " + user.getDonationCount());
        } else {
            // hide for patient / hospital
            h.tvBloodGroup.setVisibility(View.GONE);
            h.tvDonationCount.setVisibility(View.GONE);
        }

        // DELETE BUTTON
        h.btnDelete.setOnClickListener(v -> {

            new AlertDialog.Builder(context)
                    .setTitle("Delete User")
                    .setMessage("Are you sure you want to delete this user?")
                    .setPositiveButton("Delete", (dialog, which) -> {

                        String docId = user.getDocumentId();  // IMPORTANT

                        db.collection("Users")
                                .document(docId)
                                .delete()
                                .addOnSuccessListener(a -> {
                                    Toast.makeText(context, "User deleted", Toast.LENGTH_SHORT).show();

                                    list.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, list.size());
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvEmail, tvRole;

        // ⭐ ADDED THESE 2
        TextView tvBloodGroup, tvDonationCount;

        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View v) {
            super(v);

            tvName = v.findViewById(R.id.tvUserName);
            tvEmail = v.findViewById(R.id.tvUserEmail);
            tvRole = v.findViewById(R.id.tvUserRole);

            // ⭐ ADDED to match your layout
            tvBloodGroup = v.findViewById(R.id.tvBloodGroup);
            tvDonationCount = v.findViewById(R.id.tvDonationCount);

            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
