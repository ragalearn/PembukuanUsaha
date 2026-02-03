package com.example.pembukuanusaha.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pembukuanusaha.R;
import com.example.pembukuanusaha.database.DatabaseHelper;

public class InsightActivity extends AppCompatActivity {

    // =====================
    // VIEW
    // =====================
    TextView txtTerlaris, txtUntung, txtEmptyInsight;
    ProgressBar progressLoading;

    // =====================
    // DATABASE
    // =====================
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insight);

        // =====================
        // INIT VIEW
        // =====================
        txtTerlaris      = findViewById(R.id.txtTerlaris);
        txtUntung        = findViewById(R.id.txtUntung);
        txtEmptyInsight  = findViewById(R.id.txtEmptyInsight);
        progressLoading  = findViewById(R.id.progressLoading);

        // =====================
        // DATABASE
        // =====================
        db = new DatabaseHelper(this);

        // =====================
        // LOADING STATE AWAL
        // =====================
        showLoading();

        // =====================
        // DELAY UX (HALUS)
        // =====================
        new Handler(Looper.getMainLooper()).postDelayed(
                this::loadInsight,
                400
        );
    }

    // =====================
    // LOAD DATA INSIGHT
    // =====================
    private void loadInsight() {

        String produkTerlaris = db.getProdukTerlaris();
        String produkUntung   = db.getProdukPalingUntung();

        progressLoading.setVisibility(View.GONE);

        if ((produkTerlaris == null || produkTerlaris.isEmpty())
                && (produkUntung == null || produkUntung.isEmpty())) {

            showEmptyState();
            return;
        }

        txtTerlaris.setText(
                produkTerlaris != null && !produkTerlaris.isEmpty()
                        ? produkTerlaris
                        : "Belum ada data"
        );

        txtUntung.setText(
                produkUntung != null && !produkUntung.isEmpty()
                        ? produkUntung
                        : "Belum ada data"
        );

        txtTerlaris.setVisibility(View.VISIBLE);
        txtUntung.setVisibility(View.VISIBLE);
        txtEmptyInsight.setVisibility(View.GONE);
    }

    // =====================
    // UI STATE HANDLER
    // =====================
    private void showLoading() {
        progressLoading.setVisibility(View.VISIBLE);
        txtTerlaris.setVisibility(View.GONE);
        txtUntung.setVisibility(View.GONE);
        txtEmptyInsight.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        txtEmptyInsight.setVisibility(View.VISIBLE);
        txtTerlaris.setVisibility(View.GONE);
        txtUntung.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showLoading();
        new Handler(Looper.getMainLooper()).postDelayed(
                this::loadInsight,
                300
        );
    }
}
