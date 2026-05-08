SET REFERENTIAL_INTEGRITY FALSE;

DROP PROCEDURE IF EXISTS AssignProjectAndRejectOthers;
DROP TABLE IF EXISTS POSTULACION_PROYECTO;
DROP TABLE IF EXISTS PARTICIPA;
DROP TABLE IF EXISTS FORMADO_POR;
DROP TABLE IF EXISTS EVALUACION_PROFESOR;
DROP TABLE IF EXISTS EVALUACION_ORGANIZACION;
DROP TABLE IF EXISTS BITACORA_REPORTE;
DROP TABLE IF EXISTS AUTO_EVALUACION;
DROP TABLE IF EXISTS ACEPTA;
DROP TABLE IF EXISTS GRUPO_PRACTICAS;
DROP TABLE IF EXISTS PROYECTO;
DROP TABLE IF EXISTS PRACTICANTE;
DROP TABLE IF EXISTS PROFESOR;
DROP TABLE IF EXISTS COORDINADOR;
DROP TABLE IF EXISTS ADMINISTRADOR;
DROP TABLE IF EXISTS MENSAJE;
DROP TABLE IF EXISTS ACTIVIDAD;
DROP TABLE IF EXISTS ORGANIZACION_VINCULADA;
DROP TABLE IF EXISTS PERIODO_ESCOLAR;
DROP TABLE IF EXISTS ACCESS_TOKEN;
DROP TABLE IF EXISTS USUARIO;
DROP TABLE IF EXISTS ROL;

SET REFERENTIAL_INTEGRITY TRUE;

CREATE TABLE ROL (
                     NOMBRE_ROL varchar(50) NOT NULL,
                     DESCRIPCION varchar(255) DEFAULT NULL,
                     PRIMARY KEY (NOMBRE_ROL)
);

CREATE TABLE USUARIO (
                         ID_USUARIO int NOT NULL AUTO_INCREMENT,
                         NOMBRE_USUARIO varchar(20) NOT NULL UNIQUE,
                         PASSWORD varchar(255) NOT NULL,
                         NOMBRE varchar(100) NOT NULL,
                         APELLIDOS varchar(100) NOT NULL,
                         CORREO varchar(150) NOT NULL UNIQUE,
                         NOMBRE_ROL varchar(50) NOT NULL,
                         ESTADO varchar(20) NOT NULL CHECK (ESTADO IN ('Activo', 'No Activo', 'Pendiente')),
                         GENERO varchar(20) DEFAULT NULL,
                         FECHA_REGISTRO datetime DEFAULT CURRENT_TIMESTAMP,
                         FECHA_BAJA datetime DEFAULT NULL,
                         PRIMARY KEY (ID_USUARIO),
                         CONSTRAINT fk_usuario_rol FOREIGN KEY (NOMBRE_ROL) REFERENCES ROL (NOMBRE_ROL) ON DELETE RESTRICT
);

CREATE TABLE ACCESS_TOKEN (
                              TOKEN_VALUE int NOT NULL,
                              CREATION_TIME datetime DEFAULT CURRENT_TIMESTAMP,
                              NOMBRE_USUARIO varchar(20) NOT NULL,
                              PRIMARY KEY (TOKEN_VALUE),
                              CONSTRAINT access_token_usuario_USER_fk FOREIGN KEY (NOMBRE_USUARIO) REFERENCES USUARIO (NOMBRE_USUARIO) ON DELETE CASCADE
);

CREATE TABLE PERIODO_ESCOLAR (
                                 ID_PERIODO int NOT NULL AUTO_INCREMENT,
                                 NOMBRE_PERIODO varchar(50) NOT NULL,
                                 FECHA_INICIO date DEFAULT NULL,
                                 FECHA_FIN date DEFAULT NULL,
                                 ESTADO_PERIODO varchar(20) NOT NULL CHECK (ESTADO_PERIODO IN ('activo', 'concluido', 'proximo')),
                                 PRIMARY KEY (ID_PERIODO)
);

