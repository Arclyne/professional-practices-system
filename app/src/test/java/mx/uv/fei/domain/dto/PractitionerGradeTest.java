package mx.uv.fei.domain.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PractitionerGradeTest {

    private PractitionerGrade gradeA;
    private PractitionerGrade gradeB;

    @BeforeEach
    void setUp() {
        gradeA = new PractitionerGrade();
        gradeA.setPractitionerId(123);
        gradeA.setProfessorId(68);
        gradeA.setPeriod("Junio-Diciembre 2026");
        gradeA.setTentativeGrade(8.5);
        gradeA.setFinalGrade(9.0);

        gradeB = new PractitionerGrade();
        gradeB.setPractitionerId(123);
        gradeB.setProfessorId(68);
        gradeB.setPeriod("Junio-Diciembre 2026");
        gradeB.setTentativeGrade(7.0);
        gradeB.setFinalGrade(7.5);
    }

    @Test
    void equals_SamePractitionerAndPeriod_ReturnsTrue() {
        boolean result = gradeA.equals(gradeB);

        assertTrue(result);
    }

    @Test
    void equals_SameInstance_ReturnsTrue() {
        boolean result = gradeA.equals(gradeA);

        assertTrue(result);
    }

    @Test
    void equals_SamePeriodDifferentGrades_ReturnsTrue() {
        gradeB.setFinalGrade(6.0);

        boolean result = gradeA.equals(gradeB);

        assertTrue(result);
    }

    @Test
    void equals_DifferentPractitioner_ReturnsFalse() {
        gradeB.setPractitionerId(999);

        boolean result = gradeA.equals(gradeB);

        assertFalse(result);
    }

    @Test
    void equals_DifferentPeriod_ReturnsFalse() {
        gradeB.setPeriod("Enero-Junio 2027");

        boolean result = gradeA.equals(gradeB);

        assertFalse(result);
    }

    @Test
    void equals_NullPeriod_ReturnsFalse() {
        gradeB.setPeriod(null);

        boolean result = gradeA.equals(gradeB);

        assertFalse(result);
    }

    @Test
    void equals_ComparedToNull_ReturnsFalse() {
        boolean result = gradeA.equals(null);

        assertFalse(result);
    }

    @Test
    void equals_ComparedToDifferentClass_ReturnsFalse() {
        boolean result = gradeA.equals("Junio-Diciembre 2026");

        assertFalse(result);
    }

    @Test
    void hashCode_SamePractitionerAndPeriod_ProduceSameHash() {
        int hashA = gradeA.hashCode();
        int hashB = gradeB.hashCode();

        assertEquals(hashA, hashB);
    }

    @Test
    void hashCode_DifferentPeriod_ProduceDifferentHash() {
        gradeB.setPeriod("Enero-Junio 2027");

        int hashA = gradeA.hashCode();
        int hashB = gradeB.hashCode();

        assertNotEquals(hashA, hashB);
    }
}
