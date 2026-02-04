package com.example.pembukuanusaha.activity;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pembukuanusaha.R;
import com.example.pembukuanusaha.database.DatabaseHelper;
import com.example.pembukuanusaha.model.Produk;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TambahTransaksiActivity extends AppCompatActivity {

    // VIEW
    Spinner spinnerProduk;
    EditText edtJumlah, edtTanggal;
    MaterialButton btnSimpan;

    // DATA
    DatabaseHelper db;
    List<Produk> produkList = new ArrayList<>();
    Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_transaksi);

        // =====================
        // INIT VIEW
        // =====================
        spinnerProduk = findViewById(R.id.spinnerProduk);
        edtJumlah     = findViewById(R.id.edtJumlah);
        edtTanggal    = findViewById(R.id.edtTanggal);
        btnSimpan     = findViewById(R.id.btnSimpan);

        db = new DatabaseHelper(this);

        // 1. Setup Tanggal Default (Hari Ini)
        updateLabelTanggal();

        // 2. Listener Klik Tanggal (Muncul Kalender)
        edtTanggal.setOnClickListener(v -> showDatePicker());

        // 3. Load Produk
        loadProduk();

        // 4. Aksi Simpan
        btnSimpan.setOnClickListener(v -> simpanTransaksi());
    }

    private void loadProduk() {
        produkList.clear();
        List<String> namaProduk = new ArrayList<>();

        Cursor c = db.getAllProduk();
        if (c != null) {
            while (c.moveToNext()) {
                // 🔥 PERBAIKAN: Ambil URL Foto (Kolom ke-5)
                String imageUrl = null;
                try {
                    imageUrl = c.getString(5); // Ambil URL gambar jika ada
                } catch (Exception e) {
                    imageUrl = null;
                }

                // 🔥 PERBAIKAN: Gunakan Constructor 6 Parameter
                Produk p = new Produk(
                        c.getInt(0),      // ID
                        c.getString(1),   // Nama
                        c.getInt(2),      // Modal
                        c.getInt(3),      // Jual
                        c.getInt(4),      // Stok
                        imageUrl          // Foto (Image URL) -> INI YANG TADI KURANG
                );

                produkList.add(p);
                // Menampilkan nama & sisa stok di spinner
                namaProduk.add(p.getNama() + " (Sisa: " + p.getStok() + ")");
            }
            c.close();
        }

        if (produkList.isEmpty()) {
            Toast.makeText(this, "Stok kosong! Tambah produk dulu.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, namaProduk);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProduk.setAdapter(adapter);
    }

    // Tampilkan Dialog Kalender
    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabelTanggal();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    // Update Text di EditText Tanggal
    private void updateLabelTanggal() {
        String format = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        edtTanggal.setText(sdf.format(calendar.getTime()));
    }

    private void simpanTransaksi() {
        String jumlahStr = edtJumlah.getText().toString().trim();
        String tanggal   = edtTanggal.getText().toString().trim();

        // === VALIDASI INPUT ===
        if (TextUtils.isEmpty(jumlahStr)) {
            edtJumlah.setError("Isi jumlah terjual");
            return;
        }

        int jumlah = Integer.parseInt(jumlahStr);
        if (jumlah <= 0) {
            edtJumlah.setError("Minimal 1");
            return;
        }

        // Ambil Produk Terpilih
        if (produkList.isEmpty()) return;
        Produk produk = produkList.get(spinnerProduk.getSelectedItemPosition());

        // === CEK STOK ===
        if (jumlah > produk.getStok()) {
            Snackbar.make(btnSimpan, "Stok tidak cukup! Sisa: " + produk.getStok(), Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getResources().getColor(android.R.color.holo_red_dark))
                    .show();
            return;
        }

        // === HITUNG LABA ===
        int hargaJual  = produk.getHargaJual();
        int hargaModal = produk.getHargaModal();
        int laba       = (hargaJual - hargaModal) * jumlah;

        // === SIMPAN KE DATABASE ===
        boolean sukses = db.insertTransaksi(produk.getNama(), hargaJual, hargaModal, jumlah, laba, tanggal);

        if (sukses) {
            // Update Stok
            int stokBaru = produk.getStok() - jumlah;
            db.updateStokProduk(produk.getId(), stokBaru);

            // Feedback Sukses
            Snackbar.make(btnSimpan, "Transaksi Sukses! Laba: Rp " + laba, Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getResources().getColor(R.color.colorPrimary))
                    .show();

            // Delay sedikit sebelum tutup
            btnSimpan.postDelayed(this::finish, 1500);
        } else {
            Toast.makeText(this, "Gagal menyimpan transaksi", Toast.LENGTH_SHORT).show();
        }
    }
}