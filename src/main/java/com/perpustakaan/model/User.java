package com.perpustakaan.model;

import java.util.Objects;

/**
 * Entitas pengguna sesuai tabel users.
 */
public class User {

    private String idUser;
    private String namaDepan;
    private String namaBelakang;
    private String jenisKelamin;
    private String email;
    private String username;
    private String passwordHash;
    private String nomorPonsel;
    private String alamat;
    private String bio;
    private Role role = Role.MEMBER;

    public User() {
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
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

    public String getJenisKelamin() {
        return jenisKelamin;
    }

    public void setJenisKelamin(String jenisKelamin) {
        this.jenisKelamin = jenisKelamin;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getNomorPonsel() {
        return nomorPonsel;
    }

    public void setNomorPonsel(String nomorPonsel) {
        this.nomorPonsel = nomorPonsel;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role == null ? Role.MEMBER : role;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public String getNamaLengkap() {
        String depan = namaDepan == null ? "" : namaDepan.trim();
        String belakang = namaBelakang == null ? "" : namaBelakang.trim();
        String lengkap = (depan + " " + belakang).trim();
        return lengkap.isEmpty() ? username : lengkap;
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
        if (inisial.isEmpty() && username != null && !username.isEmpty()) {
            inisial = username.substring(0, 1);
        }
        return inisial.isEmpty() ? "?" : inisial.toUpperCase();
    }

    public String getJenisKelaminLabel() {
        if ("L".equalsIgnoreCase(jenisKelamin)) {
            return "Laki-laki";
        }
        if ("P".equalsIgnoreCase(jenisKelamin)) {
            return "Perempuan";
        }
        return "Tidak disebutkan";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User user)) {
            return false;
        }
        return Objects.equals(idUser, user.idUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUser);
    }

    @Override
    public String toString() {
        return getNamaLengkap();
    }
}
