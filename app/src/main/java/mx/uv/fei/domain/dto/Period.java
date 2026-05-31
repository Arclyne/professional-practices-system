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
    public boolean equals(Object object) {
        boolean isEqual = false;

        if (this == object) {
            isEqual = true;
        } else if (object != null && getClass() == object.getClass()) {
            Period that = (Period) object;
            isEqual = this.periodId == that.getPeriodId() &&
                    Objects.equals(this.periodName, that.getPeriodName()) &&
                    Objects.equals(this.startDate, that.getStartDate()) &&
                    Objects.equals(this.endDate, that.getEndDate()) &&
                    Objects.equals(this.periodStatus, that.getPeriodStatus());
        }

        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(periodId, periodName, startDate, endDate, periodStatus);
    }
}