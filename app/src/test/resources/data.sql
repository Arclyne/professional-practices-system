-- -----------------------------------------------------
-- INSERCIÓN DE DATOS DE PRUEBA
-- Alineado con el dump MySQL real (dump-202606051734).
-- La tabla de roles se llama "role" (no "roles").
-- MERGE para roles porque sobreviven entre @BeforeEach.
-- INSERT para el resto: el schema hace DROP+CREATE en
-- cada initialize(), limpiando las tablas automáticamente.
-- -----------------------------------------------------

-- 1. ROLES — tabla "role" (alineada con MySQL)
MERGE INTO role (role_name, description) KEY (role_name) VALUES
    ('Administrator', 'Administrator of the Professional Practices app');
MERGE INTO role (role_name, description) KEY (role_name) VALUES
    ('Coordinator', 'Coordinator role for professional practices');
MERGE INTO role (role_name, description) KEY (role_name) VALUES
    ('Professor', 'Professor role');
MERGE INTO role (role_name, description) KEY (role_name) VALUES
    ('Practitioner', 'Student role');

-- 2. USUARIOS base
--    user_id 13  → administrador (Administrator)
--    user_id 67  → coordinador (Coordinator) — para CoordinatorDAOTest
--    user_id 68  → profesor base — para PracticeGroupDAOTest / ProfessorDAOTest
--    user_id 123 → practicante activo — para ActivityDAOTest / AuthenticationTokenDAOTest
INSERT INTO user (user_id, username, password, name, last_name, email, role_name, status, gender) VALUES
                                                                                                      (13,  '12345',      '12345', 'adm',   'adm',              'adm@adm.com',     'Administrator', 'Active', 'Male'),
                                                                                                      (67,  'coord1',     '12345', 'Coord', 'Test',             'coord1@uv.mx',    'Coordinator',   'Active', 'Male'),
                                                                                                      (68,  'prof1',      '12345', 'Prof',  'Test',             'prof1@uv.mx',     'Professor',     'Active', 'Male'),
                                                                                                      (123, 'zS24242424', '12345', 'Angel', 'Aguilar',          'angel24@gmail.com','Practitioner', 'Active', 'Male'),
                                                                                                      (14,  'test',       'password', 'Test', 'User',           'test@uv.mx',      'Practitioner',  'Active', 'Male');

-- 3. ROLES ESPECIALIZADOS
INSERT INTO administrator (administrator_id)  VALUES (13);
INSERT INTO coordinator   (coordinator_id)    VALUES (67);
INSERT INTO professor     (professor_id)      VALUES (68);

-- 4. PERIODO ESCOLAR — necesario para PracticeGroupDAOTest (FK desde practice_group)
INSERT INTO school_period (period_id, period_name, start_date, end_date, period_status) VALUES
    (5, 'Junio-Diciembre 2026', '2026-06-01', '2026-12-12', 'Active');

-- 5. GRUPO DE PRÁCTICA — necesario para practitioner (FK desde practitioner.group_id)
INSERT INTO practice_group (group_id, section, period_id, professor_id) VALUES
    (6, 'Seccion G', 5, 68);

-- 6. PRACTICANTE — FK hacia user_id=123 y group_id=6
INSERT INTO practitioner (practitioner_id, indigenous_language, grade, group_id) VALUES
    (123, 'Ninguna', 0.00, 6);

-- 7. ORGANIZACIONES VINCULADAS — usadas por OrganizationDAOTest
INSERT INTO linked_organization (organization_id, organization_name, status, sector, email) VALUES
                                                                                                (1, 'toRecover', 'Active', 'Technology', 'torecover@uv.mx'),
                                                                                                (2, 'Dummy 1',   'Active', 'Technology', 'dummy1@uv.mx'),
                                                                                                (3, 'Dummy 2',   'Active', 'Technology', 'dummy2@uv.mx');

-- 8. TOKEN DE ACCESO — AuthenticationTokenDAOTest espera token_value=123456, username='test'
INSERT INTO access_token (token_value, username) VALUES
    (123456, 'test');

-- 9. MANEJADORES DE PROYECTO — requeridos por columna manager_id en project
INSERT INTO project_manager (manager_id, manager_name, organization_id, status) VALUES
                                                                                    (1, 'Manager toRecover', 1, 'Active'),
                                                                                    (2, 'Manager Dummy 1',   2, 'Active'),
                                                                                    (3, 'Manager Dummy 2',   3, 'Active');

-- 10. PROYECTOS — usados por ProjectDAOTest
INSERT INTO project (project_id, project_name, description, participant_capacity, manager_id, status, start_date, end_date, organization_id) VALUES
                                                                                                                                                 (1, 'toRecover', 'Project for recovery test', 2, 1, 'Active', '2026-01-01', '2026-06-01', 1),
                                                                                                                                                 (2, 'Dummy 1',   'First dummy project',        3, 2, 'Active', '2026-01-01', '2026-06-01', 2),
                                                                                                                                                 (3, 'Dummy 2',   'Second dummy project',       1, 3, 'Active', '2026-01-01', '2026-06-01', 3);

-- 11. ACTIVIDADES — ActivityDAOTest necesita actividades para practitioner_id=123
INSERT INTO activity (activity_id, practitioner_id, title, description, activity_date, duration_hours) VALUES
                                                                                                           (1, 123, 'toRecover', 'Descripcion toRecover', '2026-05-01', 5),
                                                                                                           (2, 123, 'Dummy 1',   'Descripcion Dummy 1',   '2026-05-02', 4),
                                                                                                           (3, 123, 'Dummy 2',   'Descripcion Dummy 2',   '2026-05-03', 3);


INSERT INTO monthly_report (report_id, practitioner_id, month_name, "year", start_date, end_date, status)
VALUES (1, 123, 'Mayo', 2026, '2026-05-01', '2026-05-31', 'Borrador');