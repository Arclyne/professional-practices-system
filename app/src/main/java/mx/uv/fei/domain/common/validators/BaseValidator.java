package mx.uv.fei.domain.common.validators;

import mx.uv.fei.domain.exceptions.ManagerException;

import java.util.Date;
import java.util.regex.Pattern;

public class BaseValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern ENROLLMENT_PATTERN = Pattern.compile("^(s)[0-9]{8}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PERSONAL_NUMBER_PATTERN = Pattern.compile("^\\d+$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10}$");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidEnrollment(String enrollment) {
        return enrollment != null && ENROLLMENT_PATTERN.matcher(enrollment).matches();
    }

    public static boolean isValidPersonalNumber(String personalNumber) {
        return personalNumber != null && PERSONAL_NUMBER_PATTERN.matcher(personalNumber).matches();
    }

    public static boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name).matches();
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && PHONE_PATTERN.matcher(phoneNumber).matches();
    }

    public static void validateString(String value, String errorMessage) throws ManagerException {
        if (value == null || value.trim().isEmpty()) {
            throw new ManagerException(errorMessage);
        }
    }

    public static void validateId(int id, String errorMessage) throws ManagerException {
        if (id <= 0) {
            throw new ManagerException(errorMessage);
        }
    }

    public static void validateMaxLength(String value, int maxLength, String errorMessage) throws ManagerException {
        if (value != null && value.length() > maxLength) {
            throw new ManagerException(errorMessage);
        }
    }

    public static void validateNumeric(String value, String errorMessage) throws ManagerException {
        if (!isValidPersonalNumber(value)) {
            throw new ManagerException(errorMessage);
        }
    }

    public static void validateDateRange(Date startDate, Date endDate, String errorMessage) throws ManagerException {
        if (startDate != null && endDate != null && endDate.before(startDate)) {
            throw new ManagerException(errorMessage);
        }
    }

    public static void validateStartBeforeEnd(Date startDate, Date endDate, String errorMessage) throws ManagerException {
        if (startDate != null && endDate != null && !startDate.before(endDate)) {
            throw new ManagerException(errorMessage);
        }
    }
}