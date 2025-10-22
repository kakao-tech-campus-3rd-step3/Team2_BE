/*M!999999\- enable the sandbox mode */
-- MariaDB dump 10.19  Distrib 10.11.13-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: 127.0.0.1    Database: mydatabase
-- ------------------------------------------------------
-- Server version	12.0.2-MariaDB-ubu2404

SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT;
SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS;
SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION;
SET NAMES utf8mb4;
SET @OLD_TIME_ZONE=@@TIME_ZONE;
SET TIME_ZONE='+00:00';
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO';
SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0;

--
-- Table structure for table `common_folder`
--

DROP TABLE IF EXISTS `common_folder`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `common_folder` (
                                 `sort_order` int(11) NOT NULL,
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                 `name` varchar(255) NOT NULL,
                                 `type` enum('QUESTION_SET') NOT NULL,
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `common_folder`
--

LOCK TABLES `common_folder` WRITE;
/*!40000 ALTER TABLE `common_folder` DISABLE KEYS */;
INSERT INTO `common_folder` (`sort_order`, `id`, `name`, `type`) VALUES (0,1,'전체','QUESTION_SET'),
                                                                        (1,2,'디지털영상처리','QUESTION_SET'),
                                                                        (2,3,'시스템프로그래밍','QUESTION_SET'),
                                                                        (3,4,'인공지능','QUESTION_SET');
/*!40000 ALTER TABLE `common_folder` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `member`
--

DROP TABLE IF EXISTS `member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `member` (
                          `created_at` datetime(6) NOT NULL,
                          `id` bigint(20) NOT NULL AUTO_INCREMENT,
                          `kakao_id` bigint(20) DEFAULT NULL,
                          `updated_at` datetime(6) NOT NULL,
                          `refresh_token` varchar(512) DEFAULT NULL,
                          `email` varchar(255) NOT NULL,
                          `name` varchar(255) DEFAULT NULL,
                          `role` enum('ADMIN','MEMBER') NOT NULL,
                          `status` enum('ACTIVE','BANNED','INACTIVE') DEFAULT NULL,
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `UKmbmcqelty0fbrvxp1q58dn57t` (`email`),
                          UNIQUE KEY `UKtqi1nx9ul3nx7guxpqycuvgue` (`kakao_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member`
--

LOCK TABLES `member` WRITE;
/*!40000 ALTER TABLE `member` DISABLE KEYS */;
INSERT INTO `member` (`created_at`, `id`, `kakao_id`, `updated_at`, `refresh_token`, `email`, `name`, `role`, `status`) VALUES ('2025-10-22 12:39:43.000000',1,123456789,'2025-10-22 12:39:43.000000','sample_refresh_token','test@example.com','홍길동','ADMIN','ACTIVE');
/*!40000 ALTER TABLE `member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `migration_history`
--

DROP TABLE IF EXISTS `migration_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `migration_history` (
                                     `created_at` datetime(6) NOT NULL,
                                     `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                     `updated_at` datetime(6) NOT NULL,
                                     `migration_name` varchar(255) NOT NULL,
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `UKmvh5rqlacqimatqgv28j2wiml` (`migration_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `migration_history`
--

LOCK TABLES `migration_history` WRITE;
/*!40000 ALTER TABLE `migration_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `migration_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `question`
--

DROP TABLE IF EXISTS `question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `question` (
                            `created_at` datetime(6) NOT NULL,
                            `id` bigint(20) NOT NULL AUTO_INCREMENT,
                            `question_set_id` bigint(20) DEFAULT NULL,
                            `updated_at` datetime(6) NOT NULL,
                            `dtype` varchar(31) NOT NULL,
                            `explanation` text DEFAULT NULL,
                            `question_text` text DEFAULT NULL,
                            PRIMARY KEY (`id`),
                            KEY `FKd2w5k3smcsfn7dcjiq5kxseq2` (`question_set_id`),
                            CONSTRAINT `FKd2w5k3smcsfn7dcjiq5kxseq2` FOREIGN KEY (`question_set_id`) REFERENCES `question_set` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question`
--

LOCK TABLES `question` WRITE;
/*!40000 ALTER TABLE `question` DISABLE KEYS */;
INSERT INTO `question` (`created_at`, `id`, `question_set_id`, `updated_at`, `dtype`, `explanation`, `question_text`) VALUES ('2025-10-22 21:40:23.292311',1,1,'2025-10-22 21:40:23.292311','MultipleChoiceQuestion','이산 확률 변수의 경우, 확률 변수가 가질 수 있는 모든 가능한 값에 대한 확률 질량 함수의 합은 항상 1입니다.','확률 변수 X의 가능한 모든 값에 대한 확률 질량 함수(PMF) 값의 합은 얼마입니까?'),
                                                                                                                             ('2025-10-22 21:40:23.309491',2,1,'2025-10-22 21:40:23.309491','MultipleChoiceQuestion','6면 주사위의 기대값 E[X] = ΣxP(x) = 1*(1/6) + 2*(1/6) + 3*(1/6) + 4*(1/6) + 5*(1/6) + 6*(1/6) = (1+2+3+4+5+6)/6 = 21/6 = 3.5입니다.','6면 주사위를 던졌을 때, 각 면이 나올 확률은 동일합니다. 이때, 확률 변수 X를 주사위의 눈의 값이라고 할 때, X의 기대값은 얼마입니까?'),
                                                                                                                             ('2025-10-22 21:40:23.327743',3,1,'2025-10-22 21:40:23.327743','MultipleChoiceQuestion','베르누이 분포를 따르는 확률 변수 X의 기대값 E[X]는 p입니다.','확률 변수 X가 베르누이 분포를 따를 때 (성공 확률 p), X의 기대값은 무엇입니까?'),
                                                                                                                             ('2025-10-22 21:40:23.336210',4,1,'2025-10-22 21:40:23.336210','MultipleChoiceQuestion','정규 분포 N(μ, σ²)의 기대값은 평균 μ입니다.','정규 분포를 따르는 확률 변수 X의 기대값은 무엇입니까?'),
                                                                                                                             ('2025-10-22 21:40:23.351810',5,1,'2025-10-22 21:40:23.351810','MultipleChoiceQuestion','두 확률 변수가 독립이면, 그들의 결합 확률은 각 확률의 곱과 같습니다.','두 확률 변수 X와 Y가 독립일 때, 결합 확률 P(X=x, Y=y)는 어떻게 표현됩니까?'),
                                                                                                                             ('2025-10-22 21:40:23.372212',6,1,'2025-10-22 21:40:23.372212','MultipleChoiceQuestion','조건부 확률 P(A|B)는 P(A와 B의 교집합)을 P(B)로 나눈 값입니다.','조건부 확률 P(A|B)를 계산하는 공식은 무엇입니까?'),
                                                                                                                             ('2025-10-22 21:40:23.395502',7,1,'2025-10-22 21:40:23.395502','MultipleChoiceQuestion','Law of Total Probability는 확률 변수의 모든 가능한 결과에 대한 확률의 총합이 1임을 나타냅니다.','Law of Total Probability (LTP)에 따르면, 이산 확률 변수 X의 모든 가능한 값에 대한 확률 분포 함수 값의 합은 얼마입니까?'),
                                                                                                                             ('2025-10-22 21:40:23.408999',8,1,'2025-10-22 21:40:23.408999','MultipleChoiceQuestion','결합 확률 분포에서 특정 확률 변수의 Marginal Probability를 구하려면, 다른 확률 변수에 대한 확률들을 합(또는 적분)해야 합니다.','Marginal Probability를 구하기 위해, 결합 확률 분포에서 한 확률 변수에 대한 모든 가능한 값에 대해 무엇을 해야 합니까?'),
                                                                                                                             ('2025-10-22 21:40:23.433074',9,1,'2025-10-22 21:40:23.433074','MultipleChoiceQuestion','Bayes\' Theorem은 사전 확률과 우도를 바탕으로 사후 확률을 계산하는 데 사용됩니다.','Bayes\' Theorem은 무엇을 계산하는 데 사용됩니까?'),
                                                                                                                             ('2025-10-22 21:40:23.446097',10,1,'2025-10-22 21:40:23.446097','MultipleChoiceQuestion','기대값 계산 공식에서 x는 확률 변수 X가 취할 수 있는 각 가능한 값을 나타냅니다.','이산 확률 변수의 기대값 E[X] = ΣxP(x)에서, x는 무엇을 나타냅니까?'),
                                                                                                                             ('2025-10-22 21:40:39.519485',11,2,'2025-10-22 21:40:39.519485','TrueFalseQuestion','강의 내용에 따르면, 출석을 제외한 모든 평가 기준이 과제라고 명시되어 있습니다. 중간고사 및 기말고사는 없지만, 평가 기준이 출석과 과제 \'모두\'에만 해당하는 것은 아닙니다. 과제가 주된 평가 기준입니다.','강의 내용에 따르면, 중간고사 및 기말고사는 없으며, 모든 평가 기준은 출석과 과제에만 해당된다.'),
                                                                                                                             ('2025-10-22 21:40:39.528472',12,2,'2025-10-22 21:40:39.528472','TrueFalseQuestion','함수의 정의에 따르면, 집합 X(정의역)의 각 원소는 집합 Y(공역)의 \'유일한\' 원소에 연결되어야 합니다. \'하나 이상의\' 원소에 연결될 수 있다는 설명은 틀렸습니다.','함수는 집합 X (정의역)의 각 원소를 집합 Y (공역)의 \'하나 이상의\' 원소에 연결하는 규칙이다.'),
                                                                                                                             ('2025-10-22 21:40:39.543088',13,2,'2025-10-22 21:40:39.543088','TrueFalseQuestion','표본 공간은 시행에서 얻을 수 있는 모든 가능한 결과들의 집합을 의미합니다. 문제에서 \'특정한 결과\'라고 했지만, 이는 각 시행의 가능한 결과를 포괄하는 의미로 해석할 수 있으며, 일반적으로 모든 가능한 결과의 집합으로 정의됩니다.','표본 공간(Sample Space)은 무한히 반복되는 절차인 시행(Trial)에서 얻을 수 있는 \'모든 가능한\' 결과들의 집합을 의미한다.'),
                                                                                                                             ('2025-10-22 21:40:39.564581',14,2,'2025-10-22 21:40:39.564581','TrueFalseQuestion','확률 변수는 표본 공간을 정의역으로 하지만, 공역은 \'실수 집합\'으로 한정되지 않습니다. 예를 들어, 동전 던지기에서 앞/뒤를 나타내는 경우 공역이 {앞, 뒤}가 될 수도 있습니다. 강의 내용에서는 \'특정한 확률로 발생하는 사건들의 집합\'을 공역으로 하는 함수로 설명하고 있습니다.','확률 변수(Random Variable)는 표본 공간을 정의역으로 하고, 실수 집합을 공역으로 하는 함수이다.'),
                                                                                                                             ('2025-10-22 21:40:39.574268',15,2,'2025-10-22 21:40:39.574268','TrueFalseQuestion','확률 분포 함수는 확률 변수의 가능한 출력값들 (사건들의 집합)을 정의역으로 하고, 각 사건이 발생할 확률 (0과 1 사이의 실수)을 공역으로 하는 함수입니다.','확률 분포(Probability Distribution) 함수는 사건들의 집합을 정의역으로 하고, 0 이상 1 이하의 실수를 공역으로 하는 함수이다.'),
                                                                                                                             ('2025-10-22 21:40:39.587669',16,2,'2025-10-22 21:40:39.587669','TrueFalseQuestion','연속 확률 변수는 공역의 원소 수가 \'무한한\' 확률 변수를 의미합니다. 원소 수가 유한한 경우는 이산 확률 변수입니다.','연속 확률 변수(Continuous Random Variable)는 공역의 원소 수가 유한한 확률 변수를 의미한다.'),
                                                                                                                             ('2025-10-22 21:40:39.597375',17,2,'2025-10-22 21:40:39.597375','TrueFalseQuestion','확률 질량 함수(PMF)는 \'이산\' 확률 변수를 정의역으로 하는 확률 분포 함수입니다. 연속 확률 변수의 경우 확률 밀도 함수(PDF)를 사용합니다.','확률 질량 함수(Probability Mass Function, PMF)는 연속 확률 변수를 정의역으로 하는 확률 분포 함수이다.'),
                                                                                                                             ('2025-10-22 21:40:39.607611',18,2,'2025-10-22 21:40:39.607611','TrueFalseQuestion','베르누이 분포는 성공(1) 또는 실패(0) 두 가지 결과만 가지며, 각 결과에 대한 확률 p와 1-p는 0과 1 사이의 값을 갖습니다.','베르누이 분포는 두 가지 가능한 결과(성공 또는 실패)를 가지며, 각 결과에 대한 확률은 0과 1 사이이다.'),
                                                                                                                             ('2025-10-22 21:40:39.619125',19,2,'2025-10-22 21:40:39.619125','TrueFalseQuestion','두 확률 변수가 서로 독립이라면, 두 변수에 대한 결합 확률은 각 변수에 대한 개별 확률의 곱과 같습니다 (P(x, y) = P(x)P(y)).','두 확률 변수가 독립(Independent)이면, P(x, y) = P(x)P(y) 관계가 성립한다.'),
                                                                                                                             ('2025-10-22 21:40:39.626581',20,2,'2025-10-22 21:40:39.626581','TrueFalseQuestion','조건부 확률 P(x|y)는 확률 변수 Y의 값이 \'정해진 상태\' (즉, 특정 사건이 이미 발생한 상태)에서 확률 변수 X의 사건이 발생할 확률을 나타냅니다.','조건부 확률 P(x|y)는 확률 변수 Y의 값이 정해지지 않은 상태에서 확률 변수 X의 사건이 발생할 확률을 나타낸다.'),
                                                                                                                             ('2025-10-22 21:41:33.049113',21,3,'2025-10-22 21:41:33.049113','ShortAnswerQuestion','함수는 정의역의 각 원소를 공역의 \'유일한\' 원소에 연결하는 규칙을 의미합니다. 이는 함수의 정의에 해당합니다.','함수에서 정의역의 각 원소를 공역의 유일한 원소에 연결하는 규칙을 무엇이라고 하는가?'),
                                                                                                                             ('2025-10-22 21:41:33.054988',22,3,'2025-10-22 21:41:33.054988','ShortAnswerQuestion','표본 공간은 무한히 반복되는 절차(시행)에서 얻을 수 있는 모든 가능한 결과(실현값)들의 집합을 의미합니다.','무한히 반복되는 절차인 시행에서 얻을 수 있는 특정한 결과들의 집합을 무엇이라고 하는가?'),
                                                                                                                             ('2025-10-22 21:41:33.067457',23,3,'2025-10-22 21:41:33.067457','ShortAnswerQuestion','확률 변수는 표본 공간의 각 결과에 숫자를 할당하는 함수로, 특정 사건들의 집합을 공역으로 가집니다.','표본 공간을 정의역으로 하고, 특정 사건들의 집합을 공역으로 하는 함수를 무엇이라고 하는가?'),
                                                                                                                             ('2025-10-22 21:41:33.074283',24,3,'2025-10-22 21:41:33.074283','ShortAnswerQuestion','확률 분포 함수는 특정 사건이 발생할 확률을 나타내며, 그 값은 0과 1 사이입니다.','사건들의 집합을 정의역으로 하고, 0 이상 1 이하의 실수를 공역으로 하는 함수를 무엇이라고 하는가?'),
                                                                                                                             ('2025-10-22 21:41:33.089934',25,3,'2025-10-22 21:41:33.089934','ShortAnswerQuestion','이산 확률 변수의 경우, 확률 질량 함수는 확률 변수가 가질 수 있는 모든 값에 대한 확률의 합이 항상 1이 되어야 합니다.','이산 확률 변수의 확률 질량 함수(PMF)에서 모든 확률의 합은 얼마여야 하는가?'),
                                                                                                                             ('2025-10-22 21:41:33.097837',26,3,'2025-10-22 21:41:33.097837','ShortAnswerQuestion','이산 확률 변수는 확률 변수가 취할 수 있는 값의 개수가 셀 수 있거나 유한한 경우를 의미합니다.','공역의 원소 수가 유한한 확률 변수를 무엇이라고 하는가?'),
                                                                                                                             ('2025-10-22 21:41:33.105434',27,3,'2025-10-22 21:41:33.105434','ShortAnswerQuestion','연속 확률 변수는 확률 변수가 특정 범위 내의 모든 실수 값을 가질 수 있는 경우를 의미합니다.','공역의 원소 수가 무한한 확률 변수를 무엇이라고 하는가?'),
                                                                                                                             ('2025-10-22 21:41:33.118432',28,3,'2025-10-22 21:41:33.118432','ShortAnswerQuestion','연속 확률 변수의 경우, 확률 밀도 함수를 모든 가능한 값에 대해 적분한 값은 항상 1입니다.','확률 밀도 함수(PDF)에서 확률 변수의 모든 가능한 값에 대한 적분 값은 얼마인가?'),
                                                                                                                             ('2025-10-22 21:41:33.127109',29,3,'2025-10-22 21:41:33.127109','ShortAnswerQuestion','결합 확률 분포는 두 개 이상의 확률 변수가 동시에 특정 값을 가질 확률을 나타냅니다.','두 개 이상의 서로 다른 확률 변수에 속한 사건들이 동시에 발생하는 확률 분포를 무엇이라고 하는가?'),
                                                                                                                             ('2025-10-22 21:41:33.136111',30,3,'2025-10-22 21:41:33.136111','ShortAnswerQuestion','표본 추출은 주어진 확률 분포를 따르는 데이터의 일부를 뽑아내는 과정을 말합니다.','주어진 확률 분포에서 확률 변수의 값(또는 표본)을 추출하는 과정을 무엇이라고 하는가?');
/*!40000 ALTER TABLE `question` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `question_multiple_choice`
--

DROP TABLE IF EXISTS `question_multiple_choice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `question_multiple_choice` (
                                            `id` bigint(20) NOT NULL,
                                            `answer` varchar(255) DEFAULT NULL,
                                            PRIMARY KEY (`id`),
                                            CONSTRAINT `FK7y2qqxvjta4akuk8v7y7xyvgc` FOREIGN KEY (`id`) REFERENCES `question` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question_multiple_choice`
--

LOCK TABLES `question_multiple_choice` WRITE;
/*!40000 ALTER TABLE `question_multiple_choice` DISABLE KEYS */;
INSERT INTO `question_multiple_choice` (`id`, `answer`) VALUES (1,'1'),
                                                               (2,'3.5'),
                                                               (3,'p'),
                                                               (4,'평균 (μ)'),
                                                               (5,'P(X=x) * P(Y=y)'),
                                                               (6,'P(A ∩ B) / P(B)'),
                                                               (7,'1'),
                                                               (8,'더한다 (적분)'),
                                                               (9,'사후 확률 (Posterior Probability)'),
                                                               (10,'확률 변수가 가질 수 있는 값');
/*!40000 ALTER TABLE `question_multiple_choice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `question_options`
--

DROP TABLE IF EXISTS `question_options`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `question_options` (
                                    `question_id` bigint(20) NOT NULL,
                                    `option_text` varchar(255) DEFAULT NULL,
                                    KEY `FKaewurtlqda0y6wcg9jeylyg6a` (`question_id`),
                                    CONSTRAINT `FKaewurtlqda0y6wcg9jeylyg6a` FOREIGN KEY (`question_id`) REFERENCES `question_multiple_choice` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question_options`
--

LOCK TABLES `question_options` WRITE;
/*!40000 ALTER TABLE `question_options` DISABLE KEYS */;
INSERT INTO `question_options` (`question_id`, `option_text`) VALUES (1,'0'),
                                                                     (1,'0.5'),
                                                                     (1,'1'),
                                                                     (1,'무한대'),
                                                                     (2,'2.5'),
                                                                     (2,'3'),
                                                                     (2,'3.5'),
                                                                     (2,'4'),
                                                                     (3,'1-p'),
                                                                     (3,'p'),
                                                                     (3,'1'),
                                                                     (3,'0'),
                                                                     (4,'분산 (σ²)'),
                                                                     (4,'표준편차 (σ)'),
                                                                     (4,'평균 (μ)'),
                                                                     (4,'0'),
                                                                     (5,'P(X=x) + P(Y=y)'),
                                                                     (5,'P(X=x) - P(Y=y)'),
                                                                     (5,'P(X=x) * P(Y=y)'),
                                                                     (5,'P(X=x) / P(Y=y)'),
                                                                     (6,'P(A) / P(B)'),
                                                                     (6,'P(B) / P(A)'),
                                                                     (6,'P(A ∩ B) / P(B)'),
                                                                     (6,'P(A ∩ B) / P(A)'),
                                                                     (7,'0'),
                                                                     (7,'1'),
                                                                     (7,'X의 평균값'),
                                                                     (7,'X의 분산'),
                                                                     (8,'곱한다'),
                                                                     (8,'나눈다'),
                                                                     (8,'더한다 (적분)'),
                                                                     (8,'빼준다'),
                                                                     (9,'사전 확률 (Prior Probability)'),
                                                                     (9,'우도 (Likelihood)'),
                                                                     (9,'사후 확률 (Posterior Probability)'),
                                                                     (9,'결합 확률 (Joint Probability)'),
                                                                     (10,'확률'),
                                                                     (10,'확률 변수가 가질 수 있는 값'),
                                                                     (10,'확률 변수의 분산'),
                                                                     (10,'샘플의 크기');
/*!40000 ALTER TABLE `question_options` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `question_set`
--

DROP TABLE IF EXISTS `question_set`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `question_set` (
                                `question_length` int(11) DEFAULT NULL,
                                `common_folder_id` bigint(20) DEFAULT NULL,
                                `created_at` datetime(6) NOT NULL,
                                `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                `owner_id` bigint(20) NOT NULL,
                                `updated_at` datetime(6) NOT NULL,
                                `title` varchar(150) DEFAULT NULL,
                                `difficulty` enum('EASY','HARD') DEFAULT NULL,
                                `status` enum('COMPLETE','FAILED','PENDING') DEFAULT NULL,
                                `type` enum('MULTIPLE_CHOICE','SHORT_ANSWER','SUBJECTIVE','TRUE_FALSE') DEFAULT NULL,
                                PRIMARY KEY (`id`),
                                KEY `FKklqs1htxnwjhgi6vn16u1kd82` (`common_folder_id`),
                                CONSTRAINT `FKklqs1htxnwjhgi6vn16u1kd82` FOREIGN KEY (`common_folder_id`) REFERENCES `common_folder` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question_set`
--

LOCK TABLES `question_set` WRITE;
/*!40000 ALTER TABLE `question_set` DISABLE KEYS */;
INSERT INTO `question_set` (`question_length`, `common_folder_id`, `created_at`, `id`, `owner_id`, `updated_at`, `title`, `difficulty`, `status`, `type`) VALUES (10,4,'2025-10-22 21:40:13.141013',1,1,'2025-10-22 21:57:39.129362','인공지능 기본수학 확률 및 기대값 복습 문제','EASY','COMPLETE','MULTIPLE_CHOICE'),
                                                                                                                                                                 (10,1,'2025-10-22 21:40:30.238478',2,1,'2025-10-22 21:40:39.631608','인공지능 기본 수학: 확률 및 기대값 복습 퀴즈','EASY','COMPLETE','TRUE_FALSE'),
                                                                                                                                                                 (10,1,'2025-10-22 21:41:24.435213',3,1,'2025-10-22 21:41:33.140220','인공지능 기초 수학: 확률 및 기대값 문제집','EASY','COMPLETE','SHORT_ANSWER');
/*!40000 ALTER TABLE `question_set` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `question_set_source`
--

DROP TABLE IF EXISTS `question_set_source`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `question_set_source` (
                                       `question_set_id` bigint(20) NOT NULL,
                                       `source_id` bigint(20) NOT NULL,
                                       PRIMARY KEY (`question_set_id`,`source_id`),
                                       KEY `FKk8jramh7amnwnvhftj6p9a1kw` (`source_id`),
                                       CONSTRAINT `FKk8jramh7amnwnvhftj6p9a1kw` FOREIGN KEY (`source_id`) REFERENCES `source` (`id`),
                                       CONSTRAINT `FKsgwhvpdkwp6641ui7vp51j8dm` FOREIGN KEY (`question_set_id`) REFERENCES `question_set` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question_set_source`
--

LOCK TABLES `question_set_source` WRITE;
/*!40000 ALTER TABLE `question_set_source` DISABLE KEYS */;
INSERT INTO `question_set_source` (`question_set_id`, `source_id`) VALUES (1,1),
                                                                          (2,1),
                                                                          (3,1);
/*!40000 ALTER TABLE `question_set_source` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `question_short_answer`
--

DROP TABLE IF EXISTS `question_short_answer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `question_short_answer` (
                                         `id` bigint(20) NOT NULL,
                                         `answer` varchar(255) DEFAULT NULL,
                                         PRIMARY KEY (`id`),
                                         CONSTRAINT `FKssoff9cf0px1usmsn0d1v4dr9` FOREIGN KEY (`id`) REFERENCES `question` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question_short_answer`
--

LOCK TABLES `question_short_answer` WRITE;
/*!40000 ALTER TABLE `question_short_answer` DISABLE KEYS */;
INSERT INTO `question_short_answer` (`id`, `answer`) VALUES (21,'함수'),
                                                            (22,'표본 공간'),
                                                            (23,'확률 변수'),
                                                            (24,'확률 분포 함수'),
                                                            (25,'1'),
                                                            (26,'이산 확률 변수'),
                                                            (27,'연속 확률 변수'),
                                                            (28,'1'),
                                                            (29,'결합 확률 분포'),
                                                            (30,'표본 추출');
/*!40000 ALTER TABLE `question_short_answer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `question_true_false`
--

DROP TABLE IF EXISTS `question_true_false`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `question_true_false` (
                                       `answer` bit(1) NOT NULL,
                                       `id` bigint(20) NOT NULL,
                                       PRIMARY KEY (`id`),
                                       CONSTRAINT `FK9gjexpb42svh57saot0k9y0ht` FOREIGN KEY (`id`) REFERENCES `question` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question_true_false`
--

LOCK TABLES `question_true_false` WRITE;
/*!40000 ALTER TABLE `question_true_false` DISABLE KEYS */;
INSERT INTO `question_true_false` (`answer`, `id`) VALUES ('\0',11),
                                                          ('\0',12),
                                                          ('',13),
                                                          ('\0',14),
                                                          ('',15),
                                                          ('\0',16),
                                                          ('\0',17),
                                                          ('',18),
                                                          ('',19),
                                                          ('\0',20);
/*!40000 ALTER TABLE `question_true_false` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `source`
--

DROP TABLE IF EXISTS `source`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `source` (
                          `page_count` int(11) DEFAULT NULL,
                          `created_at` datetime(6) NOT NULL,
                          `file_size_bytes` bigint(20) NOT NULL,
                          `id` bigint(20) NOT NULL AUTO_INCREMENT,
                          `member_id` bigint(20) NOT NULL,
                          `source_folder_id` bigint(20) DEFAULT NULL,
                          `updated_at` datetime(6) NOT NULL,
                          `content_type` varchar(255) NOT NULL,
                          `file_path` varchar(255) NOT NULL,
                          `original_name` varchar(255) NOT NULL,
                          `status` enum('FAILED','PROCESSING','READY','UPLOADED') NOT NULL,
                          PRIMARY KEY (`id`),
                          KEY `FKmcc76l1b8ujhbvdjjc31o492t` (`source_folder_id`),
                          CONSTRAINT `FKmcc76l1b8ujhbvdjjc31o492t` FOREIGN KEY (`source_folder_id`) REFERENCES `source_folder` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `source`
--

LOCK TABLES `source` WRITE;
/*!40000 ALTER TABLE `source` DISABLE KEYS */;
INSERT INTO `source` (`page_count`, `created_at`, `file_size_bytes`, `id`, `member_id`, `source_folder_id`, `updated_at`, `content_type`, `file_path`, `original_name`, `status`) VALUES (NULL,'2025-10-22 21:40:05.115244',1024,1,1,1,'2025-10-22 21:40:05.115244','application/pdf','learning-sources/2025/10/22/member-1/b27dd5ca-0c23-4a8b-aff5-434304d0b6f7.pdf','Basic Math.pdf','READY');
/*!40000 ALTER TABLE `source` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `source_folder`
--

DROP TABLE IF EXISTS `source_folder`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `source_folder` (
                                 `created_at` datetime(6) NOT NULL,
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                 `member_id` bigint(20) NOT NULL,
                                 `updated_at` datetime(6) NOT NULL,
                                 `color` varchar(255) DEFAULT NULL,
                                 `description` varchar(255) DEFAULT NULL,
                                 `name` varchar(255) NOT NULL,
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `source_folder`
--

LOCK TABLES `source_folder` WRITE;
/*!40000 ALTER TABLE `source_folder` DISABLE KEYS */;
INSERT INTO `source_folder` (`created_at`, `id`, `member_id`, `updated_at`, `color`, `description`, `name`) VALUES ('2025-10-22 21:40:05.095172',1,1,'2025-10-22 21:40:05.095172',NULL,NULL,'전체 폴더');
/*!40000 ALTER TABLE `source_folder` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wrong_answer`
--

DROP TABLE IF EXISTS `wrong_answer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `wrong_answer` (
                                `is_reviewed` bit(1) DEFAULT NULL,
                                `created_at` datetime(6) NOT NULL,
                                `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                `member_id` bigint(20) DEFAULT NULL,
                                `question_id` bigint(20) DEFAULT NULL,
                                `updated_at` datetime(6) NOT NULL,
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `UKk52s1oc2m2dbrrui4w2e749jt` (`member_id`,`question_id`),
                                UNIQUE KEY `UKti433dnayhh5p3qhbwqu1cv27` (`question_id`),
                                CONSTRAINT `FKovvauh9ri5jp9f22tkbkcqyvs` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wrong_answer`
--

LOCK TABLES `wrong_answer` WRITE;
/*!40000 ALTER TABLE `wrong_answer` DISABLE KEYS */;
INSERT INTO `wrong_answer` (`is_reviewed`, `created_at`, `id`, `member_id`, `question_id`, `updated_at`) VALUES ('\0','2025-10-22 21:42:01.134987',1,1,1,'2025-10-22 21:42:01.134987'),
                                                                                                                ('\0','2025-10-22 21:42:01.136896',2,1,2,'2025-10-22 21:42:01.136896'),
                                                                                                                ('\0','2025-10-22 21:42:01.137897',3,1,3,'2025-10-22 21:42:01.137897'),
                                                                                                                ('\0','2025-10-22 21:42:01.139887',4,1,4,'2025-10-22 21:42:01.139887'),
                                                                                                                ('\0','2025-10-22 21:42:01.140638',5,1,5,'2025-10-22 21:42:01.140638'),
                                                                                                                ('\0','2025-10-22 21:42:01.141314',6,1,6,'2025-10-22 21:42:01.141314'),
                                                                                                                ('\0','2025-10-22 21:42:01.142020',7,1,7,'2025-10-22 21:42:01.142020'),
                                                                                                                ('\0','2025-10-22 21:42:01.142718',8,1,8,'2025-10-22 21:42:01.142718'),
                                                                                                                ('\0','2025-10-22 21:42:01.143388',9,1,9,'2025-10-22 21:42:01.143388'),
                                                                                                                ('\0','2025-10-22 21:42:01.144061',10,1,10,'2025-10-22 21:42:01.144061'),
                                                                                                                ('\0','2025-10-22 21:42:23.796269',11,1,11,'2025-10-22 21:42:23.796269'),
                                                                                                                ('\0','2025-10-22 21:42:23.797240',12,1,14,'2025-10-22 21:42:23.797240'),
                                                                                                                ('','2025-10-22 21:42:23.797998',13,1,15,'2025-10-22 21:52:57.331510'),
                                                                                                                ('','2025-10-22 21:42:23.798870',14,1,19,'2025-10-22 21:52:57.335693'),
                                                                                                                ('\0','2025-10-22 21:42:23.815431',15,1,20,'2025-10-22 21:42:23.815431'),
                                                                                                                ('\0','2025-10-22 21:42:50.152247',16,1,21,'2025-10-22 21:42:50.152247'),
                                                                                                                ('\0','2025-10-22 21:42:50.153452',17,1,22,'2025-10-22 21:42:50.153452'),
                                                                                                                ('\0','2025-10-22 21:42:50.169351',18,1,23,'2025-10-22 21:42:50.169351'),
                                                                                                                ('\0','2025-10-22 21:42:50.170341',19,1,24,'2025-10-22 21:42:50.170341'),
                                                                                                                ('\0','2025-10-22 21:42:50.171148',20,1,25,'2025-10-22 21:42:50.171148'),
                                                                                                                ('\0','2025-10-22 21:42:50.171849',21,1,26,'2025-10-22 21:42:50.171849'),
                                                                                                                ('\0','2025-10-22 21:42:50.172495',22,1,27,'2025-10-22 21:42:50.172495'),
                                                                                                                ('\0','2025-10-22 21:42:50.173156',23,1,28,'2025-10-22 21:42:50.173156'),
                                                                                                                ('\0','2025-10-22 21:42:50.173812',24,1,29,'2025-10-22 21:42:50.173812'),
                                                                                                                ('\0','2025-10-22 21:42:50.174545',25,1,30,'2025-10-22 21:42:50.174545');
/*!40000 ALTER TABLE `wrong_answer` ENABLE KEYS */;
UNLOCK TABLES;

SET TIME_ZONE=@OLD_TIME_ZONE;
SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT;
SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS;
SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION;
SET SQL_NOTES=@OLD_SQL_NOTES;

-- Dump completed on 2025-10-22 22:30:51
