package com.example.organ_donation.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.organ_donation.R;
import com.example.organ_donation.models.DonationModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AvailableDonationAdapter extends RecyclerView.Adapter<AvailableDonationAdapter.ViewHolder> {

    Context context;
    ArrayList<DonationModel> list;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public AvailableDonationAdapter(Context context) {
        this.context = context;
        this.list = new ArrayList<>();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_donation_hospital, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        DonationModel d = list.get(position);

        holder.organ.setText(d.getOrganType());
        holder.blood.setText(d.getBloodGroup());
//        holder.city.setText(d.getCity());
        holder.hospital.setText(d.getHospitalName());
        holder.date.setText(d.getDate());

        // Call hospital
//        holder.btnCall.setOnClickListener(v -> {
//            Intent intent = new Intent(Intent.ACTION_DIAL,
//                    Uri.parse("tel:" + d.getContactNumber()));
//            context.startActivity(intent);
//        });

        // Disable request button if donation belongs to current donor
        if (d.getDonorId().equals(currentUserId)) {
            holder.btnRequest.setEnabled(false);
            holder.btnRequest.setText("Your Donation");
        }

        holder.btnRequest.setOnClickListener(v -> sendRequestToHospital(d));
    }

    private void sendRequestToHospital(DonationModel d) {

        String currentUserId = FirebaseAuth.getInstance().getUid();

        // 1️⃣ First fetch the logged-in patient’s name
        FirebaseFirestore.getInstance()
                .collection("Patients")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(doc -> {

                    String patientName = doc.getString("fullName");
                    if (patientName == null) patientName = "Unknown Patient";

                    // 2️⃣ Now create the request with real patient name
                    String requestId = db.collection("Requests").document().getId();

                    Map<String, Object> reqData = new HashMap<>();
                    reqData.put("requestId", requestId);
                    reqData.put("donationId", d.getDonationId());
                    reqData.put("donorId", d.getDonorId());
                    reqData.put("patientId", currentUserId);
                    reqData.put("requesterName", patientName);   // 🔥 FIXED HERE
                    reqData.put("requestedBy", currentUserId);
                    reqData.put("organType", d.getOrganType());
                    reqData.put("bloodGroup", d.getBloodGroup());
                    reqData.put("location", d.getCity());
                    reqData.put("status", "Pending");
                    reqData.put("urgency", "High");
                    reqData.put("createdAt",
                            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

                    // 3️⃣ Save in Firestore
                    db.collection("Requests")
                            .document(requestId)
                            .set(reqData)
                            .addOnSuccessListener(a -> {
                                Toast.makeText(context, "Request Sent Successfully", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    public void updateData(List<DonationModel> newList) {
        list.clear();
        list.addAll(newList);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView organ, blood, hospital, date;
        Button btnRequest;

        public ViewHolder(@NonNull View v) {
            super(v);

            organ = v.findViewById(R.id.tvOrgan);
            blood = v.findViewById(R.id.tvBloodGroup);
            hospital = v.findViewById(R.id.tvHospitalName);
            date = v.findViewById(R.id.tvDate);

            btnRequest = v.findViewById(R.id.btnMakeRequest);
        }
    }

}
