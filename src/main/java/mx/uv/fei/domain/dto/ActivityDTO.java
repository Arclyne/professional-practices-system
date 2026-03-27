package mx.uv.fei.domain.dto;

import java.sql.Date;

public class ActivityDTO {
    private int activityId;
    private String name;
    private Date startDate;
    private Date endDate;
    private String description;
    private String manager;

    public ActivityDTO() {
    }

    public ActivityDTO(int activityId, String name, Date startDate, Date endDate, String description, String manager) {
        this.activityId = activityId;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.manager = manager;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }
}
