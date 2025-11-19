package com.example.organ_donation.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.organ_donation.R;
import com.example.organ_donation.models.DonationModel;
import java.util.List;

public class DonationAdapter extends RecyclerView.Adapter<DonationAdapter.ViewHolder> {

    private final List<DonationModel> donationList;

    public DonationAdapter(List<DonationModel> donationList) {
        this.donationList = donationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_donation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DonationModel donation = donationList.get(position);

        holder.tvOrgan.setText("Organ: " + donation.getOrganType());
        holder.tvHospital.setText("Hospital: " + donation.getHospitalName());
        holder.tvDate.setText("Date: " + donation.getDate());
        holder.tvStatus.setText("Status: " + donation.getStatus());

        // 🟢 Set status color
        switch (donation.getStatus().toLowerCase()) {
            case "approved":
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
                break;
            case "declined":
                holder.tvStatus.setTextColor(Color.parseColor("#E53935")); // Red
                break;
            default:
                holder.tvStatus.setTextColor(Color.parseColor("#FFC107")); // Orange (Pending)
                break;
        }
    }

    @Override
    public int getItemCount() {
        return donationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrgan, tvHospital, tvDate, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrgan = itemView.findViewById(R.id.tvOrgan);
            tvHospital = itemView.findViewById(R.id.tvHospital);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
