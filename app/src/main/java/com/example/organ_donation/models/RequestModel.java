package com.example.organ_donation.models;

import java.io.Serializable;

public class RequestModel implements Serializable {

    private String requestId;
    private String patientId;
    private String donorId;
    private String organType;
    private String bloodGroup;
    private String location;
    private String urgency;     // e.g., "High", "Medium", "Low"
    private String status;
    private String requesterName;
    private String requestedBy;
    private String date;

    // 🏥 Fields for hospital-originated requests
    private String from;          // "patient" or "hospital"
    private String hospitalId;
    private String hospitalName;

    // 👤 Added fields
    private String patientName;
    private String patientContact;

    // ⚠️ Boolean urgent flag (optional use)
    private boolean urgent;

    public RequestModel() {
        // Required empty constructor for Firestore
    }

    public RequestModel(String requestId, String patientId, String donorId, String organType,
                        String bloodGroup, String location, String urgency,
                        String status, String requesterName, String requestedBy,
                        String date, String from, String hospitalId, String hospitalName,
                        String patientName, String patientContact, boolean urgent) {

        this.requestId = requestId;
        this.patientId = patientId;
        this.donorId = donorId;
        this.organType = organType;
        this.bloodGroup = bloodGroup;
        this.location = location;
        this.urgency = urgency;
        this.status = status;
        this.requesterName = requesterName;
        this.requestedBy = requestedBy;
        this.date = date;
        this.from = from;
        this.hospitalId = hospitalId;
        this.hospitalName = hospitalName;
        this.patientName = patientName;
        this.patientContact = patientContact;
        this.urgent = urgent;
    }

    // Getters & Setters
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDonorId() { return donorId; }
    public void setDonorId(String donorId) { this.donorId = donorId; }

    public String getOrganType() { return organType; }
    public void setOrganType(String organType) { this.organType = organType; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }

    public String getRequestedBy() { return requestedBy; }
    public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    // 👤 Patient Name
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    // 📞 Patient Contact
    public String getPatientContact() { return patientContact; }
    public void setPatientContact(String patientContact) { this.patientContact = patientContact; }

    // ⚠️ Urgent flag
    public boolean isUrgent() { return urgent; }
    public void setUrgent(boolean urgent) { this.urgent = urgent; }
}
