SET REFERENTIAL_INTEGRITY FALSE;
DROP ALIAS IF EXISTS AssignProjectAndRejectOthers;
DROP TABLE IF EXISTS project_application;
DROP TABLE IF EXISTS participates;
DROP TABLE IF EXISTS composed_of;
DROP TABLE IF EXISTS professor_evaluation;
DROP TABLE IF EXISTS organization_evaluation;
DROP TABLE IF EXISTS report_log;
DROP TABLE IF EXISTS self_evaluation;
DROP TABLE IF EXISTS accepts;
DROP TABLE IF EXISTS practice_group;
DROP TABLE IF EXISTS project;
DROP TABLE IF EXISTS project_manager;
DROP TABLE IF EXISTS practitioner;
DROP TABLE IF EXISTS professor;
DROP TABLE IF EXISTS coordinator;
DROP TABLE IF EXISTS administrator;
DROP TABLE IF EXISTS message;
DROP TABLE IF EXISTS activity;
DROP TABLE IF EXISTS linked_organization;
DROP TABLE IF EXISTS school_period;
DROP TABLE IF EXISTS access_token;
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS role;
SET REFERENTIAL_INTEGRITY TRUE;

CREATE TABLE role (
                      ROLE_NAME varchar(50) NOT NULL,
                      DESCRIPTION varchar(255) DEFAULT NULL,
                      PRIMARY KEY (ROLE_NAME)
);

CREATE TABLE user (
                      ID_USER int NOT NULL AUTO_INCREMENT,
                      USERNAME varchar(20) NOT NULL UNIQUE,
                      PASSWORD varchar(255) NOT NULL,
                      FIRST_NAME varchar(100) NOT NULL,
                      LAST_NAME varchar(100) NOT NULL,
                      EMAIL varchar(150) NOT NULL UNIQUE,
                      ROLE_NAME varchar(50) NOT NULL,
                      STATUS varchar(20) NOT NULL CHECK (STATUS IN ('Active', 'Inactive', 'Pending')),
                      GENDER varchar(20) DEFAULT NULL,
                      REGISTRATION_DATE datetime DEFAULT CURRENT_TIMESTAMP,
                      TERMINATION_DATE datetime DEFAULT NULL,
                      PRIMARY KEY (ID_USER),
                      CONSTRAINT fk_user_role FOREIGN KEY (ROLE_NAME) REFERENCES role (ROLE_NAME) ON DELETE RESTRICT
);

CREATE TABLE access_token (
                              TOKEN_VALUE int NOT NULL,
                              CREATION_TIME datetime DEFAULT CURRENT_TIMESTAMP,
                              USERNAME varchar(20) NOT NULL,
                              PRIMARY KEY (TOKEN_VALUE),
                              CONSTRAINT access_token_user_USERNAME_fk FOREIGN KEY (USERNAME) REFERENCES user (USERNAME) ON DELETE CASCADE
);

CREATE TABLE school_period (
                               ID_PERIOD int NOT NULL AUTO_INCREMENT,
                               PERIOD_NAME varchar(50) NOT NULL,
                               START_DATE date DEFAULT NULL,
                               END_DATE date DEFAULT NULL,
                               PERIOD_STATUS varchar(20) NOT NULL CHECK (PERIOD_STATUS IN ('active', 'concluded', 'upcoming')),
                               PRIMARY KEY (ID_PERIOD)
);

CREATE TABLE linked_organization (
                                     ID_ORGANIZATION int NOT NULL AUTO_INCREMENT,
                                     ORGANIZATION_NAME varchar(150) NOT NULL,
                                     STATE varchar(50) DEFAULT NULL,
                                     ADDRESS varchar(255) DEFAULT NULL,
                                     CITY varchar(100) DEFAULT NULL,
                                     SECTOR varchar(100) DEFAULT NULL,
                                     EMAIL varchar(150) DEFAULT NULL,
                                     PHONE varchar(20) DEFAULT NULL,
                                     PRIMARY KEY (ID_ORGANIZATION),
                                     UNIQUE KEY EMAIL (EMAIL)
);

CREATE TABLE activity (
                          ID_ACTIVITY int NOT NULL AUTO_INCREMENT,
                          NAME varchar(150) NOT NULL,
                          START_DATE date DEFAULT NULL,
                          END_DATE date DEFAULT NULL,
                          DESCRIPTION text,
                          MANAGER varchar(150) DEFAULT NULL,
                          PRIMARY KEY (ID_ACTIVITY)
);

CREATE TABLE message (
                         MESSAGE_INDEX int NOT NULL AUTO_INCREMENT,
                         SENT_DATE datetime DEFAULT CURRENT_TIMESTAMP,
                         PRIMARY KEY (MESSAGE_INDEX)
);

CREATE TABLE administrator (
                               ID_ADMINISTRATOR int NOT NULL,
                               PRIMARY KEY (ID_ADMINISTRATOR),
                               CONSTRAINT administrator_ibfk_1 FOREIGN KEY (ID_ADMINISTRATOR) REFERENCES user (ID_USER) ON DELETE CASCADE
);

