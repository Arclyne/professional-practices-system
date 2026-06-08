package mx.uv.fei.domain.dto;

import java.time.LocalDate;
import java.util.Objects;


public class SchoolPeriod {

    private int periodId;
    private String periodName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;

    public SchoolPeriod() {
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

    @Override
    public boolean equals(Object object) {
        boolean isEqual = false;

        if (this == object) {
            isEqual = true;
        } else if (object != null && getClass() == object.getClass()) {
            SchoolPeriod that = (SchoolPeriod) object;
            isEqual = Objects.equals(this.periodName, that.periodName)
                    && Objects.equals(this.startDate, that.startDate)
                    && Objects.equals(this.endDate, that.endDate);
        }

        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(periodName, startDate, endDate);
    }
}