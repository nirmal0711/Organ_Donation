package com.example.organ_donation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.organ_donation.R;
import com.example.organ_donation.models.RequestModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    private final List<RequestModel> requestList;
    private final List<String> documentIds;
    private final boolean isPatientView;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public RequestAdapter(List<RequestModel> requestList, List<String> documentIds) {
        this(requestList, documentIds, true);
    }

    public RequestAdapter(List<RequestModel> requestList, List<String> documentIds, boolean isPatientView) {
        this.requestList = requestList;
        this.documentIds = documentIds;
        this.isPatientView = isPatientView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_request_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        RequestModel req = requestList.get(position);
        String requestId = documentIds.get(position);

        // ----------------------------------------------------
        // ✅ FIX: LOAD PATIENT NAME FROM "Users" COLLECTION
        // ----------------------------------------------------
        if (req.getPatientId() != null && !req.getPatientId().isEmpty()) {
            db.collection("Users")
                    .document(req.getPatientId())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String name = doc.getString("name");
                            holder.tvHospitalName.setText("Requested by: " +
                                    (name != null ? name : "Unknown Patient"));
                        } else {
                            holder.tvHospitalName.setText("Requested by: Unknown Patient");
                        }
                    })
                    .addOnFailureListener(e ->
                            holder.tvHospitalName.setText("Requested by: Unknown Patient"));
        } else {
            holder.tvHospitalName.setText("Requested by: Unknown Patient");
        }

        // OTHER FIELDS
        holder.tvOrganType.setText("Organ Needed: " + safe(req.getOrganType()));
        holder.tvBloodGroup.setText("Blood Group: " + safe(req.getBloodGroup()));
        holder.tvLocation.setText("Location: " + safe(req.getLocation()));
        holder.tvUrgency.setText("Urgency: " + safe(req.getUrgency()));

        // URGENCY COLORS
        String urgency = safe(req.getUrgency()).toLowerCase();
        if (urgency.contains("high") || urgency.contains("critical"))
            holder.tvUrgency.setTextColor(holder.itemView.getContext().getColor(R.color.red));
        else if (urgency.contains("medium"))
            holder.tvUrgency.setTextColor(holder.itemView.getContext().getColor(R.color.orange));
        else
            holder.tvUrgency.setTextColor(holder.itemView.getContext().getColor(R.color.green));

        // STATUS
        String status = safe(req.getStatus());
        holder.tvStatus.setText("Status: " + status);

        switch (status.toLowerCase()) {
            case "accepted":
                holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(R.color.green));
                break;
            case "declined":
                holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(R.color.red));
                break;
            default:
                holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(R.color.orange));
                break;
        }

        // --------------------------
        // PATIENT VIEW (NO BUTTONS)
        // --------------------------
        if (isPatientView) {
            holder.btnAcceptRequest.setVisibility(View.GONE);
            holder.btnDeclineRequest.setVisibility(View.GONE);
            return;
        }

        // --------------------------
        // DONOR VIEW (WITH BUTTONS)
        // --------------------------
        holder.btnAcceptRequest.setVisibility(View.VISIBLE);
        holder.btnDeclineRequest.setVisibility(View.VISIBLE);

        String donorId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        boolean isPending = status.equalsIgnoreCase("Pending");

        holder.btnAcceptRequest.setEnabled(isPending);
        holder.btnDeclineRequest.setEnabled(isPending);

        holder.btnAcceptRequest.setText(isPending ? "Accept" : "Accepted");
        holder.btnDeclineRequest.setText(isPending ? "Decline" : "Declined");

        // ACCEPT REQUEST
        holder.btnAcceptRequest.setOnClickListener(v -> {
            if (donorId == null) return;

            db.collection("Requests").document(requestId)
                    .update("status", "Accepted", "donorId", donorId)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(v.getContext(), "Request Accepted", Toast.LENGTH_SHORT).show();
                        holder.tvStatus.setText("Status: Accepted");
                        holder.tvStatus.setTextColor(v.getContext().getColor(R.color.green));
                        disableButtons(holder);
                    });
        });

        // DECLINE REQUEST
        holder.btnDeclineRequest.setOnClickListener(v -> {
            if (donorId == null) return;

            db.collection("Requests").document(requestId)
                    .update("status", "Declined", "donorId", donorId)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(v.getContext(), "Request Declined", Toast.LENGTH_SHORT).show();
                        holder.tvStatus.setText("Status: Declined");
                        holder.tvStatus.setTextColor(v.getContext().getColor(R.color.red));
                        disableButtons(holder);
                    });
        });
    }

    private void disableButtons(ViewHolder h) {
        h.btnAcceptRequest.setEnabled(false);
        h.btnDeclineRequest.setEnabled(false);
    }

    private String safe(String s) {
        return (s == null || s.isEmpty()) ? "N/A" : s;
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHospitalName, tvOrganType, tvBloodGroup, tvLocation, tvUrgency, tvStatus;
        Button btnAcceptRequest, btnDeclineRequest;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHospitalName = itemView.findViewById(R.id.tvHospitalName);
            tvOrganType = itemView.findViewById(R.id.tvOrganType);
            tvBloodGroup = itemView.findViewById(R.id.tvBloodGroup);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvUrgency = itemView.findViewById(R.id.tvUrgency);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnAcceptRequest = itemView.findViewById(R.id.btnAcceptRequest);
            btnDeclineRequest = itemView.findViewById(R.id.btnDeclineRequest);
        }
    }
}
