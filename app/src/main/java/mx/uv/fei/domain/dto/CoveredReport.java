package mx.uv.fei.domain.dto;

import java.util.List;

/**
 * Agrupa un reporte mensual evaluado con sus actividades para listarlo dentro
 * del reporte de avance (intermedio o final) que cubre su rango de fechas.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
public class CoveredReport {

    private final MonthlyReport report;
    private final List<Activity> activities;

    public CoveredReport(MonthlyReport report, List<Activity> activities) {
        this.report = report;
        this.activities = activities;
    }

    public MonthlyReport getReport() { return report; }

    public List<Activity> getActivities() { return activities; }
}
