package com.example.pembukuanusaha.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.pembukuanusaha.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class BackupRestoreActivity extends AppCompatActivity {

    CardView btnBackup, btnRestore;
    private static final String DB_NAME = "pembukuan.db";
    private static final int STORAGE_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_restore);

        btnBackup = findViewById(R.id.btnBackup);
        btnRestore = findViewById(R.id.btnRestore);

        // SAAT TOMBOL DITEKAN, CEK IZIN DULU
        btnBackup.setOnClickListener(v -> {
            if (checkPermission()) {
                backupData();
            } else {
                requestPermission();
            }
        });

        btnRestore.setOnClickListener(v -> {
            if (checkPermission()) {
                konfirmasiRestore();
            } else {
                requestPermission();
            }
        });
    }

    // ==========================================
    // 1. LOGIKA IZIN (PERMISSION) - WAJIB ADA
    // ==========================================
    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ butuh izin MANAGE_EXTERNAL_STORAGE
            return Environment.isExternalStorageManager();
        } else {
            // Android 10 ke bawah butuh WRITE_EXTERNAL_STORAGE
            int write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            return write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                startActivityForResult(intent, 2296);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 2296);
            }
            Toast.makeText(this, "Mohon izinkan akses semua file untuk Backup", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Izin diberikan, silakan tekan tombol lagi", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Izin ditolak, fitur backup tidak bisa jalan", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ==========================================
    // 2. LOGIKA BACKUP (EKSEKUSI)
    // ==========================================
    private void backupData() {
        try {
            File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            // Menggunakan getDatabasePath agar lokasi DB otomatis benar
            File currentDB = getDatabasePath(DB_NAME);
            File backupDB = new File(sd, "Backup_Pembukuan.db");

            if (currentDB.exists()) {
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(this, "Backup Sukses! File ada di folder Download.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Database belum ada (Kosong).", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Backup Gagal: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ==========================================
    // 3. LOGIKA RESTORE (EKSEKUSI)
    // ==========================================
    private void konfirmasiRestore() {
        new AlertDialog.Builder(this)
                .setTitle("Restore Data")
                .setMessage("PERINGATAN: Data di aplikasi saat ini akan DITIMPA dengan data backup. Aplikasi akan restart otomatis.")
                .setPositiveButton("Ya, Restore", (dialog, which) -> restoreData())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void restoreData() {
        try {
            File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            File currentDB = getDatabasePath(DB_NAME);
            File backupDB = new File(sd, "Backup_Pembukuan.db");

            if (backupDB.exists()) {
                FileChannel src = new FileInputStream(backupDB).getChannel();
                FileChannel dst = new FileOutputStream(currentDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(this, "Restore Berhasil! Aplikasi akan restart...", Toast.LENGTH_LONG).show();

                // Restart Aplikasi agar data ter-refresh sempurna
                new android.os.Handler().postDelayed(() -> {
                    Intent i = getBaseContext().getPackageManager()
                            .getLaunchIntentForPackage(getBaseContext().getPackageName());
                    if (i != null) {
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }
                    System.exit(0);
                }, 1500);

            } else {
                Toast.makeText(this, "File 'Backup_Pembukuan.db' tidak ditemukan di folder Download.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Restore Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}