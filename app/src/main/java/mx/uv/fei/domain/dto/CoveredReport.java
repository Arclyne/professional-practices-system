package mx.uv.fei.domain.dto;

import java.util.List;


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
