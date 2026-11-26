package com.perpustakaan.model;

/**
 * Entitas buku sesuai tabel books. Semua berkas e-book berformat PDF.
 */
public class Ebook {

    private String idBook;
    private String idAuthor;
    private String idPublisher;
    private String idGenre;
    private String judul;
    private String slug;
    private Integer tahunTerbit;
    private Integer jumlahHalaman;
    private String isbn;
    private String sinopsis;
    private String coverPath;
    private String filePath;
    private String penerbit;
    private long fileSize;
    private String checksumSha256;
    private boolean active = true;

    public Ebook() {
    }

    public String getIdBook() {
        return idBook;
    }

    public void setIdBook(String idBook) {
        this.idBook = idBook;
    }

    public String getIdAuthor() {
        return idAuthor;
    }

    public void setIdAuthor(String idAuthor) {
        this.idAuthor = idAuthor;
    }

    public String getIdPublisher() {
        return idPublisher;
    }

    public void setIdPublisher(String idPublisher) {
        this.idPublisher = idPublisher;
    }

    public String getIdGenre() {
        return idGenre;
    }

    public void setIdGenre(String idGenre) {
        this.idGenre = idGenre;
    }

    public String getJudul() {
        return judul;
    }

    public void setJudul(String judul) {
        this.judul = judul;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Integer getTahunTerbit() {
        return tahunTerbit;
    }

    public void setTahunTerbit(Integer tahunTerbit) {
        this.tahunTerbit = tahunTerbit;
    }

    public Integer getJumlahHalaman() {
        return jumlahHalaman;
    }

    public void setJumlahHalaman(Integer jumlahHalaman) {
        this.jumlahHalaman = jumlahHalaman;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getSinopsis() {
        return sinopsis;
    }

    public void setSinopsis(String sinopsis) {
        this.sinopsis = sinopsis;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /** Nama penerbit (kompatibel dengan pemanggilan lama). */
    public String getPenerbit() {
        return penerbit;
    }

    public void setPenerbit(String penerbit) {
        this.penerbit = penerbit;
    }

    /** ID genre (kompatibel dengan pemanggilan lama). */
    public String getKategoriId() {
        return idGenre;
    }

    public void setKategoriId(String kategoriId) {
        this.idGenre = kategoriId;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getChecksumSha256() {
        return checksumSha256;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setChecksumSha256(String checksumSha256) {
        this.checksumSha256 = checksumSha256;
    }

    /** Setiap buku kini berupa PDF yang dapat dibuka pembaca. */
    public boolean isReaderSupported() {
        return true;
    }

    @Override
    public String toString() {
        return judul == null ? idBook : judul;
    }
}
