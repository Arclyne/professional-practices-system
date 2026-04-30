package mx.uv.fei.domain.dto;

import java.util.Objects;

public class PracticeGroup {
    private int groupIndex;
    private String section;
    private int professorId;
    private int periodId;

    public PracticeGroup() {}

    public int getGroupIndex() { 
        return groupIndex; 
    }

    public void setGroupIndex(int groupIndex) { 
        this.groupIndex = groupIndex; 
    }

    public String getSection() { 
        return section; 
    }

    public void setSection(String section) { 
        this.section = section;
    }

    public int getProfessorId() { 
        return professorId; 
    }

    public void setProfessorId(int professorId) { 
        this.professorId = professorId; 
    }

    public int getPeriodId() { 
        return periodId; 
    }
    
    public void setPeriodId(int periodId) { 
        this.periodId = periodId; 
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
    
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        PracticeGroup that = (PracticeGroup) object;

        return this.professorId == that.professorId &&
               this.periodId == that.periodId &&
               Objects.equals(this.section, that.section);
    }
}