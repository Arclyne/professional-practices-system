package mx.uv.fei.domain.dto;

import java.sql.Date;
import java.util.Objects;

public class Period {

    private int periodId;
    private String periodName;
    private Date startDate;
    private Date endDate;
    private String periodStatus;

    public Period() {
    }

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

    public String getPeriodStatus() {
        return periodStatus;
    }

    public void setPeriodStatus(String periodStatus) {
        this.periodStatus = periodStatus;
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (this == obj) {
            isEqual = true;
        } else if (obj != null && getClass() == obj.getClass()) {
            Period that = (Period) obj;
            isEqual = Objects.equals(this.periodName, that.getPeriodName());
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(periodName);
    }
}