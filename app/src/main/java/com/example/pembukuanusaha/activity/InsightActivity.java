package com.example.pembukuanusaha.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

        txtTerlaris = findViewById(R.id.txtTerlaris);
        txtPalingUntung = findViewById(R.id.txtPalingUntung);

        db = new DatabaseHelper(this);

        // Sedikit delay agar transisi terasa smooth
        new Handler(Looper.getMainLooper()).postDelayed(this::loadInsightData, 200);
    }

    private void loadInsightData() {
        // Ambil data dari metode canggih di DatabaseHelper
        String terlaris = db.getProdukTerlaris();
        String palingUntung = db.getProdukPalingUntung();

        // Tampilkan ke layar
        txtTerlaris.setText(terlaris);
        txtPalingUntung.setText(palingUntung);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data saat user kembali ke halaman ini
        loadInsightData();
    }
}