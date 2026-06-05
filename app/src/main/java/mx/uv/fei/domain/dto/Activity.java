package mx.uv.fei.domain.dto;

import java.sql.Date;
import java.util.Objects;

public class Activity {
    private int activityId;
    private int practitionerId;
    private Integer reportId;
    private String title;
    private String description;
    private Date activityDate;
    private int durationHours;
    private String fileUrl;

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

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    @Override
    public boolean equals(Object object) {
        boolean isEqual = false;
        if (this == object) {
            isEqual = true;
        } else if (object != null && getClass() == object.getClass()) {
            Activity that = (Activity) object;
            isEqual = activityId == that.activityId &&
                    practitionerId == that.practitionerId &&
                    durationHours == that.durationHours &&
                    Objects.equals(reportId, that.reportId) &&
                    Objects.equals(title, that.title) &&
                    Objects.equals(description, that.description) &&
                    Objects.equals(activityDate, that.activityDate) &&
                    Objects.equals(fileUrl, that.fileUrl);
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId, practitionerId, reportId, title, description, activityDate, durationHours, fileUrl);
    }
}