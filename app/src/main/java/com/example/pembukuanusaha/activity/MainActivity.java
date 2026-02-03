package com.example.pembukuanusaha.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pembukuanusaha.R;
import com.example.pembukuanusaha.database.DatabaseHelper;
import com.example.pembukuanusaha.session.SessionManager;
import com.example.pembukuanusaha.sync.FirestoreSyncHelper;
import com.example.pembukuanusaha.utils.RupiahFormatter;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // =====================
    // VIEW
    // =====================
    TextView txtGreeting, txtTanggal, txtOmzet, txtLaba;

    // Perbaikan: Menggunakan tipe View agar tidak terjadi ClassCastException (CardView vs Button)
    View btnTambahTransaksi,
            btnLihatTransaksi,
            btnProduk,
            btnAyoBelanja,
            btnInsight,
            btnGrafik,
            btnExport,
            btnBackupRestore;

    // =====================
    // UTIL
    // =====================
    DatabaseHelper db;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // =====================
        // 🔐 CEK LOGIN
        // =====================
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // =====================
        // SESSION
        // =====================
        session = new SessionManager(this);

        // =====================
        // INIT VIEW
        // =====================
        txtGreeting = findViewById(R.id.txtGreeting);
        txtTanggal  = findViewById(R.id.txtTanggal);
        txtOmzet    = findViewById(R.id.txtOmzet);
        txtLaba     = findViewById(R.id.txtLaba);

        // Menghubungkan View (ID ini di XML mungkin berupa CardView atau Button)
        btnTambahTransaksi = findViewById(R.id.btnTambahTransaksi);
        btnLihatTransaksi  = findViewById(R.id.btnLihatTransaksi);
        btnProduk          = findViewById(R.id.btnProduk);
        btnAyoBelanja      = findViewById(R.id.btnAyoBelanja);
        btnInsight         = findViewById(R.id.btnInsight);
        btnGrafik          = findViewById(R.id.btnGrafik);
        btnExport          = findViewById(R.id.btnExport);
        btnBackupRestore   = findViewById(R.id.btnBackupRestore);

        // =====================
        // DATABASE
        // =====================
        db = new DatabaseHelper(this);

        // =====================
        // HEADER
        // =====================
        setGreetingAndDate();
        updateDashboard();

        // =====================
        // 🔐 ROLE-BASED UI
        // =====================
        if (!session.isAdmin()) {
            if (btnProduk != null) btnProduk.setVisibility(View.GONE);
            if (btnAyoBelanja != null) btnAyoBelanja.setVisibility(View.GONE);
            if (btnBackupRestore != null) btnBackupRestore.setVisibility(View.GONE);
        }

        // =====================
        // NAVIGASI
        // =====================
        if (btnTambahTransaksi != null)
            btnTambahTransaksi.setOnClickListener(v -> startActivity(new Intent(this, TambahTransaksiActivity.class)));

        if (btnLihatTransaksi != null)
            btnLihatTransaksi.setOnClickListener(v -> startActivity(new Intent(this, DaftarTransaksiActivity.class)));

        if (btnProduk != null)
            btnProduk.setOnClickListener(v -> {
                if (session.isAdmin()) {
                    startActivity(new Intent(this, DaftarProdukActivity.class));
                } else {
                    Snackbar.make(v, "Akses ditolak. Khusus Admin.", Snackbar.LENGTH_LONG).show();
                }
            });

        if (btnAyoBelanja != null)
            btnAyoBelanja.setOnClickListener(v -> {
                if (session.isAdmin()) {
                    startActivity(new Intent(this, AyoBelanjaActivity.class));
                } else {
                    Snackbar.make(v, "Akses ditolak. Khusus Admin.", Snackbar.LENGTH_LONG).show();
                }
            });

        if (btnInsight != null)
            btnInsight.setOnClickListener(v -> startActivity(new Intent(this, InsightActivity.class)));

        if (btnGrafik != null)
            btnGrafik.setOnClickListener(v -> startActivity(new Intent(this, GrafikActivity.class)));

        if (btnExport != null)
            btnExport.setOnClickListener(v -> startActivity(new Intent(this, ExportActivity.class)));

        if (btnBackupRestore != null)
            btnBackupRestore.setOnClickListener(v -> {
                if (session.isAdmin()) {
                    startActivity(new Intent(this, BackupRestoreActivity.class));
                } else {
                    Snackbar.make(v, "Akses ditolak. Khusus Admin.", Snackbar.LENGTH_LONG).show();
                }
            });

        // =====================
        // ✨ ANIMASI
        // =====================
        animateDashboard();
    }

    private void updateDashboard() {
        if (db != null) {
            txtOmzet.setText(RupiahFormatter.format(db.getTotalOmzet()));
            txtLaba.setText(RupiahFormatter.format(db.getTotalLaba()));
        }
    }

    private void setGreetingAndDate() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour >= 4 && hour < 11) greeting = "Selamat Pagi 👋";
        else if (hour >= 11 && hour < 15) greeting = "Selamat Siang 👋";
        else if (hour >= 15 && hour < 18) greeting = "Selamat Sore 👋";
        else greeting = "Selamat Malam 👋";

        txtGreeting.setText(greeting);
        String tanggal = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("id", "ID")).format(new Date());
        txtTanggal.setText(tanggal);
    }

    private void animateDashboard() {
        txtGreeting.setAlpha(0f);
        txtTanggal.setAlpha(0f);
        txtGreeting.animate().alpha(1f).setDuration(600).start();
        txtTanggal.animate().alpha(1f).setStartDelay(200).setDuration(600).start();

        // Menggunakan array View agar kompatibel dengan CardView maupun Button
        View[] buttons = {
                btnTambahTransaksi, btnLihatTransaksi, btnProduk, btnAyoBelanja,
                btnInsight, btnGrafik, btnExport, btnBackupRestore
        };

        int delay = 300;
        for (View btn : buttons) {
            if (btn == null || btn.getVisibility() != View.VISIBLE) continue;

            btn.setScaleX(0.9f);
            btn.setScaleY(0.9f);
            btn.setAlpha(0f);

            btn.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setStartDelay(delay)
                    .setDuration(300)
                    .start();
            delay += 100;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDashboard();
        // Memanggil Helper Sinkronisasi yang sudah kita perbaiki sebelumnya
        FirestoreSyncHelper.syncTransaksi(this);
    }
}