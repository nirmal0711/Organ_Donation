package com.example.organ_donation.models;

public class UserModel {

    private String id;
    private String name;
    private String email;
    private String role;
    private boolean isBlocked;
    private String blockReason;
    private String documentId;

    // ⭐ ADDED — Blood Group for donors
    private String bloodGroup;

    // ⭐ ADDED — Number of donations for donors
    private int donationCount;

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public UserModel() {
        // Required empty constructor
    }

    public UserModel(String id, String name, String email, String role,
                     boolean isBlocked, String blockReason) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.isBlocked = isBlocked;
        this.blockReason = blockReason;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isBlocked() { return isBlocked; }
    public void setBlocked(boolean blocked) { isBlocked = blocked; }

    public String getBlockReason() { return blockReason; }
    public void setBlockReason(String blockReason) { this.blockReason = blockReason; }

    // ⭐ ADDED getter / setter for blood group
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    // ⭐ ADDED getter / setter for donation count
    public int getDonationCount() { return donationCount; }
    public void setDonationCount(int donationCount) { this.donationCount = donationCount; }
}
