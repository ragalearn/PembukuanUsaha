package com.example.pembukuanusaha.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pembukuanusaha.R;
import com.example.pembukuanusaha.database.DatabaseHelper;

public class InsightActivity extends AppCompatActivity {

    TextView txtTerlaris, txtPalingUntung;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insight);

        // Init View sesuai ID di XML baru
        txtTerlaris = findViewById(R.id.txtTerlaris);
        txtPalingUntung = findViewById(R.id.txtPalingUntung);

        db = new DatabaseHelper(this);

        loadInsightData();
    }

    private void loadInsightData() {
        // 1. LOGIKA PRODUK TERLARIS (Qty Terbanyak)
        // Kita butuh format: "Nama Produk (10 pcs)"
        String terlaris = "Belum ada data";
        try {
            // Query: Cari produk dengan total jumlah terjual tertinggi
            String query = "SELECT " + DatabaseHelper.COL_NAMA + ", SUM(" + DatabaseHelper.COL_JUMLAH + ") as total_qty " +
                    "FROM " + DatabaseHelper.TABLE_TRANSAKSI + " " +
                    "GROUP BY " + DatabaseHelper.COL_NAMA + " " +
                    "ORDER BY total_qty DESC LIMIT 1";

            Cursor c = db.getReadableDatabase().rawQuery(query, null);
            if (c.moveToFirst()) {
                String nama = c.getString(0);
                int qty = c.getInt(1);
                terlaris = nama + " (" + qty + " pcs)";
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        txtTerlaris.setText(terlaris);


        // 2. LOGIKA JUARA CUAN (Total Laba Terbesar)
        // Kita butuh format: "Nama Produk"
        String cuan = "Belum ada data";
        try {
            // Query: Cari produk dengan total laba tertinggi
            String query = "SELECT " + DatabaseHelper.COL_NAMA + ", SUM(" + DatabaseHelper.COL_LABA + ") as total_laba " +
                    "FROM " + DatabaseHelper.TABLE_TRANSAKSI + " " +
                    "GROUP BY " + DatabaseHelper.COL_NAMA + " " +
                    "ORDER BY total_laba DESC LIMIT 1";

            Cursor c = db.getReadableDatabase().rawQuery(query, null);
            if (c.moveToFirst()) {
                String nama = c.getString(0);
                cuan = nama; // Hanya nama produk
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        txtPalingUntung.setText(cuan);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data saat kembali ke halaman ini
        loadInsightData();
    }
}