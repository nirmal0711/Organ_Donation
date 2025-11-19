// File: app/src/main/java/com/example/organ_donation/models/DonationModel.java
package com.example.organ_donation.models;

import com.google.firebase.firestore.FieldValue;
import java.util.Date;

public class DonationModel {

    private String donationId;           // Document ID in Firestore
    private String donorId;
    private String hospitalId;
    private String organType;
    private String bloodGroup;
    private String hospitalName;
    private String city;
    private String contactNumber;
    private String contactEmail;
    private String status;               // available, requested, allocated
    private String date;                   // or use String if you store as string
    private FieldValue requestedAt;      // timestamp

    // Required empty constructor for Firestore
    public DonationModel() {}

    // Getters and Setters
    public String getDonationId() { return donationId; }
    public void setDonationId(String donationId) { this.donationId = donationId; }

    public String getDonorId() { return donorId; }
    public void setDonorId(String donorId) { this.donorId = donorId; }

    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }

    public String getOrganType() { return organType; }
    public void setOrganType(String organType) { this.organType = organType; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDate() { return date; }

    public void setDate(String date) { this.date = date; }

    public FieldValue getRequestedAt() { return requestedAt; }
    public void setRequestedAt(FieldValue requestedAt) { this.requestedAt = requestedAt; }
//
//    public String getDateString() {
//        if (date != null) {
//            return android.text.format.DateFormat.format("dd-MM-yyyy", date).toString();
//        }
//        return "N/A";
//    }
    // Optional: If you store date as String in Firestore
}