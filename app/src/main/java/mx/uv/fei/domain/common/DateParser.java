package mx.uv.fei.domain.common;

import java.sql.Date;
import java.time.LocalDate;

public class DateParser {

    public static Date parseDate(String day, String month, String year) {
        if (isAnyFieldBlank(day, month, year)) {
            throw new IllegalArgumentException("Campos de fecha vacíos.");
        }
        LocalDate localDate = LocalDate.of(
                Integer.parseInt(year),
                Integer.parseInt(month),
                Integer.parseInt(day)
        );
        return Date.valueOf(localDate);
    }

    private static boolean isAnyFieldBlank(String day, String month, String year) {
        return isBlank(day) || isBlank(month) || isBlank(year);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isEmpty();
    }
}