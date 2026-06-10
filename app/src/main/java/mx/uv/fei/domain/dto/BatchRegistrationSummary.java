package mx.uv.fei.domain.dto;

import java.util.Objects;

/**
 * Acumula los resultados de un proceso de registro masivo de practicantes.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
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
            BatchRegistrationSummary other = (BatchRegistrationSummary) obj;
            isEqual = this.successfulRegistrations == other.successfulRegistrations &&
                    this.failedRegistrations == other.failedRegistrations;
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(successfulRegistrations, failedRegistrations);
    }
}