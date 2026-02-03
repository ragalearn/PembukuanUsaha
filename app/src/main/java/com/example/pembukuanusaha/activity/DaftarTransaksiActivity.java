package com.example.pembukuanusaha.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pembukuanusaha.R;
import com.example.pembukuanusaha.adapter.TransaksiAdapter;
import com.example.pembukuanusaha.database.DatabaseHelper;
import com.example.pembukuanusaha.model.Transaksi;
import com.example.pembukuanusaha.sync.FirestoreSyncHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class DaftarTransaksiActivity extends AppCompatActivity {

    // =====================
    // VIEW
    // =====================
    RecyclerView recyclerTransaksi;
    LinearLayout txtEmpty; // Layout Empty State (Sesuai XML Baru)
    FloatingActionButton fabTambah;
    EditText edtCari; // FITUR BARU: Pencarian

    // =====================
    // DATA
    // =====================
    DatabaseHelper db;
    // Kita butuh 2 List untuk fitur Search:
    List<Transaksi> transaksiListMaster = new ArrayList<>();   // Menyimpan SEMUA data dari DB
    List<Transaksi> transaksiListDisplay = new ArrayList<>();  // Menyimpan data yang DITAMPILKAN (Hasil Filter)
    TransaksiAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_transaksi);

        // Init View
        recyclerTransaksi = findViewById(R.id.recyclerTransaksi);
        txtEmpty = findViewById(R.id.txtEmpty);
        fabTambah = findViewById(R.id.fabTambah);
        edtCari = findViewById(R.id.edtCari); // Init Search Bar

        recyclerTransaksi.setLayoutManager(new LinearLayoutManager(this));

        db = new DatabaseHelper(this);

        // Adapter menggunakan List Display (yang bisa berubah isinya sesuai pencarian)
        adapter = new TransaksiAdapter(transaksiListDisplay);
        recyclerTransaksi.setAdapter(adapter);

        // Aksi Tombol Tambah
        fabTambah.setOnClickListener(v -> {
            startActivity(new Intent(DaftarTransaksiActivity.this, TambahTransaksiActivity.class));
        });

        // 🔥 FITUR PENCARIAN REAL-TIME
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

        // 1. Load Data
        loadDataLokal();

        // 2. Sync Background (Fitur Lama Tetap Ada)
        FirestoreSyncHelper.syncTransaksi(this);
    }

    private void loadDataLokal() {
        transaksiListMaster.clear();

        Cursor c = db.getAllTransaksi();

        if (c != null && c.getCount() > 0) {
            while (c.moveToNext()) {
                String tanggal = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_TANGGAL));
                String nama = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_NAMA));
                int jumlah = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_JUMLAH));
                int harga = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_HARGA_JUAL));
                int laba = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_LABA));

                // Masukkan ke Master Data
                transaksiListMaster.add(new Transaksi(tanggal, nama, jumlah, harga, laba));
            }
            c.close();
        }

        // Tampilkan semua data di awal (Reset Filter)
        filterData("");
    }

    // Fungsi Pintar untuk Mencari Transaksi
    private void filterData(String keyword) {
        transaksiListDisplay.clear();

        if (keyword.isEmpty()) {
            // Jika pencarian kosong, tampilkan semua dari Master
            transaksiListDisplay.addAll(transaksiListMaster);
        } else {
            // Jika ada ketikan, cari yang cocok
            String query = keyword.toLowerCase();
            for (Transaksi t : transaksiListMaster) {
                // Cari berdasarkan Nama Produk
                if (t.getNamaProduk().toLowerCase().contains(query)) {
                    transaksiListDisplay.add(t);
                }
            }
        }

        adapter.notifyDataSetChanged();

        // Update Tampilan Kosong/Ada Isi
        if (transaksiListDisplay.isEmpty()) {
            txtEmpty.setVisibility(View.VISIBLE);
            recyclerTransaksi.setVisibility(View.GONE);
        } else {
            txtEmpty.setVisibility(View.GONE);
            recyclerTransaksi.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDataLokal(); // Refresh saat kembali
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) db.close();
    }
}