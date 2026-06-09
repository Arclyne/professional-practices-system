package mx.uv.fei.domain.dto;

import java.util.Objects;

public class BatchRegistrationSummary {
    private int successfulRegistrations;
    private int failedRegistrations;

    public BatchRegistrationSummary() {
        this.successfulRegistrations = 0;
        this.failedRegistrations = 0;
    }

    public void incrementSuccess() {
        this.successfulRegistrations++;
    }

    public void incrementFailure() {
        this.failedRegistrations++;
    }

    public int getSuccessfulRegistrations() {
        return successfulRegistrations;
    }

    public int getFailedRegistrations() {
        return failedRegistrations;
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (this == obj) {
            isEqual = true;
        } else if (obj != null && getClass() == obj.getClass()) {
            BatchRegistrationSummary that = (BatchRegistrationSummary) obj;
            isEqual = this.successfulRegistrations == that.successfulRegistrations &&
                    this.failedRegistrations == that.failedRegistrations;
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(successfulRegistrations, failedRegistrations);
    }
}