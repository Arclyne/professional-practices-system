package mx.uv.fei.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProgressReportTest {

    private static final int PRACTITIONER_ID = 123;
    private static final String INTERMEDIATE_TYPE = "Intermedio";
    private static final String FINAL_TYPE = "Final";

    private ProgressReport baseReport;
    private ProgressReport comparedReport;

    @BeforeEach
    void setUp() {
        baseReport = new ProgressReport();
        baseReport.setPractitionerId(PRACTITIONER_ID);
        baseReport.setReportType(INTERMEDIATE_TYPE);
        baseReport.setTotalHoursAtSubmission(215.0);

        comparedReport = new ProgressReport();
        comparedReport.setPractitionerId(PRACTITIONER_ID);
        comparedReport.setReportType(INTERMEDIATE_TYPE);
        comparedReport.setTotalHoursAtSubmission(310.0);
    }

    @Test
    void equals_SamePractitionerAndType_ReturnsTrue() {
        boolean isEqual = baseReport.equals(comparedReport);

        assertTrue(isEqual);
    }

    @Test
    void equals_SameInstance_ReturnsTrue() {
        boolean isEqual = baseReport.equals(baseReport);

        assertTrue(isEqual);
    }

    @Test
    void equals_SameTypeButDifferentHours_ReturnsTrue() {
        comparedReport.setTotalHoursAtSubmission(420.0);

        boolean isEqual = baseReport.equals(comparedReport);

        assertTrue(isEqual);
    }

    @Test
    void equals_DifferentPractitioner_ReturnsFalse() {
        comparedReport.setPractitionerId(999);

        boolean isEqual = baseReport.equals(comparedReport);

        assertFalse(isEqual);
    }

    @Test
    void equals_DifferentType_ReturnsFalse() {
        comparedReport.setReportType(FINAL_TYPE);

        boolean isEqual = baseReport.equals(comparedReport);

        assertFalse(isEqual);
    }

    @Test
    void equals_NullType_ReturnsFalse() {
        comparedReport.setReportType(null);

        boolean isEqual = baseReport.equals(comparedReport);

        assertFalse(isEqual);
    }

    @Test
    void equals_ComparedToNull_ReturnsFalse() {
        boolean isEqual = baseReport.equals(null);

        assertFalse(isEqual);
    }

    @Test
    void equals_ComparedToDifferentClass_ReturnsFalse() {
        boolean isEqual = baseReport.equals(INTERMEDIATE_TYPE);

        assertFalse(isEqual);
    }

    @Test
    void hashCode_SamePractitionerAndType_ProduceSameHash() {
        int baseHash = baseReport.hashCode();
        int comparedHash = comparedReport.hashCode();

        assertEquals(baseHash, comparedHash);
    }

    @Test
    void hashCode_DifferentType_ProduceDifferentHash() {
        comparedReport.setReportType(FINAL_TYPE);

        int baseHash = baseReport.hashCode();
        int comparedHash = comparedReport.hashCode();

        assertNotEquals(baseHash, comparedHash);
    }
}
