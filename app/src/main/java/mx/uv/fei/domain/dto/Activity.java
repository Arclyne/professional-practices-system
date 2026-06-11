package mx.uv.fei.domain.dto;

import java.sql.Date;
import java.util.Objects;

/**
 * Representa una actividad registrada por un practicante en su bitácora.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
public class Activity {

    private int activityId;
    private int practitionerId;
    private Integer reportId;
    private String title;
    private String description;
    private Date activityDate;
    private int durationHours;

    public Activity() {}

    public int getActivityId() { return activityId; }
    public void setActivityId(int activityId) { this.activityId = activityId; }

    public int getPractitionerId() { return practitionerId; }
    public void setPractitionerId(int practitionerId) { this.practitionerId = practitionerId; }

    public Integer getReportId() { return reportId; }
    public void setReportId(Integer reportId) { this.reportId = reportId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getActivityDate() { return activityDate; }
    public void setActivityDate(Date activityDate) { this.activityDate = activityDate; }

    public int getDurationHours() { return durationHours; }
    public void setDurationHours(int durationHours) { this.durationHours = durationHours; }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (this == obj) {
            isEqual = true;
        } else if (obj != null && getClass() == obj.getClass()) {
            Activity other = (Activity) obj;
            isEqual = this.practitionerId == other.practitionerId &&
                    Objects.equals(this.title, other.title) &&
                    Objects.equals(this.activityDate, other.activityDate);
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(practitionerId, title, activityDate);
    }
}