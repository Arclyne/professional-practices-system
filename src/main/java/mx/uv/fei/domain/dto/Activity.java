package mx.uv.fei.domain.dto;

import java.sql.Date;
import java.util.Objects;

public class Activity {
    private int activityId;
    private String name;
    private Date startDate;
    private Date endDate;
    private String description;
    private String manager;

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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        Activity that = (Activity) o;

        return activityId == that.getActivityId() &&
                name == that.getName() &&
                manager == that.getManager();
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId, name, manager);
    }
}
