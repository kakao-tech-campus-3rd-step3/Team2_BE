SET FOREIGN_KEY_CHECKS=0;

-- Table structure for table `source`
--

LOCK TABLES `source` WRITE;
/*!40000 ALTER TABLE `source` DISABLE KEYS */;
INSERT INTO `source` (`page_count`, `created_at`, `file_size_bytes`, `id`, `member_id`, `source_folder_id`, `updated_at`, `content_type`, `file_path`, `original_name`, `status`) VALUES (NULL,'2025-10-05 18:58:23.883694',536842,1,1,1,'2025-10-05 18:58:23.883694','application/pdf','learning-sources/2025/10/05/member-1/f1753099-b8f0-47ed-b483-c2fbdb9a850f.pdf','241105_카카오테크_캠퍼스_앱_배포_특강.pdf','UPLOADED'),
                                                                                                                                                                                         (NULL,'2025-10-05 18:58:40.204798',514428,2,1,1,'2025-10-05 18:58:40.204798','application/pdf','learning-sources/2025/10/05/member-1/b35e9b9c-bdda-4c74-ab4a-93e1bbfe3837.pdf','2020학년도 학사과정 교육과정(컴퓨터공학과).pdf','UPLOADED');
/*!40000 ALTER TABLE `source` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `source_folder`
--

LOCK TABLES `source_folder` WRITE;
/*!40000 ALTER TABLE `source_folder` DISABLE KEYS */;
INSERT INTO `source_folder` (`created_at`, `id`, `member_id`, `updated_at`, `color`, `description`, `name`) VALUES ('2025-10-05 18:58:23.880792',1,1,'2025-10-05 18:58:23.880792',NULL,NULL,'전체 폴더');
/*!40000 ALTER TABLE `source_folder` ENABLE KEYS */;
UNLOCK TABLES;
