package com.example.pembukuanusaha.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pembukuanusaha.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class BackupRestoreActivity extends AppCompatActivity {

    private static final String DB_NAME = "pembukuan.db";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_restore);

        Button btnBackup = findViewById(R.id.btnBackup);
        Button btnRestore = findViewById(R.id.btnRestore);

        btnBackup.setOnClickListener(v -> backupDatabase());
        btnRestore.setOnClickListener(v -> restoreDatabase());
    }

    private void backupDatabase() {
        try {
            File dbFile = getDatabasePath(DB_NAME);

            File backupDir = new File(getExternalFilesDir(null), "backup");
            if (!backupDir.exists()) backupDir.mkdirs();

            File backupFile = new File(backupDir, DB_NAME);

            copyFile(dbFile, backupFile);

            Toast.makeText(this, "Backup berhasil", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Backup gagal: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void restoreDatabase() {
        try {
            File backupFile = new File(
                    getExternalFilesDir(null) + "/backup/" + DB_NAME
            );

            if (!backupFile.exists()) {
                Toast.makeText(this, "File backup tidak ditemukan", Toast.LENGTH_LONG).show();
                return;
            }

            File dbFile = getDatabasePath(DB_NAME);

            copyFile(backupFile, dbFile);

            Toast.makeText(this, "Restore berhasil. Buka ulang aplikasi.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Restore gagal: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void copyFile(File src, File dst) throws Exception {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();

        inChannel.transferTo(0, inChannel.size(), outChannel);

        inChannel.close();
        outChannel.close();
    }
}
