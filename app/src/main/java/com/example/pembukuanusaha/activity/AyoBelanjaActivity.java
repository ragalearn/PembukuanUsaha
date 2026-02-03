package com.example.pembukuanusaha.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pembukuanusaha.R;
import com.example.pembukuanusaha.database.DatabaseHelper;
import com.example.pembukuanusaha.model.Produk;
import com.example.pembukuanusaha.session.SessionManager;
import com.example.pembukuanusaha.utils.RupiahFormatter; // Pakai formatter biar rapi
import com.google.android.material.button.MaterialButton; // Update ke MaterialButton

import java.util.ArrayList;
import java.util.List;

public class AyoBelanjaActivity extends AppCompatActivity {

    Spinner spinnerProduk;
    EditText edtJumlah;
    MaterialButton btnHitung; // Tipe baru
    TextView txtHasil;

    DatabaseHelper db;
    List<Produk> produkList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // =====================
        // SESSION CHECK (ADMIN ONLY)
        // =====================
        SessionManager session = new SessionManager(this);
        if (!session.isAdmin()) {
            Toast.makeText(this, "Akses Ditolak: Hanya Admin", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_ayo_belanja);

        // =====================
        // INIT VIEW
        // =====================
        spinnerProduk = findViewById(R.id.spinnerProduk);
        edtJumlah     = findViewById(R.id.edtJumlah);
        btnHitung     = findViewById(R.id.btnHitung);
        txtHasil      = findViewById(R.id.txtHasil);

        db = new DatabaseHelper(this);

        loadProduk();

        btnHitung.setOnClickListener(v -> hitungModal());
    }

    private void loadProduk() {
        produkList.clear();
        List<String> namaProduk = new ArrayList<>();

        Cursor c = db.getAllProduk();
        if (c != null) {
            while (c.moveToNext()) {
                Produk p = new Produk(
                        c.getInt(0),
                        c.getString(1),
                        c.getInt(2),
                        c.getInt(3),
                        c.getInt(4)
                );
                produkList.add(p);
                // Tampilkan nama + harga modal di spinner
                namaProduk.add(p.getNama() + " (@" + RupiahFormatter.format(p.getHargaModal()) + ")");
            }
            c.close();
        }

        if (produkList.isEmpty()) {
            Toast.makeText(this, "Belum ada produk di database", Toast.LENGTH_LONG).show();
            // Opsional: Matikan tombol jika kosong
            btnHitung.setEnabled(false);
            return;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                namaProduk
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProduk.setAdapter(adapter);
    }

    private void hitungModal() {
        String jumlahStr = edtJumlah.getText().toString().trim();

        if (jumlahStr.isEmpty()) {
            edtJumlah.setError("Masukkan jumlah beli");
            edtJumlah.requestFocus();
            return;
        }

        int jumlah;
        try {
            jumlah = Integer.parseInt(jumlahStr);
        } catch (NumberFormatException e) {
            edtJumlah.setError("Angka tidak valid");
            return;
        }

        if (jumlah <= 0) {
            edtJumlah.setError("Minimal 1");
            return;
        }

        if (produkList.isEmpty()) return;

        int posisi = spinnerProduk.getSelectedItemPosition();
        Produk produk = produkList.get(posisi);

        int totalModal = produk.getHargaModal() * jumlah;

        // Tampilkan Hasil dengan Format Rupiah
        txtHasil.setText(RupiahFormatter.format(totalModal));
    }
}