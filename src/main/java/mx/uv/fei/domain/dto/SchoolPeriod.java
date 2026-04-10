package mx.uv.fei.domain.dto;

import java.time.LocalDate;

public class SchoolPeriod {
    private int periodId;
    private String periodName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;

    public SchoolPeriod() {}

    public int getPeriodId() { 
        return periodId; 
    }

    public void setPeriodId(int periodId) { 
        this.periodId = periodId; 
    }

    public String getPeriodName() { 
        return periodName; 
    }

    public void setPeriodName(String periodName) { 
        this.periodName = periodName; 
    }

    public LocalDate getStartDate() { 
        return startDate; 
    }

    public void setStartDate(LocalDate startDate) { 
        this.startDate = startDate; 
    }

    public LocalDate getEndDate() { 
        return endDate; 
    }

    public void setEndDate(LocalDate endDate) { 
        this.endDate = endDate; 
    }

    public String getStatus() { 
        return status; 
    }
    
    public void setStatus(String status) { 
        this.status = status; 
    }
}