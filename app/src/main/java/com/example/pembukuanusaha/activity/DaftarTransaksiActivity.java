package com.example.pembukuanusaha.activity;

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
import com.example.pembukuanusaha.adapter.TransaksiAdapter;
import com.example.pembukuanusaha.model.Transaksi;
import com.example.pembukuanusaha.session.SessionManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class DaftarTransaksiActivity extends AppCompatActivity {

    // =====================
    // VIEW
    // =====================
    RecyclerView recyclerTransaksi;
    TextView txtEmpty;
    ProgressBar progressLoading;

    // =====================
    // DATA
    // =====================
    List<Transaksi> transaksiList;
    TransaksiAdapter adapter;

    // =====================
    // FIRESTORE & SESSION
    // =====================
    FirebaseFirestore firestore;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_transaksi);

        // =====================
        // INIT VIEW
        // =====================
        recyclerTransaksi = findViewById(R.id.recyclerTransaksi);
        txtEmpty = findViewById(R.id.txtEmpty);
        progressLoading = findViewById(R.id.progressLoading);

        recyclerTransaksi.setLayoutManager(new LinearLayoutManager(this));

        transaksiList = new ArrayList<>();
        adapter = new TransaksiAdapter(transaksiList);
        recyclerTransaksi.setAdapter(adapter);

        // =====================
        // INIT FIRESTORE & SESSION
        // =====================
        firestore = FirebaseFirestore.getInstance();
        session = new SessionManager(this);

        // =====================
        // LOADING STATE AWAL
        // =====================
        showLoading();

        // =====================
        // DELAY UX (OPSIONAL TAPI CAKEP)
        // =====================
        new Handler(Looper.getMainLooper()).postDelayed(
                this::loadTransaksiRealtime,
                500
        );
    }

    // =====================
    // REALTIME TRANSAKSI
    // =====================
    private void loadTransaksiRealtime() {

        String usahaId = session.getUsahaId();
        String cabangId = session.getCabangId();

        if (usahaId == null || cabangId == null ||
                usahaId.isEmpty() || cabangId.isEmpty()) {

            showEmptyState();
            return;
        }

        firestore.collection("usaha")
                .document(usahaId)
                .collection("cabang")
                .document(cabangId)
                .collection("transaksi")
                .orderBy("created_at")
                .addSnapshotListener((snapshots, error) -> {

                    progressLoading.setVisibility(View.GONE);

                    if (error != null || snapshots == null) {
                        showEmptyState();
                        return;
                    }

                    transaksiList.clear();

                    for (DocumentSnapshot doc : snapshots) {

                        Transaksi transaksi = new Transaksi(
                                doc.getString("tanggal"),
                                doc.getString("nama_produk"),
                                doc.getLong("jumlah") != null
                                        ? doc.getLong("jumlah").intValue() : 0,
                                doc.getLong("harga_jual") != null
                                        ? doc.getLong("harga_jual").intValue() : 0,
                                doc.getLong("laba") != null
                                        ? doc.getLong("laba").intValue() : 0
                        );

                        transaksiList.add(transaksi);
                    }

                    updateUI();
                    adapter.notifyDataSetChanged();
                });
    }

    // =====================
    // UI STATE HANDLER
    // =====================
    private void showLoading() {
        progressLoading.setVisibility(View.VISIBLE);
        recyclerTransaksi.setVisibility(View.GONE);
        txtEmpty.setVisibility(View.GONE);
    }

    private void updateUI() {
        if (transaksiList.isEmpty()) {
            showEmptyState();
        } else {
            txtEmpty.setVisibility(View.GONE);
            recyclerTransaksi.setVisibility(View.VISIBLE);
        }
    }

    private void showEmptyState() {
        progressLoading.setVisibility(View.GONE);
        txtEmpty.setVisibility(View.VISIBLE);
        recyclerTransaksi.setVisibility(View.GONE);
    }
}