CREATE TABLE coordinator (
                             ID_COORDINADOR int NOT NULL,
                             PRIMARY KEY (ID_COORDINADOR),
                             CONSTRAINT coordinator_ibfk_1 FOREIGN KEY (ID_COORDINADOR) REFERENCES user (ID_USER) ON DELETE CASCADE
);

CREATE TABLE professor (
                           ID_PROFESSOR int NOT NULL,
                           PRIMARY KEY (ID_PROFESSOR),
                           CONSTRAINT professor_ibfk_1 FOREIGN KEY (ID_PROFESSOR) REFERENCES user (ID_USER) ON DELETE CASCADE
);

CREATE TABLE practitioner (
                              ID_PRACTITIONER int NOT NULL,
                              INDIGENOUS_LANGUAGE varchar(100) DEFAULT NULL,
                              GRADE decimal(5,2) DEFAULT NULL,
                              PRIMARY KEY (ID_PRACTITIONER),
                              CONSTRAINT practitioner_ibfk_1 FOREIGN KEY (ID_PRACTITIONER) REFERENCES user (ID_USER) ON DELETE CASCADE
);

CREATE TABLE project_manager (
                                 ID_MANAGER int NOT NULL AUTO_INCREMENT,
                                 MANAGER_NAME varchar(150) NOT NULL,
                                 PHONE varchar(20) DEFAULT NULL,
                                 EMAIL varchar(150) DEFAULT NULL,
                                 ID_ORGANIZATION int DEFAULT NULL,
                                 PRIMARY KEY (ID_MANAGER),
                                 CONSTRAINT FK_MANAGER_ORGANIZATION FOREIGN KEY (ID_ORGANIZATION) REFERENCES linked_organization (ID_ORGANIZATION) ON DELETE SET NULL
);

CREATE TABLE project (
                         ID_PROJECT int NOT NULL AUTO_INCREMENT,
                         PROJECT_NAME varchar(200) NOT NULL,
                         DESCRIPTION text,
                         VACANCIES int NOT NULL,
                         ID_MANAGER int NOT NULL,
                         STATUS varchar(50) DEFAULT 'Active',
                         START_DATE date DEFAULT NULL,
                         END_DATE date DEFAULT NULL,
                         ID_ORGANIZATION int NOT NULL,
                         PRIMARY KEY (ID_PROJECT),
                         CONSTRAINT fk_project_organization FOREIGN KEY (ID_ORGANIZATION) REFERENCES linked_organization (ID_ORGANIZATION) ON DELETE CASCADE,
                         CONSTRAINT fk_project_manager FOREIGN KEY (ID_MANAGER) REFERENCES project_manager (ID_MANAGER) ON DELETE RESTRICT
);

CREATE TABLE project_application (
                                     ID_APPLICATION int NOT NULL AUTO_INCREMENT,
                                     ID_PRACTITIONER int NOT NULL,
                                     ID_PROJECT int NOT NULL,
                                     PRIORITY_LEVEL int NOT NULL,
                                     APPLICATION_DATE datetime DEFAULT CURRENT_TIMESTAMP,
                                     APPLICATION_STATUS varchar(20) DEFAULT 'Pending',
                                     PRIMARY KEY (ID_APPLICATION),
                                     CONSTRAINT fk_application_practitioner FOREIGN KEY (ID_PRACTITIONER) REFERENCES practitioner (ID_PRACTITIONER) ON DELETE CASCADE,
                                     CONSTRAINT fk_application_project FOREIGN KEY (ID_PROJECT) REFERENCES project (ID_PROJECT) ON DELETE CASCADE
);

CREATE TABLE practice_group (
                                GROUP_INDEX int NOT NULL AUTO_INCREMENT,
                                SECTION varchar(50) NOT NULL,
                                ID_PERIOD int NOT NULL,
                                ID_PROFESSOR int NOT NULL,
                                PRIMARY KEY (GROUP_INDEX),
                                CONSTRAINT practice_group_ibfk_1 FOREIGN KEY (ID_PROFESSOR) REFERENCES professor (ID_PROFESSOR) ON DELETE CASCADE,
                                CONSTRAINT practice_group_ibfk_2 FOREIGN KEY (ID_PERIOD) REFERENCES school_period (ID_PERIOD) ON DELETE CASCADE
);

CREATE TABLE accepts (
                         ID_COORDINATOR int NOT NULL,
                         ID_PROJECT int NOT NULL,
                         PRIMARY KEY (ID_COORDINATOR, ID_PROJECT),
                         CONSTRAINT accepts_ibfk_1 FOREIGN KEY (ID_COORDINATOR) REFERENCES coordinator (ID_COORDINADOR) ON DELETE CASCADE,
                         CONSTRAINT accepts_ibfk_2 FOREIGN KEY (ID_PROJECT) REFERENCES project (ID_PROJECT) ON DELETE CASCADE
);

