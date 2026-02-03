package com.example.pembukuanusaha.repository;

import android.content.Context;

import com.example.pembukuanusaha.database.DatabaseHelper;

public class TransaksiRepository {

    private DatabaseHelper db;

    public TransaksiRepository(Context context) {
        db = new DatabaseHelper(context);
    }

    // 🔹 SIMPAN TRANSAKSI (LOCAL DULU)
    public boolean insertTransaksi(
            String nama,
            int hargaJual,
            int hargaModal,
            int jumlah,
            int laba,
            String tanggal
    ) {
        // NANTI DI SINI:
        // 1. Simpan ke SQLite
        // 2. Sync ke Cloud (opsional)

        return db.insertTransaksi(
                nama,
                hargaJual,
                hargaModal,
                jumlah,
                laba,
                tanggal
        );
    }
}
