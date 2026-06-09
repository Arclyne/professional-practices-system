SET REFERENTIAL_INTEGRITY FALSE;

DROP ALIAS IF EXISTS assign_project_and_reject_others;

-- ORDEN ESTRICTO DE BORRADO (Hijos primero)
DROP TABLE IF EXISTS practitioner_grade;
DROP TABLE IF EXISTS professor_evaluation;
DROP TABLE IF EXISTS organization_evaluation;
DROP TABLE IF EXISTS self_evaluation;
DROP TABLE IF EXISTS message_participant;
DROP TABLE IF EXISTS message;
DROP TABLE IF EXISTS activity;
DROP TABLE IF EXISTS monthly_report;
DROP TABLE IF EXISTS project_postulation;
DROP TABLE IF EXISTS project_acceptance;
DROP TABLE IF EXISTS project;
DROP TABLE IF EXISTS project_manager;
DROP TABLE IF EXISTS progress_report;
DROP TABLE IF EXISTS practitioner;
DROP TABLE IF EXISTS practice_group;
DROP TABLE IF EXISTS school_period;
DROP TABLE IF EXISTS professor;
DROP TABLE IF EXISTS coordinator;
DROP TABLE IF EXISTS administrator;
DROP TABLE IF EXISTS access_token;
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS role;
DROP TABLE IF EXISTS linked_organization;

SET REFERENTIAL_INTEGRITY TRUE;

