package mx.uv.fei.domain.dto;

import java.util.Objects;

/**
 * Representa la postulación de un practicante a un proyecto de prácticas profesionales.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
public class ProjectPostulation {

    private int practitionerId;
    private int projectId;
    private String projectName;
    private int priorityLevel;
    private String postulationStatus;

    public int getPractitionerId() { return practitionerId; }
    public void setPractitionerId(int practitionerId) { this.practitionerId = practitionerId; }

    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public int getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(int priorityLevel) { this.priorityLevel = priorityLevel; }

    public String getPostulationStatus() { return postulationStatus; }
    public void setPostulationStatus(String postulationStatus) { this.postulationStatus = postulationStatus; }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (this == obj) {
            isEqual = true;
        } else if (obj != null && getClass() == obj.getClass()) {
            ProjectPostulation other = (ProjectPostulation) obj;
            isEqual = this.practitionerId == other.practitionerId &&
                    this.projectId == other.projectId;
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(practitionerId, projectId);
    }
}