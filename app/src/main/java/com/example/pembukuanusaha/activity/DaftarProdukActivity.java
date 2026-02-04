package com.example.pembukuanusaha.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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
    // VIEW
    // =====================
    RecyclerView recyclerProduk;
    LinearLayout layoutEmpty;
    FloatingActionButton fabTambah;
    EditText edtCari;

    // =====================
    // DATA
    // =====================
    DatabaseHelper db;
    List<Produk> produkListMaster = new ArrayList<>();
    List<Produk> produkListDisplay = new ArrayList<>();
    ProdukAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // =====================
        // 🔐 ROLE PROTECTION
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

        // 🔥 PERBAIKAN 1: ADAPTER
        // Adapter baru butuh (Context, List). Context (this) harus di urutan pertama.
        adapter = new ProdukAdapter(this, produkListDisplay);
        recyclerProduk.setAdapter(adapter);

        // Load Data Awal
        loadData();

        // Listener Tambah
        fabTambah.setOnClickListener(v -> {
            startActivity(new Intent(DaftarProdukActivity.this, TambahProdukActivity.class));
        });

        // Listener Pencarian
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
                // 🔥 PERBAIKAN 2: AMBIL FOTO DARI DB
                String imageUrl = null;
                try {
                    // Cek apakah kolom image_url ada (biasanya index 5)
                    int imageIndex = c.getColumnIndex(DatabaseHelper.COL_PRODUK_IMAGE);
                    if (imageIndex != -1) {
                        imageUrl = c.getString(imageIndex);
                    }
                } catch (Exception e) {
                    imageUrl = null; // Aman jika kolom belum ada/error
                }

                // 🔥 PERBAIKAN 3: GUNAKAN CONSTRUCTOR 6 PARAMETER (Lengkap dengan Foto)
                produkListMaster.add(new Produk(
                        c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_PRODUK_ID)),
                        c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PRODUK_NAMA)),
                        c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_PRODUK_MODAL)),
                        c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_PRODUK_JUAL)),
                        c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_PRODUK_STOK)),
                        imageUrl // Masukkan URL Foto ke sini
                ));
            }
            c.close();
        }

        // Tampilkan data
        filterData(edtCari.getText().toString());
    }

    private void filterData(String keyword) {
        produkListDisplay.clear();

        if (keyword.isEmpty()) {
            produkListDisplay.addAll(produkListMaster);
        } else {
            String query = keyword.toLowerCase();
            for (Produk p : produkListMaster) {
                if (p.getNama().toLowerCase().contains(query)) {
                    produkListDisplay.add(p);
                }
            }
        }

        adapter.notifyDataSetChanged();

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
        loadData();
    }
}