-- TABLAS BASE
CREATE TABLE role (role_name VARCHAR(50) NOT NULL, description VARCHAR(255) DEFAULT NULL, PRIMARY KEY (role_name));
CREATE TABLE user (user_id INT NOT NULL AUTO_INCREMENT, username VARCHAR(20) NOT NULL UNIQUE, password VARCHAR(255) NOT NULL, name VARCHAR(100) NOT NULL, last_name VARCHAR(100) NOT NULL, email VARCHAR(150) NOT NULL UNIQUE, role_name VARCHAR(50) NOT NULL, status VARCHAR(20) NOT NULL CHECK (status IN ('Active', 'Inactive', 'Pending')), gender VARCHAR(20) DEFAULT NULL CHECK (gender IN ('Male', 'Female', 'Other')), registration_date DATETIME DEFAULT CURRENT_TIMESTAMP, discharge_date DATETIME DEFAULT NULL, PRIMARY KEY (user_id), CONSTRAINT fk_user_role FOREIGN KEY (role_name) REFERENCES role (role_name) ON DELETE RESTRICT);
CREATE TABLE access_token (token_value INT NOT NULL, creation_time DATETIME DEFAULT CURRENT_TIMESTAMP, username VARCHAR(20) NOT NULL, PRIMARY KEY (token_value), CONSTRAINT fk_access_token_user FOREIGN KEY (username) REFERENCES user (username) ON DELETE CASCADE);
CREATE TABLE administrator (administrator_id INT NOT NULL, PRIMARY KEY (administrator_id), CONSTRAINT fk_administrator_user FOREIGN KEY (administrator_id) REFERENCES user (user_id) ON DELETE CASCADE);
CREATE TABLE coordinator (coordinator_id INT NOT NULL, PRIMARY KEY (coordinator_id), CONSTRAINT fk_coordinator_user FOREIGN KEY (coordinator_id) REFERENCES user (user_id) ON DELETE CASCADE);
CREATE TABLE professor (professor_id INT NOT NULL, PRIMARY KEY (professor_id), CONSTRAINT fk_professor_user FOREIGN KEY (professor_id) REFERENCES user (user_id) ON DELETE CASCADE);
CREATE TABLE school_period (period_id INT NOT NULL AUTO_INCREMENT, period_name VARCHAR(50) NOT NULL, start_date DATE DEFAULT NULL, end_date DATE DEFAULT NULL, period_status VARCHAR(20) NOT NULL CHECK (period_status IN ('Active', 'Concluded', 'Upcoming')), PRIMARY KEY (period_id));
CREATE TABLE practice_group (group_id INT NOT NULL AUTO_INCREMENT, section VARCHAR(50) NOT NULL, period_id INT NOT NULL, professor_id INT NOT NULL, PRIMARY KEY (group_id), CONSTRAINT fk_group_period FOREIGN KEY (period_id) REFERENCES school_period (period_id) ON DELETE CASCADE, CONSTRAINT fk_group_professor FOREIGN KEY (professor_id) REFERENCES professor (professor_id) ON DELETE CASCADE);
CREATE TABLE practitioner (practitioner_id INT NOT NULL, indigenous_language VARCHAR(100) DEFAULT NULL, grade DECIMAL(5, 2) DEFAULT NULL, group_id INT DEFAULT NULL, PRIMARY KEY (practitioner_id), CONSTRAINT fk_practitioner_user FOREIGN KEY (practitioner_id) REFERENCES user (user_id) ON DELETE CASCADE, CONSTRAINT fk_practitioner_practice_group FOREIGN KEY (group_id) REFERENCES practice_group (group_id) ON DELETE SET NULL);
CREATE TABLE linked_organization (organization_id INT NOT NULL AUTO_INCREMENT, organization_name VARCHAR(150) NOT NULL, status VARCHAR(50) DEFAULT NULL, address VARCHAR(255) DEFAULT NULL, city VARCHAR(100) DEFAULT NULL, sector VARCHAR(100) DEFAULT NULL, email VARCHAR(150) DEFAULT NULL UNIQUE, phone VARCHAR(20) DEFAULT NULL, PRIMARY KEY (organization_id));
CREATE TABLE project_manager (manager_id INT NOT NULL AUTO_INCREMENT, manager_name VARCHAR(150) NOT NULL, phone VARCHAR(20) DEFAULT NULL, email VARCHAR(150) DEFAULT NULL, organization_id INT DEFAULT NULL, status VARCHAR(20) DEFAULT 'Active' CHECK (status IN ('Active', 'Inactive')), PRIMARY KEY (manager_id), CONSTRAINT fk_manager_organization FOREIGN KEY (organization_id) REFERENCES linked_organization (organization_id));
CREATE TABLE project (project_id INT NOT NULL AUTO_INCREMENT, project_name VARCHAR(200) NOT NULL, description TEXT, participant_capacity INT NOT NULL, manager_id INT NOT NULL, status VARCHAR(50) DEFAULT 'Active', start_date DATE DEFAULT NULL, end_date DATE DEFAULT NULL, organization_id INT NOT NULL, PRIMARY KEY (project_id), CONSTRAINT fk_project_organization FOREIGN KEY (organization_id) REFERENCES linked_organization (organization_id) ON DELETE CASCADE);
CREATE TABLE project_acceptance (coordinator_id INT NOT NULL, project_id INT NOT NULL, PRIMARY KEY (coordinator_id, project_id), CONSTRAINT fk_acceptance_coordinator FOREIGN KEY (coordinator_id) REFERENCES coordinator (coordinator_id) ON DELETE CASCADE, CONSTRAINT fk_acceptance_project FOREIGN KEY (project_id) REFERENCES project (project_id) ON DELETE CASCADE);
CREATE TABLE project_postulation (postulation_id INT NOT NULL AUTO_INCREMENT, practitioner_id INT NOT NULL, project_id INT NOT NULL, priority_level INT NOT NULL, postulation_date DATETIME DEFAULT CURRENT_TIMESTAMP, postulation_status VARCHAR(20) DEFAULT 'Pending', PRIMARY KEY (postulation_id), CONSTRAINT fk_postulation_practitioner FOREIGN KEY (practitioner_id) REFERENCES practitioner (practitioner_id) ON DELETE CASCADE, CONSTRAINT fk_postulation_project FOREIGN KEY (project_id) REFERENCES project (project_id) ON DELETE CASCADE);
CREATE TABLE monthly_report (report_id INT NOT NULL AUTO_INCREMENT, practitioner_id INT NOT NULL, month_name VARCHAR(20) NOT NULL, year INT NOT NULL, start_date DATE NOT NULL, end_date DATE NOT NULL, grade DECIMAL(5, 2) DEFAULT NULL, professor_feedback TEXT, status VARCHAR(50) DEFAULT 'Borrador', signed_file_url VARCHAR(500) DEFAULT NULL, PRIMARY KEY (report_id), CONSTRAINT fk_monthly_report_practitioner FOREIGN KEY (practitioner_id) REFERENCES practitioner (practitioner_id) ON DELETE CASCADE);
CREATE TABLE activity (activity_id INT NOT NULL AUTO_INCREMENT, practitioner_id INT NOT NULL, report_id INT DEFAULT NULL, title VARCHAR(255) NOT NULL, description TEXT, activity_date DATE NOT NULL, duration_hours INT DEFAULT '0', PRIMARY KEY (activity_id), CONSTRAINT fk_activity_practitioner FOREIGN KEY (practitioner_id) REFERENCES practitioner (practitioner_id) ON DELETE CASCADE, CONSTRAINT fk_activity_report FOREIGN KEY (report_id) REFERENCES monthly_report (report_id) ON DELETE SET NULL);
CREATE TABLE message (message_id INT NOT NULL AUTO_INCREMENT, send_date DATETIME DEFAULT CURRENT_TIMESTAMP, subject VARCHAR(255) NOT NULL DEFAULT 'Notificacion del Sistema', body TEXT NOT NULL, PRIMARY KEY (message_id));
CREATE TABLE message_participant (message_id INT NOT NULL, sender_id INT NOT NULL, receiver_id INT NOT NULL, PRIMARY KEY (message_id), CONSTRAINT fk_participant_message FOREIGN KEY (message_id) REFERENCES message (message_id) ON DELETE CASCADE, CONSTRAINT fk_participant_sender FOREIGN KEY (sender_id) REFERENCES user (user_id) ON DELETE CASCADE, CONSTRAINT fk_participant_receiver FOREIGN KEY (receiver_id) REFERENCES user (user_id) ON DELETE CASCADE);

