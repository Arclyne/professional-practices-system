package mx.uv.fei.domain.enums;

public enum Month {

    ENERO(1),
    FEBRERO(2),
    MARZO(3),
    ABRIL(4),
    MAYO(5),
    JUNIO(6),
    JULIO(7),
    AGOSTO(8),
    SEPTIEMBRE(9),
    OCTUBRE(10),
    NOVIEMBRE(11),
    DICIEMBRE(12);

    private final int monthNumber;

    Month(int monthNumber) {
        this.monthNumber = monthNumber;
    }

    public int getMonthNumber() {
        return monthNumber;
    }

    public static Month fromString(String monthName) {
        for (Month month : values()) {
            if (month.name().equalsIgnoreCase(monthName)) {
                return month;
            }
        }
        throw new IllegalArgumentException("Mes no reconocido: " + monthName);
    }
}