CREATE TABLE self_evaluation (
                                 ID_SELF_EVAL int NOT NULL AUTO_INCREMENT,
                                 PERIOD varchar(50) NOT NULL,
                                 GRADE decimal(5,2) NOT NULL,
                                 DESCRIPTION text,
                                 ID_PRACTITIONER int NOT NULL,
                                 EVIDENCE text,
                                 PRIMARY KEY (ID_SELF_EVAL),
                                 CONSTRAINT self_evaluation_ibfk_1 FOREIGN KEY (ID_PRACTITIONER) REFERENCES practitioner (ID_PRACTITIONER) ON DELETE CASCADE
);

CREATE TABLE report_log (
                            REPORT_INDEX int NOT NULL AUTO_INCREMENT,
                            DATE date NOT NULL,
                            ID_PRACTITIONER int NOT NULL,
                            PRIMARY KEY (REPORT_INDEX),
                            CONSTRAINT report_log_ibfk_1 FOREIGN KEY (ID_PRACTITIONER) REFERENCES practitioner (ID_PRACTITIONER) ON DELETE CASCADE
);

CREATE TABLE organization_evaluation (
                                         ID_EXTERNAL_EVAL int NOT NULL AUTO_INCREMENT,
                                         PERIOD varchar(50) NOT NULL,
                                         GRADE decimal(5,2) NOT NULL,
                                         DESCRIPTION text,
                                         ID_PRACTITIONER int NOT NULL,
                                         ID_ORGANIZATION int DEFAULT NULL,
                                         PRIMARY KEY (ID_EXTERNAL_EVAL),
                                         CONSTRAINT organization_evaluation_ibfk_1 FOREIGN KEY (ID_PRACTITIONER) REFERENCES practitioner (ID_PRACTITIONER) ON DELETE CASCADE,
                                         CONSTRAINT organization_evaluation_ibfk_2 FOREIGN KEY (ID_ORGANIZATION) REFERENCES linked_organization (ID_ORGANIZATION) ON DELETE SET NULL
);

CREATE TABLE professor_evaluation (
                                      ID_EXTERNAL_EVAL int NOT NULL AUTO_INCREMENT,
                                      PERIOD varchar(50) NOT NULL,
                                      GRADE decimal(5,2) NOT NULL,
                                      DESCRIPTION text,
                                      ID_PRACTITIONER int NOT NULL,
                                      ID_PROFESSOR int DEFAULT NULL,
                                      PRIMARY KEY (ID_EXTERNAL_EVAL),
                                      CONSTRAINT professor_evaluation_ibfk_1 FOREIGN KEY (ID_PRACTITIONER) REFERENCES practitioner (ID_PRACTITIONER) ON DELETE CASCADE,
                                      CONSTRAINT professor_evaluation_ibfk_2 FOREIGN KEY (ID_PROFESSOR) REFERENCES professor (ID_PROFESSOR) ON DELETE SET NULL
);

CREATE TABLE composed_of (
                             ID_PROJECT int NOT NULL,
                             ID_ACTIVITY int NOT NULL,
                             PRIMARY KEY (ID_PROJECT, ID_ACTIVITY),
                             CONSTRAINT composed_of_ibfk_1 FOREIGN KEY (ID_PROJECT) REFERENCES project (ID_PROJECT) ON DELETE CASCADE,
                             CONSTRAINT composed_of_ibfk_2 FOREIGN KEY (ID_ACTIVITY) REFERENCES activity (ID_ACTIVITY) ON DELETE CASCADE
);

CREATE TABLE participates (
                              MESSAGE_INDEX int NOT NULL,
                              ID_SENDER int NOT NULL,
                              ID_RECEIVER int NOT NULL,
                              PRIMARY KEY (MESSAGE_INDEX),
                              CONSTRAINT participates_ibfk_1 FOREIGN KEY (MESSAGE_INDEX) REFERENCES message (MESSAGE_INDEX) ON DELETE CASCADE,
                              CONSTRAINT participates_ibfk_2 FOREIGN KEY (ID_SENDER) REFERENCES user (ID_USER) ON DELETE CASCADE,
                              CONSTRAINT participates_ibfk_3 FOREIGN KEY (ID_RECEIVER) REFERENCES user (ID_USER) ON DELETE CASCADE
);

CREATE ALIAS AssignProjectAndRejectOthers AS $$
void assignProject(Connection conn, int targetPractitionerIdentifier, int targetProjectIdentifier) throws SQLException {
    conn.setAutoCommit(false);
    try (PreparedStatement ps1 = conn.prepareStatement("UPDATE project_application SET APPLICATION_STATUS = 'Assigned' WHERE ID_PRACTITIONER = ? AND ID_PROJECT = ?");
         PreparedStatement ps2 = conn.prepareStatement("UPDATE project_application SET APPLICATION_STATUS = 'Rejected' WHERE ID_PRACTITIONER = ? AND ID_PROJECT != ?")) {
        ps1.setInt(1, targetPractitionerIdentifier);
        ps1.setInt(2, targetProjectIdentifier);
        ps1.executeUpdate();
        ps2.setInt(1, targetPractitionerIdentifier);
        ps2.setInt(2, targetProjectIdentifier);
        ps2.executeUpdate();
        conn.commit();
} catch (SQLException e) {
        conn.rollback();
        throw e;
}
}
$$;