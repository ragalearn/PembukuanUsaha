package com.example.pembukuanusaha.sync;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.pembukuanusaha.database.DatabaseHelper;
import com.example.pembukuanusaha.session.BusinessSession;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirestoreSyncHelper {

    private static final String TAG = "FirestoreSyncHelper";

    public static void syncTransaksi(Context context) {
        // PERBAIKAN 1: Pindahkan semua inisialisasi ke awal & lakukan pengecekan
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "Sync dihentikan: User belum login.");
            return;
        }

        BusinessSession bs = new BusinessSession(context);
        if (!bs.isReady()) {
            Log.w(TAG, "Sync dihentikan: Sesi bisnis belum siap (Usaha/Cabang belum dipilih).");
            return;
        }

        // Ambil ID Usaha dan Cabang sekali saja di luar loop
        String idUsaha = bs.getUsaha();
        String idCabang = bs.getCabang();

        // PERBAIKAN 2: Validasi ID Usaha dan Cabang sebelum memulai
        if (idUsaha == null || idUsaha.isEmpty() || idCabang == null || idCabang.isEmpty()) {
            Log.e(TAG, "Sync GAGAL: ID Usaha atau ID Cabang kosong. Usaha: " + idUsaha + ", Cabang: " + idCabang);
            return;
        }

        DatabaseHelper db = new DatabaseHelper(context);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        Cursor c = null; // Inisialisasi cursor dengan null

        try {
            c = db.getTransaksiBelumSync();
            if (c == null || c.getCount() == 0) {
                Log.i(TAG, "Tidak ada transaksi baru untuk disinkronkan.");
                return; // Keluar jika tidak ada data
            }

            Log.i(TAG, "Memulai sinkronisasi untuk " + c.getCount() + " item.");

            while (c.moveToNext()) {
                final int id = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_ID));

                // Ambil semua data dari cursor dan masukkan ke dalam Map
                Map<String, Object> data = new HashMap<>();
                try {
                    data.put("uid", currentUser.getUid());
                    data.put("nama_produk", c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_NAMA)));
                    data.put("harga_jual", c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_HARGA_JUAL)));
                    data.put("harga_modal", c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_HARGA_MODAL)));
                    data.put("jumlah", c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_JUMLAH)));
                    data.put("laba", c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_LABA)));
                    data.put("tanggal", c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_TANGGAL)));
                    data.put("created_at", System.currentTimeMillis());
                    data.put("sync_source", "android");
                } catch (Exception e) {
                    // PERBAIKAN 3: Tangani jika ada kolom yang hilang atau tipe data salah
                    Log.e(TAG, "Error saat mengambil data dari cursor untuk ID: " + id + ". Kolom mungkin tidak ada atau tipe data salah. Pesan: " + e.getMessage());
                    continue; // Lanjutkan ke data berikutnya, jangan hentikan seluruh proses
                }


                firestore.collection("usaha").document(idUsaha)
                        .collection("cabang").document(idCabang)
                        .collection("transaksi").add(data)
                        .addOnSuccessListener(documentReference -> {
                            Log.d(TAG, "Berhasil sinkron ID lokal: " + id + " ke Firestore.");
                            // Pastikan objek db masih valid untuk digunakan
                            DatabaseHelper dbSuccess = new DatabaseHelper(context);
                            dbSuccess.tandaiTransaksiSudahSync(id);
                            dbSuccess.close(); // Tutup koneksi setelah selesai
                        })
                        .addOnFailureListener(e -> Log.e(TAG, "Gagal sinkron ID " + id + ": " + e.getMessage()));
            }

        } catch (Exception e) {
            // Ini akan menangkap error seperti "column 'nama_kolom' does not exist"
            Log.e(TAG, "FATAL ERROR saat proses sinkronisasi: " + e.getMessage());
            e.printStackTrace(); // Cetak stack trace untuk debugging mendalam
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
            if (db != null) {
                db.close(); // PERBAIKAN 4: Tutup koneksi database utama di akhir
            }
            Log.i(TAG, "Proses sinkronisasi selesai.");
        }
    }
}
