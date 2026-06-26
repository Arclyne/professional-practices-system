package mx.uv.fei.domain.enums;

public enum TemplateToken {

    NOMBRE_PRACTICANTE("%NOMBRE_PRACTICANTE%", "Nombre completo del practicante"),
    MATRICULA("%MATRICULA%", "Matrícula del practicante"),
    NOMBRE_MAESTRO("%NOMBRE_MAESTRO%", "Nombre completo del maestro asignado"),
    PERIODO_ESCOLAR("%PERIODO_ESCOLAR%", "Nombre del período escolar activo"),
    PROYECTO_NOMBRE("%PROYECTO_NOMBRE%", "Nombre del proyecto asignado"),
    ACTIVIDAD_DEL_MES("%ACTIVIDAD_DEL_MES%", "Actividad principal realizada en el mes"),
    FECHA_ACTUAL("%FECHA_ACTUAL%", "Fecha del día de generación del documento"),
    HORAS_REPORTADAS("%HORAS_REPORTADAS%", "Horas trabajadas en este reporte"),
    HORAS_TOTALES("%HORAS_TOTALES%", "Horas acumuladas en total"),
    OBSERVACIONES("%OBSERVACIONES%", "Observaciones del responsable");

    private final String placeholder;
    private final String description;

    TemplateToken(String placeholder, String description) {
        this.placeholder = placeholder;
        this.description = description;
    }

    public String getPlaceholder() { return placeholder; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return description + "  →  " + placeholder;
    }
}