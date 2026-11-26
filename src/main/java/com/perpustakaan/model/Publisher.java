package com.perpustakaan.model;

/**
 * Entitas penerbit sesuai tabel publishers.
 */
public class Publisher {

    private String idPublisher;
    private String nama;
    private String slug;
    private String kota;
    private long jumlahBuku;

    public Publisher() {
    }

    public Publisher(String idPublisher, String nama, String slug, String kota, long jumlahBuku) {
        this.idPublisher = idPublisher;
        this.nama = nama;
        this.slug = slug;
        this.kota = kota;
        this.jumlahBuku = jumlahBuku;
    }

    public String getIdPublisher() {
        return idPublisher;
    }

    public void setIdPublisher(String idPublisher) {
        this.idPublisher = idPublisher;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getKota() {
        return kota;
    }

    public void setKota(String kota) {
        this.kota = kota;
    }

    public long getJumlahBuku() {
        return jumlahBuku;
    }

    public void setJumlahBuku(long jumlahBuku) {
        this.jumlahBuku = jumlahBuku;
    }

    @Override
    public String toString() {
        return nama == null ? idPublisher : nama;
    }
}
