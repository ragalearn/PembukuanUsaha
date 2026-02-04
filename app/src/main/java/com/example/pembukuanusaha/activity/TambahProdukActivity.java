package com.example.pembukuanusaha.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.pembukuanusaha.R; // 🔥 PASTIKAN IMPORT INI ADA DAN BENAR
import com.example.pembukuanusaha.database.DatabaseHelper;
import com.example.pembukuanusaha.model.Produk;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class TambahProdukActivity extends AppCompatActivity {

    // Views
    TextView txtJudul;
    TextInputEditText edtNama, edtModal, edtJual, edtStok;
    MaterialButton btnSimpan;
    FrameLayout btnPilihFoto;
    ImageView imgPreview;
    LinearLayout layoutPlaceholder;
    ProgressBar progressBar;

    // Data
    DatabaseHelper db;
    Uri selectedImageUri;
    Produk produkEdit;
    boolean isEditMode = false;

    // Firebase Storage
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_produk); // XML Final

        // Init DB & Firebase
        db = new DatabaseHelper(this);
        storageRef = FirebaseStorage.getInstance().getReference("produk_images");

        // Init Views (ID ini sudah sinkron dengan XML Final)
        txtJudul = findViewById(R.id.txtJudul);
        edtNama = findViewById(R.id.edtNamaProduk);
        edtModal = findViewById(R.id.edtHargaModal);
        edtJual = findViewById(R.id.edtHargaJual);
        edtStok = findViewById(R.id.edtStok);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnPilihFoto = findViewById(R.id.btnPilihFoto);
        imgPreview = findViewById(R.id.imgPreview);
        layoutPlaceholder = findViewById(R.id.layoutPlaceholder);
        progressBar = findViewById(R.id.progressBar);

        // Mode Edit?
        if (getIntent().hasExtra("extra_produk")) {
            isEditMode = true;
            produkEdit = (Produk) getIntent().getSerializableExtra("extra_produk");
            setupEditMode();
        }

        // Listeners
        btnPilihFoto.setOnClickListener(v -> openGallery());
        btnSimpan.setOnClickListener(v -> validateAndSave());
    }

    private void setupEditMode() {
        txtJudul.setText("Edit Produk");
        btnSimpan.setText("UPDATE PRODUK");

        edtNama.setText(produkEdit.getNama());
        edtModal.setText(String.valueOf(produkEdit.getHargaModal()));
        edtJual.setText(String.valueOf(produkEdit.getHargaJual()));
        edtStok.setText(String.valueOf(produkEdit.getStok()));

        if (produkEdit.getImageUrl() != null && !produkEdit.getImageUrl().isEmpty()) {
            layoutPlaceholder.setVisibility(View.GONE);
            Glide.with(this).load(produkEdit.getImageUrl()).into(imgPreview);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    imgPreview.setImageURI(selectedImageUri);
                    layoutPlaceholder.setVisibility(View.GONE);
                }
            }
    );

    private void validateAndSave() {
        String nama = edtNama.getText().toString().trim();
        String modalStr = edtModal.getText().toString().trim();
        String jualStr = edtJual.getText().toString().trim();
        String stokStr = edtStok.getText().toString().trim();

        if (TextUtils.isEmpty(nama) || TextUtils.isEmpty(modalStr) ||
                TextUtils.isEmpty(jualStr) || TextUtils.isEmpty(stokStr)) {
            Toast.makeText(this, "Isi semua data!", Toast.LENGTH_SHORT).show();
            return;
        }

        int modal = Integer.parseInt(modalStr);
        int jual = Integer.parseInt(jualStr);
        int stok = Integer.parseInt(stokStr);

        if (jual < modal) {
            edtJual.setError("Rugi dong! Harga jual harus lebih besar dari modal");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSimpan.setEnabled(false);

        if (selectedImageUri != null) {
            uploadImageAndSave(nama, modal, jual, stok);
        } else {
            String imageUrl = isEditMode ? produkEdit.getImageUrl() : null;
            saveToDatabase(nama, modal, jual, stok, imageUrl);
        }
    }

    private void uploadImageAndSave(String nama, int modal, int jual, int stok) {
        String fileName = "produk_" + System.currentTimeMillis() + ".jpg";
        StorageReference fileRef = storageRef.child(fileName);

        fileRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    saveToDatabase(nama, modal, jual, stok, downloadUrl);
                }))
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSimpan.setEnabled(true);
                    Toast.makeText(this, "Gagal upload foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveToDatabase(String nama, int modal, int jual, int stok, String imageUrl) {
        boolean success;

        if (isEditMode) {
            success = db.updateProduk(produkEdit.getId(), nama, modal, jual, stok, imageUrl);
        } else {
            success = db.insertProduk(nama, modal, jual, stok, imageUrl);
        }

        progressBar.setVisibility(View.GONE);
        btnSimpan.setEnabled(true);

        if (success) {
            Toast.makeText(this, isEditMode ? "Produk Diupdate!" : "Produk Disimpan!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Gagal menyimpan ke database", Toast.LENGTH_SHORT).show();
        }
    }
}