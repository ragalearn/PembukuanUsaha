package com.example.pembukuanusaha.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pembukuanusaha.R;
import com.example.pembukuanusaha.adapter.TransaksiAdapter;
import com.example.pembukuanusaha.database.DatabaseHelper;
import com.example.pembukuanusaha.model.Transaksi;
import com.example.pembukuanusaha.sync.FirestoreSyncHelper;

import java.util.ArrayList;
import java.util.List;

public class DaftarTransaksiActivity extends AppCompatActivity {

    RecyclerView recyclerTransaksi;
    TextView txtEmpty;
    DatabaseHelper db; // Kita pakai Database Lokal
    List<Transaksi> transaksiList;
    TransaksiAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_transaksi);

        recyclerTransaksi = findViewById(R.id.recyclerTransaksi);
        txtEmpty = findViewById(R.id.txtEmpty);

        recyclerTransaksi.setLayoutManager(new LinearLayoutManager(this));

        db = new DatabaseHelper(this);
        transaksiList = new ArrayList<>();
        adapter = new TransaksiAdapter(transaksiList);
        recyclerTransaksi.setAdapter(adapter);

        // 1. Tampilkan data dari HP (Cepat & Bisa Offline)
        loadDataLokal();

        // 2. Coba sync ke internet di background (tanpa ganggu user)
        FirestoreSyncHelper.syncTransaksi(this);
    }

    private void loadDataLokal() {
        transaksiList.clear();

        // AMBIL DARI SQLITE (LOKAL)
        Cursor c = db.getAllTransaksi();

        if (c != null && c.getCount() > 0) {
            while (c.moveToNext()) {
                // Pastikan urutan ambil datanya sesuai kolom database
                String tanggal = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_TANGGAL));
                String nama = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_NAMA));
                int jumlah = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_JUMLAH));
                int harga = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_HARGA_JUAL));
                int laba = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_LABA));

                transaksiList.add(new Transaksi(tanggal, nama, jumlah, harga, laba));
            }
            c.close();
        }

        // Update UI
        if (transaksiList.isEmpty()) {
            txtEmpty.setVisibility(View.VISIBLE);
            recyclerTransaksi.setVisibility(View.GONE);
        } else {
            txtEmpty.setVisibility(View.GONE);
            recyclerTransaksi.setVisibility(View.VISIBLE);
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDataLokal(); // Refresh data saat user kembali ke layar ini
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) db.close(); // Tutup koneksi database agar hemat memori
    }
}