package mx.uv.fei.domain.dto;

import java.util.Objects;

/**
 * Representa un grupo de prácticas profesionales asignado a un profesor en un periodo académico.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
public class PracticeGroup {

    private int groupId;
    private String section;
    private int professorId;
    private int periodId;

    public PracticeGroup() {}

    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public int getProfessorId() { return professorId; }
    public void setProfessorId(int professorId) { this.professorId = professorId; }

    public int getPeriodId() { return periodId; }
    public void setPeriodId(int periodId) { this.periodId = periodId; }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (this == obj) {
            isEqual = true;
        } else if (obj != null && getClass() == obj.getClass()) {
            PracticeGroup other = (PracticeGroup) obj;
            isEqual = this.professorId == other.professorId &&
                    this.periodId == other.periodId &&
                    Objects.equals(this.section, other.section);
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(section, professorId, periodId);
    }
}