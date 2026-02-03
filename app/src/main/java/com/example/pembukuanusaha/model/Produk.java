package com.example.pembukuanusaha.model;

public class Produk {

    private int id, hargaModal, hargaJual, stok;
    private String nama;

    public Produk(int id, String nama, int hargaModal, int hargaJual, int stok) {
        this.id = id;
        this.nama = nama;
        this.hargaModal = hargaModal;
        this.hargaJual = hargaJual;
        this.stok = stok;
    }

    public int getId() {
        return id;
    }

    public String getNama() {
        return nama;
    }

    public int getHargaModal() {
        return hargaModal;
    }

    public int getHargaJual() {
        return hargaJual;
    }

    public int getStok() {
        return stok;
    }
}