CREATE TABLE ORGANIZACION_VINCULADA (
                                        ID_ORGANIZACION int NOT NULL AUTO_INCREMENT,
                                        NOMBRE_ORGANIZACION varchar(150) NOT NULL,
                                        ESTADO varchar(50) DEFAULT NULL,
                                        DIRECCION varchar(255) DEFAULT NULL,
                                        CIUDAD varchar(100) DEFAULT NULL,
                                        SECTOR varchar(100) DEFAULT NULL,
                                        CORREO varchar(150) DEFAULT NULL,
                                        TELEFONO varchar(20) DEFAULT NULL,
                                        PRIMARY KEY (ID_ORGANIZACION),
                                        UNIQUE KEY CORREO (CORREO)
);

CREATE TABLE ACTIVIDAD (
                           ID_ACTIVIDAD int NOT NULL AUTO_INCREMENT,
                           NOMBRE varchar(150) NOT NULL,
                           FECHA_INICIO date DEFAULT NULL,
                           FECHA_END date DEFAULT NULL,
                           DESCRIPCION text,
                           ENCARGADO varchar(150) DEFAULT NULL,
                           PRIMARY KEY (ID_ACTIVIDAD)
);

CREATE TABLE MENSAJE (
                         INDEX_MENSAJE int NOT NULL AUTO_INCREMENT,
                         FECHA_ENVIO datetime DEFAULT CURRENT_TIMESTAMP,
                         PRIMARY KEY (INDEX_MENSAJE)
);

CREATE TABLE ADMINISTRADOR (
                               ID_ADMINISTRADOR int NOT NULL,
                               PRIMARY KEY (ID_ADMINISTRADOR),
                               CONSTRAINT administrador_ibfk_1 FOREIGN KEY (ID_ADMINISTRADOR) REFERENCES USUARIO (ID_USUARIO) ON DELETE CASCADE
);

CREATE TABLE COORDINADOR (
                             ID_COORDINADOR int NOT NULL,
                             PRIMARY KEY (ID_COORDINADOR),
                             CONSTRAINT coordinador_ibfk_1 FOREIGN KEY (ID_COORDINADOR) REFERENCES USUARIO (ID_USUARIO) ON DELETE CASCADE
);

CREATE TABLE PROFESOR (
                          ID_PROFESOR int NOT NULL,
                          PRIMARY KEY (ID_PROFESOR),
                          CONSTRAINT profesor_ibfk_1 FOREIGN KEY (ID_PROFESOR) REFERENCES USUARIO (ID_USUARIO) ON DELETE CASCADE
);

CREATE TABLE PRACTICANTE (
                             ID_PRACTICANTE int NOT NULL,
                             LENGUA_INDIGENA varchar(100) DEFAULT NULL,
                             CALIFICACION decimal(5,2) DEFAULT NULL,
                             PRIMARY KEY (ID_PRACTICANTE),
                             CONSTRAINT practicante_ibfk_1 FOREIGN KEY (ID_PRACTICANTE) REFERENCES USUARIO (ID_USUARIO) ON DELETE CASCADE
);

CREATE TABLE PROYECTO (
                          ID_PROYECTO int NOT NULL AUTO_INCREMENT,
                          NOMBRE_PROYECTO varchar(200) NOT NULL,
                          DESCRIPCION text,
                          CUPO_PARTICIPANTES int NOT NULL,
                          ENCARGADO varchar(150) DEFAULT NULL,
                          ESTADO varchar(50) DEFAULT 'Activo',
                          FECHA_INICIO date DEFAULT NULL,
                          FECHA_END date DEFAULT NULL,
                          ID_ORGANIZACION int NOT NULL,
                          PRIMARY KEY (ID_PROYECTO),
                          CONSTRAINT fk_proyecto_organizacion FOREIGN KEY (ID_ORGANIZACION) REFERENCES ORGANIZACION_VINCULADA (ID_ORGANIZACION) ON DELETE CASCADE
);

