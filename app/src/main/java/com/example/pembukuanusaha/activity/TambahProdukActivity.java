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
        // INIT VIEW
        // =====================
        edtNama   = findViewById(R.id.edtNama);
        edtModal  = findViewById(R.id.edtModal);
        edtJual   = findViewById(R.id.edtJual);
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

        // === VALIDASI WAJIB ===
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

        int modal, jual, stok;

        try {
            modal = Integer.parseInt(modalStr);
            jual  = Integer.parseInt(jualStr);
            stok  = Integer.parseInt(stokStr);
        } catch (NumberFormatException e) {
            showError("Input angka tidak valid");
            return;
        }

        if (modal <= 0) {
            edtModal.setError("Harga modal harus lebih dari 0");
            edtModal.requestFocus();
            return;
        }

        if (jual <= 0) {
            edtJual.setError("Harga jual harus lebih dari 0");
            edtJual.requestFocus();
            return;
        }

        if (jual < modal) {
            edtJual.setError("Harga jual tidak boleh lebih kecil dari modal");
            edtJual.requestFocus();
            return;
        }

        if (stok < 0) {
            edtStok.setError("Stok tidak boleh negatif");
            edtStok.requestFocus();
            return;
        }

        // === SIMPAN KE DATABASE ===
        boolean success = db.insertProduk(nama, modal, jual, stok);

        if (success) {
            Snackbar.make(
                    findViewById(android.R.id.content),
                    "Produk berhasil disimpan ✅",
                    Snackbar.LENGTH_SHORT
            ).show();
            finish(); // kembali ke daftar produk
        } else {
            showError("Gagal menyimpan produk");
        }
    }

    // ======================
    // SNACKBAR ERROR GLOBAL
    // ======================
    private void showError(String message) {
        Snackbar.make(
                findViewById(android.R.id.content),
                message,
                Snackbar.LENGTH_LONG
        ).show();
    }
}
