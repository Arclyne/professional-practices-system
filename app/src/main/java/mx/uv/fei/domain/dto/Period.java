package mx.uv.fei.domain.dto;

import java.sql.Date;
import java.util.Objects;

/**
 * Representa un periodo académico dentro del programa de prácticas profesionales.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
public class Period {

    private int periodId;
    private String periodName;
    private Date startDate;
    private Date endDate;
    private String periodStatus;

    public Period() {}

    public int getPeriodId() { return periodId; }
    public void setPeriodId(int periodId) { this.periodId = periodId; }

    public String getPeriodName() { return periodName; }
    public void setPeriodName(String periodName) { this.periodName = periodName; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public String getPeriodStatus() { return periodStatus; }
    public void setPeriodStatus(String periodStatus) { this.periodStatus = periodStatus; }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (this == obj) {
            isEqual = true;
        } else if (obj != null && getClass() == obj.getClass()) {
            Period other = (Period) obj;
            isEqual = Objects.equals(this.periodName, other.periodName);
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(periodName);
    }
}