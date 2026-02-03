package com.example.pembukuanusaha.activity;

import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.pembukuanusaha.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class BackupRestoreActivity extends AppCompatActivity {

    CardView btnBackup, btnRestore;
    private static final String DB_NAME = "pembukuan.db";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_restore);

        btnBackup = findViewById(R.id.btnBackup);
        btnRestore = findViewById(R.id.btnRestore);

        btnBackup.setOnClickListener(v -> backupData());
        btnRestore.setOnClickListener(v -> konfirmasiRestore());
    }

    private void backupData() {
        try {
            File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//" + getPackageName() + "//databases//" + DB_NAME;
                String backupDBPath = "Backup_Pembukuan.db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Toast.makeText(this, "Backup Sukses! Disimpan di Download/" + backupDBPath, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Database belum ada.", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Backup Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void konfirmasiRestore() {
        new AlertDialog.Builder(this)
                .setTitle("Restore Data")
                .setMessage("PERINGATAN: Data saat ini akan ditimpa dengan data backup. Lanjutkan?")
                .setPositiveButton("Ya, Restore", (dialog, which) -> restoreData())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void restoreData() {
        try {
            File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File data = Environment.getDataDirectory();

            String currentDBPath = "//data//" + getPackageName() + "//databases//" + DB_NAME;
            String backupDBPath = "Backup_Pembukuan.db";
            File currentDB = new File(data, currentDBPath);
            File backupDB = new File(sd, backupDBPath);

            if (backupDB.exists()) {
                FileChannel src = new FileInputStream(backupDB).getChannel();
                FileChannel dst = new FileOutputStream(currentDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Toast.makeText(this, "Restore Berhasil! Silakan restart aplikasi.", Toast.LENGTH_LONG).show();

                // Opsional: Tutup aplikasi agar database refresh
                // finishAffinity();
            } else {
                Toast.makeText(this, "File backup tidak ditemukan di Download/Backup_Pembukuan.db", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Restore Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}