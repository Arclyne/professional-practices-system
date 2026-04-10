package mx.uv.fei.domain.dto;

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
}