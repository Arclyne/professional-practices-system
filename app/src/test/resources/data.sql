MERGE INTO role (role_name, description) KEY (role_name) VALUES
    ('Administrator', 'Administrator of the Professional Practices app'),
    ('Coordinator', 'Coordinator role for professional practices'),
    ('Professor', 'Professor role'),
    ('Practitioner', 'Student role');

INSERT INTO user (user_id, username, password, name, last_name, email, role_name, status, gender) VALUES
                                                                                                      (13,  '12345',      '12345', 'adm',   'adm',              'adm@adm.com',     'Administrator', 'Active', 'Male'),
                                                                                                      (67,  'coord1',     '12345', 'Coord', 'Test',             'coord1@uv.mx',    'Coordinator',   'Active', 'Male'),
                                                                                                      (68,  'prof1',      '12345', 'Prof',  'Test',             'prof1@uv.mx',     'Professor',     'Active', 'Male'),
                                                                                                      (123, 'zS24242424', '12345', 'Angel', 'Aguilar',          'angel24@gmail.com','Practitioner', 'Active', 'Male'),
                                                                                                      (14,  'test',       'password', 'Test', 'User',           'test@uv.mx',      'Practitioner',  'Active', 'Male');

INSERT INTO administrator (administrator_id)  VALUES (13);
INSERT INTO coordinator   (coordinator_id)    VALUES (67);
INSERT INTO professor     (professor_id)      VALUES (68);

INSERT INTO school_period (period_id, period_name, start_date, end_date, period_status) VALUES
    (5, 'Junio-Diciembre 2026', '2026-06-01', '2026-12-12', 'Active');

INSERT INTO practice_group (group_id, section, period_id, professor_id) VALUES
    (6, 'Seccion G', 5, 68);

INSERT INTO practitioner (practitioner_id, indigenous_language, grade, group_id) VALUES
    (123, 'Ninguna', 0.00, 6);

INSERT INTO linked_organization (organization_id, organization_name, status, sector, email) VALUES
                                                                                                (1, 'toRecover', 'Active', 'Technology', 'torecover@uv.mx'),
                                                                                                (2, 'Dummy 1',   'Active', 'Technology', 'dummy1@uv.mx'),
                                                                                                (3, 'Dummy 2',   'Active', 'Technology', 'dummy2@uv.mx');

INSERT INTO access_token (token_value, username) VALUES (123456, 'test');

INSERT INTO project_manager (manager_id, manager_name, organization_id, status) VALUES
                                                                                    (1, 'Manager toRecover', 1, 'Active'),
                                                                                    (2, 'Manager Dummy 1',   2, 'Active'),
                                                                                    (3, 'Manager Dummy 2',   3, 'Active');

INSERT INTO project (project_id, project_name, description, participant_capacity, manager_id, status, start_date, end_date, organization_id) VALUES
                                                                                                                                                 (1, 'toRecover', 'Project for recovery test', 2, 1, 'Active', '2026-01-01', '2026-06-01', 1),
                                                                                                                                                 (2, 'Dummy 1',   'First dummy project',        3, 2, 'Active', '2026-01-01', '2026-06-01', 2),
                                                                                                                                                 (3, 'Dummy 2',   'Second dummy project',       1, 3, 'Active', '2026-01-01', '2026-06-01', 3);

INSERT INTO activity (activity_id, practitioner_id, title, description, activity_date, duration_hours) VALUES
                                                                                                           (1, 123, 'toRecover', 'Descripcion toRecover', '2026-05-01', 5),
                                                                                                           (2, 123, 'Dummy 1',   'Descripcion Dummy 1',   '2026-05-02', 4),
                                                                                                           (3, 123, 'Dummy 2',   'Descripcion Dummy 2',   '2026-05-03', 3),
                                                                                                           (4, 123, 'Actividad Junio Valida', 'Descripcion Junio', '2026-06-15', 5);

INSERT INTO monthly_report (report_id, practitioner_id, month_name, "year", start_date, end_date, status)
VALUES (1, 123, 'Mayo', 2026, '2026-05-01', '2026-05-31', 'Borrador');

-- REPORTE 1: Para pruebas de actualizar/recuperar
INSERT INTO progress_report (report_id, practitioner_id, report_type, generation_date, period_covered_start, period_covered_end, total_hours_at_submission, status)
VALUES (1, 123, 'Final', '2026-06-01', '2026-01-01', '2026-06-01', 480.0, 'Pendiente de Firma');

-- REPORTE 2: "Virgen" para la prueba de registrar nueva evaluación
INSERT INTO progress_report (report_id, practitioner_id, report_type, generation_date, period_covered_start, period_covered_end, total_hours_at_submission, status)
VALUES (2, 123, 'Final', '2026-06-01', '2026-01-01', '2026-06-01', 480.0, 'Pendiente de Firma');

-- EVALUACIÓN LIGADA AL REPORTE 1 (Para markAsReviewed y submitEvidence)
INSERT INTO self_evaluation (self_eval_id, description, evidence, practitioner_id, report_id, status)
VALUES (1, 'Cumplimiento excelente', 'http://pdf.url', 123, 1, 'Pendiente');