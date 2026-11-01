package com.perpustakaan.model;

/**
 * Entitas kategori.
 */
public class Kategori {

    private String idKategori;
    private String namaKategori;
    private String deskripsi;
    private long jumlahBuku;

    public Kategori() {
    }

    public Kategori(String idKategori, String namaKategori, String deskripsi, long jumlahBuku) {
        this.idKategori = idKategori;
        this.namaKategori = namaKategori;
        this.deskripsi = deskripsi;
        this.jumlahBuku = jumlahBuku;
    }

    public String getIdKategori() {
        return idKategori;
    }

    public void setIdKategori(String idKategori) {
        this.idKategori = idKategori;
    }

    public String getNamaKategori() {
        return namaKategori;
    }

    public void setNamaKategori(String namaKategori) {
        this.namaKategori = namaKategori;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public long getJumlahBuku() {
        return jumlahBuku;
    }

    public void setJumlahBuku(long jumlahBuku) {
        this.jumlahBuku = jumlahBuku;
    }

    @Override
    public String toString() {
        return namaKategori == null ? idKategori : namaKategori;
    }
}
