package com.perpustakaan.model;

/**
 * Entitas penulis.
 */
public class Author {

    private String idAuthor;
    private String namaDepan;
    private String namaBelakang;
    private String email;
    private String negaraAsal;
    private String bio;
    private long jumlahBuku;

    public Author() {
    }

    public Author(String idAuthor, String namaDepan, String namaBelakang, String email,
                  String negaraAsal, String bio, long jumlahBuku) {
        this.idAuthor = idAuthor;
        this.namaDepan = namaDepan;
        this.namaBelakang = namaBelakang;
        this.email = email;
        this.negaraAsal = negaraAsal;
        this.bio = bio;
        this.jumlahBuku = jumlahBuku;
    }

    public String getIdAuthor() {
        return idAuthor;
    }

    public void setIdAuthor(String idAuthor) {
        this.idAuthor = idAuthor;
    }

    public String getNamaDepan() {
        return namaDepan;
    }

    public void setNamaDepan(String namaDepan) {
        this.namaDepan = namaDepan;
    }

    public String getNamaBelakang() {
        return namaBelakang;
    }

    public void setNamaBelakang(String namaBelakang) {
        this.namaBelakang = namaBelakang;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNegaraAsal() {
        return negaraAsal;
    }

    public void setNegaraAsal(String negaraAsal) {
        this.negaraAsal = negaraAsal;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public long getJumlahBuku() {
        return jumlahBuku;
    }

    public void setJumlahBuku(long jumlahBuku) {
        this.jumlahBuku = jumlahBuku;
    }

    public String getNamaLengkap() {
        String depan = namaDepan == null ? "" : namaDepan.trim();
        String belakang = namaBelakang == null ? "" : namaBelakang.trim();
        return (depan + " " + belakang).trim();
    }

    public String getInisial() {
        String depan = namaDepan == null ? "" : namaDepan.trim();
        String belakang = namaBelakang == null ? "" : namaBelakang.trim();
        String inisial = "";
        if (!depan.isEmpty()) {
            inisial += depan.substring(0, 1);
        }
        if (!belakang.isEmpty()) {
            inisial += belakang.substring(0, 1);
        }
        return inisial.isEmpty() ? "?" : inisial.toUpperCase();
    }

    @Override
    public String toString() {
        return getNamaLengkap();
    }
}
