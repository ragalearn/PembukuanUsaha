package com.example.pembukuanusaha.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
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
import com.example.pembukuanusaha.R;
import com.example.pembukuanusaha.database.DatabaseHelper;
import com.example.pembukuanusaha.model.Produk;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
    String finalBase64Image = null; // 🔥 Pengganti URL Firebase
    Produk produkEdit;
    boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_produk);

        // Init DB (Firebase dibuang)
        db = new DatabaseHelper(this);

        // Init Views
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

        // 🔥 LOGIKA BARU: Tampilkan foto dari kode Base64
        if (produkEdit.getImageUrl() != null && !produkEdit.getImageUrl().isEmpty()) {
            layoutPlaceholder.setVisibility(View.GONE);
            try {
                byte[] decodedString = Base64.decode(produkEdit.getImageUrl(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imgPreview.setImageBitmap(decodedByte);
                finalBase64Image = produkEdit.getImageUrl(); // Simpan data lama
            } catch (Exception e) {
                // Fallback jika data lama masih berupa URL/rusak
                Glide.with(this).load(produkEdit.getImageUrl()).into(imgPreview);
                finalBase64Image = produkEdit.getImageUrl();
            }
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

                    try {
                        // 🔥 Ambil Gambar & Langsung Ubah ke Base64
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                        imgPreview.setImageBitmap(bitmap);
                        layoutPlaceholder.setVisibility(View.GONE);

                        // Kompres dan simpan ke variabel string
                        finalBase64Image = bitmapToBase64(bitmap);

                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Gagal ambil gambar", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    // 🔥 Fungsi Sakti: Ubah Gambar jadi Teks
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // Kompres 50% biar database gak berat
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

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

        // 🔥 LANGSUNG SIMPAN KE DB (Tanpa Upload)
        saveToDatabase(nama, modal, jual, stok, finalBase64Image);
    }

    private void saveToDatabase(String nama, int modal, int jual, int stok, String imageCode) {
        boolean success;

        if (isEditMode) {
            success = db.updateProduk(produkEdit.getId(), nama, modal, jual, stok, imageCode);
        } else {
            success = db.insertProduk(nama, modal, jual, stok, imageCode);
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