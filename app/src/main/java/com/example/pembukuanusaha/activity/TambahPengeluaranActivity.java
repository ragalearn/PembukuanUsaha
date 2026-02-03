package com.example.pembukuanusaha.activity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pembukuanusaha.R;
import com.example.pembukuanusaha.database.DatabaseHelper;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TambahPengeluaranActivity extends AppCompatActivity {

    EditText edtNama, edtNominal;
    Spinner spinnerKategori;
    MaterialButton btnSimpan;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_pengeluaran);

        // Init View (ID Sesuai XML Baru)
        edtNama = findViewById(R.id.edtNama);
        edtNominal = findViewById(R.id.edtNominal);
        spinnerKategori = findViewById(R.id.spinnerKategori);
        btnSimpan = findViewById(R.id.btnSimpan);

        db = new DatabaseHelper(this);

        setupSpinner();

        // Listener Tombol
        btnSimpan.setOnClickListener(v -> simpanData());
    }

    private void setupSpinner() {
        // Daftar Kategori Standar (Sesuai kodemu sebelumnya)
        String[] kategori = {
                "Operasional (Listrik/Air/Internet)",
                "Gaji Karyawan",
                "Belanja Bahan Baku",
                "Sewa Tempat",
                "Transportasi",
                "Lain-lain"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, kategori);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKategori.setAdapter(adapter);
    }

    private void simpanData() {
        String nama = edtNama.getText().toString().trim();
        String nominalStr = edtNominal.getText().toString().trim();

        // Validasi Input Lebih Cantik (Muncul tanda seru merah di kolom)
        if (nama.isEmpty()) {
            edtNama.setError("Nama pengeluaran wajib diisi");
            edtNama.requestFocus();
            return;
        }

        if (nominalStr.isEmpty()) {
            edtNominal.setError("Nominal biaya wajib diisi");
            edtNominal.requestFocus();
            return;
        }

        try {
            int nominal = Integer.parseInt(nominalStr);
            String kategori = spinnerKategori.getSelectedItem().toString();
            String tanggal = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            // Panggil method Database milikmu (insertPengeluaran)
            boolean sukses = db.insertPengeluaran(nama, nominal, kategori, tanggal);

            if (sukses) {
                Toast.makeText(this, "Pengeluaran Berhasil Dicatat!", Toast.LENGTH_SHORT).show();
                finish(); // Kembali ke dashboard
            } else {
                Toast.makeText(this, "Gagal menyimpan data ke database", Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            edtNominal.setError("Format angka tidak valid");
        } catch (Exception e) {
            Toast.makeText(this, "Terjadi kesalahan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}