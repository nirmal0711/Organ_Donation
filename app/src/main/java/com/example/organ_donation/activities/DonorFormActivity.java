package com.example.organ_donation.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.bumptech.glide.Glide;
import com.example.organ_donation.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.util.*;

public class DonorFormActivity extends AppCompatActivity {

    private EditText etFullName, etAge, etPhone, etAddress, etHealthConditions, etEmergencyContact;
    private EditText editDescription;
    private Spinner spinnerGender, spinnerBloodGroup;
    private Switch switchAvailability;
    private Button btnSubmit, btnUploadImage;
    private ImageView imgProfilePreview;
    private GridLayout gridOrgans;

    private final List<CheckBox> organCheckBoxes = new ArrayList<>();
    private Uri imageUri;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private static final int PICK_IMAGE_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_form);

        // Firebase setup
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // UI setup
        etFullName = findViewById(R.id.etFullName);
        etAge = findViewById(R.id.etAge);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etHealthConditions = findViewById(R.id.etHealthConditions);
        etEmergencyContact = findViewById(R.id.etEmergencyContact);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerBloodGroup = findViewById(R.id.spinnerBloodGroup);
        switchAvailability = findViewById(R.id.switchAvailability);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        imgProfilePreview = findViewById(R.id.imgProfilePreview);
        editDescription = findViewById(R.id.editTextDescription);

        gridOrgans = findViewById(R.id.gridOrgans);

        // Collect all organ checkboxes
        for (int i = 0; i < gridOrgans.getChildCount(); i++) {
            View view = gridOrgans.getChildAt(i);
            if (view instanceof CheckBox) organCheckBoxes.add((CheckBox) view);
        }

        checkStoragePermission();
        setupSpinners();
        loadExistingData();

        btnUploadImage.setOnClickListener(v -> selectImage());
        btnSubmit.setOnClickListener(v -> saveProfile());
    }

    // -----------------------------
    // 🔹 Spinners setup
    // -----------------------------
    private void setupSpinners() {
        setupSpinner(spinnerGender, new String[]{"Select Gender", "Male", "Female", "Other"});
        setupSpinner(spinnerBloodGroup, new String[]{"Select Blood Group", "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"});
    }

    private void setupSpinner(Spinner spinner, String[] options) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, options) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setTextColor(position == 0 ? Color.parseColor("#777777") : Color.BLACK);
                ((TextView) view).setTextSize(16);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.WHITE);
                view.setBackgroundColor(Color.parseColor("#1C1C1C"));
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    // -----------------------------
    // 🔹 Permission check
    // -----------------------------
    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 1);
            }
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    // -----------------------------
    // 🔹 Image picker
    // -----------------------------
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).circleCrop().into(imgProfilePreview);
        }
    }

    // -----------------------------
    // 🔹 Load existing data
    // -----------------------------
    private void loadExistingData() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        db.collection("Donors").document(uid)
                .get()
                .addOnSuccessListener(this::fillFormWithExistingData)
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show());
    }

    private void fillFormWithExistingData(DocumentSnapshot doc) {
        if (!doc.exists()) return;

        try {
            etFullName.setText(doc.getString("fullName"));
            etAge.setText(doc.getString("age"));
            etPhone.setText(doc.getString("phone"));
            etAddress.setText(doc.getString("address"));
            etHealthConditions.setText(doc.getString("healthConditions"));
            etEmergencyContact.setText(doc.getString("emergencyContact"));
            editDescription.setText(doc.getString("description"));


            selectSpinner(spinnerGender, doc.getString("gender"));
            selectSpinner(spinnerBloodGroup, doc.getString("bloodGroup"));

            Boolean available = doc.getBoolean("available");
            switchAvailability.setChecked(available != null && available);

            String imageUrl = doc.getString("profileImage");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this).load(imageUrl).circleCrop().into(imgProfilePreview);
            }

            List<String> organs = (List<String>) doc.get("organsDonated");
            if (organs != null) {
                for (CheckBox cb : organCheckBoxes) {
                    cb.setChecked(organs.contains(cb.getText().toString().trim().toLowerCase()));
                }
            }

            btnSubmit.setText("Update Profile");
        } catch (Exception e) {
            Log.e("FORM_LOAD", "Error filling form: " + e.getMessage());
        }
    }

    private void selectSpinner(Spinner spinner, String value) {
        if (value == null) return;
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        spinner.setSelection(adapter.getPosition(value));
    }

    // -----------------------------
    // 🔹 Save profile
    // -----------------------------
    private void saveProfile() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Saving profile...");
        dialog.setCancelable(false);
        dialog.show();
        String descriptionTxt = editDescription.getText().toString().trim();

        Map<String, Object> donor = new HashMap<>();
        donor.put("fullName", safeText(etFullName));
        donor.put("age", safeText(etAge));
        donor.put("gender", spinnerGender.getSelectedItem().toString());
        donor.put("bloodGroup", spinnerBloodGroup.getSelectedItem().toString().toUpperCase());
        donor.put("phone", safeText(etPhone));
        donor.put("address", safeText(etAddress));
        donor.put("healthConditions", safeText(etHealthConditions));
        donor.put("emergencyContact", safeText(etEmergencyContact));
        donor.put("available", switchAvailability.isChecked());
        donor.put("description", descriptionTxt);

        // ✅ Normalize organs: lowercase + singular
        List<String> selectedOrgans = new ArrayList<>();
        for (CheckBox cb : organCheckBoxes) {
            if (cb.isChecked()) {
                String organ = cb.getText().toString().trim().toLowerCase();
                if (organ.endsWith("s")) organ = organ.substring(0, organ.length() - 1);
                selectedOrgans.add(organ);
            }
        }
        donor.put("organsDonated", selectedOrgans);

        if (imageUri != null) {
            StorageReference imgRef = storageRef.child("donor_profiles/" + uid + ".jpg");
            imgRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot ->
                            imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                donor.put("profileImage", uri.toString());
                                saveDonorData(uid, donor, dialog);
                            }))
                    .addOnFailureListener(e -> {
                        dialog.dismiss();
                        Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show();
                    });
        } else {
            saveDonorData(uid, donor, dialog);
        }
    }

    private String safeText(EditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void saveDonorData(String uid, Map<String, Object> donor, ProgressDialog dialog) {
        db.collection("Donors").document(uid)
                .set(donor)
                .addOnSuccessListener(aVoid -> {
                    syncDonationCollection(uid, donor);
                    dialog.dismiss();
                    Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();

                    Intent i = new Intent(this, DonorDashboardActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Error saving: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // -----------------------------
    // 🔹 Sync Donations collection
    // -----------------------------
    private void syncDonationCollection(String uid, Map<String, Object> donor) {
        String fullName = (String) donor.getOrDefault("fullName", "Unknown");
        String bloodGroup = (String) donor.getOrDefault("bloodGroup", "-");
        List<String> selectedOrgans = donor.get("organsDonated") instanceof List
                ? (List<String>) donor.get("organsDonated") : new ArrayList<>();

        db.collection("Donations")
                .whereEqualTo("donorId", uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<String> existingOrgans = new ArrayList<>();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String organ = doc.getString("organType");
                        if (organ != null) existingOrgans.add(organ.toLowerCase());
                    }

                    // ✅ Add or update selected organs
                    for (String organ : selectedOrgans) {
                        String docId = uid + "_" + organ.replace(" ", "_").toLowerCase();
                        Map<String, Object> donation = new HashMap<>();
                        donation.put("donorId", uid);
                        donation.put("donorName", fullName);
                        donation.put("organType", organ);
                        donation.put("bloodGroup", bloodGroup);
                        donation.put("hospitalName", "Not assigned yet");
                        donation.put("date", DateFormat.getDateInstance().format(new Date()));
                        donation.put("status", "Pending");

                        db.collection("Donations").document(docId)
                                .set(donation)
                                .addOnSuccessListener(aVoid -> Log.d("DONATION_SYNC", "Added/Updated: " + organ))
                                .addOnFailureListener(e -> Log.e("DONATION_SYNC", "Failed to save " + organ + ": " + e.getMessage()));
                    }

                    // ❌ Delete unchecked organs
                    for (String organ : existingOrgans) {
                        if (!selectedOrgans.contains(organ)) {
                            String docId = uid + "_" + organ.replace(" ", "_").toLowerCase();
                            db.collection("Donations").document(docId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> Log.d("DONATION_SYNC", "Deleted unchecked organ: " + organ))
                                    .addOnFailureListener(e -> Log.e("DONATION_SYNC", "Failed to delete: " + e.getMessage()));
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("DONATION_SYNC", "Failed to load existing donations: " + e.getMessage()));
    }
}
