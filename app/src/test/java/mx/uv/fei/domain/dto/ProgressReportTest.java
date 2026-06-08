package mx.uv.fei.domain.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProgressReportTest {

    private ProgressReport reportA;
    private ProgressReport reportB;

    @BeforeEach
    void setUp() {
        reportA = new ProgressReport();
        reportA.setPractitionerId(123);
        reportA.setReportType("Intermedio");
        reportA.setTotalHoursAtSubmission(215.0);

        reportB = new ProgressReport();
        reportB.setPractitionerId(123);
        reportB.setReportType("Intermedio");
        reportB.setTotalHoursAtSubmission(310.0);
    }


    @Test
    void equals_SamePractitionerAndType_ReturnsTrue() {
        boolean result = reportA.equals(reportB);

        assertTrue(result);
    }

    @Test
    void equals_SameInstance_ReturnsTrue() {
        boolean result = reportA.equals(reportA);

        assertTrue(result);
    }

    @Test
    void equals_SameTypeButDifferentHours_ReturnsTrue() {
        reportB.setTotalHoursAtSubmission(420.0);

        boolean result = reportA.equals(reportB);

        assertTrue(result);
    }



    @Test
    void equals_DifferentPractitioner_ReturnsFalse() {
        reportB.setPractitionerId(999);

        boolean result = reportA.equals(reportB);

        assertFalse(result);
    }

    @Test
    void equals_DifferentType_ReturnsFalse() {
        reportB.setReportType("Final");

        boolean result = reportA.equals(reportB);

        assertFalse(result);
    }

    @Test
    void equals_NullType_ReturnsFalse() {
        reportB.setReportType(null);

        boolean result = reportA.equals(reportB);

        assertFalse(result);
    }

    @Test
    void equals_ComparedToNull_ReturnsFalse() {
        boolean result = reportA.equals(null);

        assertFalse(result);
    }

    @Test
    void equals_ComparedToDifferentClass_ReturnsFalse() {
        boolean result = reportA.equals("Intermedio");

        assertFalse(result);
    }


    @Test
    void hashCode_SamePractitionerAndType_ProduceSameHash() {
        int hashA = reportA.hashCode();
        int hashB = reportB.hashCode();

        assertEquals(hashA, hashB);
    }

    @Test
    void hashCode_DifferentType_ProduceDifferentHash() {
        reportB.setReportType("Final");

        int hashA = reportA.hashCode();
        int hashB = reportB.hashCode();

        assertNotEquals(hashA, hashB);
    }
}
