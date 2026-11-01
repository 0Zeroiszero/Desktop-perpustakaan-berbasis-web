-- MariaDB dump 10.19  Distrib 10.4.32-MariaDB, for Win64 (AMD64)
--
-- Host: localhost    Database: db_pustaka
-- ------------------------------------------------------
-- Server version	10.4.32-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `author`
--

DROP TABLE IF EXISTS `author`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `author` (
  `ID_AUTHOR` varchar(10) NOT NULL,
  `NAMA_DEPAN` varchar(50) NOT NULL,
  `NAMA_BELAKANG` varchar(50) DEFAULT NULL,
  `EMAIL` varchar(100) DEFAULT NULL,
  `NEGARA_ASAL` varchar(50) DEFAULT NULL,
  `BIO` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`ID_AUTHOR`),
  UNIQUE KEY `UQ_AUTHORS_EMAIL` (`EMAIL`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `author`
--

LOCK TABLES `author` WRITE;
/*!40000 ALTER TABLE `author` DISABLE KEYS */;
INSERT INTO `author` VALUES ('AUT01','Pramoedya','Ananta Toer','pram@pustaka.id','Indonesia','Sastrawan legendaris penulis Tetralogi Buru.'),('AUT02','Andrea','Hirata','andrea@pustaka.id','Indonesia','Penulis novel Laskar Pelangi.'),('AUT03','J.K.','Rowling','jk@pustaka.id','Inggris','Penulis seri Harry Potter.'),('AUT04','George','R.R. Martin','grrm@pustaka.id','Amerika','Penulis A Song of Ice and Fire.'),('AUT05','Tere','Liye','tere@pustaka.id','Indonesia','Penulis produktif novel populer.'),('AUT06','Dee','Lestari','dee@pustaka.id','Indonesia','Penulis seri Supernova.'),('AUT07','Ahmad','Tohari','ahmad@pustaka.id','Indonesia','Sastrawan Ronggeng Dukuh Paruk.'),('AUT08','Haruki','Murakami','haruki@pustaka.id','Jepang','Novelis surrealis ternama.'),('AUT09','Raditya','Dika','raditya@pustaka.id','Indonesia','Penulis dan komika.'),('AUT10','Sapardi','Djoko Damono','sapardi@pustaka.id','Indonesia','Penyair dan maestro sastra.');
/*!40000 ALTER TABLE `author` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ebook`
--

DROP TABLE IF EXISTS `ebook`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ebook` (
  `ID_BOOK` varchar(10) NOT NULL,
  `JUDUL` varchar(255) NOT NULL,
  `TAHUN_TERBIT` smallint(6) DEFAULT NULL,
  `JUMLAH_HALAMAN` int(11) DEFAULT NULL,
  `ISBN` varchar(20) DEFAULT NULL,
  `PENERBIT` varchar(150) DEFAULT NULL,
  `SINOPSIS` varchar(2000) DEFAULT NULL,
  `COVER` varchar(255) DEFAULT NULL,
  `FILE_BOOK` varchar(255) DEFAULT NULL,
  `FORMAT_FILE` varchar(10) DEFAULT NULL,
  `KATEGORI_ID_KATEGORI` varchar(10) NOT NULL,
  PRIMARY KEY (`ID_BOOK`),
  UNIQUE KEY `BOOK_ISBN_UN` (`ISBN`),
  KEY `IDX_EBOOK_KATEGORI` (`KATEGORI_ID_KATEGORI`),
  CONSTRAINT `BOOK_KATEGORI_FK` FOREIGN KEY (`KATEGORI_ID_KATEGORI`) REFERENCES `kategori` (`ID_KATEGORI`),
  CONSTRAINT `EBOOK_TAHUN_CK` CHECK (`TAHUN_TERBIT` between 1000 and 2100),
  CONSTRAINT `EBOOK_FORMAT_CK` CHECK (`FORMAT_FILE` in ('PDF','EPUB','MOBI'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ebook`
--

LOCK TABLES `ebook` WRITE;
/*!40000 ALTER TABLE `ebook` DISABLE KEYS */;
INSERT INTO `ebook` VALUES ('BK001','Dasar-Dasar Basis Data',2021,250,'978-602-0001-01-1',NULL,'Pengantar konsep basis data, ERD, dan normalisasi.','cover/bk001.jpg','ebook/bk001.pdf','PDF','KAT01'),('BK002','Belajar Java OOP',2020,320,'978-602-0001-02-8',NULL,'Panduan belajar Java dari dasar hingga OOP.','cover/bk002.jpg','ebook/bk002.pdf','PDF','KAT01'),('BK003','Laskar Pelangi',2005,529,'978-979-3062-79-2',NULL,'Kisah perjuangan sepuluh anak di Belitung.','cover/bk003.jpg','ebook/bk003.epub','EPUB','KAT02'),('BK004','Fisika Dasar Modern',2019,410,'978-602-0001-04-2',NULL,'Pembahasan konsep fisika modern secara ringkas.','cover/bk004.jpg','ebook/bk004.pdf','PDF','KAT03'),('BK005','Sejarah Nusantara',2018,380,'978-602-0001-05-9',NULL,'Rangkuman sejarah kerajaan di Nusantara.','cover/bk005.jpg','ebook/bk005.epub','EPUB','KAT04'),('BK006','Strategi Bisnis Digital',2022,200,'978-602-0001-06-6',NULL,'Langkah praktis membangun usaha berbasis digital.','cover/bk006.jpg','ebook/bk006.pdf','PDF','KAT05'),('BK007','Harry Potter 1',1997,350,'978-602-0001-07-3',NULL,'Kisah penyihir muda bernama Harry Potter.','cover/bk007.jpg','ebook/bk007.pdf','PDF','KAT02'),('BK008','Metode Pembelajaran Aktif',2021,180,'978-602-0001-08-0',NULL,'Kumpulan metode belajar mengajar interaktif.','cover/bk008.jpg','ebook/bk008.mobi','MOBI','KAT06'),('BK009','Psikologi Remaja',2016,290,'978-602-0001-09-7',NULL,'Ulasan tahap perkembangan psikologis remaja.','cover/bk009.jpg','ebook/bk009.epub','EPUB','KAT07'),('BK010','Prinsip Desain UI/UX',2023,220,'978-602-0001-10-3',NULL,'Prinsip dasar merancang antarmuka yang ramah pengguna.','cover/bk010.jpg','ebook/bk010.pdf','PDF','KAT09');
/*!40000 ALTER TABLE `ebook` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ebook_author`
--

DROP TABLE IF EXISTS `ebook_author`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ebook_author` (
  `BOOK_ID_BOOK` varchar(10) NOT NULL,
  `AUTHOR_ID_AUTHOR` varchar(10) NOT NULL,
  PRIMARY KEY (`BOOK_ID_BOOK`,`AUTHOR_ID_AUTHOR`),
  KEY `IDX_EA_AUTHOR` (`AUTHOR_ID_AUTHOR`),
  CONSTRAINT `BOOK_AUTHOR_AUTHOR_FK` FOREIGN KEY (`AUTHOR_ID_AUTHOR`) REFERENCES `author` (`ID_AUTHOR`),
  CONSTRAINT `BOOK_AUTHOR_BOOK_FK` FOREIGN KEY (`BOOK_ID_BOOK`) REFERENCES `ebook` (`ID_BOOK`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ebook_author`
--

LOCK TABLES `ebook_author` WRITE;
/*!40000 ALTER TABLE `ebook_author` DISABLE KEYS */;
INSERT INTO `ebook_author` VALUES ('BK001','AUT01'),('BK001','AUT02'),('BK002','AUT03'),('BK003','AUT04'),('BK003','AUT05'),('BK004','AUT05'),('BK005','AUT06'),('BK006','AUT07'),('BK007','AUT08'),('BK008','AUT09'),('BK009','AUT10'),('BK010','AUT01');
/*!40000 ALTER TABLE `ebook_author` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `kategori`
--

DROP TABLE IF EXISTS `kategori`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `kategori` (
  `ID_KATEGORI` varchar(10) NOT NULL,
  `NAMA_KATEGORI` varchar(100) NOT NULL,
  `DESKRIPSI` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`ID_KATEGORI`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `kategori`
--

LOCK TABLES `kategori` WRITE;
/*!40000 ALTER TABLE `kategori` DISABLE KEYS */;
INSERT INTO `kategori` VALUES ('KAT01','Teknologi','Buku seputar IT, komputer, dan pemrograman.'),('KAT02','Novel','Buku fiksi, roman, dan cerita panjang.'),('KAT03','Sains','Buku ilmu pengetahuan alam dan fisika.'),('KAT04','Sejarah','Buku peristiwa masa lalu dan budaya.'),('KAT05','Bisnis','Buku manajemen, keuangan, dan kewirausahaan.'),('KAT06','Pendidikan','Buku pendukung pembelajaran dan kurikulum.'),('KAT07','Psikologi','Buku tentang perilaku dan kejiwaan manusia.'),('KAT08','Agama','Buku keagamaan, spiritual, dan moral.'),('KAT09','Desain','Buku tentang desain grafis, UI, dan UX.'),('KAT10','Komik','Karya cerita bergambar berbagai genre.');
/*!40000 ALTER TABLE `kategori` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reading_progress`
--

DROP TABLE IF EXISTS `reading_progress`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `reading_progress` (
  `ID_PROGRESS` varchar(10) NOT NULL,
  `USER_ID_USER` varchar(10) NOT NULL,
  `BOOK_ID_BOOK` varchar(10) NOT NULL,
  `PROGRESS_PERCENT` decimal(5,2) DEFAULT NULL,
  `LAST_READ` date DEFAULT NULL,
  `LAST_POSITION` int(11) DEFAULT NULL,
  `START_DATE` date DEFAULT NULL,
  `END_DATE` date DEFAULT NULL,
  `STATUS_BACA` varchar(20) DEFAULT 'Akan dibaca',
  PRIMARY KEY (`ID_PROGRESS`),
  KEY `IDX_RP_USER` (`USER_ID_USER`),
  KEY `IDX_RP_BOOK` (`BOOK_ID_BOOK`),
  CONSTRAINT `READING_PROGRESS_BOOK_FK` FOREIGN KEY (`BOOK_ID_BOOK`) REFERENCES `ebook` (`ID_BOOK`),
  CONSTRAINT `READING_PROGRESS_USER_FK` FOREIGN KEY (`USER_ID_USER`) REFERENCES `users` (`ID_USER`),
  CONSTRAINT `READING_PROGRESS_STATUS_CK` CHECK (`STATUS_BACA` in ('Akan dibaca','Sedang dibaca','Telah dibaca','Tidak selesai'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reading_progress`
--

LOCK TABLES `reading_progress` WRITE;
/*!40000 ALTER TABLE `reading_progress` DISABLE KEYS */;
INSERT INTO `reading_progress` VALUES ('PRG001','USR002','BK001',100.00,'2024-01-10',500,'2024-01-01','2024-01-10','Telah dibaca'),('PRG002','USR003','BK003',50.00,'2024-01-20',150,'2024-01-15',NULL,'Sedang dibaca'),('PRG003','USR004','BK009',0.00,NULL,0,NULL,NULL,'Akan dibaca'),('PRG004','USR005','BK007',10.00,'2024-02-12',25,'2024-02-10',NULL,'Tidak selesai'),('PRG005','USR006','BK006',100.00,'2024-03-15',410,'2024-03-01','2024-03-15','Telah dibaca'),('PRG006','USR007','BK002',75.00,'2024-03-25',280,'2024-03-20',NULL,'Sedang dibaca'),('PRG007','USR008','BK005',0.00,NULL,0,NULL,NULL,'Akan dibaca'),('PRG008','USR009','BK004',5.00,'2024-04-16',10,'2024-04-15',NULL,'Tidak selesai'),('PRG009','USR010','BK010',100.00,'2024-05-05',120,'2024-05-01','2024-05-05','Telah dibaca'),('PRG010','USR001','BK008',40.00,'2024-05-12',180,'2024-05-10',NULL,'Sedang dibaca');
/*!40000 ALTER TABLE `reading_progress` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `ID_USER` varchar(10) NOT NULL,
  `NAMA_DEPAN` varchar(50) NOT NULL,
  `NAMA_BELAKANG` varchar(50) DEFAULT NULL,
  `JENIS_KELAMIN` char(1) DEFAULT NULL,
  `EMAIL` varchar(100) DEFAULT NULL,
  `USERNAME` varchar(60) DEFAULT NULL,
  `PASSWORD` varchar(255) NOT NULL,
  `USER_ROLE` varchar(20) DEFAULT NULL,
  `NOMOR_PONSEL` varchar(20) DEFAULT NULL,
  `ALAMAT` varchar(256) DEFAULT NULL,
  `BIO` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`ID_USER`),
  UNIQUE KEY `USERS_EMAIL_UN` (`EMAIL`),
  UNIQUE KEY `USERS_USERNAME_UN` (`USERNAME`),
  CONSTRAINT `USERS_GENDER_CK` CHECK (`JENIS_KELAMIN` in ('L','P') or `JENIS_KELAMIN` is null),
  CONSTRAINT `USERS_ROLE_CK` CHECK (`USER_ROLE` in ('Admin','User','admin','anggota') or `USER_ROLE` is null)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES ('USR001','Admin','Pustaka','L','admin@pustaka.com','adminpustaka','$2a$12$f7xeRJb4gEN4J11JQbfa5.3YXa.JgjbFZt/OCJsapqy0Ehc1GWAqi','Admin','081234567801','Pekanbaru','Administrator sistem.'),('USR002','Andi','Saputra','L','andi@email.com','andisaputra','$2y$12$NoyWLgsKP4mn61ppkV4c1Or0OWIJIq63dvjbpjiRD1kmsHryWh/EW','anggota','081234567802','Pekanbaru','Mahasiswa yang gemar membaca.'),('USR003','Siti','Aulia','P','siti@email.com','sitiaulia','$2y$12$URh37V939Zzo4vk1NwBKROrjWRGD6nYWwME.vRcDqsHccxmqBodfW','anggota','081234567803','Dumai','Menyukai buku fiksi.'),('USR004','Budi','Pratama','L','budi@email.com','budipratama','$2y$12$P3j9EyVVfRbsqivTuFFoBu.JNEqa3lar4E57zuBRWCdao7nIEFLgW','anggota','081234567804','Bangkinang','Menyukai buku teknologi.'),('USR005','Dewi','Lestari','P','dewi@email.com','dewilestari','$2y$12$ToyHaDa.L0RJmLLGezS1S.Y1V0y/RUVsX9msR.o90ysn6c.AWXTLe','anggota','081234567805','Siak','Menyukai novel.'),('USR006','Rizky','Ramadhan','L','rizky@email.com','rizkyramadhan','$2y$12$kGr35mHYxOZnSjBy4.WW1eYzFncNeKJizQs0tipMys8jXBot4tFLa','anggota','081234567806','Rengat','Membaca buku pengembangan diri.'),('USR007','Putri','Maharani','P','putri@email.com','putrimaharani','$2y$12$ZceqtApRr4Q4hmvGynk9huuR1ULZ3aOQVdc6pF73dT541kWXctQ2e','anggota','081234567807','Tembilahan','Menyukai buku pendidikan.'),('USR008','Fajar','Hidayat','L','fajar@email.com','fajarhidayat','$2y$12$ibmZPLfAhMcOl.BtxajT5OFz9ZPSSEgIBtVEm7lzgp7os8.s5WsRu','anggota','081234567808','Pelalawan','Menyukai buku sejarah.'),('USR009','Nadia','Permata','P','nadia@email.com','nadiapermata','$2y$12$K1Yzj2i0VC829yOvVoDOte1YLkQDeq1Qe7yHFf0tO3Tw7F9hYqfui','anggota','081234567809','Bengkalis','Menyukai buku sains.'),('USR010','Dimas','Firmansyah','L','dimas@email.com','dimasfirmansyah','$2y$12$drIVbhl9/PDoeHGgyhCVteMLXmj7qD9rJ9smT0UKO1VvRN6AjXsdm','anggota','081234567810','Duri','Menyukai buku pemrograman.');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'db_pustaka'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-01 14:09:48