CREATE TABLE progress_report (report_id INT NOT NULL AUTO_INCREMENT, practitioner_id INT NOT NULL, report_type VARCHAR(20) NOT NULL, generation_date DATE NOT NULL, period_covered_start DATE NOT NULL, period_covered_end DATE NOT NULL, total_hours_at_submission DECIMAL(6,2) NOT NULL, status VARCHAR(50) DEFAULT 'Pendiente de Firma', signed_file_url VARCHAR(500) DEFAULT NULL, grade DECIMAL(5,2) DEFAULT NULL, professor_feedback TEXT, PRIMARY KEY (report_id), CONSTRAINT fk_progress_practitioner FOREIGN KEY (practitioner_id) REFERENCES practitioner (practitioner_id) ON DELETE CASCADE);
CREATE TABLE self_evaluation (
                                 self_eval_id    INT           NOT NULL AUTO_INCREMENT,
                                 q1              INT           NOT NULL,
                                 q2              INT           NOT NULL,
                                 q3              INT           NOT NULL,
                                 q4              INT           NOT NULL,
                                 q5              INT           NOT NULL,
                                 q6              INT           NOT NULL,
                                 q7              INT           NOT NULL,
                                 q8              INT           NOT NULL,
                                 q9              INT           NOT NULL,
                                 q10             INT           NOT NULL,
                                 evidence        TEXT,
                                 practitioner_id INT           NOT NULL,
                                 report_id       INT           NOT NULL,
                                 status          VARCHAR(50)   DEFAULT 'Pendiente',
                                 PRIMARY KEY (self_eval_id),
                                 CONSTRAINT uq_selfeval_report UNIQUE (report_id),
                                 CONSTRAINT fk_selfeval_practitioner FOREIGN KEY (practitioner_id) REFERENCES practitioner (practitioner_id) ON DELETE CASCADE,
                                 CONSTRAINT fk_selfeval_report       FOREIGN KEY (report_id)       REFERENCES progress_report (report_id)    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE organization_evaluation (org_eval_id INT NOT NULL AUTO_INCREMENT, period VARCHAR(50) NOT NULL, grade DECIMAL(5, 2) NOT NULL, description TEXT, practitioner_id INT NOT NULL, organization_id INT DEFAULT NULL, PRIMARY KEY (org_eval_id), CONSTRAINT fk_orgeval_practitioner FOREIGN KEY (practitioner_id) REFERENCES practitioner (practitioner_id) ON DELETE CASCADE, CONSTRAINT fk_orgeval_organization FOREIGN KEY (organization_id) REFERENCES linked_organization (organization_id) ON DELETE SET NULL);
CREATE TABLE professor_evaluation (prof_eval_id INT NOT NULL AUTO_INCREMENT, period VARCHAR(50) NOT NULL, grade DECIMAL(5, 2) NOT NULL, description TEXT, practitioner_id INT NOT NULL, professor_id INT DEFAULT NULL, PRIMARY KEY (prof_eval_id), CONSTRAINT fk_profeval_practitioner FOREIGN KEY (practitioner_id) REFERENCES practitioner (practitioner_id) ON DELETE CASCADE, CONSTRAINT fk_profeval_professor FOREIGN KEY (professor_id) REFERENCES professor (professor_id) ON DELETE SET NULL);
CREATE TABLE practitioner_grade (grade_id INT NOT NULL AUTO_INCREMENT, practitioner_id INT NOT NULL, professor_id INT DEFAULT NULL, tentative_grade DECIMAL(5,2) NOT NULL, final_grade DECIMAL(5,2) DEFAULT NULL, period VARCHAR(100) NOT NULL, graded_at DATETIME DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (grade_id), CONSTRAINT uq_grade_period UNIQUE (practitioner_id, period), CONSTRAINT fk_grade_practitioner FOREIGN KEY (practitioner_id) REFERENCES practitioner (practitioner_id) ON DELETE CASCADE, CONSTRAINT fk_grade_professor FOREIGN KEY (professor_id) REFERENCES professor (professor_id) ON DELETE SET NULL);

CREATE ALIAS assign_project_and_reject_others AS $$
void assignProject(Connection conn, int targetPractitionerId, int targetProjectId) throws SQLException {
    conn.setAutoCommit(false);
    try (PreparedStatement ps1 = conn.prepareStatement("UPDATE project_postulation SET postulation_status = 'Assigned' WHERE practitioner_id = ? AND project_id = ?"); PreparedStatement ps2 = conn.prepareStatement("UPDATE project_postulation SET postulation_status = 'Rejected' WHERE practitioner_id = ? AND project_id != ?")) {
        ps1.setInt(1, targetPractitionerId); ps1.setInt(2, targetProjectId); ps1.executeUpdate();
        ps2.setInt(1, targetPractitionerId); ps2.setInt(2, targetProjectId); ps2.executeUpdate();
        conn.commit();
} catch (SQLException e) { conn.rollback(); throw e; }
}
$$;