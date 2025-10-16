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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DonorFormActivity extends AppCompatActivity {

    private EditText etFullName, etAge, etPhone, etAddress, etHealthConditions, etEmergencyContact;
    private Spinner spinnerGender, spinnerBloodGroup;
    private Switch switchAvailability;
    private Button btnSubmit, btnUploadImage;
    private ImageView imgProfilePreview;

    private GridLayout gridOrgans;
    private List<CheckBox> organCheckBoxes = new ArrayList<>();

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

        // UI components
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
        gridOrgans = findViewById(R.id.gridOrgans);

        // Collect CheckBoxes from GridLayout
        for (int i = 0; i < gridOrgans.getChildCount(); i++) {
            if (gridOrgans.getChildAt(i) instanceof CheckBox) {
                organCheckBoxes.add((CheckBox) gridOrgans.getChildAt(i));
            }
        }

        checkStoragePermission();
        setupSpinners();
        loadExistingData();

        btnUploadImage.setOnClickListener(v -> selectImage());
        btnSubmit.setOnClickListener(v -> saveProfile());
    }

    // ✅ Custom spinner adapters with visible text colors
    private void setupSpinners() {
        // ---- Gender ----
        String[] genders = {"Select Gender", "Male", "Female", "Other"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, genders) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;
                if (position == 0) {
                    textView.setTextColor(Color.parseColor("#777777")); // hint
                } else {
                    textView.setTextColor(Color.parseColor("#000000")); // black selected
                }
                textView.setTextSize(16);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(Color.WHITE);
                textView.setBackgroundColor(Color.parseColor("#1C1C1C"));
                return view;
            }
        };
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        // ---- Blood Group ----
        String[] bloodGroups = {"Select Blood Group", "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"};
        ArrayAdapter<String> bloodAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, bloodGroups) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;
                if (position == 0) {
                    textView.setTextColor(Color.parseColor("#777777")); // hint
                } else {
                    textView.setTextColor(Color.parseColor("#000000")); // selected
                }
                textView.setTextSize(16);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(Color.WHITE);
                textView.setBackgroundColor(Color.parseColor("#1C1C1C"));
                return view;
            }
        };
        bloodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBloodGroup.setAdapter(bloodAdapter);
    }

    // Permission
    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 1);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    // Select Image
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).circleCrop().into(imgProfilePreview);
        }
    }

    // Load data
    private void loadExistingData() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        db.collection("Donors").document(uid)
                .get()
                .addOnSuccessListener(this::fillFormWithExistingData)
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show());
    }

    // Fill Form
    private void fillFormWithExistingData(DocumentSnapshot doc) {
        if (!doc.exists()) return;
        try {
            etFullName.setText(doc.getString("fullName"));
            etAge.setText(doc.getString("age"));
            etPhone.setText(doc.getString("phone"));
            etAddress.setText(doc.getString("address"));
            etHealthConditions.setText(doc.getString("healthConditions"));
            etEmergencyContact.setText(doc.getString("emergencyContact"));

            String gender = doc.getString("gender");
            if (gender != null) {
                ArrayAdapter adapter = (ArrayAdapter) spinnerGender.getAdapter();
                spinnerGender.setSelection(adapter.getPosition(gender));
            }

            String blood = doc.getString("bloodGroup");
            if (blood != null) {
                ArrayAdapter adapter = (ArrayAdapter) spinnerBloodGroup.getAdapter();
                spinnerBloodGroup.setSelection(adapter.getPosition(blood));
            }

            Boolean available = doc.getBoolean("available");
            switchAvailability.setChecked(available != null && available);

            String imageUrl = doc.getString("profileImage");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this).load(imageUrl).circleCrop().into(imgProfilePreview);
            }

            // ✅ Safely get organ list
            List<String> organs = new ArrayList<>();
            Object organsObj = doc.get("organsDonated");
            if (organsObj instanceof List<?>) {
                for (Object o : (List<?>) organsObj) {
                    if (o instanceof String) organs.add(((String) o).trim().toLowerCase());
                }
            }

            // ✅ Pre-check the correct boxes
            for (CheckBox cb : organCheckBoxes) {
                String organText = cb.getText().toString().trim().toLowerCase();
                cb.setChecked(organs.contains(organText));
            }

            btnSubmit.setText("Update Profile");
        } catch (Exception e) {
            Log.e("FORM_LOAD", "Error filling form: " + e.getMessage());
        }
    }

    // Save Data
    private void saveProfile() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Saving profile...");
        dialog.setCancelable(false);
        dialog.show();

        Map<String, Object> donor = new HashMap<>();
        donor.put("fullName", safeText(etFullName));
        donor.put("age", safeText(etAge));
        donor.put("gender", spinnerGender.getSelectedItem().toString());
        donor.put("bloodGroup", spinnerBloodGroup.getSelectedItem().toString());
        donor.put("phone", safeText(etPhone));
        donor.put("address", safeText(etAddress));
        donor.put("healthConditions", safeText(etHealthConditions));
        donor.put("emergencyContact", safeText(etEmergencyContact));
        donor.put("available", switchAvailability.isChecked());

        // ✅ Collect selected organs
        List<String> selectedOrgans = new ArrayList<>();
        for (CheckBox cb : organCheckBoxes) {
            if (cb.isChecked()) selectedOrgans.add(cb.getText().toString().trim());
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

    // Save Firestore and redirect
    private void saveDonorData(String uid, Map<String, Object> donor, ProgressDialog dialog) {
        db.collection("Donors").document(uid)
                .set(donor)
                .addOnSuccessListener(aVoid -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();

                    // Redirect to Dashboard
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
}
