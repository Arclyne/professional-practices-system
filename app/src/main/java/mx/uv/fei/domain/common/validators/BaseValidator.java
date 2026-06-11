package mx.uv.fei.domain.common.validators;

import mx.uv.fei.domain.exceptions.ManagerException;

import java.util.Date;
import java.util.regex.Pattern;

public class BaseValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern ENROLLMENT_PATTERN =
            Pattern.compile("^(zs|s)[0-9]{8}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PERSONAL_NUMBER_PATTERN =
            Pattern.compile("^\\d+$");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidEnrollment(String enrollment) {
        return enrollment != null && ENROLLMENT_PATTERN.matcher(enrollment).matches();
    }

    public static boolean isValidPersonalNumber(String personalNumber) {
        return personalNumber == null || !PERSONAL_NUMBER_PATTERN.matcher(personalNumber).matches();
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

    public static void validateDateRange(Date startDate, Date endDate, String errorMessage) throws ManagerException {
        if (startDate != null && endDate != null && endDate.before(startDate)) {
            throw new ManagerException(errorMessage);
        }
    }
}