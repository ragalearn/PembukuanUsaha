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

    // =====================
    // LOAD PRODUK KE SPINNER
    // =====================
    private void loadProduk() {
        Cursor c = db.getAllProduk();
        List<String> namaProduk = new ArrayList<>();
        produkList.clear();

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
                namaProduk.add(p.getNama());
            }
            c.close();
        }

        if (produkList.isEmpty()) {
            Snackbar.make(
                    findViewById(android.R.id.content),
                    "Belum ada produk. Tambahkan produk dulu.",
                    Snackbar.LENGTH_LONG
            ).show();
            finish();
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

    // =====================
    // SIMPAN TRANSAKSI
    // =====================
    private void simpanTransaksi() {

        String jumlahStr = edtJumlah.getText().toString().trim();
        if (jumlahStr.isEmpty()) {
            edtJumlah.setError("Jumlah wajib diisi");
            return;
        }

        int jumlah;
        try {
            jumlah = Integer.parseInt(jumlahStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Jumlah tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        if (jumlah <= 0) {
            edtJumlah.setError("Jumlah harus lebih dari 0");
            return;
        }

        Produk produk = produkList.get(spinnerProduk.getSelectedItemPosition());

        // CEK STOK
        if (jumlah > produk.getStok()) {
            Snackbar.make(
                    findViewById(android.R.id.content),
                    "Stok tidak mencukupi",
                    Snackbar.LENGTH_LONG
            ).show();
            return;
        }

        int hargaJual  = produk.getHargaJual();
        int hargaModal = produk.getHargaModal();
        int laba       = (hargaJual - hargaModal) * jumlah;

        String tanggal = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date());

        boolean sukses = db.insertTransaksi(
                produk.getNama(),
                hargaJual,
                hargaModal,
                jumlah,
                laba,
                tanggal
        );

        if (sukses) {
            txtHasil.setText("Laba: Rp " + laba);
            Snackbar.make(
                    findViewById(android.R.id.content),
                    "Transaksi berhasil disimpan",
                    Snackbar.LENGTH_LONG
            ).show();
            finish();
        } else {
            Snackbar.make(
                    findViewById(android.R.id.content),
                    "Gagal menyimpan transaksi",
                    Snackbar.LENGTH_LONG
            ).show();
        }
    }
}