CREATE TABLE POSTULACION_PROYECTO (
                                      ID_POSTULACION int NOT NULL AUTO_INCREMENT,
                                      ID_PRACTICANTE int NOT NULL,
                                      ID_PROYECTO int NOT NULL,
                                      NIVEL_PRIORIDAD int NOT NULL,
                                      FECHA_POSTULACION datetime DEFAULT CURRENT_TIMESTAMP,
                                      ESTADO_POSTULACION varchar(20) DEFAULT 'Pendiente',
                                      PRIMARY KEY (ID_POSTULACION),
                                      CONSTRAINT fk_postulacion_practicante FOREIGN KEY (ID_PRACTICANTE) REFERENCES PRACTICANTE (ID_PRACTICANTE) ON DELETE CASCADE,
                                      CONSTRAINT fk_postulacion_proyecto FOREIGN KEY (ID_PROYECTO) REFERENCES PROYECTO (ID_PROYECTO) ON DELETE CASCADE
);

CREATE TABLE GRUPO_PRACTICAS (
                                 INDEX_GRUPO int NOT NULL AUTO_INCREMENT,
                                 SECCION varchar(50) NOT NULL,
                                 ID_PERIODO int NOT NULL,
                                 ID_PROFESOR int NOT NULL,
                                 PRIMARY KEY (INDEX_GRUPO),
                                 CONSTRAINT grupo_practicas_ibfk_1 FOREIGN KEY (ID_PROFESOR) REFERENCES PROFESOR (ID_PROFESOR) ON DELETE CASCADE,
                                 CONSTRAINT grupo_practicas_ibfk_2 FOREIGN KEY (ID_PERIODO) REFERENCES PERIODO_ESCOLAR (ID_PERIODO) ON DELETE CASCADE
);

CREATE TABLE ACEPTA (
                        ID_COORDINADOR int NOT NULL,
                        ID_PROYECTO int NOT NULL,
                        PRIMARY KEY (ID_COORDINADOR,ID_PROYECTO),
                        CONSTRAINT acepta_ibfk_1 FOREIGN KEY (ID_COORDINADOR) REFERENCES COORDINADOR (ID_COORDINADOR) ON DELETE CASCADE,
                        CONSTRAINT acepta_ibfk_2 FOREIGN KEY (ID_PROYECTO) REFERENCES PROYECTO (ID_PROYECTO) ON DELETE CASCADE
);

CREATE TABLE AUTO_EVALUACION (
                                 ID_AUTO_EVAL int NOT NULL AUTO_INCREMENT,
                                 PERIODO varchar(50) NOT NULL,
                                 CALIFICACION decimal(5,2) NOT NULL,
                                 DESCRIPCION text,
                                 ID_PRACTICANTE int NOT NULL,
                                 EVIDENCIA text,
                                 PRIMARY KEY (ID_AUTO_EVAL),
                                 CONSTRAINT auto_evaluacion_ibfk_1 FOREIGN KEY (ID_PRACTICANTE) REFERENCES PRACTICANTE (ID_PRACTICANTE) ON DELETE CASCADE
);

CREATE TABLE BITACORA_REPORTE (
                                  INDEX_REPORTE int NOT NULL AUTO_INCREMENT,
                                  FECHA date NOT NULL,
                                  ID_PRACTICANTE int NOT NULL,
                                  PRIMARY KEY (INDEX_REPORTE),
                                  CONSTRAINT bitacora_reporte_ibfk_1 FOREIGN KEY (ID_PRACTICANTE) REFERENCES PRACTICANTE (ID_PRACTICANTE) ON DELETE CASCADE
);

