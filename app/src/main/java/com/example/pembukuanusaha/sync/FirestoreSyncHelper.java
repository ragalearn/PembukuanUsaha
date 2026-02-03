package com.example.pembukuanusaha.sync;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.example.pembukuanusaha.database.DatabaseHelper;
import com.example.pembukuanusaha.session.SessionManager; // GUNAKAN SESSION MANAGER
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirestoreSyncHelper {

    private static final String TAG = "FirestoreSyncHelper";

    public static void syncTransaksi(Context context) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) return;

        // FIX: Gunakan SessionManager agar sesuai dengan LoginActivity
        SessionManager session = new SessionManager(context);

        String idUsaha = session.getUsahaId();
        String idCabang = session.getCabangId();

        // Validasi Sesi
        if (idUsaha == null || idUsaha.isEmpty() || idCabang == null || idCabang.isEmpty()) {
            Log.e(TAG, "Sync GAGAL: Data sesi kosong. User harus login ulang.");
            return;
        }

        DatabaseHelper db = new DatabaseHelper(context);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        Cursor c = null;

        try {
            // Ambil data lokal yang status_sync = 0
            c = db.getTransaksiBelumSync();
            if (c == null || c.getCount() == 0) return;

            while (c.moveToNext()) {
                int id = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_ID));

                Map<String, Object> data = new HashMap<>();
                data.put("uid", currentUser.getUid());
                data.put("nama_produk", c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_NAMA)));
                data.put("harga_jual", c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_HARGA_JUAL)));
                data.put("harga_modal", c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_HARGA_MODAL)));
                data.put("jumlah", c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_JUMLAH)));
                data.put("laba", c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_LABA)));
                data.put("tanggal", c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_TANGGAL)));
                data.put("created_at", System.currentTimeMillis());
                data.put("sync_source", "android");

                // Upload ke Firestore
                firestore.collection("usaha").document(idUsaha)
                        .collection("cabang").document(idCabang)
                        .collection("transaksi").add(data)
                        .addOnSuccessListener(ref -> {
                            // Jika sukses upload, update status lokal jadi SYNCED (1)
                            // Hati-hati: Buka koneksi baru untuk update status
                            DatabaseHelper dbUpdate = new DatabaseHelper(context);
                            dbUpdate.tandaiTransaksiSudahSync(id);
                            dbUpdate.close();
                        });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error Sync: " + e.getMessage());
        } finally {
            if (c != null) c.close();
            db.close();
        }
    }
}