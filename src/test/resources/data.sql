INSERT INTO USUARIO (ID_USUARIO, PASSWORD, NOMBRE, APELLIDOS, ESTADO, GENERO) 
VALUES (1, 'hash_password_segura', 'Josep', 'Prueba', 'Activo', 'Masculino');

INSERT INTO PRACTICANTE (ID_PRACTICANTE, LENGUA_INDIGENA, CALIFICACION) 
VALUES (1, 'Ninguna', 9.5);

INSERT INTO ORGANIZACION_VINCULADA (ID_ORGANIZACION, NOMBRE_ORGANIZACION, ESTADO, SECTOR, CORREO) VALUES 
(1, 'toRecover', 'Veracruz', 'Technology', 'torecover@uv.mx'),
(2, 'Dummy 1', 'Veracruz', 'Technology', 'dummy1@uv.mx'),
(3, 'Dummy 2', 'Veracruz', 'Technology', 'dummy2@uv.mx');

INSERT INTO ACCESS_TOKEN (TOKEN_VALUE, ID_USUARIO) VALUES (123456, 1);

INSERT INTO ACTIVIDAD (ID_ACTIVIDAD, NOMBRE, ENCARGADO) VALUES 
(1, 'toRecover', 'toRecover'),
(2, 'Dummy 1', 'Dummy 1'),
(3, 'Dummy 2', 'Dummy 2');

INSERT INTO PROYECTO (ID_PROYECTO, NOMBRE_PROYECTO, DESCRIPCION, CUPO_PARTICIPANTES, ENCARGADO, ESTADO, FECHA_INICIO, FECHA_END, ID_ORGANIZACION) VALUES 
(1, 'toRecover', 'Project for recovery test', 2, 'toRecover', 'Active', '2026-01-01', '2026-06-01', 1),
(2, 'Dummy 1', 'First dummy project', 3, 'Dummy 1', 'Active', '2026-01-01', '2026-06-01', 1),
(3, 'Dummy 2', 'Second dummy project', 1, 'Dummy 2', 'Active', '2026-01-01', '2026-06-01', 1);