CREATE TABLE EVALUACION_ORGANIZACION (
                                         ID_EVAL_EXTERNA int NOT NULL AUTO_INCREMENT,
                                         PERIODO varchar(50) NOT NULL,
                                         CALIFICACION decimal(5,2) NOT NULL,
                                         DESCRIPCION text,
                                         ID_PRACTICANTE int NOT NULL,
                                         ID_ORGANIZACION int DEFAULT NULL,
                                         PRIMARY KEY (ID_EVAL_EXTERNA),
                                         CONSTRAINT evaluacion_ORGANIZACION_ibfk_1 FOREIGN KEY (ID_PRACTICANTE) REFERENCES PRACTICANTE (ID_PRACTICANTE) ON DELETE CASCADE,
                                         CONSTRAINT evaluacion_ORGANIZACION_ibfk_2 FOREIGN KEY (ID_ORGANIZACION) REFERENCES ORGANIZACION_VINCULADA (ID_ORGANIZACION) ON DELETE SET NULL
);

CREATE TABLE EVALUACION_PROFESOR (
                                     ID_EVAL_EXTERNA int NOT NULL AUTO_INCREMENT,
                                     PERIODO varchar(50) NOT NULL,
                                     CALIFICACION decimal(5,2) NOT NULL,
                                     DESCRIPCION text,
                                     ID_PRACTICANTE int NOT NULL,
                                     ID_PROFESOR int DEFAULT NULL,
                                     PRIMARY KEY (ID_EVAL_EXTERNA),
                                     CONSTRAINT evaluacion_profesor_ibfk_1 FOREIGN KEY (ID_PRACTICANTE) REFERENCES PRACTICANTE (ID_PRACTICANTE) ON DELETE CASCADE,
                                     CONSTRAINT evaluacion_profesor_ibfk_2 FOREIGN KEY (ID_PROFESOR) REFERENCES PROFESOR (ID_PROFESOR) ON DELETE SET NULL
);

CREATE TABLE FORMADO_POR (
                             ID_PROYECTO int NOT NULL,
                             ID_ACTIVIDAD int NOT NULL,
                             PRIMARY KEY (ID_PROYECTO,ID_ACTIVIDAD),
                             CONSTRAINT formado_por_ibfk_1 FOREIGN KEY (ID_PROYECTO) REFERENCES PROYECTO (ID_PROYECTO) ON DELETE CASCADE,
                             CONSTRAINT formado_por_ibfk_2 FOREIGN KEY (ID_ACTIVIDAD) REFERENCES ACTIVIDAD (ID_ACTIVIDAD) ON DELETE CASCADE
);

CREATE TABLE PARTICIPA (
                           INDEX_MENSAJE int NOT NULL,
                           ID_SENDER int NOT NULL,
                           ID_RECEIVER int NOT NULL,
                           PRIMARY KEY (INDEX_MENSAJE),
                           CONSTRAINT participa_ibfk_1 FOREIGN KEY (INDEX_MENSAJE) REFERENCES MENSAJE (INDEX_MENSAJE) ON DELETE CASCADE,
                           CONSTRAINT participa_ibfk_2 FOREIGN KEY (ID_SENDER) REFERENCES USUARIO (ID_USUARIO) ON DELETE CASCADE,
                           CONSTRAINT participa_ibfk_3 FOREIGN KEY (ID_RECEIVER) REFERENCES USUARIO (ID_USUARIO) ON DELETE CASCADE
);

DELIMITER //
CREATE PROCEDURE AssignProjectAndRejectOthers(
    IN targetPractitionerIdentifier INT,
    IN targetProjectIdentifier INT
)
BEGIN
START TRANSACTION;

UPDATE POSTULACION_PROYECTO
SET ESTADO_POSTULACION = 'Asignado'
WHERE ID_PRACTICANTE = targetPractitionerIdentifier AND ID_PROYECTO = targetProjectIdentifier;

UPDATE POSTULACION_PROYECTO
SET ESTADO_POSTULACION = 'Rechazado'
WHERE ID_PRACTICANTE = targetPractitionerIdentifier AND ID_PROYECTO != targetProjectIdentifier;

COMMIT;
END //
DELIMITER ;