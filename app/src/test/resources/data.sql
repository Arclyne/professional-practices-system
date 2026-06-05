-- -----------------------------------------------------
-- INSERCIÓN DE DATOS DE PRUEBA (Mapeados al nuevo Schema)
-- Estos datos son críticos para que DAOTest pasen
-- -----------------------------------------------------

-- 1. Insertar ROLES
INSERT INTO role (role_name, description) VALUES
                                              ('Administrator', 'Administrador global del sistema'),
                                              ('Coordinator', 'Coordinador de practicas profesionales'),
                                              ('Professor', 'Profesor a cargo de un grupo escolar'),
                                              ('Practitioner', 'Estudiante de practicas profesionales');

-- 2. Insertar USUARIOS (Mapeando los de tus pruebas)
INSERT INTO user (user_id, username, password, name, last_name, email, role_name, status, gender) VALUES
                                                                                                      (1, 'S21012345', 'hash_password_segura', 'Josep', 'Prueba', 'josep@gmail.com', 'Practitioner', 'Active', 'Male'),
                                                                                                      (13, '12345', '12345', 'adm', 'adm', 'adm@adm.com', 'Administrator', 'Active', 'Male');

-- 3. Insertar PRACTICANTES
INSERT INTO practitioner (practitioner_id, indigenous_language, grade) VALUES
    (1, 'Ninguna', 9.5);

-- 4. Insertar ORGANIZACIONES VINCULADAS
INSERT INTO linked_organization (organization_id, organization_name, status, sector, email) VALUES
                                                                                                (1, 'toRecover', 'Active', 'Technology', 'torecover@uv.mx'),
                                                                                                (2, 'Dummy 1', 'Active', 'Technology', 'dummy1@uv.mx'),
                                                                                                (3, 'Dummy 2', 'Active', 'Technology', 'dummy2@uv.mx');

-- 5. Insertar TOKENS (El test de AuthenticationToken asume que el token_value=123456 y username='S21012345')
INSERT INTO access_token (token_value, username) VALUES
    (123456, 'S21012345');

-- 6. Insertar MANEJADORES DE PROYECTOS
INSERT INTO project_manager (manager_id, manager_name, organization_id, status) VALUES
                                                                                    (1, 'toRecover', 1, 'Active'),
                                                                                    (2, 'Dummy 1', 2, 'Active'),
                                                                                    (3, 'Dummy 2', 3, 'Active');

-- 7. Insertar PROYECTOS
INSERT INTO project (project_id, project_name, description, participant_capacity, manager_id, status, start_date, end_date, organization_id) VALUES
                                                                                                                                                 (1, 'toRecover', 'Project for recovery test', 2, 1, 'Active', '2026-01-01', '2026-06-01', 1),
                                                                                                                                                 (2, 'Dummy 1', 'First dummy project', 3, 2, 'Active', '2026-01-01', '2026-06-01', 2),
                                                                                                                                                 (3, 'Dummy 2', 'Second dummy project', 1, 3, 'Active', '2026-01-01', '2026-06-01', 3);

-- 8. Insertar ACTIVIDADES
INSERT INTO activity (activity_id, practitioner_id, title, description, activity_date, duration_hours) VALUES
                                                                                                           (1, 1, 'toRecover', 'Descripción toRecover', '2026-05-01', 5),
                                                                                                           (2, 1, 'Dummy 1', 'Descripción Dummy 1', '2026-05-02', 4),
                                                                                                           (3, 1, 'Dummy 2', 'Descripción Dummy 2', '2026-05-03', 3);