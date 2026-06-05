package mx.uv.fei.domain.common;

import mx.uv.fei.domain.dto.*;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class Validator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern ENROLLMENT_PATTERN = Pattern.compile("^(zs|s)[0-9]{8}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PERSONAL_NUMBER_PATTERN = Pattern.compile("^\\d+$");
    private static final Pattern HAS_UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern HAS_NUMBER = Pattern.compile("[0-9]");

    private static final String MSG_INVALID_EMAIL = "The provided email format is invalid.";
    private static final String MSG_INVALID_ENROLLMENT = "The provided enrollment format is invalid.";
    private static final String MSG_INVALID_PERSONAL_NUM = "Invalid format: The personal number must contain only numbers.";

    private static final String MSG_REQ_PROJ_NAME = "The project name is required.";
    private static final String MSG_REQ_PROJ_ORG = "You must select a linked organization.";
    private static final String MSG_REQ_PROJ_MGR = "You must select a manager for the project.";

    private static final String MSG_REQ_PERSONAL_NUM = "The personal number is required.";
    private static final String MSG_REQ_PRAC_ENROLL = "The practitioner's enrollment is required.";

    private static final String MSG_REQ_MGR_NAME = "The manager's name is required.";
    private static final String MSG_REQ_MGR_PHONE = "The manager's phone is required.";
    private static final String MSG_REQ_MGR_EMAIL = "The manager's email is required.";
    private static final String MSG_REQ_MGR_ORG = "You must select the organization they belong to.";

    private static final String MSG_REQ_USER_NAME = "The %s's name is required.";
    private static final String MSG_REQ_USER_LASTNAME = "The %s's last name is required.";
    private static final String MSG_REQ_USER_GENDER = "The %s's gender is required.";
    private static final String MSG_REQ_USER_EMAIL = "The %s's email is required.";

    private static final String MSG_REQ_PWD_EMPTY = "The password is required and cannot be empty.";
    private static final String MSG_REQ_PWD_LENGTH = "The password must be at least 8 characters long.";
    private static final String MSG_REQ_PWD_UPPER = "The password must contain at least one uppercase letter.";
    private static final String MSG_REQ_PWD_NUM = "The password must contain at least one number.";

    private static final String MSG_DATE_RANGE = "The end date cannot be earlier than the start date.";

    private static final String MSG_REQ_ACT_TITLE = "El título de la actividad es obligatorio.";
    private static final String MSG_REQ_ACT_DESC = "La descripción es obligatoria.";
    private static final String MSG_REQ_ACT_DATE = "Debes seleccionar la fecha de la actividad.";
    private static final String MSG_INVALID_DURATION = "La duración de la actividad debe ser mayor a 0 horas.";

    private static final String MSG_REQ_REP_MONTH = "Debes seleccionar un mes para el reporte.";
    private static final String MSG_REQ_REP_YEAR = "El año del reporte debe ser mayor a cero.";
    private static final String MSG_REQ_REP_DATES = "Las fechas de inicio y fin son obligatorias.";
    private static final String MSG_NO_ACTIVITIES = "Debes seleccionar al menos una actividad libre para generar el reporte.";

    private static final String MSG_REQ_EVAL_GRADE = "La calificación es obligatoria.";
    private static final String MSG_INVALID_GRADE = "La calificación debe ser un valor entre 0 y 10.";
    private static final String MSG_REQ_EVAL_FEEDBACK = "La retroalimentación para el practicante es obligatoria.";
    private static final String MSG_REQ_SIGNED_URL = "No se detectó el documento. El archivo PDF es obligatorio.";

    private static final String MSG_DATE_MONTH_MISMATCH = "Las fechas seleccionadas no coinciden con el mes y año especificados para el reporte.";

    public static boolean isValidEmail(String email) {
        boolean isValid = false;
        if (email != null && EMAIL_PATTERN.matcher(email).matches()) {
            isValid = true;
        }
        return isValid;
    }

    public static boolean isValidEnrollment(String enrollment) {
        boolean isValid = false;
        if (enrollment != null && ENROLLMENT_PATTERN.matcher(enrollment).matches()) {
            isValid = true;
        }
        return isValid;
    }

    public static boolean isValidPersonalNumber(String personalNumber) {
        boolean isValid = false;
        if (personalNumber != null && PERSONAL_NUMBER_PATTERN.matcher(personalNumber).matches()) {
            isValid = true;
        }
        return isValid;
    }

    public static void validateSignedReport(String signedFileUrl) throws ManagerException {
        Validator.validateString(signedFileUrl, MSG_REQ_SIGNED_URL);
    }


    public static void validateLogbookActivity(Activity activity) throws ManagerException {
        validateString(activity.getTitle(), MSG_REQ_ACT_TITLE);
        validateString(activity.getDescription(), MSG_REQ_ACT_DESC);

        if (activity.getActivityDate() == null) {
            throw new ManagerException(MSG_REQ_ACT_DATE);
        }
        if (activity.getDurationHours() <= 0) {
            throw new ManagerException(MSG_INVALID_DURATION);
        }
    }

    public static void validateMonthlyReportCreation(MonthlyReport report, List<Activity> activities) throws ManagerException {
        validateString(report.getMonthName(), MSG_REQ_REP_MONTH);

        if (report.getYear() <= 0) {
            throw new ManagerException(MSG_REQ_REP_YEAR);
        }
        if (report.getStartDate() == null || report.getEndDate() == null) {
            throw new ManagerException(MSG_REQ_REP_DATES);
        }

        validateDateRange(report.getStartDate(), report.getEndDate());

        validateReportDatesMatchMonthAndYear(report.getStartDate(), report.getEndDate(), report.getMonthName(), report.getYear());

        if (activities == null || activities.isEmpty()) {
            throw new ManagerException(MSG_NO_ACTIVITIES);
        }

        for (Activity activity : activities) {
            if (activity.getActivityDate().before(report.getStartDate()) ||
                    activity.getActivityDate().after(report.getEndDate())) {

                throw new ManagerException("La actividad '" + activity.getTitle() +
                        "' (" + activity.getActivityDate() + ") está fuera del rango de fechas del reporte.");
            }
        }
    }

    private static void validateReportDatesMatchMonthAndYear(java.sql.Date startDate, java.sql.Date endDate, String monthName, int year) throws ManagerException {
        int expectedMonth = getMonthNumber(monthName);
        java.time.LocalDate startLocal = startDate.toLocalDate();
        java.time.LocalDate endLocal = endDate.toLocalDate();

        boolean isStartValid = (startLocal.getMonthValue() == expectedMonth && startLocal.getYear() == year);
        boolean isEndValid = (endLocal.getMonthValue() == expectedMonth && endLocal.getYear() == year);

        if (!isStartValid || !isEndValid) {
            throw new ManagerException(MSG_DATE_MONTH_MISMATCH);
        }
    }

    private static int getMonthNumber(String monthName) {
        int month = -1;
        switch (monthName.toLowerCase()) {
            case "enero": month = 1; break;
            case "febrero": month = 2; break;
            case "marzo": month = 3; break;
            case "abril": month = 4; break;
            case "mayo": month = 5; break;
            case "junio": month = 6; break;
            case "julio": month = 7; break;
            case "agosto": month = 8; break;
            case "septiembre": month = 9; break;
            case "octubre": month = 10; break;
            case "noviembre": month = 11; break;
            case "diciembre": month = 12; break;
        }
        return month;
    }

    public static void validateReportEvaluation(Double grade, String feedback) throws ManagerException {
        if (grade == null) {
            throw new ManagerException(MSG_REQ_EVAL_GRADE);
        }
        if (grade < 0.0 || grade > 10.0) {
            throw new ManagerException(MSG_INVALID_GRADE);
        }
        validateString(feedback, MSG_REQ_EVAL_FEEDBACK);
    }

    public static void validateProjectData(Project projectToValidate) throws ManagerException {
        validateString(projectToValidate.getProjectName(), MSG_REQ_PROJ_NAME);
        validateId(projectToValidate.getCompanyId(), MSG_REQ_PROJ_ORG);
        validateId(projectToValidate.getManagerId(), MSG_REQ_PROJ_MGR);
        validateDateRange(projectToValidate.getStartDate(), projectToValidate.getEndDate());
    }

    public static void validateProfessorData(Professor professorToValidate) throws ManagerException {
        validateUser(professorToValidate, "professor");
        validateString(professorToValidate.getUserName(), MSG_REQ_PERSONAL_NUM);

        if (!isValidPersonalNumber(professorToValidate.getUserName())) {
            throw new ManagerException(MSG_INVALID_PERSONAL_NUM);
        }
    }

    public static void validateCoordinatorData(Coordinator coordinatorToValidate) throws ManagerException {
        validateUser(coordinatorToValidate, "coordinator");
        validateString(coordinatorToValidate.getUserName(), MSG_REQ_PERSONAL_NUM);

        if (!isValidPersonalNumber(coordinatorToValidate.getUserName())) {
            throw new ManagerException(MSG_INVALID_PERSONAL_NUM);
        }
    }

    public static void validatePractitioner(Practitioner practitionerToValidate) throws ManagerException {
        validateUser(practitionerToValidate, "practitioner");
        validateString(practitionerToValidate.getEnrollment(), MSG_REQ_PRAC_ENROLL);

        if (!isValidEnrollment(practitionerToValidate.getEnrollment())) {
            throw new ManagerException(MSG_INVALID_ENROLLMENT);
        }
    }

    public static void validateAdministratorData(Administrator administratorToValidate) throws ManagerException {
        validateUser(administratorToValidate, "administrator");
        validateString(administratorToValidate.getUserName(), MSG_REQ_PERSONAL_NUM);

        if (!isValidPersonalNumber(administratorToValidate.getUserName())) {
            throw new ManagerException(MSG_INVALID_PERSONAL_NUM);
        }
    }

    public static void validateManagerData(Manager managerToValidate) throws ManagerException {
        validateString(managerToValidate.getName(), MSG_REQ_MGR_NAME);
        validateString(managerToValidate.getPhone(), MSG_REQ_MGR_PHONE);
        validateString(managerToValidate.getEmail(), MSG_REQ_MGR_EMAIL);

        if (!isValidEmail(managerToValidate.getEmail())) {
            throw new ManagerException(MSG_INVALID_EMAIL);
        }

        validateId(managerToValidate.getOrganizationId(), MSG_REQ_MGR_ORG);
    }

    private static void validateUser(User userToValidate, String role) throws ManagerException {
        validateString(userToValidate.getName(), String.format(MSG_REQ_USER_NAME, role));
        validateString(userToValidate.getLastName(), String.format(MSG_REQ_USER_LASTNAME, role));
        validatePassword(userToValidate.getPassword());

        if (userToValidate.getGender() == null) {
            throw new ManagerException(String.format(MSG_REQ_USER_GENDER, role));
        }

        validateString(userToValidate.getEmail(), String.format(MSG_REQ_USER_EMAIL, role));

        if (!isValidEmail(userToValidate.getEmail())) {
            throw new ManagerException(MSG_INVALID_EMAIL);
        }
    }

    private static void validateId(int id, String errorMessage) throws ManagerException {
        if (id <= 0) {
            throw new ManagerException(errorMessage);
        }
    }

    public static void validatePassword(String password) throws ManagerException {
        if (password == null || password.isBlank()) {
            throw new ManagerException(MSG_REQ_PWD_EMPTY);
        }
        if (password.length() < 8) {
            throw new ManagerException(MSG_REQ_PWD_LENGTH);
        }
        if (!HAS_UPPERCASE.matcher(password).find()) {
            throw new ManagerException(MSG_REQ_PWD_UPPER);
        }
        if (!HAS_NUMBER.matcher(password).find()) {
            throw new ManagerException(MSG_REQ_PWD_NUM);
        }
    }

    private static void validateString(String value, String errorMessage) throws ManagerException {
        if (value == null || value.trim().isEmpty()) {
            throw new ManagerException(errorMessage);
        }
    }

    private static void validateDateRange(Date startDate, Date endDate) throws ManagerException {
        if (startDate != null && endDate != null && endDate.before(startDate)) {
            throw new ManagerException(MSG_DATE_RANGE);
        }
    }
}