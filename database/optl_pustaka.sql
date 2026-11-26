-- phpMyAdmin SQL Dump
-- version 5.2.3
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3306
-- Generation Time: Nov 26, 2026 at 10:19 PM
-- Server version: 8.0.40
-- PHP Version: 8.5.12

START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `optl_pustaka`
--
CREATE DATABASE IF NOT EXISTS `optl_pustaka` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE optl_pustaka;

-- --------------------------------------------------------

--
-- Table structure for table `authors`
--

DROP TABLE IF EXISTS `authors`;
CREATE TABLE IF NOT EXISTS `authors` (
  `author_id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `slug` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `country` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `bio` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`author_id`),
  UNIQUE KEY `uq_authors_slug` (`slug`)
) ENGINE=InnoDB AUTO_INCREMENT=124 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `authors`
--

INSERT INTO `authors` (`author_id`, `name`, `slug`, `country`, `bio`, `created_at`) VALUES
(1, 'Pramoedya Ananta Toer', 'pramoedya-ananta-toer', 'Indonesia', 'Sastrawan legendaris penulis Tetralogi Buru.', '2026-11-23 13:52:10'),
(2, 'Andrea Hirata', 'andrea-hirata', 'Indonesia', 'Penulis novel Laskar Pelangi.', '2026-11-23 13:52:10'),
(3, 'J.K. Rowling', 'j-k-rowling', 'Inggris', 'Penulis seri Harry Potter.', '2026-11-23 13:52:10'),
(4, 'George R.R. Martin', 'george-r-r-martin', 'Amerika', 'Penulis A Song of Ice and Fire.', '2026-11-23 13:52:10'),
(5, 'Tere Liye', 'tere-liye', 'Indonesia', 'Penulis produktif novel populer.', '2026-11-23 13:52:10'),
(6, 'Dee Lestari', 'dee-lestari', 'Indonesia', 'Penulis seri Supernova.', '2026-11-23 13:52:10'),
(7, 'Ahmad Tohari', 'ahmad-tohari', 'Indonesia', 'Sastrawan Ronggeng Dukuh Paruk.', '2026-11-23 13:52:10'),
(8, 'Haruki Murakami', 'haruki-murakami', 'Jepang', 'Novelis surrealis ternama.', '2026-11-23 13:52:10'),
(9, 'Raditya Dika', 'raditya-dika', 'Indonesia', 'Penulis dan komika.', '2026-11-23 13:52:10'),
(10, 'Sapardi Djoko Damono', 'sapardi-djoko-damono', 'Indonesia', 'Penyair dan maestro sastra.', '2026-11-23 13:52:10'),
(71, 'James Clear', 'james-clear', '', '', '2026-11-23 16:31:56'),
(76, 'Penulis Uji', 'penulis-uji', '', NULL, '2026-11-26 18:22:23');

-- --------------------------------------------------------

--
-- Table structure for table `books`
--

DROP TABLE IF EXISTS `books`;
CREATE TABLE IF NOT EXISTS `books` (
  `book_id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `author_id` int UNSIGNED NOT NULL,
  `publisher_id` int UNSIGNED NOT NULL,
  `genre_id` int UNSIGNED NOT NULL,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `slug` varchar(220) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `published_year` smallint UNSIGNED DEFAULT NULL,
  `isbn` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `storage_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_size` int UNSIGNED NOT NULL DEFAULT '0',
  `checksum_sha256` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `page_count` int UNSIGNED NOT NULL DEFAULT '0',
  `cover_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`book_id`),
  UNIQUE KEY `uq_books_slug` (`slug`),
  UNIQUE KEY `uq_books_storage` (`storage_path`),
  UNIQUE KEY `uq_books_isbn` (`isbn`),
  KEY `idx_books_author` (`author_id`),
  KEY `idx_books_publisher` (`publisher_id`),
  KEY `idx_books_genre` (`genre_id`)
) ENGINE=InnoDB AUTO_INCREMENT=142 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `books`
--

INSERT INTO `books` (`book_id`, `author_id`, `publisher_id`, `genre_id`, `title`, `slug`, `description`, `published_year`, `isbn`, `storage_path`, `file_size`, `checksum_sha256`, `page_count`, `cover_path`, `is_active`, `created_at`) VALUES
(1, 1, 5, 1, 'Dasar-Dasar Basis Data', 'dasar-dasar-basis-data', 'Pengantar konsep basis data, ERD, dan normalisasi.', 2021, '978-602-0001-01-1', 'storage/ebooks/1.pdf', 709, 'a60748074c8c40299fc4a73ec7f29048d4225bdc00942580b6dc9acd7fd78201', 1, 'uploads/covers/1.jpg', 1, '2026-11-23 13:52:10'),
(2, 3, 4, 1, 'Belajar Java OOP', 'belajar-java-oop', 'Panduan belajar Java dari dasar hingga OOP.', 2020, '978-602-0001-02-8', 'storage/ebooks/2.pdf', 679, 'a8373e7c32ec6f6c9c41ddf224cb2773acf938d4957632973799fc1f0612d9ca', 1, 'uploads/covers/2.jpg', 1, '2026-11-23 13:52:10'),
(3, 4, 2, 2, 'Laskar Pelangi', 'laskar-pelangi', 'Kisah perjuangan sepuluh anak di Belitung.', 2005, '978-979-3062-79-2', 'storage/ebooks/3.pdf', 5704, '8d05c821f1ab4f41880a7c9e385cda3eb632e8898e8400cb3587255d3b6872a9', 12, 'uploads/covers/3.jpg', 1, '2026-11-23 13:52:10'),
(4, 5, 4, 3, 'Fisika Dasar Modern', 'fisika-dasar-modern', 'Pembahasan konsep fisika modern secara ringkas.', 2019, '978-602-0001-04-2', 'storage/ebooks/4.pdf', 679, 'b328c6300f036e4eda744ce5bb7357f2a280fb14278c7772a6c7253fad4a3e06', 1, 'uploads/covers/4.jpg', 1, '2026-11-23 13:52:10'),
(5, 6, 2, 4, 'Sejarah Nusantara', 'sejarah-nusantara', 'Rangkuman sejarah kerajaan di Nusantara.', 2018, '978-602-0001-05-9', 'storage/ebooks/5.pdf', 5740, '9f1c3eac374eaed2005aa6e8d750270ece6f8b28f5b73115508cc48ac1d26bc5', 12, 'uploads/covers/5.jpg', 1, '2026-11-23 13:52:10'),
(6, 7, 1, 5, 'Strategi Bisnis Digital', 'strategi-bisnis-digital', 'Langkah praktis membangun usaha berbasis digital.', 2022, '978-602-0001-06-6', 'storage/ebooks/6.pdf', 686, '3692b229a3fbb5981bb29582ff1d4f7a3d430bd6d33c8a6c0d9293374dcf36af', 1, 'uploads/covers/6.jpg', 1, '2026-11-23 13:52:10'),
(7, 8, 1, 2, 'Harry Potter 1', 'harry-potter-1', 'Kisah penyihir muda bernama Harry Potter.', 1997, '978-602-0001-07-3', 'storage/ebooks/7.pdf', 680, '4f37637b1f45ad8d3de6f051fa50b0f1216a7977aa91ffdae1af054bc7e4996b', 1, 'uploads/covers/7.jpg', 1, '2026-11-23 13:52:10'),
(8, 9, 4, 6, 'Metode Pembelajaran Aktif', 'metode-pembelajaran-aktif', 'Kumpulan metode belajar mengajar interaktif.', 2021, '978-602-0001-08-0', 'storage/ebooks/8.pdf', 5836, '6428022505327831eca60df89cebe1a1b23caefa58bedf2adc7aa889f7804ff4', 12, 'uploads/covers/8.jpg', 1, '2026-11-23 13:52:10'),
(9, 10, 2, 7, 'Psikologi Remaja', 'psikologi-remaja', 'Ulasan tahap perkembangan psikologis remaja.', 2016, '978-602-0001-09-7', 'storage/ebooks/9.pdf', 5728, 'eba55a99a1d38660cd988177aa1c6453048b9688e795c108a0dec960cf2d7066', 12, 'uploads/covers/9.jpg', 1, '2026-11-23 13:52:10'),
(10, 1, 5, 9, 'Prinsip Desain UI/UX', 'prinsip-desain-ui-ux', 'Prinsip dasar merancang antarmuka yang ramah pengguna.', 2023, '978-602-0001-10-3', 'storage/ebooks/10.pdf', 692, '63c49238562aa5a1c7969534010fc96e0f6b51b4f47edba024e2c05d57661346', 1, 'uploads/covers/10.jpg', 1, '2026-11-23 13:52:10'),
(84, 71, 36, 7, 'Atomic Habits: The life-changing million copy bestseller', 'atomic-habits-the-life-changing-million-copy-bestseller', 'No matter your goals, Atomic Habits offers a proven framework for improving every day. James Clear, one of the world\'s leading experts on habit formation, reveals practical strategies that will teach you exactly how to form good habits, break bad ones, and master the tiny behaviors that lead to remarkable results.', 2018, NULL, 'storage/ebooks/84.pdf', 6150949, '96369e43aff90e81e61aec2db61e6d6fd4da9a00a1cb26f66155cb995e1f9a9e', 285, 'uploads/covers/84.jpg', 1, '2026-11-23 16:34:27'),
(109, 7, 2, 5, 'tes', 'tes', NULL, NULL, NULL, 'storage/ebooks/109.pdf', 556379, '8b508cc07c4e188e0e054e3e174c25ee70ab4329d5232cde11d713cf6395400c', 7, 'uploads/covers/109.jpg', 0, '2026-11-26 19:38:45'),
(110, 7, 1, 9, 'tes', 'tes-2', NULL, NULL, NULL, 'storage/ebooks/110.pdf', 556379, '8b508cc07c4e188e0e054e3e174c25ee70ab4329d5232cde11d713cf6395400c', 7, '', 0, '2026-11-26 19:39:09'),
(135, 76, 39, 7, 'skenario berapa', 'skenario-berapa', NULL, NULL, NULL, 'storage/ebooks/135.pdf', 556379, '8b508cc07c4e188e0e054e3e174c25ee70ab4329d5232cde11d713cf6395400c', 7, 'uploads/covers/135.jpg', 1, '2026-11-26 21:26:26');

-- --------------------------------------------------------

--
-- Table structure for table `collections`
--

DROP TABLE IF EXISTS `collections`;
CREATE TABLE IF NOT EXISTS `collections` (
  `collection_id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` int UNSIGNED NOT NULL,
  `book_id` int UNSIGNED NOT NULL,
  `status` enum('to_read','reading','not_interested','finished') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'to_read',
  `status_source` enum('system','user') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'system',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`collection_id`),
  UNIQUE KEY `uq_collections_user_book` (`user_id`,`book_id`),
  KEY `fk_collections_book` (`book_id`)
) ENGINE=InnoDB AUTO_INCREMENT=834 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `collections`
--

INSERT INTO `collections` (`collection_id`, `user_id`, `book_id`, `status`, `status_source`, `created_at`) VALUES
(2, 3, 3, 'reading', 'user', '2024-01-14 17:00:00'),
(3, 4, 9, 'to_read', 'user', '2024-01-31 17:00:00'),
(4, 5, 7, 'not_interested', 'user', '2024-02-09 17:00:00'),
(5, 6, 6, 'finished', 'user', '2024-02-29 17:00:00'),
(6, 7, 2, 'reading', 'user', '2024-03-19 17:00:00'),
(7, 8, 5, 'to_read', 'user', '2024-03-31 17:00:00'),
(8, 9, 4, 'not_interested', 'user', '2024-04-14 17:00:00'),
(9, 10, 10, 'finished', 'user', '2024-04-30 17:00:00'),
(53, 1, 8, 'reading', 'system', '2024-05-09 17:00:00'),
(220, 1, 84, 'reading', 'user', '2026-11-23 16:35:33'),
(233, 130, 84, 'reading', 'system', '2026-11-23 16:54:59'),
(234, 130, 8, 'to_read', 'user', '2026-11-23 16:55:02'),
(243, 2, 1, 'finished', 'user', '2026-11-23 17:05:25'),
(289, 2, 84, 'reading', 'user', '2026-11-23 16:59:39'),
(690, 1, 135, 'finished', 'user', '2026-11-26 21:26:35');

-- --------------------------------------------------------

--
-- Table structure for table `genres`
--

DROP TABLE IF EXISTS `genres`;
CREATE TABLE IF NOT EXISTS `genres` (
  `genre_id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`genre_id`),
  UNIQUE KEY `uq_genres_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=83 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `genres`
--

INSERT INTO `genres` (`genre_id`, `name`, `description`) VALUES
(1, 'Teknologi', 'Buku seputar IT, komputer, dan pemrograman.'),
(2, 'Novel', 'Buku fiksi, roman, dan cerita panjang.'),
(3, 'Sains', 'Buku ilmu pengetahuan alam dan fisika.'),
(4, 'Sejarah', 'Buku peristiwa masa lalu dan budaya.'),
(5, 'Bisnis', 'Buku manajemen, keuangan, dan kewirausahaan.'),
(6, 'Pendidikan', 'Buku pendukung pembelajaran dan kurikulum.'),
(7, 'Psikologi', 'Buku tentang perilaku dan kejiwaan manusia.'),
(8, 'Agama', 'Buku keagamaan, spiritual, dan moral.'),
(9, 'Desain', 'Buku tentang desain grafis, UI, dan UX.'),
(10, 'Komik', 'Karya cerita bergambar berbagai genre.'),
(35, 'Genre Uji', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `publishers`
--

DROP TABLE IF EXISTS `publishers`;
CREATE TABLE IF NOT EXISTS `publishers` (
  `publisher_id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `slug` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `city` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`publisher_id`),
  UNIQUE KEY `uq_publishers_slug` (`slug`)
) ENGINE=InnoDB AUTO_INCREMENT=87 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `publishers`
--

