package com.perpustakaan.service;

import com.perpustakaan.exception.AppException;
import com.perpustakaan.model.Author;
import com.perpustakaan.model.BookListItem;
import com.perpustakaan.model.Kategori;
import com.perpustakaan.model.Publisher;
import com.perpustakaan.model.User;
import com.perpustakaan.repository.AuthorRepository;
import com.perpustakaan.repository.EbookRepository;
import com.perpustakaan.repository.KategoriRepository;
import com.perpustakaan.repository.PublisherRepository;
import com.perpustakaan.security.AccessControl;

import java.util.List;

/**
 * Data untuk halaman profil penulis, penerbit, dan genre.
 */
public class ProfileService {

    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final KategoriRepository kategoriRepository;
    private final EbookRepository ebookRepository;

    public ProfileService(AuthorRepository authorRepository, PublisherRepository publisherRepository,
                          KategoriRepository kategoriRepository, EbookRepository ebookRepository) {
        this.authorRepository = authorRepository;
        this.publisherRepository = publisherRepository;
        this.kategoriRepository = kategoriRepository;
        this.ebookRepository = ebookRepository;
    }

    public ProfileInfo load(User actor, String type, String id) {
        AccessControl.requireAuthenticated(actor);
        String userId = actor.getIdUser();
        return switch (type) {
            case "author" -> {
                Author author = authorRepository.findById(id);
                if (author == null) {
                    throw new AppException("Penulis tidak ditemukan.");
                }
                yield new ProfileInfo("Penulis", author.getNamaLengkap(),
                        "ID: " + author.getIdAuthor() + "  •  Slug: " + author.getSlug(),
                        author.getNegaraAsal(), author.getBio(), author.getJumlahBuku(),
                        ebookRepository.findByMaster("author", id, userId, false));
            }
            case "publisher" -> {
                Publisher publisher = publisherRepository.findById(id);
                if (publisher == null) {
                    throw new AppException("Penerbit tidak ditemukan.");
                }
                yield new ProfileInfo("Penerbit", publisher.getNama(),
                        "ID: " + publisher.getIdPublisher() + "  •  Slug: " + publisher.getSlug(),
                        "Kota: " + publisher.getKota(), "", publisher.getJumlahBuku(),
                        ebookRepository.findByMaster("publisher", id, userId, false));
            }
            case "genre" -> {
                Kategori genre = kategoriRepository.findById(id);
                if (genre == null) {
                    throw new AppException("Genre tidak ditemukan.");
                }
                yield new ProfileInfo("Genre", genre.getNamaKategori(), "ID: " + genre.getIdKategori(),
                        "", genre.getDeskripsi(), genre.getJumlahBuku(),
                        ebookRepository.findByMaster("genre", id, userId, false));
            }
            default -> throw new AppException("Jenis profil tidak dikenal.");
        };
    }

    public record ProfileInfo(String jenis, String nama, String identitas, String keterangan,
                              String bio, long jumlahBuku, List<BookListItem> books) {
    }
}
