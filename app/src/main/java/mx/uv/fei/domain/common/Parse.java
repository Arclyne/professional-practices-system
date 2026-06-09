package mx.uv.fei.domain.common;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Parse {

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
}
