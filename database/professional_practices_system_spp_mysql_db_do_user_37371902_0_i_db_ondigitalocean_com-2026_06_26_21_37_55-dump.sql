-- MySQL dump 10.13  Distrib 9.5.0, for macos15.4 (arm64)
--
-- Host: spp-mysql-db-do-user-37371902-0.i.db.ondigitalocean.com    Database: professional-practices-system
-- ------------------------------------------------------
-- Server version	8.4.8

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '54c40b8d-515f-11f1-8fd7-4222da89324a:1-1268';

--
-- Table structure for table `access_token`
--

DROP TABLE IF EXISTS `access_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `access_token` (
  `token_value` int NOT NULL,
  `creation_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `username` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`token_value`),
  KEY `fk_access_token_user` (`username`),
  CONSTRAINT `fk_access_token_user` FOREIGN KEY (`username`) REFERENCES `user` (`username`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `access_token`
--

LOCK TABLES `access_token` WRITE;
/*!40000 ALTER TABLE `access_token` DISABLE KEYS */;
INSERT INTO `access_token` VALUES (191815,'2026-06-26 14:16:49','70123'),(199290,'2026-06-26 14:21:49','S25010003'),(208920,'2026-06-26 14:32:21','S25010005'),(289445,'2026-06-26 14:19:09','S25010005'),(331731,'2026-06-26 12:59:10','S25010005'),(359158,'2026-06-26 14:34:40','70123'),(418916,'2026-06-26 13:35:21','S25010005'),(464298,'2026-06-26 14:42:45','S25010005'),(495684,'2026-06-26 14:28:31','S25010003'),(540657,'2026-06-26 12:37:02','S25010005'),(631858,'2026-06-26 14:23:17','S25010002'),(700811,'2026-06-26 14:27:20','S25010006'),(781312,'2026-06-26 14:24:32','70123'),(888773,'2026-06-26 14:32:37','70124'),(902173,'2026-06-26 14:06:23','S25010005'),(970055,'2026-06-26 12:34:06','S25010010'),(978830,'2026-06-26 14:14:16','S25010005'),(981542,'2026-06-26 13:31:06','S25010005');
/*!40000 ALTER TABLE `access_token` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `activity`
--

DROP TABLE IF EXISTS `activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `activity` (
  `activity_id` int NOT NULL AUTO_INCREMENT,
  `practitioner_id` int NOT NULL,
  `report_id` int DEFAULT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `duration_hours` int DEFAULT '0',
  PRIMARY KEY (`activity_id`),
  KEY `fk_activity_practitioner` (`practitioner_id`),
  KEY `fk_activity_report` (`report_id`),
  CONSTRAINT `fk_activity_practitioner` FOREIGN KEY (`practitioner_id`) REFERENCES `practitioner` (`practitioner_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_activity_report` FOREIGN KEY (`report_id`) REFERENCES `monthly_report` (`report_id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=8053 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity`
--

LOCK TABLES `activity` WRITE;
/*!40000 ALTER TABLE `activity` DISABLE KEYS */;
INSERT INTO `activity` VALUES (8017,866,8013,'Induccion y configuracion del entorno','Instalacion de herramientas y revision de la arquitectura del sistema hospitalario.','2026-02-02','2026-02-13',60),(8018,866,8013,'Levantamiento de requerimientos','Reuniones con el responsable para documentar requerimientos del modulo de citas.','2026-02-16','2026-02-27',50),(8019,866,8014,'Diseno de base de datos','Modelado del esquema relacional para el modulo de citas medicas.','2026-03-02','2026-03-13',70),(8020,866,8014,'Prototipo de interfaz','Maquetado de pantallas de agendado de citas.','2026-03-16','2026-03-31',40),(8021,867,8015,'Configuracion del proyecto e-commerce','Preparacion del repositorio y entorno de desarrollo.','2026-02-02','2026-02-13',60),(8022,867,8015,'Analisis de requerimientos del carrito','Documentacion de casos de uso del carrito de compras.','2026-02-16','2026-02-27',50),(8023,867,8016,'Desarrollo del modulo de catalogo','Implementacion del listado y busqueda de productos.','2026-03-02','2026-03-13',70),(8024,867,8016,'Integracion de pasarela de pago (sandbox)','Conexion con API de pagos en ambiente de pruebas.','2026-03-16','2026-03-31',40),(8025,868,8017,'Analisis de la app de movilidad','Estudio de requerimientos de la aplicacion de movilidad urbana.','2026-02-02','2026-02-13',60),(8026,868,8017,'Diseno de arquitectura','Definicion de la arquitectura de microservicios.','2026-02-16','2026-02-27',50),(8027,868,8018,'Desarrollo del backend de rutas','Implementacion del servicio de calculo de rutas.','2026-03-02','2026-03-13',70),(8028,868,8018,'Pruebas unitarias del backend','Cobertura de pruebas del servicio de rutas.','2026-03-16','2026-03-31',40),(8029,868,8019,'Desarrollo de la app movil','Pantallas de seguimiento de viaje en tiempo real.','2026-04-01','2026-04-15',60),(8030,868,8019,'Integracion con mapas','Consumo de API de mapas y geolocalizacion.','2026-04-16','2026-04-30',50),(8031,868,8020,'Despliegue en ambiente de pruebas','Configuracion de CI/CD y despliegue del sistema.','2026-05-01','2026-05-15',70),(8032,868,8020,'Documentacion final y capacitacion','Manual de usuario y sesion de capacitacion.','2026-05-16','2026-05-29',40),(8033,876,8021,'Induccion y configuracion del entorno','Instalacion de herramientas y revision de la arquitectura del sistema hospitalario.','2026-02-02','2026-02-13',60),(8034,876,8021,'Levantamiento de requerimientos','Reuniones con el responsable para documentar requerimientos del modulo de citas.','2026-02-16','2026-02-27',50),(8035,876,8022,'Diseno de base de datos','Modelado del esquema relacional para el modulo de citas medicas.','2026-03-02','2026-03-13',70),(8036,876,8022,'Prototipo de interfaz','Maquetado de pantallas de agendado de citas.','2026-03-16','2026-03-31',40),(8037,877,8023,'Configuracion del proyecto e-commerce','Preparacion del repositorio y entorno de desarrollo.','2026-02-02','2026-02-13',60),(8038,877,8023,'Analisis de requerimientos del carrito','Documentacion de casos de uso del carrito de compras.','2026-02-16','2026-02-27',50),(8039,877,8024,'Desarrollo del modulo de catalogo','Implementacion del listado y busqueda de productos.','2026-03-02','2026-03-13',70),(8040,877,8024,'Integracion de pasarela de pago (sandbox)','Conexion con API de pagos en ambiente de pruebas.','2026-03-16','2026-03-31',40),(8041,878,8025,'Analisis de la app de movilidad','Estudio de requerimientos de la aplicacion de movilidad urbana.','2026-02-02','2026-02-13',60),(8042,878,8025,'Diseno de arquitectura','Definicion de la arquitectura de microservicios.','2026-02-16','2026-02-27',50),(8043,878,8026,'Desarrollo del backend de rutas','Implementacion del servicio de calculo de rutas.','2026-03-02','2026-03-13',70),(8044,878,8026,'Pruebas unitarias del backend','Cobertura de pruebas del servicio de rutas.','2026-03-16','2026-03-31',40),(8045,878,8027,'Desarrollo de la app movil','Pantallas de seguimiento de viaje en tiempo real.','2026-04-01','2026-04-15',60),(8046,878,8027,'Integracion con mapas','Consumo de API de mapas y geolocalizacion.','2026-04-16','2026-04-30',50),(8047,878,8028,'Despliegue en ambiente de pruebas','Configuracion de CI/CD y despliegue del sistema.','2026-05-01','2026-05-15',70),(8048,878,8028,'Documentacion final y capacitacion','Manual de usuario y sesion de capacitacion.','2026-05-16','2026-05-29',40),(8049,880,8029,'Test 1','asdasd','2026-06-01','2026-06-24',120),(8050,880,8030,'test 2','asdasd','2026-07-01','2026-07-20',120),(8051,880,8031,'test 3','asdasd','2026-05-01','2026-05-24',200),(8052,875,8032,'Construcion de servidores compartidos','Se montaron servidores para','2026-06-01','2026-06-18',210);
/*!40000 ALTER TABLE `activity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `administrator`
--

DROP TABLE IF EXISTS `administrator`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `administrator` (
  `administrator_id` int NOT NULL,
  PRIMARY KEY (`administrator_id`),
  CONSTRAINT `fk_administrator_user` FOREIGN KEY (`administrator_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `administrator`
--

LOCK TABLES `administrator` WRITE;
/*!40000 ALTER TABLE `administrator` DISABLE KEYS */;
INSERT INTO `administrator` VALUES (38);
/*!40000 ALTER TABLE `administrator` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coordinator`
--

DROP TABLE IF EXISTS `coordinator`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coordinator` (
  `coordinator_id` int NOT NULL,
  PRIMARY KEY (`coordinator_id`),
  CONSTRAINT `fk_coordinator_user` FOREIGN KEY (`coordinator_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coordinator`
--

LOCK TABLES `coordinator` WRITE;
/*!40000 ALTER TABLE `coordinator` DISABLE KEYS */;
INSERT INTO `coordinator` VALUES (39);
/*!40000 ALTER TABLE `coordinator` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `document_type`
--

DROP TABLE IF EXISTS `document_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `document_type` (
  `document_type_id` int NOT NULL AUTO_INCREMENT,
  `type_code` varchar(50) NOT NULL,
  `type_name` varchar(100) NOT NULL,
  `category` varchar(20) NOT NULL,
  PRIMARY KEY (`document_type_id`),
  UNIQUE KEY `type_code` (`type_code`),
  CONSTRAINT `chk_document_type_category` CHECK ((`category` in (_utf8mb4'Initial',_utf8mb4'Final')))
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `document_type`
--

LOCK TABLES `document_type` WRITE;
/*!40000 ALTER TABLE `document_type` DISABLE KEYS */;
INSERT INTO `document_type` VALUES (1,'CURP','CURP','Initial'),(2,'SCHEDULE','Horario','Initial'),(3,'INE','INE','Initial'),(4,'ACCEPTANCE_LETTER','Carta de aceptacion','Initial'),(5,'RELEASE_LETTER','Carta de liberacion','Final'),(16,'ORGANIZATION_EVALUATION','Evaluacion de la organizacion','Final'),(17,'PROFESSOR_EVALUATION','Evaluacion del profesor','Final');
/*!40000 ALTER TABLE `document_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_enrollment`
--

DROP TABLE IF EXISTS `group_enrollment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_enrollment` (
  `enrollment_id` int NOT NULL AUTO_INCREMENT,
  `practitioner_id` int NOT NULL,
  `group_id` int NOT NULL,
  `period_id` int NOT NULL,
  `opportunity_number` tinyint NOT NULL DEFAULT '1',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Active',
  `enrollment_date` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`enrollment_id`),
  UNIQUE KEY `uq_enrollment_practitioner_period` (`practitioner_id`,`period_id`),
  KEY `fk_enrollment_group` (`group_id`),
  KEY `fk_enrollment_period` (`period_id`),
  CONSTRAINT `fk_enrollment_group` FOREIGN KEY (`group_id`) REFERENCES `practice_group` (`group_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_enrollment_period` FOREIGN KEY (`period_id`) REFERENCES `school_period` (`period_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_enrollment_practitioner` FOREIGN KEY (`practitioner_id`) REFERENCES `practitioner` (`practitioner_id`) ON DELETE CASCADE,
  CONSTRAINT `chk_enrollment_status` CHECK ((`status` in (_utf8mb4'Active',_utf8mb4'Inactive'))),
  CONSTRAINT `chk_group_enrollment_opportunity` CHECK ((`opportunity_number` between 1 and 2))
) ENGINE=InnoDB AUTO_INCREMENT=82 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_enrollment`
--

LOCK TABLES `group_enrollment` WRITE;
/*!40000 ALTER TABLE `group_enrollment` DISABLE KEYS */;
INSERT INTO `group_enrollment` VALUES (61,858,807,801,1,'Active','2026-06-25 07:44:45'),(62,861,807,801,1,'Active','2026-06-25 12:53:50'),(63,862,807,801,1,'Active','2026-06-25 12:54:31'),(64,862,810,804,1,'Active','2026-06-25 13:03:45'),(65,864,811,804,1,'Active','2026-06-25 13:04:52'),(66,865,811,804,1,'Active','2026-06-25 13:04:52'),(67,866,811,804,1,'Active','2026-06-25 13:04:53'),(68,867,811,804,1,'Active','2026-06-25 13:04:54'),(69,868,811,804,1,'Active','2026-06-25 13:04:56'),(70,869,807,801,1,'Active','2026-06-25 13:06:03'),(71,870,807,801,1,'Active','2026-06-25 13:09:29'),(72,871,807,801,1,'Active','2026-06-25 13:10:56'),(73,872,807,801,1,'Active','2026-06-25 13:11:38'),(74,874,812,804,1,'Active','2026-06-25 13:14:38'),(75,875,812,804,1,'Active','2026-06-25 13:14:39'),(76,876,812,804,1,'Active','2026-06-25 13:14:40'),(77,877,812,804,1,'Active','2026-06-25 13:14:41'),(78,878,812,804,1,'Active','2026-06-25 13:14:42'),(79,880,813,804,1,'Active','2026-06-25 13:54:14'),(80,880,807,801,2,'Active','2026-06-25 14:29:59'),(81,881,807,801,1,'Active','2026-06-26 14:45:34');
/*!40000 ALTER TABLE `group_enrollment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `linked_organization`
--

DROP TABLE IF EXISTS `linked_organization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `linked_organization` (
  `organization_id` int NOT NULL AUTO_INCREMENT,
  `organization_name` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `city` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sector` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`organization_id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=803 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `linked_organization`
--

LOCK TABLES `linked_organization` WRITE;
/*!40000 ALTER TABLE `linked_organization` DISABLE KEYS */;
INSERT INTO `linked_organization` VALUES (801,'LigthYear Code','Active','Xalapa','Veracruz','videojugos','infoLightCode@lightcode.com','786662233'),(802,'Innovatech Soluciones Digitales','Active','Av. Murillo Vidal 1234, Col. Cuauhtemoc','Xalapa','Desarrollo de Software','contacto@innovatech.com.mx','2288123456');
/*!40000 ALTER TABLE `linked_organization` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `message`
--

DROP TABLE IF EXISTS `message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `message` (
  `message_id` int NOT NULL AUTO_INCREMENT,
  `send_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `subject` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Notificacion del Sistema',
  `body` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `message`
--

LOCK TABLES `message` WRITE;
/*!40000 ALTER TABLE `message` DISABLE KEYS */;
/*!40000 ALTER TABLE `message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `message_participant`
--

DROP TABLE IF EXISTS `message_participant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `message_participant` (
  `message_id` int NOT NULL,
  `sender_id` int NOT NULL,
  `receiver_id` int NOT NULL,
  PRIMARY KEY (`message_id`),
  KEY `fk_participant_sender` (`sender_id`),
  KEY `fk_participant_receiver` (`receiver_id`),
  CONSTRAINT `fk_participant_message` FOREIGN KEY (`message_id`) REFERENCES `message` (`message_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_participant_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_participant_sender` FOREIGN KEY (`sender_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `message_participant`
--

LOCK TABLES `message_participant` WRITE;
/*!40000 ALTER TABLE `message_participant` DISABLE KEYS */;
/*!40000 ALTER TABLE `message_participant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `monthly_report`
--

DROP TABLE IF EXISTS `monthly_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `monthly_report` (
  `report_id` int NOT NULL AUTO_INCREMENT,
  `practitioner_id` int NOT NULL,
  `month_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `year` int NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `grade` decimal(5,2) DEFAULT NULL,
  `professor_feedback` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'Borrador',
  `signed_file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`report_id`),
  UNIQUE KEY `uq_monthly_report_practitioner_month` (`practitioner_id`,`year`,`month_name`),
  KEY `fk_monthly_report_practitioner` (`practitioner_id`),
  CONSTRAINT `fk_monthly_report_practitioner` FOREIGN KEY (`practitioner_id`) REFERENCES `practitioner` (`practitioner_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8033 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `monthly_report`
--

LOCK TABLES `monthly_report` WRITE;
/*!40000 ALTER TABLE `monthly_report` DISABLE KEYS */;
INSERT INTO `monthly_report` VALUES (8013,866,'febrero',2026,'2026-02-02','2026-02-27',8.00,'asd','Evaluado','/documents/generated/S25010003_reporte_febrero.pdf'),(8014,866,'marzo',2026,'2026-03-02','2026-03-31',7.00,'asd','Evaluado','/documents/generated/S25010003_reporte_marzo.pdf'),(8015,867,'febrero',2026,'2026-02-02','2026-02-27',9.00,'Buen arranque, documentacion clara.','Evaluado','/documents/generated/S25010004_reporte_febrero.pdf'),(8016,867,'marzo',2026,'2026-03-02','2026-03-31',8.50,'Avance constante, cuida los tiempos de entrega.','Evaluado','/documents/generated/S25010004_reporte_marzo.pdf'),(8017,868,'febrero',2026,'2026-02-02','2026-02-27',9.00,'Excelente inicio.','Evaluado','/documents/generated/S25010005_reporte_febrero.pdf'),(8018,868,'marzo',2026,'2026-03-02','2026-03-31',9.50,'Muy buen avance tecnico.','Evaluado','/documents/generated/S25010005_reporte_marzo.pdf'),(8019,868,'abril',2026,'2026-04-01','2026-04-30',9.00,'Buen trabajo en la app movil.','Evaluado','/documents/generated/S25010005_reporte_abril.pdf'),(8020,868,'mayo',2026,'2026-05-01','2026-05-29',9.50,'Cierre de practicas sobresaliente.','Evaluado','/documents/generated/S25010005_reporte_mayo.pdf'),(8021,876,'febrero',2026,'2026-02-02','2026-02-27',NULL,NULL,'Entregado','/documents/generated/S25010008_reporte_febrero.pdf'),(8022,876,'marzo',2026,'2026-03-02','2026-03-31',NULL,NULL,'Entregado','/documents/generated/S25010008_reporte_marzo.pdf'),(8023,877,'febrero',2026,'2026-02-02','2026-02-27',9.00,'Buen arranque, documentacion clara.','Evaluado','/documents/generated/S25010009_reporte_febrero.pdf'),(8024,877,'marzo',2026,'2026-03-02','2026-03-31',8.50,'Avance constante, cuida los tiempos de entrega.','Evaluado','/documents/generated/S25010009_reporte_marzo.pdf'),(8025,878,'febrero',2026,'2026-02-02','2026-02-27',9.00,'Excelente inicio.','Evaluado','/documents/generated/S25010010_reporte_febrero.pdf'),(8026,878,'marzo',2026,'2026-03-02','2026-03-31',9.50,'Muy buen avance tecnico.','Evaluado','/documents/generated/S25010010_reporte_marzo.pdf'),(8027,878,'abril',2026,'2026-04-01','2026-04-30',10.00,'Buen trabajo en la app movil.','Evaluado','/documents/generated/S25010010_reporte_abril.pdf'),(8028,878,'mayo',2026,'2026-05-01','2026-05-29',9.50,'Cierre de practicas sobresaliente.','Evaluado','/documents/generated/S25010010_reporte_mayo.pdf'),(8029,880,'Junio',2026,'2026-06-01','2026-06-30',7.00,'asdasd','Evaluado','file:///C:/Users/USER/SimuladorOneDrive_FEI/7b0bdf0a_PRAIS-03_Autoevaluacion.pdf'),(8030,880,'Julio',2026,'2026-07-01','2026-07-31',7.00,'asdad','Evaluado','file:///C:/Users/USER/SimuladorOneDrive_FEI/cd370827_Reporte_Avance_Intermedio.pdf'),(8031,880,'Mayo',2026,'2026-05-01','2026-05-31',7.00,'asdasd','Evaluado','file:///C:/Users/USER/SimuladorOneDrive_FEI/846843b3_PRAIS-03_Autoevaluacion.pdf'),(8032,875,'Junio',2026,'2026-06-01','2026-06-30',7.00,'sSas','Evaluado','file:///C:/Users/josep/SimuladorOneDrive_FEI/a60f1177_Reporte_Junio_2026.pdf');
/*!40000 ALTER TABLE `monthly_report` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `practice_group`
--

DROP TABLE IF EXISTS `practice_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `practice_group` (
  `group_id` int NOT NULL AUTO_INCREMENT,
  `section` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `period_id` int NOT NULL,
  `professor_id` int NOT NULL,
  PRIMARY KEY (`group_id`),
  UNIQUE KEY `uq_section_period` (`section`,`period_id`),
  UNIQUE KEY `uq_group_period_section` (`period_id`,`section`),
  KEY `fk_group_period` (`period_id`),
  KEY `fk_group_professor` (`professor_id`),
  CONSTRAINT `fk_group_period` FOREIGN KEY (`period_id`) REFERENCES `school_period` (`period_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_group_professor` FOREIGN KEY (`professor_id`) REFERENCES `professor` (`professor_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=816 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `practice_group`
--

LOCK TABLES `practice_group` WRITE;
/*!40000 ALTER TABLE `practice_group` DISABLE KEYS */;
INSERT INTO `practice_group` VALUES (807,'60123',801,857),(810,'6053',804,857),(811,'60201',804,863),(812,'60202',804,873),(813,'111111',804,879),(814,'222222222',804,879),(815,'505058',804,863);
/*!40000 ALTER TABLE `practice_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `practitioner`
--

DROP TABLE IF EXISTS `practitioner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `practitioner` (
  `practitioner_id` int NOT NULL,
  `indigenous_language` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `grade` decimal(5,2) DEFAULT NULL,
  PRIMARY KEY (`practitioner_id`),
  CONSTRAINT `fk_practitioner_user` FOREIGN KEY (`practitioner_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `practitioner`
--

LOCK TABLES `practitioner` WRITE;
/*!40000 ALTER TABLE `practitioner` DISABLE KEYS */;
INSERT INTO `practitioner` VALUES (858,'Ninguna',0.00),(861,'Ninguna',0.00),(862,'Ninguna',0.00),(864,NULL,NULL),(865,NULL,NULL),(866,NULL,NULL),(867,NULL,NULL),(868,NULL,NULL),(869,'Ninguna',0.00),(870,'Ninguna',0.00),(871,'Ninguna',0.00),(872,'Ninguna',0.00),(874,NULL,NULL),(875,NULL,NULL),(876,NULL,NULL),(877,NULL,NULL),(878,NULL,NULL),(880,'Ninguna',0.00),(881,'Ninguna',0.00);
/*!40000 ALTER TABLE `practitioner` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `practitioner_document`
--

DROP TABLE IF EXISTS `practitioner_document`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `practitioner_document` (
  `document_id` int NOT NULL AUTO_INCREMENT,
  `practitioner_id` int NOT NULL,
  `document_type_id` int NOT NULL,
  `document_name` varchar(255) NOT NULL,
  `stored_file_url` varchar(500) NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'Pending',
  `review_comment` varchar(500) DEFAULT NULL,
  `upload_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `review_date` datetime DEFAULT NULL,
  PRIMARY KEY (`document_id`),
  UNIQUE KEY `uq_practitioner_document_type` (`practitioner_id`,`document_type_id`),
  KEY `fk_document_type` (`document_type_id`),
  CONSTRAINT `fk_document_practitioner` FOREIGN KEY (`practitioner_id`) REFERENCES `practitioner` (`practitioner_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_document_type` FOREIGN KEY (`document_type_id`) REFERENCES `document_type` (`document_type_id`),
  CONSTRAINT `chk_document_status` CHECK ((`status` in (_utf8mb4'Pending',_utf8mb4'Accepted',_utf8mb4'Rejected')))
) ENGINE=InnoDB AUTO_INCREMENT=8091 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `practitioner_document`
--

LOCK TABLES `practitioner_document` WRITE;
/*!40000 ALTER TABLE `practitioner_document` DISABLE KEYS */;
INSERT INTO `practitioner_document` VALUES (8046,865,1,'CURP - Valeria Cordero','/documents/generated/S25010002_CURP.pdf','Pending',NULL,'2026-02-10 09:00:00',NULL),(8047,865,2,'Horario - Valeria Cordero','/documents/generated/S25010002_Horario.pdf','Pending',NULL,'2026-02-10 09:02:00',NULL),(8048,865,3,'INE - Valeria Cordero','/documents/generated/S25010002_INE.pdf','Pending',NULL,'2026-02-10 09:03:00',NULL),(8049,865,4,'Carta de aceptacion - Valeria Cordero','/documents/generated/S25010002_CartaAceptacion.pdf','Pending',NULL,'2026-02-10 09:05:00',NULL),(8050,866,1,'CURP - Andres Salas','/documents/generated/S25010003_CURP.pdf','Accepted','Documento legible y correcto.','2026-02-05 10:00:00','2026-02-07 12:00:00'),(8051,866,2,'Horario - Andres Salas','/documents/generated/S25010003_Horario.pdf','Accepted','Horario validado.','2026-02-05 10:02:00','2026-02-07 12:01:00'),(8052,866,3,'INE - Andres Salas','/documents/generated/S25010003_INE.pdf','Accepted','Identificacion vigente.','2026-02-05 10:03:00','2026-02-07 12:02:00'),(8053,866,4,'Carta de aceptacion - Andres Salas','/documents/generated/S25010003_CartaAceptacion.pdf','Accepted','Carta sellada por la organizacion.','2026-02-05 10:05:00','2026-02-07 12:03:00'),(8054,867,1,'CURP - Paola Rios','/documents/generated/S25010004_CURP.pdf','Accepted','Correcto.','2026-02-04 09:00:00','2026-02-06 11:00:00'),(8055,867,2,'Horario - Paola Rios','/documents/generated/S25010004_Horario.pdf','Accepted','Validado.','2026-02-04 09:02:00','2026-02-06 11:01:00'),(8056,867,3,'INE - Paola Rios','/documents/generated/S25010004_INE.pdf','Accepted','Vigente.','2026-02-04 09:03:00','2026-02-06 11:02:00'),(8057,867,4,'Carta de aceptacion - Paola Rios','/documents/generated/S25010004_CartaAceptacion.pdf','Accepted','Sellada y firmada.','2026-02-04 09:05:00','2026-02-06 11:03:00'),(8058,868,1,'CURP - Ricardo Mendez','/documents/generated/S25010005_CURP.pdf','Accepted','Correcto.','2026-02-03 08:00:00','2026-02-05 10:00:00'),(8059,868,2,'Horario - Ricardo Mendez','/documents/generated/S25010005_Horario.pdf','Accepted','Validado.','2026-02-03 08:02:00','2026-02-05 10:01:00'),(8060,868,3,'INE - Ricardo Mendez','/documents/generated/S25010005_INE.pdf','Accepted','Vigente.','2026-02-03 08:03:00','2026-02-05 10:02:00'),(8062,875,1,'CURP - Sergio Pacheco','/documents/generated/S25010007_CURP.pdf','Accepted',NULL,'2026-02-10 09:00:00','2026-06-25 20:32:21'),(8063,875,2,'Horario - Sergio Pacheco','/documents/generated/S25010007_Horario.pdf','Accepted',NULL,'2026-02-10 09:02:00','2026-06-25 20:32:28'),(8064,875,3,'INE - Sergio Pacheco','/documents/generated/S25010007_INE.pdf','Accepted',NULL,'2026-02-10 09:03:00','2026-06-25 20:32:35'),(8065,875,4,'Carta de aceptacion - Sergio Pacheco','/documents/generated/S25010007_CartaAceptacion.pdf','Accepted',NULL,'2026-02-10 09:05:00','2026-06-25 20:32:44'),(8066,876,1,'CURP - Daniela Romero','/documents/generated/S25010008_CURP.pdf','Accepted','Documento legible y correcto.','2026-02-05 10:00:00','2026-02-07 12:00:00'),(8067,876,2,'Horario - Daniela Romero','/documents/generated/S25010008_Horario.pdf','Accepted','Horario validado.','2026-02-05 10:02:00','2026-02-07 12:01:00'),(8068,876,3,'INE - Daniela Romero','/documents/generated/S25010008_INE.pdf','Accepted','Identificacion vigente.','2026-02-05 10:03:00','2026-02-07 12:02:00'),(8069,876,4,'Carta de aceptacion - Daniela Romero','/documents/generated/S25010008_CartaAceptacion.pdf','Accepted','Carta sellada por la organizacion.','2026-02-05 10:05:00','2026-02-07 12:03:00'),(8070,877,1,'CURP - Hugo Castillo','/documents/generated/S25010009_CURP.pdf','Accepted','Correcto.','2026-02-04 09:00:00','2026-02-06 11:00:00'),(8071,877,2,'Horario - Hugo Castillo','/documents/generated/S25010009_Horario.pdf','Accepted','Validado.','2026-02-04 09:02:00','2026-02-06 11:01:00'),(8072,877,3,'INE - Hugo Castillo','/documents/generated/S25010009_INE.pdf','Accepted','Vigente.','2026-02-04 09:03:00','2026-02-06 11:02:00'),(8073,877,4,'Carta de aceptacion - Hugo Castillo','/documents/generated/S25010009_CartaAceptacion.pdf','Accepted','Sellada y firmada.','2026-02-04 09:05:00','2026-02-06 11:03:00'),(8074,878,1,'CURP - Fernanda Aguirre','/documents/generated/S25010010_CURP.pdf','Accepted','Correcto.','2026-02-03 08:00:00','2026-02-05 10:00:00'),(8075,878,2,'Horario - Fernanda Aguirre','/documents/generated/S25010010_Horario.pdf','Accepted','Validado.','2026-02-03 08:02:00','2026-02-05 10:01:00'),(8076,878,3,'INE - Fernanda Aguirre','/documents/generated/S25010010_INE.pdf','Accepted','Vigente.','2026-02-03 08:03:00','2026-02-05 10:02:00'),(8077,878,4,'Carta de aceptacion - Fernanda Aguirre','/documents/generated/S25010010_CartaAceptacion.pdf','Accepted','Sellada y firmada.','2026-02-03 08:05:00','2026-02-05 10:03:00'),(8079,880,1,'Evidencia_Maria_Rosa_Vaca.pdf','file:///C:/Users/USER/SimuladorOneDrive_FEI/c932f62b_Evidencia_Maria_Rosa_Vaca.pdf','Accepted',NULL,'2026-06-25 13:59:56','2026-06-25 14:07:39'),(8080,880,2,'Reporte_Mayo_2026.pdf','file:///C:/Users/USER/SimuladorOneDrive_FEI/493be6c7_Reporte_Mayo_2026.pdf','Accepted',NULL,'2026-06-25 14:00:09','2026-06-25 14:07:41'),(8081,880,3,'Reporte_Mayo_2026.pdf','file:///C:/Users/USER/SimuladorOneDrive_FEI/8385e4e2_Reporte_Mayo_2026.pdf','Accepted',NULL,'2026-06-25 14:00:19','2026-06-25 14:07:44'),(8082,880,4,'Reporte_Avance_Final.pdf','file:///C:/Users/USER/SimuladorOneDrive_FEI/d8ce2897_Reporte_Avance_Final.pdf','Accepted',NULL,'2026-06-25 14:00:35','2026-06-25 14:07:49'),(8083,874,1,'Hospital.pdf','file:///C:/Users/josep/SimuladorOneDrive_FEI/0b081085_Hospital.pdf','Pending',NULL,'2026-06-25 20:01:16',NULL),(8087,868,4,'PRAIS-03_Autoevaluacion.pdf','file:///C:/Users/josep/SimuladorOneDrive_FEI/a4793561_PRAIS-03_Autoevaluacion.pdf','Accepted',NULL,'2026-06-26 20:15:59','2026-06-26 20:18:18'),(8088,868,5,'Proyecto_Construcion.pdf','file:///C:/Users/josep/SimuladorOneDrive_FEI/f17bac40_Proyecto_Construcion.pdf','Accepted',NULL,'2026-06-26 20:33:45','2026-06-26 20:36:05'),(8089,868,16,'Proyecto_Principios_Diseno__1_ (1).pdf','file:///C:/Users/josep/SimuladorOneDrive_FEI/82b8f81a_Proyecto_Principios_Diseno__1__(1).pdf','Accepted',NULL,'2026-06-26 20:34:33','2026-06-26 20:36:11'),(8090,868,17,'PRAIS-03_Autoevaluacion.pdf','file:///C:/Users/josep/SimuladorOneDrive_FEI/6a17bafb_PRAIS-03_Autoevaluacion.pdf','Accepted',NULL,'2026-06-26 20:35:30','2026-06-26 20:36:16');
/*!40000 ALTER TABLE `practitioner_document` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `practitioner_grade`
--

DROP TABLE IF EXISTS `practitioner_grade`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `practitioner_grade` (
  `grade_id` int NOT NULL AUTO_INCREMENT,
  `practitioner_id` int NOT NULL,
  `professor_id` int DEFAULT NULL,
  `tentative_grade` decimal(5,2) NOT NULL,
  `final_grade` decimal(5,2) DEFAULT NULL,
  `period` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `graded_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`grade_id`),
  UNIQUE KEY `uq_grade_period` (`practitioner_id`,`period`),
  KEY `fk_grade_professor` (`professor_id`),
  CONSTRAINT `fk_grade_practitioner` FOREIGN KEY (`practitioner_id`) REFERENCES `practitioner` (`practitioner_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_grade_professor` FOREIGN KEY (`professor_id`) REFERENCES `professor` (`professor_id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `practitioner_grade`
--

LOCK TABLES `practitioner_grade` WRITE;
/*!40000 ALTER TABLE `practitioner_grade` DISABLE KEYS */;
INSERT INTO `practitioner_grade` VALUES (6,868,863,9.20,9.00,'Febrero-Julio 2026','2026-06-26 20:39:10');
/*!40000 ALTER TABLE `practitioner_grade` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `professor`
--

DROP TABLE IF EXISTS `professor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `professor` (
  `professor_id` int NOT NULL,
  PRIMARY KEY (`professor_id`),
  CONSTRAINT `fk_professor_user` FOREIGN KEY (`professor_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `professor`
--

LOCK TABLES `professor` WRITE;
/*!40000 ALTER TABLE `professor` DISABLE KEYS */;
INSERT INTO `professor` VALUES (857),(863),(873),(879);
/*!40000 ALTER TABLE `professor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `progress_report`
--

DROP TABLE IF EXISTS `progress_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `progress_report` (
  `report_id` int NOT NULL AUTO_INCREMENT,
  `practitioner_id` int NOT NULL,
  `report_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `generation_date` date NOT NULL,
  `period_covered_start` date NOT NULL,
  `period_covered_end` date NOT NULL,
  `total_hours_at_submission` decimal(6,2) NOT NULL,
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'Pendiente de Firma',
  `signed_file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `grade` decimal(5,2) DEFAULT NULL,
  `professor_feedback` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`report_id`),
  UNIQUE KEY `uq_progress_report_type` (`practitioner_id`,`report_type`),
  UNIQUE KEY `uq_progress_report_practitioner_type` (`practitioner_id`,`report_type`),
  CONSTRAINT `fk_progress_practitioner` FOREIGN KEY (`practitioner_id`) REFERENCES `practitioner` (`practitioner_id`) ON DELETE CASCADE,
  CONSTRAINT `progress_report_chk_1` CHECK ((`report_type` in (_utf8mb4'Intermedio',_utf8mb4'Final')))
) ENGINE=InnoDB AUTO_INCREMENT=8018 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `progress_report`
--

LOCK TABLES `progress_report` WRITE;
/*!40000 ALTER TABLE `progress_report` DISABLE KEYS */;
INSERT INTO `progress_report` VALUES (8010,867,'Intermedio','2026-04-01','2026-02-01','2026-03-31',220.00,'Evaluado','/documents/generated/S25010004_parcial_intermedio.pdf',10.00,'1urtsgf'),(8011,868,'Intermedio','2026-04-01','2026-02-01','2026-03-31',220.00,'Evaluado','/documents/generated/S25010005_parcial_intermedio.pdf',9.00,'Reporte intermedio aprobado.'),(8012,868,'Final','2026-06-01','2026-02-01','2026-05-31',440.00,'Evaluado','/documents/generated/S25010005_reporte_final.pdf',9.20,'Reporte final aprobado, excelente desempeno.'),(8013,877,'Intermedio','2026-04-01','2026-02-01','2026-03-31',220.00,'Entregado','/documents/generated/S25010009_parcial_intermedio.pdf',NULL,NULL),(8014,878,'Intermedio','2026-04-01','2026-02-01','2026-03-31',220.00,'Evaluado','/documents/generated/S25010010_parcial_intermedio.pdf',9.00,'Reporte intermedio aprobado.'),(8015,878,'Final','2026-06-01','2026-02-01','2026-05-31',440.00,'Evaluado','/documents/generated/S25010010_reporte_final.pdf',9.20,'Reporte final aprobado, excelente desempeno.'),(8016,880,'Intermedio','2026-06-25','2026-02-01','2026-06-30',320.00,'Evaluado','file:///C:/Users/USER/SimuladorOneDrive_FEI/8db5a9cc_Reporte_Mayo_2026.pdf',10.00,'adasda'),(8017,866,'Intermedio','2026-06-26','2026-02-01','2026-06-30',220.00,'Evaluado','file:///C:/Users/USER/SimuladorOneDrive_FEI/082e242c_PRAIS-03_Autoevaluacion.pdf',5.00,'10');
/*!40000 ALTER TABLE `progress_report` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project`
--

DROP TABLE IF EXISTS `project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project` (
  `project_id` int NOT NULL AUTO_INCREMENT,
  `project_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `participant_capacity` int NOT NULL,
  `manager_id` int NOT NULL,
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'Active',
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `organization_id` int NOT NULL,
  PRIMARY KEY (`project_id`),
  KEY `fk_project_organization` (`organization_id`),
  CONSTRAINT `fk_project_organization` FOREIGN KEY (`organization_id`) REFERENCES `linked_organization` (`organization_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=809 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project`
--

LOCK TABLES `project` WRITE;
/*!40000 ALTER TABLE `project` DISABLE KEYS */;
INSERT INTO `project` VALUES (801,'Elden Ring 2','Segunda version del juego',20,801,'Active','2024-12-01','2030-01-16',801),(802,'Forza15','Graficos, server, fullstack',1,801,'Active','2025-04-01','2031-02-02',801),(803,'ShooterShot','',15,801,'Active','2022-04-01','2029-12-06',801),(804,'BackEnd Web','Mantenimiento de la pagina web de la empresa',1,801,'Active','1999-02-01','2999-12-31',801),(805,'Sistema de Gestion Hospitalaria','Modulo de expedientes clinicos electronicos y agenda medica.',10,802,'Active','2026-02-01','2026-07-31',802),(806,'Plataforma de Comercio Electronico','Tienda en linea con carrito, pagos y panel administrativo.',10,802,'Active','2026-02-01','2026-07-31',802),(807,'Aplicacion de Movilidad Urbana','App movil para rutas de transporte publico en tiempo real.',10,802,'Active','2026-02-01','2026-07-31',802),(808,'tetris','',4,801,'Active','2022-01-01','2029-12-31',801);
/*!40000 ALTER TABLE `project` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_acceptance`
--

DROP TABLE IF EXISTS `project_acceptance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_acceptance` (
  `coordinator_id` int NOT NULL,
  `project_id` int NOT NULL,
  PRIMARY KEY (`coordinator_id`,`project_id`),
  KEY `fk_acceptance_project` (`project_id`),
  CONSTRAINT `fk_acceptance_coordinator` FOREIGN KEY (`coordinator_id`) REFERENCES `coordinator` (`coordinator_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_acceptance_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`project_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_acceptance`
--

LOCK TABLES `project_acceptance` WRITE;
/*!40000 ALTER TABLE `project_acceptance` DISABLE KEYS */;
/*!40000 ALTER TABLE `project_acceptance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_manager`
--

DROP TABLE IF EXISTS `project_manager`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_manager` (
  `manager_id` int NOT NULL AUTO_INCREMENT,
  `manager_name` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `organization_id` int DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'Active',
  PRIMARY KEY (`manager_id`),
  KEY `fk_manager_organization` (`organization_id`),
  CONSTRAINT `fk_manager_organization` FOREIGN KEY (`organization_id`) REFERENCES `linked_organization` (`organization_id`),
  CONSTRAINT `project_manager_chk_1` CHECK ((`status` in (_utf8mb4'Active',_utf8mb4'Inactive')))
) ENGINE=InnoDB AUTO_INCREMENT=804 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_manager`
--

LOCK TABLES `project_manager` WRITE;
/*!40000 ALTER TABLE `project_manager` DISABLE KEYS */;
INSERT INTO `project_manager` VALUES (801,'Jose Morelos','2289565623','Jmorelos@lightcode.com',801,'Active'),(802,'Roberto Mendez Lara','2288998877','roberto.mendez@innovatech.com.mx',802,'Active'),(803,'Jose fuentes martienes','2266005588','infuentes@innoDigi.com',802,'Active');
/*!40000 ALTER TABLE `project_manager` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_postulation`
--

DROP TABLE IF EXISTS `project_postulation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_postulation` (
  `postulation_id` int NOT NULL AUTO_INCREMENT,
  `practitioner_id` int NOT NULL,
  `project_id` int NOT NULL,
  `priority_level` int NOT NULL,
  `postulation_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `postulation_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'Pending',
  PRIMARY KEY (`postulation_id`),
  KEY `fk_postulation_practitioner` (`practitioner_id`),
  KEY `fk_postulation_project` (`project_id`),
  CONSTRAINT `fk_postulation_practitioner` FOREIGN KEY (`practitioner_id`) REFERENCES `practitioner` (`practitioner_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_postulation_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`project_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=86 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_postulation`
--

LOCK TABLES `project_postulation` WRITE;
/*!40000 ALTER TABLE `project_postulation` DISABLE KEYS */;
INSERT INTO `project_postulation` VALUES (46,865,803,1,'2026-06-25 13:04:52','Assigned'),(47,866,805,1,'2026-06-25 13:04:53','Assigned'),(48,867,806,1,'2026-06-25 13:04:54','Assigned'),(49,868,807,1,'2026-06-25 13:04:56','Assigned'),(51,875,803,1,'2026-06-25 13:14:39','Assigned'),(52,876,805,1,'2026-06-25 13:14:40','Assigned'),(53,877,806,1,'2026-06-25 13:14:41','Assigned'),(54,878,807,1,'2026-06-25 13:14:42','Assigned'),(55,880,808,1,'2026-06-25 13:57:08','Assigned'),(56,880,803,2,'2026-06-25 13:57:08','Rejected'),(57,880,807,3,'2026-06-25 13:57:08','Rejected'),(58,880,805,4,'2026-06-25 13:57:08','Rejected'),(59,880,806,5,'2026-06-25 13:57:08','Rejected'),(60,880,801,6,'2026-06-25 13:57:08','Rejected'),(61,880,804,7,'2026-06-25 13:57:09','Rejected'),(62,880,802,8,'2026-06-25 13:57:09','Rejected'),(63,864,801,1,'2026-06-25 14:27:34','Assigned'),(64,864,807,2,'2026-06-25 14:27:34','Rejected'),(65,864,805,3,'2026-06-25 14:27:34','Rejected'),(66,864,806,4,'2026-06-25 14:27:34','Rejected'),(67,864,803,5,'2026-06-25 14:27:34','Rejected'),(68,864,804,6,'2026-06-25 14:27:34','Rejected'),(69,864,802,7,'2026-06-25 14:27:34','Rejected'),(70,864,808,8,'2026-06-25 14:27:34','Rejected'),(71,874,802,1,'2026-06-25 14:28:21','Rejected'),(72,874,804,2,'2026-06-25 14:28:22','Assigned'),(73,874,803,3,'2026-06-25 14:28:22','Rejected'),(74,874,801,4,'2026-06-25 14:28:22','Rejected'),(75,874,805,5,'2026-06-25 14:28:22','Rejected'),(76,874,806,6,'2026-06-25 14:28:22','Rejected'),(77,874,807,7,'2026-06-25 14:28:22','Rejected'),(78,874,808,8,'2026-06-25 14:28:22','Rejected'),(79,881,801,1,'2026-06-26 14:53:39','Pending'),(80,881,807,2,'2026-06-26 14:53:39','Pending'),(81,881,806,3,'2026-06-26 14:53:39','Pending'),(82,881,803,4,'2026-06-26 14:53:39','Pending'),(83,881,805,5,'2026-06-26 14:53:39','Pending'),(84,881,808,6,'2026-06-26 14:53:39','Pending'),(85,881,802,7,'2026-06-26 14:53:39','Pending');
/*!40000 ALTER TABLE `project_postulation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role` (
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`role_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES ('Administrator','Administrator of the Professional Practices app'),('Coordinator','Coordinator role for professional practices'),('Practitioner','Student role'),('Professor','Professor role');
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `school_period`
--

DROP TABLE IF EXISTS `school_period`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `school_period` (
  `period_id` int NOT NULL AUTO_INCREMENT,
  `period_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `period_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `active_period_guard` int GENERATED ALWAYS AS ((case when (`period_status` = _utf8mb4'Active') then 1 end)) VIRTUAL,
  PRIMARY KEY (`period_id`),
  UNIQUE KEY `uq_school_period_active` (`active_period_guard`),
  CONSTRAINT `school_period_chk_1` CHECK ((`period_status` in (_utf8mb4'Active',_utf8mb4'Concluded',_utf8mb4'Upcoming')))
) ENGINE=InnoDB AUTO_INCREMENT=805 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `school_period`
--

LOCK TABLES `school_period` WRITE;
/*!40000 ALTER TABLE `school_period` DISABLE KEYS */;
INSERT INTO `school_period` (`period_id`, `period_name`, `start_date`, `end_date`, `period_status`) VALUES (801,'Febrero-Agosot 2026','2026-02-01','2026-07-31','Concluded'),(803,'Febrero-junio','2026-06-02','2026-06-30','Concluded'),(804,'Febrero-Julio 2026','2026-02-01','2026-07-31','Active');
/*!40000 ALTER TABLE `school_period` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `self_evaluation`
--

DROP TABLE IF EXISTS `self_evaluation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `self_evaluation` (
  `self_eval_id` int NOT NULL AUTO_INCREMENT,
  `evidence` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `practitioner_id` int NOT NULL,
  `report_id` int NOT NULL,
  `q1` int NOT NULL DEFAULT '0',
  `q2` int NOT NULL DEFAULT '0',
  `q3` int NOT NULL DEFAULT '0',
  `q4` int NOT NULL DEFAULT '0',
  `q5` int NOT NULL DEFAULT '0',
  `q6` int NOT NULL DEFAULT '0',
  `q7` int NOT NULL DEFAULT '0',
  `q8` int NOT NULL DEFAULT '0',
  `q9` int NOT NULL DEFAULT '0',
  `q10` int NOT NULL DEFAULT '0',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'Pendiente',
  `review_comment` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`self_eval_id`),
  UNIQUE KEY `uq_selfeval_report` (`report_id`),
  KEY `fk_selfeval_practitioner` (`practitioner_id`),
  CONSTRAINT `fk_selfeval_practitioner` FOREIGN KEY (`practitioner_id`) REFERENCES `practitioner` (`practitioner_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_selfeval_report` FOREIGN KEY (`report_id`) REFERENCES `progress_report` (`report_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `self_evaluation`
--

LOCK TABLES `self_evaluation` WRITE;
/*!40000 ALTER TABLE `self_evaluation` DISABLE KEYS */;
INSERT INTO `self_evaluation` VALUES (6,'C:\\Users\\josep\\Documents\\PRAIS-03_Autoevaluacion.pdf',868,8012,5,4,2,4,4,4,4,4,4,5,'Revisada',NULL);
/*!40000 ALTER TABLE `self_evaluation` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`doadmin`@`%`*/ /*!50003 TRIGGER `trg_verify_final_report_self_eval` BEFORE INSERT ON `self_evaluation` FOR EACH ROW BEGIN
    DECLARE v_report_type VARCHAR(20);

    -- Consultar el tipo de reporte al que se está intentando ligar
    SELECT report_type INTO v_report_type
    FROM progress_report
    WHERE report_id = NEW.report_id;

    -- Bloquear la inserción si el reporte no es 'Final'
    IF v_report_type != 'Final' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Operacion denegada: La autoevaluacion solo puede registrarse para un reporte de tipo Final.';
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `last_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `gender` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `registration_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `discharge_date` datetime DEFAULT NULL,
  `active_singleton_role` varchar(50) COLLATE utf8mb4_unicode_ci GENERATED ALWAYS AS ((case when ((`status` = _utf8mb4'Active') and (`role_name` in (_utf8mb4'Administrator',_utf8mb4'Coordinator'))) then `role_name` end)) VIRTUAL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `uq_user_active_singleton_role` (`active_singleton_role`),
  KEY `fk_user_role` (`role_name`),
  CONSTRAINT `fk_user_role` FOREIGN KEY (`role_name`) REFERENCES `role` (`role_name`) ON DELETE RESTRICT,
  CONSTRAINT `user_chk_1` CHECK ((`status` in (_utf8mb4'Active',_utf8mb4'Inactive',_utf8mb4'Pending'))),
  CONSTRAINT `user_chk_2` CHECK ((`gender` in (_utf8mb4'Male',_utf8mb4'Female',_utf8mb4'Other')))
) ENGINE=InnoDB AUTO_INCREMENT=882 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` (`user_id`, `username`, `password`, `name`, `last_name`, `email`, `role_name`, `status`, `gender`, `registration_date`, `discharge_date`) VALUES (38,'99999999999999999999','$argon2id$v=19$m=19456,t=3,p=1$3jNi9Uc6qGQ0oSH88R/6/w$DwknDS2H+cPNgAa9juetKroKWY8jg/1B3fP/sabzmRo','Angel de','Aguilar','angel2@gmail.com','Administrator','Active','Male','2026-06-23 19:12:18',NULL),(39,'2','$argon2id$v=19$m=19456,t=3,p=1$U+SzsvGr4/b7m37keriGXQ$kew7wSkjQTtYecg2AmWIeQN6QVeq3YJFzSs1J6GPSp8','Pedro','Martines','Pmartines@uv.com','Coordinator','Active','Female','2026-06-23 19:15:39',NULL),(857,'3','$argon2id$v=19$m=19456,t=3,p=1$B1hzyWqvcGVCX1+Uo52vyA$NIxUvrpoXjE4nNCTzkH0iJjFDkvhKz6OqwKp6nszhSc','Juan Carlos','Perez','juan@gmail.com','Professor','Active','Male','2026-06-25 07:39:42',NULL),(858,'S24013324','$argon2id$v=19$m=19456,t=3,p=1$0IVY9d4JPNs7Y5iGKseX2g$9AZ2dRc0q0Yk7zHXslafra+HE00HEuSkltAxf1sbbUs','Angel','Aguilar','angel@gmail.com','Practitioner','Active','Male','2026-06-25 07:44:43',NULL),(861,'S24033333','$argon2id$v=19$m=19456,t=3,p=1$GdMpzH2Ay/C+dYswomBtPA$WnuUDWpy0Xizl8RxQXRcdCGM+IYLzI/pfn3MrDObfoc','Juan Carlos','Badilla','jp1234q@gmail.com','Practitioner','Pending','Male','2026-06-25 12:53:48',NULL),(862,'S24033334','$argon2id$v=19$m=19456,t=3,p=1$n+pFrzy1wzS0JoT1N3h2UQ$eVKh819OWL43sG3RV1ZGqaUKdwb0stO3APF21tzq/zA','Maria','Rosa Cuevas','rosa34_XXX@gmail.com','Practitioner','Pending','Female','2026-06-25 12:54:29',NULL),(863,'70123','$argon2id$v=19$m=19456,t=3,p=1$ywDkRg+iULt9KFfkMKHiWA$Y6o7JI9RzVxJ302agWjz8iw3BaD+xsw4h6w+D41YN4E','Laura','Mendoza Rios','laura.mendoza@uv.mx','Professor','Active','Female','2026-06-25 13:04:51',NULL),(864,'S25010001','$argon2id$v=19$m=19456,t=3,p=1$HTXwMGkLcz3G7y+yxnTdUA$IcwxVPaaG5g+RuDvEgTpuWUwFV8OobXEqZGCmDRlw/g','Diego','Hernandez Luna','diego.hernandez@estudiantes.uv.mx','Practitioner','Active','Male','2026-06-25 13:04:52',NULL),(865,'S25010002','$argon2id$v=19$m=19456,t=3,p=1$ZkQ8Dcs9qQP5oo8omh4otQ$wMaXKuUCRA5b3gWWwcgygbcGG5FmlaqTUvuFaumERKk','Valeria','Cordero Nava','valeria.cordero@estudiantes.uv.mx','Practitioner','Active','Female','2026-06-25 13:04:52',NULL),(866,'S25010003','$argon2id$v=19$m=19456,t=3,p=1$+D/Gz/mblD+7mHtUfEmf3w$6EfMg6zlWO/TgXH39cH9hO4NzlT3OW2XRy/u7je1evQ','Andres','Salas Bravo','andres.salas@estudiantes.uv.mx','Practitioner','Active','Male','2026-06-25 13:04:53',NULL),(867,'S25010004','$argon2id$v=19$m=19456,t=3,p=1$4wKOuG6zkD9W89UcraEqiQ$X08/ddpFHGkzJd/KmRu26Rkix2vA71IkDzqg/vf8U6c','Paola','Rios Fuentes','paola.rios@estudiantes.uv.mx','Practitioner','Active','Female','2026-06-25 13:04:54',NULL),(868,'S25010005','$argon2id$v=19$m=19456,t=3,p=1$CPO6o95JzftIK3KPkn8TRw$Hu7elpT49KnA2p66tBVhnJ5ZnkXurMs/ZhFaq3/qgqk','Ricardo','Mendez Soto','ricardo.mendez@estudiantes.uv.mx','Practitioner','Active','Male','2026-06-25 13:04:56',NULL),(869,'S23033335','$argon2id$v=19$m=19456,t=3,p=1$VJ4CU3ld37/PQPJbVlEBPA$AqO0dntL6pGuEauCcNiLR0JfSe/oqZsTb38f1B4JdzY','Juan','hernandez Perez','jperez2123q@gmail.com','Practitioner','Active','Male','2026-06-25 13:06:01',NULL),(870,'S23033336','$argon2id$v=19$m=19456,t=3,p=1$IKYVuZFVztZYqz8gztmYvw$7xh3LydKAGv9bYr9u2f97kq3qTigebIB8HiNc3r9RV0','Pablo','hernandez gomez','minecraft_xxQQ@gmail.com','Practitioner','Pending','Male','2026-06-25 13:09:27',NULL),(871,'S23033337','$argon2id$v=19$m=19456,t=3,p=1$0GpAPaAAPAU6kzRL+n+8/g$4WA9/R1f7yht5szHsCtCWvaoh0DUyjVEAAnxJTwRSm4','Maria Rosa','del Alva romero','rosaRosaXOXOXO@gmail.com','Practitioner','Pending','Male','2026-06-25 13:10:54',NULL),(872,'S23033338','$argon2id$v=19$m=19456,t=3,p=1$DxmBGS9CLZKixg7rRdKLyg$LTH+KoCwePJAvQfnW82QbXLp611gd1bo/3yzIk+DF+c','Martin','Hernandez Arriaga','halo3_62@gmail.com','Practitioner','Pending','Male','2026-06-25 13:11:36',NULL),(873,'70124','$argon2id$v=19$m=19456,t=3,p=1$W/NtkjOLApGnWikujranfg$fBREz/p4rme35rIZvhc7zK+FGev/DiKuBCmG9o7GxCU','Carlos','Vega Ortiz','carlos.vega@uv.mx','Professor','Active','Male','2026-06-25 13:14:37',NULL),(874,'S25010006','$argon2id$v=19$m=19456,t=3,p=1$Rabfi5RYlWC2YS60lpmufg$2IHlMwf3L0BjHZxZxvKeYU8otKQIcDREt2HBR1LIUpI','Mariana','Lopez Trejo','mariana.lopez@estudiantes.uv.mx','Practitioner','Active','Female','2026-06-25 13:14:38',NULL),(875,'S25010007','$argon2id$v=19$m=19456,t=3,p=1$45oqf0w8tEskF6pawpJnRQ$x2CNet58dtiwy7X/hJwhqcMlkGPtCbSp9FtOW8tGjCw','Sergio','Pacheco Lara','sergio.pacheco@estudiantes.uv.mx','Practitioner','Active','Male','2026-06-25 13:14:39',NULL),(876,'S25010008','$argon2id$v=19$m=19456,t=3,p=1$0itNRzOpMgNig71ekxboDg$xtAUrlGHO/ED54FaQHoNweB0HJ/gTthE1ofmwwItmrM','Daniela','Romero Vidal','daniela.romero@estudiantes.uv.mx','Practitioner','Active','Female','2026-06-25 13:14:39',NULL),(877,'S25010009','$argon2id$v=19$m=19456,t=3,p=1$NNO98FC29uwNiZczb5UATg$+PmrCZl735ReSbHC7fAl6NNke8wJdOTZqBcpV/QABoo','Hugo','Castillo Mena','hugo.castillo@estudiantes.uv.mx','Practitioner','Active','Male','2026-06-25 13:14:41',NULL),(878,'S25010010','$argon2id$v=19$m=19456,t=3,p=1$MTrSFzoCQTGGxCvAhfgboQ$ET2BeWswwL3b29Hy86Nmab5X3EtjPOiuvtiSAvJvfOU','Fernanda','Aguirre Solis','fernanda.aguirre@estudiantes.uv.mx','Practitioner','Active','Female','2026-06-25 13:14:42',NULL),(879,'5','$argon2id$v=19$m=19456,t=3,p=1$4DL5i86tMAt0SsQHoVLzNg$Euopzk+MqGx4H1kchWgjJ61lqqNevjSZp3TojjBajB8','Juan','Gutierres','jgutierres@uv.com','Professor','Active','Male','2026-06-25 13:49:33',NULL),(880,'S12345678','$argon2id$v=19$m=19456,t=3,p=1$I9JwV5ftDKGj6jkhbKujLQ$UkyuVHMRPvfhS1wYW9FkgCoCLPXtvREolsKmmY56X0I','Juan','Testing','test@gmai.com','Practitioner','Active','Male','2026-06-25 13:54:11',NULL),(881,'S44444444','$argon2id$v=19$m=19456,t=3,p=1$C8EWpmbAgOh/xXfELRhtlw$ZKN+Jqxqmn0wd6ysaPjnOmCvrgZrLnDqXGJQ0BnDqm0','Juan','Test','practest@gmail.com','Practitioner','Active','Female','2026-06-26 14:45:31',NULL);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'professional-practices-system'
--
--
-- WARNING: can't read the INFORMATION_SCHEMA.libraries table. It's most probably an old server 8.4.8.
--
--
-- WARNING: can't read the INFORMATION_SCHEMA.libraries table. It's most probably an old server 8.4.8.
--
/*!50003 DROP PROCEDURE IF EXISTS `assign_project_and_reject_others` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`doadmin`@`%` PROCEDURE `assign_project_and_reject_others`(
    IN p_practitioner_id INT,
    IN p_project_id      INT
)
BEGIN
    UPDATE project_postulation
    SET    postulation_status = 'Assigned'
    WHERE  practitioner_id = p_practitioner_id
      AND  project_id = p_project_id;

    UPDATE project_postulation
    SET    postulation_status = 'Rejected'
    WHERE  practitioner_id = p_practitioner_id
      AND  project_id != p_project_id;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-26 21:38:21
