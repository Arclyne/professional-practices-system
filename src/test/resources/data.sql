-- 1. Insertar en USUARIO (Asegúrate de incluir CORREO y que el orden sea el correcto)
INSERT INTO USUARIO (ID_USUARIO, PASSWORD, NOMBRE, APELLIDOS, CORREO, ESTADO, GENERO)
VALUES (1, 'hash_password_segura', 'Josep', 'Prueba', 'josep@gmail.com', 'Activo', 'Masculino');

-- 2. Insertar en PRACTICANTE (Se debe incluir la MATRICULA)
INSERT INTO PRACTICANTE (ID_PRACTICANTE, MATRICULA, LENGUA_INDIGENA, CALIFICACION)
VALUES (1, 'S21012345', 'Ninguna', 9.5);

-- 3. Insertar Organizaciones (Se mantiene igual, ya que el esquema no cambió significativamente aquí)
INSERT INTO ORGANIZACION_VINCULADA (ID_ORGANIZACION, NOMBRE_ORGANIZACION, ESTADO, SECTOR, CORREO) VALUES
(1, 'toRecover', 'Veracruz', 'Technology', 'torecover@uv.mx'),
(2, 'Dummy 1', 'Veracruz', 'Technology', 'dummy1@uv.mx'),
(3, 'Dummy 2', 'Veracruz', 'Technology', 'dummy2@uv.mx');

-- 4. Insertar Token de Acceso
INSERT INTO ACCESS_TOKEN (TOKEN_VALUE, ID_USUARIO) VALUES (123456, 1);

-- 5. Insertar Actividades
INSERT INTO ACTIVIDAD (ID_ACTIVIDAD, NOMBRE, ENCARGADO) VALUES
                                                            (1, 'toRecover', 'toRecover'),
                                                            (2, 'Dummy 1', 'Dummy 1'),
                                                            (3, 'Dummy 2', 'Dummy 2');

-- 6. Insertar Proyectos (Asegúrate de que las fechas coincidan con el formato DATE de H2)
INSERT INTO PROYECTO (ID_PROYECTO, NOMBRE_PROYECTO, DESCRIPCION, CUPO_PARTICIPANTES, ENCARGADO, ESTADO, FECHA_INICIO, FECHA_END, ID_ORGANIZACION) VALUES
                                                                                                                                                      (1, 'toRecover', 'Project for recovery test', 2, 'toRecover', 'Activo', '2026-01-01', '2026-06-01', 1),
                                                                                                                                                      (2, 'Dummy 1', 'First dummy project', 3, 'Dummy 1', 'Activo', '2026-01-01', '2026-06-01', 1),
                                                                                                                                                      (3, 'Dummy 2', 'Second dummy project', 1, 'Dummy 2', 'Activo', '2026-01-01', '2026-06-01', 1);