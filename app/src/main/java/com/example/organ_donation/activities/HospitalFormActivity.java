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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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

public class HospitalFormActivity extends AppCompatActivity {

    private EditText etHospitalName, etLicenseNumber, etContactPerson, etPhone, etEmail, etAddress, etDescription;
    private Switch switchVerified;
    private ImageView imgHospitalLogo;
    private Button btnUploadLogo, btnSubmit;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private Uri logoUri;
    private static final int PICK_IMAGE_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_form);

        // 🔹 Firebase setup
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // 🔹 UI references
        etHospitalName = findViewById(R.id.etHospitalName);
        etLicenseNumber = findViewById(R.id.etLicenseNumber);
        etContactPerson = findViewById(R.id.etContactPerson);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etAddress = findViewById(R.id.etAddress);
        etDescription = findViewById(R.id.etDescription);
        switchVerified = findViewById(R.id.switchVerified);
        imgHospitalLogo = findViewById(R.id.imgHospitalLogo);
        btnUploadLogo = findViewById(R.id.btnUploadLogo);
        btnSubmit = findViewById(R.id.btnSubmit);

        checkStoragePermission();
        btnUploadLogo.setOnClickListener(v -> selectImage());
        btnSubmit.setOnClickListener(v -> saveHospitalProfile());

        loadExistingProfile();
    }

    // -----------------------------
    // 🔹 Select hospital logo
    // -----------------------------
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Hospital Logo"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            logoUri = data.getData();
            Glide.with(this).load(logoUri).circleCrop().into(imgHospitalLogo);
        }
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
    // 🔹 Load existing hospital profile
    // -----------------------------
    private void loadExistingProfile() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        db.collection("Hospitals").document(uid)
                .get()
                .addOnSuccessListener(this::fillExistingData)
                .addOnFailureListener(e -> Log.e("HOSPITAL_FORM", "Load failed: " + e.getMessage()));
    }

    private void fillExistingData(DocumentSnapshot doc) {
        if (!doc.exists()) return;

        etHospitalName.setText(doc.getString("hospitalName"));
        etLicenseNumber.setText(doc.getString("license"));
        etContactPerson.setText(doc.getString("contactPerson"));
        etPhone.setText(doc.getString("phone"));
        etEmail.setText(doc.getString("email"));
        etAddress.setText(doc.getString("address"));
        etDescription.setText(doc.getString("description"));
        switchVerified.setChecked("true".equals(doc.getString("verified")));

        String logoUrl = doc.getString("imageUrl");
        if (logoUrl != null && !logoUrl.isEmpty()) {
            Glide.with(this).load(logoUrl).circleCrop().into(imgHospitalLogo);
        }

        btnSubmit.setText("Update Profile");
    }

    // -----------------------------
    // 🔹 Save hospital data
    // -----------------------------
    private void saveHospitalProfile() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Saving hospital profile...");
        dialog.setCancelable(false);
        dialog.show();

        Map<String, Object> hospital = new HashMap<>();
        hospital.put("hospitalName", etHospitalName.getText().toString().trim());
        hospital.put("license", etLicenseNumber.getText().toString().trim());
        hospital.put("contactPerson", etContactPerson.getText().toString().trim());
        hospital.put("phone", etPhone.getText().toString().trim());
        hospital.put("email", etEmail.getText().toString().trim());
        hospital.put("address", etAddress.getText().toString().trim());
        hospital.put("description", etDescription.getText().toString().trim());
        hospital.put("verified", switchVerified.isChecked() ? "true" : "false");

        if (logoUri != null) {
            StorageReference imgRef = storageRef.child("hospital_logos/" + uid + ".jpg");
            imgRef.putFile(logoUri)
                    .addOnSuccessListener(taskSnapshot ->
                            imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                hospital.put("imageUrl", uri.toString());
                                saveToFirestore(uid, hospital, dialog);
                            }))
                    .addOnFailureListener(e -> {
                        dialog.dismiss();
                        Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            saveToFirestore(uid, hospital, dialog);
        }
    }

    private void saveToFirestore(String uid, Map<String, Object> hospital, ProgressDialog dialog) {
        db.collection("Hospitals").document(uid)
                .set(hospital)
                .addOnSuccessListener(aVoid -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(this, HospitalDashboardActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                })
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
