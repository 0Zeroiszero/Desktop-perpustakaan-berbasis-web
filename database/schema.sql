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
