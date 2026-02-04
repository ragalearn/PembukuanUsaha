package com.example.pembukuanusaha.model;

import java.io.Serializable;

// Implement Serializable agar objek bisa dikirim lewat Intent (untuk fitur Edit)
public class Produk implements Serializable {

    private int id;
    private String nama;
    private int hargaModal;
    private int hargaJual;
    private int stok;

    // 🔥 Field Baru untuk Foto
    private String imageUrl;

    // Constructor Kosong (Wajib untuk Firebase/Tools)
    public Produk() {
    }

    // Constructor Lengkap
    public Produk(int id, String nama, int hargaModal, int hargaJual, int stok, String imageUrl) {
        this.id = id;
        this.nama = nama;
        this.hargaModal = hargaModal;
        this.hargaJual = hargaJual;
        this.stok = stok;
        this.imageUrl = imageUrl;
    }

    public int getId() { return id; }
    public String getNama() { return nama; }
    public int getHargaModal() { return hargaModal; }
    public int getHargaJual() { return hargaJual; }
    public int getStok() { return stok; }

    // Getter Setter ImageUrl
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}