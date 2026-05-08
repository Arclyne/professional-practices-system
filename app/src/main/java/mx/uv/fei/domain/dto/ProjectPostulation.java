package mx.uv.fei.domain.dto;

public class ProjectPostulation {

    private int practitionerIdentifier;
    private int projectIdentifier;
    private String projectName;
    private int priorityLevel;
    private String postulationStatus;

    public int getPractitionerIdentifier() {
        return practitionerIdentifier;
    }

    public void setPractitionerIdentifier(int practitionerIdentifier) {
        this.practitionerIdentifier = practitionerIdentifier;
    }

    public int getProjectIdentifier() {
        return projectIdentifier;
    }

    public void setProjectIdentifier(int projectIdentifier) {
        this.projectIdentifier = projectIdentifier;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public int getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(int priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    public String getPostulationStatus() {
        return postulationStatus;
    }

    public void setPostulationStatus(String postulationStatus) {
        this.postulationStatus = postulationStatus;
    }
}