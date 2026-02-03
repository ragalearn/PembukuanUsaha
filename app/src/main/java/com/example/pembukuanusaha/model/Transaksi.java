package com.example.pembukuanusaha.model;

public class Transaksi {

    private String tanggal;
    private String namaProduk;
    private int jumlah;
    private int hargaJual;
    private int laba;

    // =====================
    // CONSTRUCTOR FINAL
    // =====================
    public Transaksi(String tanggal,
                     String namaProduk,
                     int jumlah,
                     int hargaJual,
                     int laba) {

        this.tanggal = tanggal;
        this.namaProduk = namaProduk;
        this.jumlah = jumlah;
        this.hargaJual = hargaJual;
        this.laba = laba;
    }

    // =====================
    // GETTER
    // =====================
    public String getTanggal() {
        return tanggal;
    }

    public String getNamaProduk() {
        return namaProduk;
    }

    public int getJumlah() {
        return jumlah;
    }

    public int getHargaJual() {
        return hargaJual;
    }

    public int getLaba() {
        return laba;
    }
}
