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
import com.example.organ_donation.models.DonationModel;
import com.example.organ_donation.models.RequestModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class HospitalUnifiedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final List<RequestModel> requests;
    private final List<DonationModel> donations;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final int TYPE_REQUEST = 0;
    private static final int TYPE_DONATION = 1;

    public HospitalUnifiedAdapter(Context context, List<RequestModel> requests, List<DonationModel> donations) {
        this.context = context;
        this.requests = requests;
        this.donations = donations;
    }

    @Override
    public int getItemViewType(int position) {
        return (position < requests.size()) ? TYPE_REQUEST : TYPE_DONATION;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == TYPE_REQUEST) {
            return new RequestVH(inflater.inflate(R.layout.item_request_hospital, parent, false));
        } else {
            // 🔥 NEW — Use item_donation_hospital.xml
            return new DonationVH(inflater.inflate(R.layout.item_donation_hospital, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (getItemViewType(position) == TYPE_REQUEST) {

            RequestModel r = requests.get(position);
            RequestVH v = (RequestVH) holder;

            v.tvOrgan.setText("Organ: " + r.getOrganType());
            v.tvPatient.setText("Patient: " + (r.getPatientName() != null ? r.getPatientName() : "Unknown"));
            v.tvStatus.setText("Status: " + r.getStatus());

            v.btnAccept.setOnClickListener(l -> updateRequestStatus(r.getRequestId(), "Accepted"));
            v.btnDecline.setOnClickListener(l -> updateRequestStatus(r.getRequestId(), "Declined"));

        } else {

            DonationModel d = donations.get(position - requests.size());
            DonationVH v = (DonationVH) holder;

            v.tvOrgan.setText("Organ: " + d.getOrganType());
            v.tvBloodGroup.setText("Blood Group: " + d.getBloodGroup());
            v.tvHospital.setText("Hospital: " + d.getHospitalName());
            v.tvContact.setText("Contact: " + d.getContactNumber());
            v.tvDate.setText("Date: " + d.getDate());
            v.tvStatus.setText("Status: " + d.getStatus());
        }
    }

    private void updateRequestStatus(String id, String status) {
        db.collection("Requests").document(id).update("status", status)
                .addOnSuccessListener(a ->
                        Toast.makeText(context, "Request " + status, Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public int getItemCount() {
        return requests.size() + donations.size();
    }

    // -------------------- VIEW HOLDERS --------------------

    static class RequestVH extends RecyclerView.ViewHolder {

        TextView tvOrgan, tvPatient, tvStatus;
        Button btnAccept, btnDecline;

        RequestVH(View item) {
            super(item);
            tvOrgan = item.findViewById(R.id.tvOrgan);
            tvPatient = item.findViewById(R.id.tvPatient);
            tvStatus = item.findViewById(R.id.tvStatus);
            btnAccept = item.findViewById(R.id.btnAccept);
            btnDecline = item.findViewById(R.id.btnDecline);
        }
    }

    static class DonationVH extends RecyclerView.ViewHolder {

        TextView tvOrgan, tvBloodGroup, tvHospital, tvContact, tvDate, tvStatus;

        DonationVH(View item) {
            super(item);
            tvOrgan = item.findViewById(R.id.tvOrgan);
            tvBloodGroup = item.findViewById(R.id.tvBloodGroup);
            tvHospital = item.findViewById(R.id.tvHospital);
//            tvContact = item.findViewById(R.id.tvContact);
            tvDate = item.findViewById(R.id.tvDate);
            tvStatus = item.findViewById(R.id.tvStatus);
        }
    }
}
