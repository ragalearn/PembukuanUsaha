package com.example.pembukuanusaha.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

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

    TextView txtGreeting, txtTanggal, txtOmzet, txtLaba;
    ImageView btnKeluar;

    // Menu Buttons
    CardView btnTambahTransaksi, btnPengeluaran, btnLihatTransaksi, btnProduk,
            btnAyoBelanja, btnInsight, btnGrafik, btnExport, btnBackupRestore,
            btnTambahKaryawan;

    DatabaseHelper db;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        session = new SessionManager(this);
        db = new DatabaseHelper(this);

        // Init View Header
        txtGreeting = findViewById(R.id.txtGreeting);
        txtTanggal  = findViewById(R.id.txtTanggal);
        txtOmzet    = findViewById(R.id.txtOmzet);
        txtLaba     = findViewById(R.id.txtLaba);
        btnKeluar   = findViewById(R.id.btnKeluar);

        // Init Tombol Menu (CardView)
        btnTambahTransaksi = findViewById(R.id.btnTambahTransaksi);
        btnPengeluaran     = findViewById(R.id.btnPengeluaran);
        btnLihatTransaksi  = findViewById(R.id.btnLihatTransaksi);
        btnProduk          = findViewById(R.id.btnProduk);
        btnAyoBelanja      = findViewById(R.id.btnAyoBelanja);
        btnInsight         = findViewById(R.id.btnInsight);
        btnGrafik          = findViewById(R.id.btnGrafik);
        btnExport          = findViewById(R.id.btnExport);
        btnBackupRestore   = findViewById(R.id.btnBackupRestore);
        btnTambahKaryawan  = findViewById(R.id.btnTambahKaryawan);

        setGreetingAndDate();
        updateDashboard();

        // 🔐 ROLE PROTECTION (Hanya Admin yang bisa lihat menu sensitif)
        if (!session.isAdmin()) {
            // 🔥 SOLUSI RAPI: Hapus tombol dari layout (bukan cuma GONE)
            // Agar tombol di bawahnya naik ke atas mengisi celah kosong
            removeButton(btnProduk);
            removeButton(btnAyoBelanja);
            removeButton(btnBackupRestore);
            removeButton(btnTambahKaryawan);
        }

        setupButtons();
        animateDashboard();
    }

    // 🔥 METHOD BARU: Menghapus Tombol Sampai ke Akarnya agar Layout Rapi
    private void removeButton(View view) {
        if (view != null && view.getParent() instanceof ViewGroup) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }

    private void setupButtons() {
        // Menu Umum (Semua Bisa Akses)
        btnTambahTransaksi.setOnClickListener(v -> startActivity(new Intent(this, TambahTransaksiActivity.class)));
        btnPengeluaran.setOnClickListener(v -> startActivity(new Intent(this, TambahPengeluaranActivity.class)));
        btnLihatTransaksi.setOnClickListener(v -> startActivity(new Intent(this, DaftarTransaksiActivity.class)));
        btnInsight.setOnClickListener(v -> startActivity(new Intent(this, InsightActivity.class)));
        btnGrafik.setOnClickListener(v -> startActivity(new Intent(this, GrafikActivity.class)));
        btnExport.setOnClickListener(v -> startActivity(new Intent(this, ExportActivity.class)));

        // Menu Admin (Listener tetap dipasang, tapi tombolnya sudah dihapus di atas jika bukan admin)
        if (session.isAdmin()) {
            btnProduk.setOnClickListener(v -> startActivity(new Intent(this, DaftarProdukActivity.class)));
            btnAyoBelanja.setOnClickListener(v -> startActivity(new Intent(this, AyoBelanjaActivity.class)));
            btnBackupRestore.setOnClickListener(v -> startActivity(new Intent(this, BackupRestoreActivity.class)));
            if (btnTambahKaryawan != null) {
                btnTambahKaryawan.setOnClickListener(v -> startActivity(new Intent(this, TambahKaryawanActivity.class)));
            }
        }

        btnKeluar.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Yakin ingin keluar dari akun?")
                    .setPositiveButton("Ya", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        session.logoutUser();
                        finish();
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });
    }

    private void showAccessDenied(View v) {
        Snackbar.make(v, "Akses ditolak. Khusus Admin.", Snackbar.LENGTH_SHORT).show();
    }

    private void updateDashboard() {
        if (db != null) {
            int omzet = db.getTotalOmzet();
            int laba = db.getTotalLaba();
            txtOmzet.setText(RupiahFormatter.format(omzet));
            txtLaba.setText(RupiahFormatter.format(laba));
        }
    }

    private void setGreetingAndDate() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour >= 4 && hour < 11) greeting = "Selamat Pagi 👋";
        else if (hour >= 11 && hour < 15) greeting = "Selamat Siang 👋";
        else if (hour >= 15 && hour < 18) greeting = "Selamat Sore 👋";
        else greeting = "Selamat Malam 👋";

        String nama = session.getUserDetails().get(SessionManager.KEY_EMAIL);
        if (nama != null && nama.contains("@")) nama = nama.split("@")[0];

        txtGreeting.setText(greeting + (nama != null ? "\n" + nama : ""));

        String tanggal = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("id", "ID")).format(new Date());
        txtTanggal.setText(tanggal);
    }

    private void animateDashboard() {
        txtGreeting.setAlpha(0f);
        txtTanggal.setAlpha(0f);
        txtGreeting.animate().alpha(1f).setDuration(600).start();
        txtTanggal.animate().alpha(1f).setStartDelay(200).setDuration(600).start();

        // Animasi hanya untuk tombol yang masih ada (tidak null & terlihat)
        View[] buttons = {
                btnTambahTransaksi, btnPengeluaran, btnLihatTransaksi, btnProduk,
                btnAyoBelanja, btnInsight, btnGrafik, btnExport, btnBackupRestore,
                btnTambahKaryawan
        };

        int delay = 300;
        for (View btn : buttons) {
            // Cek apakah tombol masih ada di layar (belum dihapus/null)
            if (btn == null || btn.getParent() == null) continue;

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
            delay += 50;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDashboard();
        FirestoreSyncHelper.syncTransaksi(this);
    }
}