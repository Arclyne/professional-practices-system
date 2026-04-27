package mx.uv.fei.domain.dto;

import java.sql.Date;
import java.util.Objects;

public class Project {
    private int projectId;
    private String projectName;
    private String description;
    private int participantCapacity;
    private String manager;
    private String status;
    private Date startDate;
    private Date endDate;
    private int companyId;

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getParticipantCapacity() {
        return participantCapacity;
    }

    public void setParticipantCapacity(int participantCapacity) {
        this.participantCapacity = participantCapacity;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;

        if (object == null || getClass() != object.getClass())
            return false;

        Project that = (Project) object;
        return Objects.equals(projectName, that.getProjectName()) &&
                Objects.equals(manager, that.getManager()) &&
                Objects.equals(companyId, that.getCompanyId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectName, manager, companyId);
    }
}
