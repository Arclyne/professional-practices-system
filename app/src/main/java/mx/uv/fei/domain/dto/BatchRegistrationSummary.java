package mx.uv.fei.domain.dto;

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
}