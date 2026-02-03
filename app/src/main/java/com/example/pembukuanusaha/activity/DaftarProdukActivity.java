package com.example.pembukuanusaha.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

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
    // VIEW
    // =====================
    RecyclerView recyclerProduk;
    TextView txtEmptyProduk;
    ProgressBar progressLoading;
    FloatingActionButton fabTambahProduk;

    // =====================
    // DATA
    // =====================
    List<Produk> produkList;
    ProdukAdapter adapter;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // =====================
        // 🔐 ROLE PROTECTION (ANTI BYPASS)
        // =====================
        SessionManager session = new SessionManager(this);
        if (!session.isAdmin()) {
            finish();
            return;
        }

        setContentView(R.layout.activity_daftar_produk);

        // =====================
        // INIT VIEW
        // =====================
        recyclerProduk   = findViewById(R.id.recyclerProduk);
        txtEmptyProduk   = findViewById(R.id.txtEmptyProduk);
        progressLoading  = findViewById(R.id.progressLoading);
        fabTambahProduk  = findViewById(R.id.fabTambahProduk);

        recyclerProduk.setLayoutManager(new LinearLayoutManager(this));

        // =====================
        // DATABASE
        // =====================
        db = new DatabaseHelper(this);

        produkList = new ArrayList<>();
        adapter = new ProdukAdapter(produkList, db);
        recyclerProduk.setAdapter(adapter);

        // =====================
        // LOADING STATE AWAL
        // =====================
        showLoading();

        // =====================
        // DELAY UX (HALUS & MANUSIAWI)
        // =====================
        new Handler(Looper.getMainLooper()).postDelayed(
                this::loadData,
                400
        );

        // =====================
        // FAB TAMBAH PRODUK
        // =====================
        fabTambahProduk.setOnClickListener(v ->
                startActivity(new Intent(
                        DaftarProdukActivity.this,
                        TambahProdukActivity.class
                ))
        );
    }

    // =====================
    // LOAD DATA PRODUK
    // =====================
    private void loadData() {
        produkList.clear();

        Cursor c = db.getAllProduk();
        if (c != null) {
            while (c.moveToNext()) {
                produkList.add(new Produk(
                        c.getInt(0),
                        c.getString(1),
                        c.getInt(2),
                        c.getInt(3),
                        c.getInt(4)
                ));
            }
            c.close();
        }

        progressLoading.setVisibility(View.GONE);
        updateUI();
        adapter.notifyDataSetChanged();
    }

    // =====================
    // UI STATE HANDLER
    // =====================
    private void showLoading() {
        progressLoading.setVisibility(View.VISIBLE);
        recyclerProduk.setVisibility(View.GONE);
        txtEmptyProduk.setVisibility(View.GONE);
    }

    private void updateUI() {
        if (produkList.isEmpty()) {
            txtEmptyProduk.setVisibility(View.VISIBLE);
            recyclerProduk.setVisibility(View.GONE);
        } else {
            txtEmptyProduk.setVisibility(View.GONE);
            recyclerProduk.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showLoading();
        new Handler(Looper.getMainLooper()).postDelayed(
                this::loadData,
                300
        );
    }
}