INSERT INTO `publishers` (`publisher_id`, `name`, `slug`, `city`, `created_at`) VALUES
(1, 'Gramedia Pustaka Utama', 'gramedia-pustaka-utama', 'Jakarta', '2026-11-23 13:52:10'),
(2, 'Bentang Pustaka', 'bentang-pustaka', 'Yogyakarta', '2026-11-23 13:52:10'),
(3, 'Mizan Publishing', 'mizan-publishing', 'Bandung', '2026-11-23 13:52:10'),
(4, 'Erlangga', 'erlangga', 'Jakarta', '2026-11-23 13:52:10'),
(5, 'Pustaka Digital Press', 'pustaka-digital-press', 'Pekanbaru', '2026-11-23 13:52:10'),
(36, 'Random House Business', 'random-house-business', 'New York', '2026-11-23 16:31:41'),
(39, 'Penerbit Uji', 'penerbit-uji', '', '2026-11-26 18:22:23');

-- --------------------------------------------------------

--
-- Table structure for table `reading_progress`
--

DROP TABLE IF EXISTS `reading_progress`;
CREATE TABLE IF NOT EXISTS `reading_progress` (
  `progress_id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` int UNSIGNED NOT NULL,
  `book_id` int UNSIGNED NOT NULL,
  `last_page` int UNSIGNED NOT NULL DEFAULT '1',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`progress_id`),
  UNIQUE KEY `uq_progress_user_book` (`user_id`,`book_id`),
  KEY `fk_progress_book` (`book_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1044 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `reading_progress`
--

INSERT INTO `reading_progress` (`progress_id`, `user_id`, `book_id`, `last_page`, `updated_at`) VALUES
(1, 2, 1, 1, '2024-01-09 17:00:00'),
(2, 3, 3, 12, '2024-01-19 17:00:00'),
(3, 5, 7, 1, '2024-02-11 17:00:00'),
(4, 6, 6, 1, '2024-03-14 17:00:00'),
(5, 7, 2, 1, '2024-03-24 17:00:00'),
(6, 9, 4, 1, '2024-04-15 17:00:00'),
(7, 10, 10, 1, '2024-05-04 17:00:00'),
(8, 1, 8, 1, '2026-11-23 16:30:19'),
(449, 1, 84, 98, '2026-11-26 19:29:07'),
(554, 130, 84, 15, '2026-11-23 16:56:35'),
(655, 2, 84, 206, '2026-11-26 21:28:37'),
(937, 1, 135, 7, '2026-11-26 21:27:16');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
CREATE TABLE IF NOT EXISTS `users` (
  `user_id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `display_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('admin','member') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'member',
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uq_users_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=135 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `email`, `password_hash`, `display_name`, `role`, `is_active`, `created_at`) VALUES
(1, 'admin@pustaka.com', '$2y$12$iYAY01OO6d2pObrZqD3n7On877KKS3GUny.dXh56oY0IMNOMcI78u', 'Admin Pustaka', 'admin', 1, '2026-11-23 13:52:10'),
(2, 'andi@email.com', '$2y$12$4ehiq2FWUuarvd77mspoEu/kTV.mvBXwnasn7CG9uv2Dygf5bz0C6', 'Andi Saputra', 'member', 1, '2026-11-23 13:52:10'),
(3, 'siti@email.com', '$2y$12$6WhyoqOA7ay0sCJKjrU39uhzWen82.v0TELnj.z5QOLjfSusZ6WKS', 'Siti Aulia', 'member', 1, '2026-11-23 13:52:10'),
(4, 'budi@email.com', '$2y$12$y7wr9yXIOLrdIf6knoOELuqhxDv5ehgDsp2C.MgGChnEQKEqiBbTG', 'Budi Pratama', 'member', 1, '2026-11-23 13:52:10'),
(5, 'dewi@email.com', '$2y$12$rbacQmpwbembQet/1WjWFOvhmdQ3yU5AW4q2s3OzFF7BI8HF7caG6', 'Dewi Lestari', 'member', 1, '2026-11-23 13:52:10'),
(6, 'rizky@email.com', '$2y$12$jXtrzOTmymAwN7eCXrfzmuJ4Ma9YluTypLDgjqThAjWCOzSMlzXOa', 'Rizky Ramadhan', 'member', 1, '2026-11-23 13:52:10'),
(7, 'putri@email.com', '$2y$12$3xGNveYswXfDimAt/a05pOW7TtdMXby5aYZ11bryMJzGH54KL3iIS', 'Putri Maharani', 'member', 1, '2026-11-23 13:52:10'),
(8, 'fajar@email.com', '$2y$12$S2.SrBkO35cpXQNSIeAuVeoaaumufRJUQ2rUm3zy0A2cRoFspcA6y', 'Fajar Hidayat', 'member', 1, '2026-11-23 13:52:10'),
(9, 'nadia@email.com', '$2y$12$6zaeUgheJhlFm6tkYZjxl.pdVfrYNJdIvROdyS051cQmVCyLGsc9m', 'Nadia Permata', 'member', 1, '2026-11-23 13:52:10'),
(10, 'dimas@email.com', '$2y$12$Z7Y4bSL6IjauudX.jjai9.h2aRglmDo.M..KSwWRq90cL3kxJ3od2', 'Dimas Firmansyah', 'member', 1, '2026-11-23 13:52:10'),
(130, 'sapi@mata.com', '$2y$12$zMJsYZWQdn9XCUFs0jPb4Ozdlx0jf/PV5sqwWcxLWhfPmOuKw7hrC', 'Mata Sapi', 'member', 0, '2026-11-23 16:54:33');

-- --------------------------------------------------------

--
-- Stand-in structure for view `v_books_catalog`
-- (See below for the actual view)
--
DROP VIEW IF EXISTS `v_books_catalog`;
CREATE TABLE IF NOT EXISTS `v_books_catalog` (
`author_bio` text
,`author_country` varchar(60)
,`author_name` varchar(100)
,`author_slug` varchar(120)
,`book_id` int unsigned
,`checksum_sha256` char(64)
,`cover_path` varchar(255)
,`created_at` timestamp
,`description` text
,`file_size` int unsigned
,`genre_id` int unsigned
,`genre_name` varchar(100)
,`is_active` tinyint(1)
,`isbn` varchar(20)
,`page_count` int unsigned
,`published_year` smallint unsigned
,`publisher_city` varchar(100)
,`publisher_name` varchar(100)
,`publisher_slug` varchar(120)
,`slug` varchar(220)
,`storage_path` varchar(255)
,`title` varchar(200)
);

-- --------------------------------------------------------

--
-- Structure for view `v_books_catalog`
--
DROP TABLE IF EXISTS `v_books_catalog`;

DROP VIEW IF EXISTS `v_books_catalog`;
CREATE OR REPLACE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_books_catalog`  AS SELECT `b`.`book_id` AS `book_id`, `b`.`title` AS `title`, `b`.`slug` AS `slug`, `b`.`description` AS `description`, `b`.`published_year` AS `published_year`, `b`.`isbn` AS `isbn`, `b`.`storage_path` AS `storage_path`, `b`.`file_size` AS `file_size`, `b`.`checksum_sha256` AS `checksum_sha256`, `b`.`page_count` AS `page_count`, `b`.`cover_path` AS `cover_path`, `b`.`is_active` AS `is_active`, `b`.`created_at` AS `created_at`, `a`.`name` AS `author_name`, `a`.`slug` AS `author_slug`, `a`.`country` AS `author_country`, `a`.`bio` AS `author_bio`, `g`.`genre_id` AS `genre_id`, `g`.`name` AS `genre_name`, `p`.`name` AS `publisher_name`, `p`.`slug` AS `publisher_slug`, `p`.`city` AS `publisher_city` FROM (((`books` `b` join `authors` `a` on((`a`.`author_id` = `b`.`author_id`))) join `genres` `g` on((`g`.`genre_id` = `b`.`genre_id`))) join `publishers` `p` on((`p`.`publisher_id` = `b`.`publisher_id`))) ;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `books`
--
ALTER TABLE `books`
  ADD CONSTRAINT `fk_books_author` FOREIGN KEY (`author_id`) REFERENCES `authors` (`author_id`),
  ADD CONSTRAINT `fk_books_genre` FOREIGN KEY (`genre_id`) REFERENCES `genres` (`genre_id`),
  ADD CONSTRAINT `fk_books_publisher` FOREIGN KEY (`publisher_id`) REFERENCES `publishers` (`publisher_id`);

--
-- Constraints for table `collections`
--
ALTER TABLE `collections`
  ADD CONSTRAINT `fk_collections_book` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`),
  ADD CONSTRAINT `fk_collections_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);

--
-- Constraints for table `reading_progress`
--
ALTER TABLE `reading_progress`
  ADD CONSTRAINT `fk_progress_book` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`),
  ADD CONSTRAINT `fk_progress_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
