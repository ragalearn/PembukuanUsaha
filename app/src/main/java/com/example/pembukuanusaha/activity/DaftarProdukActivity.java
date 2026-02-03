package com.example.pembukuanusaha.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pembukuanusaha.R;
import com.example.pembukuanusaha.adapter.ProdukAdapter;
import com.example.pembukuanusaha.database.DatabaseHelper;
import com.example.pembukuanusaha.model.Produk;
import com.example.pembukuanusaha.session.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class DaftarProdukActivity extends AppCompatActivity {

    // =====================
    // VIEW (Sesuai XML Baru)
    // =====================
    RecyclerView recyclerProduk;
    LinearLayout layoutEmpty; // Dulu TextView, sekarang Layout agar lebih cantik
    FloatingActionButton fabTambah;
    EditText edtCari; // FITUR BARU: Kolom Pencarian

    // =====================
    // DATA
    // =====================
    DatabaseHelper db;
    List<Produk> produkListMaster = new ArrayList<>(); // Menyimpan SEMUA data
    List<Produk> produkListDisplay = new ArrayList<>(); // Menyimpan data yang DITAMPILKAN (Hasil Filter)
    ProdukAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // =====================
        // 🔐 ROLE PROTECTION (Kode Lamamu)
        // =====================
        SessionManager session = new SessionManager(this);
        if (!session.isAdmin()) {
            Toast.makeText(this, "Akses Ditolak: Khusus Admin", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_daftar_produk);

        // =====================
        // INIT VIEW
        // =====================
        recyclerProduk = findViewById(R.id.recyclerProduk);
        layoutEmpty    = findViewById(R.id.layoutEmpty);
        fabTambah      = findViewById(R.id.fabTambah);
        edtCari        = findViewById(R.id.edtCari);

        recyclerProduk.setLayoutManager(new LinearLayoutManager(this));
        db = new DatabaseHelper(this);

        // Init Adapter dengan List Display (Kosong dulu)
        adapter = new ProdukAdapter(produkListDisplay, db);
        recyclerProduk.setAdapter(adapter);

        // =====================
        // LOAD DATA
        // =====================
        loadData();

        // =====================
        // LISTENER
        // =====================
        fabTambah.setOnClickListener(v -> {
            startActivity(new Intent(DaftarProdukActivity.this, TambahProdukActivity.class));
        });

        // 🔥 LOGIKA PENCARIAN (REAL-TIME)
        edtCari.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadData() {
        produkListMaster.clear();

        Cursor c = db.getAllProduk();
        if (c != null && c.getCount() > 0) {
            while (c.moveToNext()) {
                produkListMaster.add(new Produk(
                        c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_PRODUK_ID)),
                        c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PRODUK_NAMA)),
                        c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_PRODUK_MODAL)),
                        c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_PRODUK_JUAL)),
                        c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_PRODUK_STOK))
                ));
            }
            c.close();
        }

        // Setelah data diambil, tampilkan semua (reset filter)
        filterData("");
    }

    // Fungsi Pintar untuk Mencari Barang
    private void filterData(String keyword) {
        produkListDisplay.clear();

        if (keyword.isEmpty()) {
            // Jika pencarian kosong, tampilkan semua
            produkListDisplay.addAll(produkListMaster);
        } else {
            // Jika ada ketikan, cari yang cocok
            String query = keyword.toLowerCase();
            for (Produk p : produkListMaster) {
                if (p.getNama().toLowerCase().contains(query)) {
                    produkListDisplay.add(p);
                }
            }
        }

        // Update UI
        adapter.notifyDataSetChanged();

        // Cek apakah hasil pencarian kosong?
        if (produkListDisplay.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerProduk.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerProduk.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data saat kembali dari halaman Tambah/Edit
        loadData();
    }
}