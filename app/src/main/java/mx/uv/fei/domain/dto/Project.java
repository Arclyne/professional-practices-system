package mx.uv.fei.domain.dto;

import java.sql.Date;
import java.util.Objects;


public class Project {

    private int projectId;
    private String projectName;
    private String description;
    private int participantCapacity;
    private int managerId;
    private String status;
    private Date startDate;
    private Date endDate;
    private int companyId;

    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getParticipantCapacity() { return participantCapacity; }
    public void setParticipantCapacity(int participantCapacity) { this.participantCapacity = participantCapacity; }

    public int getManagerId() { return managerId; }
    public void setManagerId(int managerId) { this.managerId = managerId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public int getCompanyId() { return companyId; }
    public void setCompanyId(int companyId) { this.companyId = companyId; }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (this == obj) {
            isEqual = true;
        } else if (obj != null && getClass() == obj.getClass()) {
            Project other = (Project) obj;
            isEqual = this.managerId == other.managerId &&
                    this.companyId == other.companyId &&
                    Objects.equals(this.projectName, other.projectName);
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectName, managerId, companyId);
    }
}