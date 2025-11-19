package com.example.organ_donation.activities;

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
import android.widget.*;
import com.bumptech.glide.Glide;
import com.example.organ_donation.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PatientFormActivity extends AppCompatActivity {

    private EditText etFullName, etAge, etPhone, etAddress, etMedicalHistory;
    private Spinner spinnerGender, spinnerBloodGroup;
    private ImageView imgProfile;
    private Button btnUpload, btnSave;
    private Uri imageUri;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private static final int PICK_IMAGE_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_form);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // UI setup
        etFullName = findViewById(R.id.etFullName);
        etAge = findViewById(R.id.etAge);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etMedicalHistory = findViewById(R.id.etMedicalHistory);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerBloodGroup = findViewById(R.id.spinnerBloodGroup);
        imgProfile = findViewById(R.id.imgProfile);
        btnUpload = findViewById(R.id.btnUpload);
        btnSave = findViewById(R.id.btnSave);

        setupSpinners();
        checkStoragePermission();

        btnUpload.setOnClickListener(v -> selectImage());
        btnSave.setOnClickListener(v -> savePatientProfile());

        loadExistingPatientData();
    }

    private void setupSpinners() {
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Select Gender", "Male", "Female", "Other"});
        spinnerGender.setAdapter(genderAdapter);

        ArrayAdapter<String> bloodAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Select Blood Group", "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"});
        spinnerBloodGroup.setAdapter(bloodAdapter);
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).circleCrop().into(imgProfile);
        }
    }

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

    private void loadExistingPatientData() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        db.collection("Patients").document(uid).get()
                .addOnSuccessListener(this::fillForm)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show());
    }

    private void fillForm(DocumentSnapshot doc) {
        if (!doc.exists()) return;
        etFullName.setText(doc.getString("fullName"));
        etAge.setText(doc.getString("age"));
        etPhone.setText(doc.getString("phone"));
        etAddress.setText(doc.getString("address"));
        etMedicalHistory.setText(doc.getString("medicalHistory"));
        selectSpinnerValue(spinnerGender, doc.getString("gender"));
        selectSpinnerValue(spinnerBloodGroup, doc.getString("bloodGroup"));

        String imageUrl = doc.getString("profileImage");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).circleCrop().into(imgProfile);
        }
    }

    private void selectSpinnerValue(Spinner spinner, String value) {
        if (value == null) return;
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        int pos = adapter.getPosition(value);
        if (pos >= 0) spinner.setSelection(pos);
    }

    private void savePatientProfile() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Saving profile...");
        dialog.setCancelable(false);
        dialog.show();

        Map<String, Object> patient = new HashMap<>();
        patient.put("fullName", etFullName.getText().toString().trim());
        patient.put("age", etAge.getText().toString().trim());
        patient.put("gender", spinnerGender.getSelectedItem().toString());
        patient.put("bloodGroup", spinnerBloodGroup.getSelectedItem().toString());
        patient.put("phone", etPhone.getText().toString().trim());
        patient.put("address", etAddress.getText().toString().trim());
        patient.put("medicalHistory", etMedicalHistory.getText().toString().trim());

        if (imageUri != null) {
            StorageReference imgRef = storageRef.child("patient_profiles/" + uid + ".jpg");
            imgRef.putFile(imageUri)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful())
                            throw Objects.requireNonNull(task.getException());
                        return imgRef.getDownloadUrl();
                    })
                    .addOnSuccessListener(uri -> {
                        patient.put("profileImage", uri.toString());
                        saveToFirestore(uid, patient, dialog);
                    })
                    .addOnFailureListener(e -> {
                        dialog.dismiss();
                        Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            saveToFirestore(uid, patient, dialog);
        }
    }

    private void saveToFirestore(String uid, Map<String, Object> patient, ProgressDialog dialog) {
        db.collection("Patients").document(uid)
                .set(patient)
                .addOnSuccessListener(aVoid -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, PatientDashboardActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                    finish();
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Error saving: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
