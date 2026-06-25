package mx.uv.fei.domain.dto;

import mx.uv.fei.domain.enums.EnrollmentStatus;

import java.util.Objects;

/**
 * Representa la inscripción de un practicante en un grupo de prácticas durante un periodo escolar.
 *
 * Desacopla a la persona (practicante) de su participación, permitiendo que un mismo practicante
 * se reinscriba en periodos distintos manteniendo una sola inscripción por periodo.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
public class GroupEnrollment {

    private int enrollmentId;
    private int practitionerId;
    private int groupId;
    private int periodId;
    private int opportunityNumber;
    private EnrollmentStatus status;

    public GroupEnrollment() {}

    public int getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(int enrollmentId) { this.enrollmentId = enrollmentId; }

    public int getPractitionerId() { return practitionerId; }
    public void setPractitionerId(int practitionerId) { this.practitionerId = practitionerId; }

    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }

    public int getPeriodId() { return periodId; }
    public void setPeriodId(int periodId) { this.periodId = periodId; }

    public int getOpportunityNumber() { return opportunityNumber; }
    public void setOpportunityNumber(int opportunityNumber) { this.opportunityNumber = opportunityNumber; }

    public EnrollmentStatus getStatus() { return status; }
    public void setStatus(EnrollmentStatus status) { this.status = status; }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (this == obj) {
            isEqual = true;
        } else if (obj != null && getClass() == obj.getClass()) {
            GroupEnrollment other = (GroupEnrollment) obj;
            isEqual = this.practitionerId == other.practitionerId &&
                    this.periodId == other.periodId;
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(practitionerId, periodId);
    }
}
