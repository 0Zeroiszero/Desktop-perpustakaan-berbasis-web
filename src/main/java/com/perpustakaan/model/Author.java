package com.perpustakaan.model;

import java.util.Locale;

/**
 * Entitas penulis sesuai tabel authors.
 */
public class Author {

    private String idAuthor;
    private String namaLengkap;
    private String slug;
    private String negaraAsal;
    private String bio;
    private long jumlahBuku;

    public Author() {
    }

    public Author(String idAuthor, String namaLengkap, String slug, String negaraAsal,
                  String bio, long jumlahBuku) {
        this.idAuthor = idAuthor;
        this.namaLengkap = namaLengkap;
        this.slug = slug;
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

    public String getNamaLengkap() {
        return namaLengkap;
    }

    public void setNamaLengkap(String namaLengkap) {
        this.namaLengkap = namaLengkap;
    }

    /** Alias untuk field name pada tabel authors. */
    public String getNama() {
        return namaLengkap;
    }

    public void setNama(String nama) {
        this.namaLengkap = nama;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
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

    public String getInisial() {
        if (namaLengkap == null || namaLengkap.isBlank()) {
            return "?";
        }
        String[] parts = namaLengkap.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < Math.min(2, parts.length); i++) {
            if (!parts[i].isEmpty()) {
                initials.append(parts[i].charAt(0));
            }
        }
        return initials.length() == 0 ? "?" : initials.toString().toUpperCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return namaLengkap == null ? idAuthor : namaLengkap;
    }
}
