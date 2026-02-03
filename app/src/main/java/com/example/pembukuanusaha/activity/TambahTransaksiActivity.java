package com.example.pembukuanusaha.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pembukuanusaha.R;
import com.example.pembukuanusaha.database.DatabaseHelper;
import com.example.pembukuanusaha.model.Produk;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TambahTransaksiActivity extends AppCompatActivity {

    Spinner spinnerProduk;
    EditText edtJumlah;
    MaterialButton btnHitung;
    TextView txtHasil;

    DatabaseHelper db;
    List<Produk> produkList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_transaksi);

        spinnerProduk = findViewById(R.id.spinnerProduk);
        edtJumlah     = findViewById(R.id.edtJumlah);
        btnHitung     = findViewById(R.id.btnHitung);
        txtHasil      = findViewById(R.id.txtHasil);

        db = new DatabaseHelper(this);
        loadProduk();
        btnHitung.setOnClickListener(v -> simpanTransaksi());
    }

    private void loadProduk() {
        Cursor c = db.getAllProduk();
        List<String> namaProduk = new ArrayList<>();
        produkList.clear();

        if (c != null) {
            while (c.moveToNext()) {
                Produk p = new Produk(c.getInt(0), c.getString(1), c.getInt(2), c.getInt(3), c.getInt(4));
                produkList.add(p);
                namaProduk.add(p.getNama() + " (Stok: " + p.getStok() + ")"); // Tampilkan sisa stok di spinner
            }
            c.close();
        }

        if (produkList.isEmpty()) {
            Toast.makeText(this, "Belum ada produk. Tambahkan dulu!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, namaProduk);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProduk.setAdapter(adapter);
    }

    private void simpanTransaksi() {
        String jumlahStr = edtJumlah.getText().toString().trim();
        if (jumlahStr.isEmpty()) {
            edtJumlah.setError("Isi jumlah!");
            return;
        }

        int jumlah = Integer.parseInt(jumlahStr);
        if (jumlah <= 0) {
            edtJumlah.setError("Minimal 1");
            return;
        }

        Produk produk = produkList.get(spinnerProduk.getSelectedItemPosition());

        // CEK STOK
        if (jumlah > produk.getStok()) {
            Snackbar.make(btnHitung, "Stok tidak cukup! Sisa: " + produk.getStok(), Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getResources().getColor(android.R.color.holo_red_dark))
                    .show();
            return;
        }

        // HITUNG DATA
        int hargaJual  = produk.getHargaJual();
        int hargaModal = produk.getHargaModal();
        int laba       = (hargaJual - hargaModal) * jumlah;
        String tanggal = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // SIMPAN TRANSAKSI
        boolean sukses = db.insertTransaksi(produk.getNama(), hargaJual, hargaModal, jumlah, laba, tanggal);

        if (sukses) {
            // 🔥 UPDATE STOK BARU (KURANGI STOK)
            int stokBaru = produk.getStok() - jumlah;
            db.updateStokProduk(produk.getId(), stokBaru);

            Toast.makeText(this, "Transaksi Sukses! Laba: Rp " + laba, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Gagal menyimpan", Toast.LENGTH_SHORT).show();
        }
    }
}