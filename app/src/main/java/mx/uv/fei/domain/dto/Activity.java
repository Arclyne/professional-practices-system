package mx.uv.fei.domain.dto;

import java.sql.Date;
import java.util.Objects;

public class Activity {
    private int activityId;
    private String name;
    private Date startDate;
    private Date endDate;
    private String description;
    private int groupId;

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

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    @Override
    public boolean equals(Object object) {
        boolean isEqual = false;

        if (this == object) {
            isEqual = true;
        } else if (object != null && getClass() == object.getClass()) {
            Activity that = (Activity) object;
            isEqual = groupId == that.getGroupId() &&
                    Objects.equals(name, that.getName());
        }

        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, groupId);
    }
}