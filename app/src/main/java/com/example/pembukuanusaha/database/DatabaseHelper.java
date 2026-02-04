package com.example.pembukuanusaha.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pembukuan.db";
    // 🔥 VERSI NAIK JADI 5 (Karena ada kolom Foto Produk)
    private static final int DATABASE_VERSION = 5;

    // TABEL TRANSAKSI
    public static final String TABLE_TRANSAKSI = "transaksi";
    public static final String COL_ID = "id";
    public static final String COL_NAMA = "nama_produk";
    public static final String COL_HARGA_JUAL = "harga_jual";
    public static final String COL_HARGA_MODAL = "harga_modal";
    public static final String COL_JUMLAH = "jumlah";
    public static final String COL_LABA = "laba";
    public static final String COL_TANGGAL = "tanggal";
    public static final String COL_SYNC = "status_sync";

    // TABEL PRODUK
    public static final String TABLE_PRODUK = "produk";
    public static final String COL_PRODUK_ID = "id";
    public static final String COL_PRODUK_NAMA = "nama";
    public static final String COL_PRODUK_MODAL = "harga_modal";
    public static final String COL_PRODUK_JUAL = "harga_jual";
    public static final String COL_PRODUK_STOK = "stok";
    // 🔥 KOLOM BARU FOTO
    public static final String COL_PRODUK_IMAGE = "image_url";

    // TABEL PENGELUARAN
    public static final String TABLE_PENGELUARAN = "pengeluaran";
    public static final String COL_OUT_ID = "id";
    public static final String COL_OUT_NAMA = "nama_pengeluaran";
    public static final String COL_OUT_NOMINAL = "nominal";
    public static final String COL_OUT_KATEGORI = "kategori";
    public static final String COL_OUT_TANGGAL = "tanggal";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tabel Transaksi
        db.execSQL("CREATE TABLE " + TABLE_TRANSAKSI + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAMA + " TEXT, " +
                COL_HARGA_JUAL + " INTEGER, " +
                COL_HARGA_MODAL + " INTEGER, " +
                COL_JUMLAH + " INTEGER, " +
                COL_LABA + " INTEGER, " +
                COL_TANGGAL + " TEXT, " +
                COL_SYNC + " INTEGER DEFAULT 0)");

        // Tabel Produk (Updated dengan IMAGE_URL)
        db.execSQL("CREATE TABLE " + TABLE_PRODUK + " (" +
                COL_PRODUK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PRODUK_NAMA + " TEXT, " +
                COL_PRODUK_MODAL + " INTEGER, " +
                COL_PRODUK_JUAL + " INTEGER, " +
                COL_PRODUK_STOK + " INTEGER, " +
                COL_PRODUK_IMAGE + " TEXT)");

        // Tabel Pengeluaran
        db.execSQL("CREATE TABLE " + TABLE_PENGELUARAN + " (" +
                COL_OUT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_OUT_NAMA + " TEXT, " +
                COL_OUT_NOMINAL + " INTEGER, " +
                COL_OUT_KATEGORI + " TEXT, " +
                COL_OUT_TANGGAL + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Reset tabel jika versi berubah (Cara aman untuk dev)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSAKSI);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PENGELUARAN);
        onCreate(db);
    }

    // ======================
    // CRUD PENGELUARAN
    // ======================
    public boolean insertPengeluaran(String nama, int nominal, String kategori, String tanggal) {
        ContentValues cv = new ContentValues();
        cv.put(COL_OUT_NAMA, nama);
        cv.put(COL_OUT_NOMINAL, nominal);
        cv.put(COL_OUT_KATEGORI, kategori);
        cv.put(COL_OUT_TANGGAL, tanggal);
        return getWritableDatabase().insert(TABLE_PENGELUARAN, null, cv) != -1;
    }

    public Cursor getAllPengeluaran() {
        return getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_PENGELUARAN + " ORDER BY " + COL_OUT_TANGGAL + " DESC", null);
    }

    public int getTotalPengeluaran() {
        Cursor c = getReadableDatabase().rawQuery("SELECT SUM(" + COL_OUT_NOMINAL + ") FROM " + TABLE_PENGELUARAN, null);
        int total = (c.moveToFirst()) ? c.getInt(0) : 0;
        c.close();
        return total;
    }

    // ======================
    // PRODUK (UPDATED FOTO)
    // ======================
    // 🔥 Method insertProduk baru menerima parameter imageUrl
    public boolean insertProduk(String nama, int modal, int jual, int stok, String imageUrl) {
        ContentValues cv = new ContentValues();
        cv.put(COL_PRODUK_NAMA, nama);
        cv.put(COL_PRODUK_MODAL, modal);
        cv.put(COL_PRODUK_JUAL, jual);
        cv.put(COL_PRODUK_STOK, stok);
        cv.put(COL_PRODUK_IMAGE, imageUrl); // Simpan URL
        return getWritableDatabase().insert(TABLE_PRODUK, null, cv) != -1;
    }

    // 🔥 Update Produk (Untuk Fitur Edit)
    public boolean updateProduk(int id, String nama, int modal, int jual, int stok, String imageUrl) {
        ContentValues cv = new ContentValues();
        cv.put(COL_PRODUK_NAMA, nama);
        cv.put(COL_PRODUK_MODAL, modal);
        cv.put(COL_PRODUK_JUAL, jual);
        cv.put(COL_PRODUK_STOK, stok);
        if (imageUrl != null) { // Update foto hanya jika ada foto baru
            cv.put(COL_PRODUK_IMAGE, imageUrl);
        }
        return getWritableDatabase().update(TABLE_PRODUK, cv, COL_PRODUK_ID + "=?", new String[]{String.valueOf(id)}) > 0;
    }

    public Cursor getAllProduk() {
        return getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_PRODUK, null);
    }

    public boolean deleteProduk(int id) {
        return getWritableDatabase().delete(TABLE_PRODUK, COL_PRODUK_ID + "=?", new String[]{String.valueOf(id)}) > 0;
    }

    public void updateStokProduk(int id, int stokBaru) {
        ContentValues cv = new ContentValues();
        cv.put(COL_PRODUK_STOK, stokBaru);
        getWritableDatabase().update(TABLE_PRODUK, cv, COL_PRODUK_ID + "=?", new String[]{String.valueOf(id)});
    }

    public Cursor getProdukHampirHabis() {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM " + TABLE_PRODUK + " WHERE " + COL_PRODUK_STOK + " <= 5 ORDER BY " + COL_PRODUK_STOK + " ASC",
                null
        );
    }

    // ======================
    // TRANSAKSI
    // ======================
    public boolean insertTransaksi(String nama, int hargaJual, int hargaModal, int jumlah, int laba, String tanggal) {
        ContentValues cv = new ContentValues();
        cv.put(COL_NAMA, nama);
        cv.put(COL_HARGA_JUAL, hargaJual);
        cv.put(COL_HARGA_MODAL, hargaModal);
        cv.put(COL_JUMLAH, jumlah);
        cv.put(COL_LABA, laba);
        cv.put(COL_TANGGAL, tanggal);
        cv.put(COL_SYNC, 0);
        return getWritableDatabase().insert(TABLE_TRANSAKSI, null, cv) != -1;
    }

    public Cursor getAllTransaksi() {
        return getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_TRANSAKSI + " ORDER BY " + COL_TANGGAL + " DESC", null);
    }

    public int getTotalOmzet() {
        Cursor c = getReadableDatabase().rawQuery("SELECT SUM(" + COL_HARGA_JUAL + " * " + COL_JUMLAH + ") FROM " + TABLE_TRANSAKSI, null);
        int total = (c.moveToFirst()) ? c.getInt(0) : 0;
        c.close();
        return total;
    }

    public int getTotalLaba() {
        Cursor c = getReadableDatabase().rawQuery("SELECT SUM(" + COL_LABA + ") FROM " + TABLE_TRANSAKSI, null);
        int total = (c.moveToFirst()) ? c.getInt(0) : 0;
        c.close();
        return total;
    }

    public Cursor getOmzetHarian() {
        return getReadableDatabase().rawQuery(
                "SELECT " + COL_TANGGAL + ", SUM(" + COL_HARGA_JUAL + " * " + COL_JUMLAH + ") as omzet " +
                        "FROM " + TABLE_TRANSAKSI +
                        " GROUP BY " + COL_TANGGAL +
                        " ORDER BY " + COL_TANGGAL + " ASC LIMIT 7",
                null
        );
    }

    // ======================
    // INSIGHT
    // ======================
    public String getProdukTerlaris() {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT " + COL_NAMA + ", SUM(" + COL_JUMLAH + ") total " +
                        "FROM " + TABLE_TRANSAKSI +
                        " GROUP BY " + COL_NAMA +
                        " ORDER BY total DESC LIMIT 1",
                null
        );
        String hasil = c.moveToFirst() ? c.getString(0) + " (" + c.getInt(1) + " terjual)" : "Belum ada data";
        c.close();
        return hasil;
    }

    public String getProdukPalingUntung() {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT " + COL_NAMA + ", SUM(" + COL_LABA + ") total " +
                        "FROM " + TABLE_TRANSAKSI +
                        " GROUP BY " + COL_NAMA +
                        " ORDER BY total DESC LIMIT 1",
                null
        );
        String hasil = c.moveToFirst() ? c.getString(0) + " (Rp " + c.getInt(1) + ")" : "Belum ada data";
        c.close();
        return hasil;
    }

    // ======================
    // SYNC
    // ======================
    public Cursor getTransaksiBelumSync() {
        return getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_TRANSAKSI + " WHERE " + COL_SYNC + " = 0", null);
    }

    public void tandaiTransaksiSudahSync(int id) {
        ContentValues cv = new ContentValues();
        cv.put(COL_SYNC, 1);
        getWritableDatabase().update(TABLE_TRANSAKSI, cv, COL_ID + "=?", new String[]{String.valueOf(id)});
    }
}