package mx.uv.fei.domain.common;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import mx.uv.fei.domain.dto.Project;

public class CommonParse {

    public static Date parseDate(String dayString, String monthString, String yearString)
            throws IllegalArgumentException, DateTimeParseException {
        if (dayString == null || dayString.isEmpty() || monthString == null || monthString.isEmpty()
                || yearString == null || yearString.isEmpty()) {
            throw new IllegalArgumentException("Campos de fecha vacíos");
        }

        int parsedDay = Integer.parseInt(dayString);
        int parsedMonth = Integer.parseInt(monthString);
        int parsedYear = Integer.parseInt(yearString);

        LocalDate convertedLocalDate = LocalDate.of(parsedYear, parsedMonth, parsedDay);
        return Date.valueOf(convertedLocalDate);
    }

    public static void validateProjectData(Project projectToValidate) {
        if (projectToValidate.getProjectName() == null || projectToValidate.getProjectName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del proyecto es obligatorio.");
        }

        if (projectToValidate.getManager() == null || projectToValidate.getManager().trim().isEmpty()) {
            throw new IllegalArgumentException("El encargado del proyecto es obligatorio.");
        }

        if (projectToValidate.getStartDate() != null && projectToValidate.getEndDate() != null) {
            if (projectToValidate.getEndDate().before(projectToValidate.getStartDate())) {
                throw new IllegalArgumentException("La fecha de término no puede ser anterior a la fecha de inicio.");
            }
        }
    }

}