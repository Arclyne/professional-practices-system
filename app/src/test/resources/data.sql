MERGE INTO role (role_name, description) KEY (role_name) VALUES
    ('Administrator', 'Administrator of the Professional Practices app'),
    ('Coordinator', 'Coordinator role for professional practices'),
    ('Professor', 'Professor role'),
    ('Practitioner', 'Student role');

INSERT INTO user (user_id, username, password, name, last_name, email, role_name, status, gender) VALUES
    (13,  '30011111',   'AdminFei2026',     'Ricardo',       'Marquez Sosa',       'rmarquez@uv.mx',               'Administrator', 'Active', 'Male'),
    (67,  '30022222',   'CoordFei2026',     'Marco Antonio', 'Rodriguez Castillo', 'mrodriguez@uv.mx',             'Coordinator',   'Active', 'Male'),
    (68,  '30033333',   'ProfeFei2026',     'Jose Eduardo',  'Prior Hernandez',    'eprior@uv.mx',                 'Professor',     'Active', 'Male'),
    (123, 'zS24242424', 'PracticasUv2026',  'Angel Gabriel', 'Aguilar Hernandez',  'zS24242424@estudiantes.uv.mx', 'Practitioner',  'Active', 'Male'),
    (14,  'zS23151617', 'EstudianteUv2026', 'Daniela',       'Morales Vazquez',    'zS23151617@estudiantes.uv.mx', 'Practitioner',  'Active', 'Female');

INSERT INTO administrator (administrator_id)  VALUES (13);
INSERT INTO coordinator   (coordinator_id)    VALUES (67);
INSERT INTO professor     (professor_id)      VALUES (68);

INSERT INTO school_period (period_id, period_name, start_date, end_date, period_status) VALUES
    (5, 'Junio-Diciembre 2026', '2026-06-01', '2026-12-12', 'Active');

INSERT INTO practice_group (group_id, section, period_id, professor_id) VALUES
    (6, 'Seccion 601', 5, 68);

INSERT INTO practitioner (practitioner_id, indigenous_language, grade, group_id, reports_access_granted) VALUES
    (123, 'Ninguna', 0.00, 6, TRUE);

INSERT INTO linked_organization (organization_id, organization_name, status, sector, email) VALUES
    (1, 'Tecnologias Web del Golfo',  'Active', 'Technology', 'contacto@tecgolfo.mx'),
    (2, 'Consultoria Digital Xalapa', 'Active', 'Technology', 'contacto@cdxalapa.mx'),
    (3, 'Software Veracruzano',       'Active', 'Technology', 'contacto@softver.mx');

INSERT INTO access_token (token_value, username) VALUES (123456, 'zS23151617');

INSERT INTO project_manager (manager_id, manager_name, organization_id, status) VALUES
    (1, 'Roberto Sanchez Luna',         1, 'Active'),
    (2, 'Maria Fernanda Ortiz Cabrera', 2, 'Active'),
    (3, 'Carlos Dominguez Ruiz',        3, 'Active');

INSERT INTO project (project_id, project_name, description, participant_capacity, manager_id, status, start_date, end_date, organization_id) VALUES
    (1, 'Sistema de Inventario Web',  'Desarrollo de un sistema web para el control de inventario',     2, 1, 'Active', '2026-01-01', '2026-06-01', 1),
    (2, 'Aplicacion Movil de Ventas', 'Desarrollo de una aplicacion movil para la gestion de ventas',   3, 2, 'Active', '2026-01-01', '2026-06-01', 2),
    (3, 'Portal de Recursos Humanos', 'Mantenimiento del portal interno de recursos humanos',           1, 3, 'Active', '2026-01-01', '2026-06-01', 3);

INSERT INTO monthly_report (report_id, practitioner_id, month_name, "year", start_date, end_date, status) VALUES
    (1, 123, 'Mayo', 2026, '2026-05-01', '2026-05-31', 'Borrador');

INSERT INTO activity (activity_id, practitioner_id, report_id, title, description, activity_date, duration_hours) VALUES
    (1, 123, 1,    'Levantamiento de requisitos',          'Entrevistas con el personal del area de sistemas',             '2026-05-01', 5),
    (2, 123, NULL, 'Desarrollo de pantallas de inventario', 'Maquetado de las pantallas del sistema de inventario',        '2026-05-02', 4),
    (3, 123, NULL, 'Pruebas de formularios de inventario',  'Pruebas funcionales sobre los formularios de captura',        '2026-05-03', 3),
    (4, 123, NULL, 'Manual de usuario del sistema',         'Avance del manual de usuario para el personal de la empresa', '2026-06-15', 5);

INSERT INTO project_postulation (practitioner_id, project_id, postulation_status, priority_level) VALUES
    (123, 1, 'Assigned', 1);

INSERT INTO progress_report (report_id, practitioner_id, report_type, generation_date, period_covered_start, period_covered_end, total_hours_at_submission, status) VALUES
    (1, 123, 'Final', '2026-06-01', '2026-01-01', '2026-06-01', 480.0, 'Pendiente de Firma'),
    (2, 123, 'Final', '2026-06-01', '2026-01-01', '2026-06-01', 480.0, 'Pendiente de Firma');

INSERT INTO self_evaluation (self_eval_id, q1, q2, q3, q4, q5, q6, q7, q8, q9, q10, evidence, practitioner_id, report_id, status) VALUES
    (1, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 'https://storage.uv.mx/evidencias/autoevaluacion_zS24242424.pdf', 123, 1, 'Pendiente');

INSERT INTO practitioner_document (document_id, practitioner_id, document_name, document_type, stored_file_url, status, upload_date, review_date) VALUES
    (1, 123, 'carta_aceptacion.pdf', 'Inicial', 'file:///SimuladorOneDrive_FEI/a1b2c3d4_carta_aceptacion.pdf', 'Pendiente', '2026-06-10 09:15:00', NULL),
    (2, 123, 'carta_aceptacion.pdf', 'Inicial', 'file:///SimuladorOneDrive_FEI/e5f6a7b8_carta_aceptacion.pdf', 'Aceptado',  '2026-06-12 11:30:00', '2026-06-13 08:00:00');
