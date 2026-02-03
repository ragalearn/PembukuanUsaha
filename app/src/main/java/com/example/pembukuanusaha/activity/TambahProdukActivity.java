package com.example.pembukuanusaha.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pembukuanusaha.R;
import com.example.pembukuanusaha.database.DatabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

public class TambahProdukActivity extends AppCompatActivity {

    // =====================
    // VIEW
    // =====================
    EditText edtNama, edtModal, edtJual, edtStok;
    MaterialButton btnSimpan;

    // =====================
    // DATABASE
    // =====================
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_produk);

        // =====================
        // INIT VIEW (SESUAIKAN ID DENGAN XML BARU)
        // =====================
        // Perhatikan ID di sini berubah sesuai desain baru (edtNamaProduk, dll)
        edtNama   = findViewById(R.id.edtNamaProduk);
        edtModal  = findViewById(R.id.edtHargaModal);
        edtJual   = findViewById(R.id.edtHargaJual);
        edtStok   = findViewById(R.id.edtStok);
        btnSimpan = findViewById(R.id.btnSimpan);

        // =====================
        // DATABASE
        // =====================
        db = new DatabaseHelper(this);

        btnSimpan.setOnClickListener(v -> simpanProduk());
    }

    // ======================
    // VALIDASI & SIMPAN PRODUK
    // ======================
    private void simpanProduk() {

        String nama     = edtNama.getText().toString().trim();
        String modalStr = edtModal.getText().toString().trim();
        String jualStr  = edtJual.getText().toString().trim();
        String stokStr  = edtStok.getText().toString().trim();

        // === 1. VALIDASI INPUT KOSONG ===
        if (TextUtils.isEmpty(nama)) {
            edtNama.setError("Nama produk wajib diisi");
            edtNama.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(modalStr)) {
            edtModal.setError("Harga modal wajib diisi");
            edtModal.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(jualStr)) {
            edtJual.setError("Harga jual wajib diisi");
            edtJual.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(stokStr)) {
            edtStok.setError("Stok wajib diisi");
            edtStok.requestFocus();
            return;
        }

        // === 2. VALIDASI ANGKA & LOGIKA BISNIS ===
        int modal, jual, stok;

        try {
            modal = Integer.parseInt(modalStr);
            jual  = Integer.parseInt(jualStr);
            stok  = Integer.parseInt(stokStr);
        } catch (NumberFormatException e) {
            showError("Pastikan harga dan stok berupa angka valid");
            return;
        }

        if (modal <= 0) {
            edtModal.setError("Harga modal harus lebih dari 0");
            return;
        }

        if (jual <= 0) {
            edtJual.setError("Harga jual harus lebih dari 0");
            return;
        }

        // 🔥 Fitur Cerdas: Cek Anti Rugi
        if (jual < modal) {
            edtJual.setError("Awas Rugi! Harga jual lebih kecil dari modal");
            edtJual.requestFocus();
            return;
        }

        if (stok < 0) {
            edtStok.setError("Stok tidak boleh negatif");
            return;
        }

        // === 3. SIMPAN KE DATABASE ===
        // Menggunakan method insertProduk milikmu
        boolean success = db.insertProduk(nama, modal, jual, stok);

        if (success) {
            Snackbar.make(
                    findViewById(android.R.id.content),
                    "Produk berhasil disimpan ke Gudang! 📦",
                    Snackbar.LENGTH_SHORT
            ).setBackgroundTint(getColor(R.color.colorPrimary)).show(); // Warna snackbar hijau

            finish(); // Kembali ke daftar produk
        } else {
            showError("Gagal menyimpan produk. Coba nama lain.");
        }
    }

    // ======================
    // SNACKBAR ERROR
    // ======================
    private void showError(String message) {
        Snackbar.make(
                findViewById(android.R.id.content),
                message,
                Snackbar.LENGTH_LONG
        ).setBackgroundTint(getColor(R.color.red_error)).show(); // Warna snackbar merah
    }
}