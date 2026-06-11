package mx.uv.fei.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PractitionerGradeTest {

    private static final int PRACTITIONER_ID = 123;
    private static final int PROFESSOR_ID = 68;
    private static final String PERIOD = "Junio-Diciembre 2026";

    private PractitionerGrade baseGrade;
    private PractitionerGrade comparedGrade;

    @BeforeEach
    void setUp() {
        baseGrade = new PractitionerGrade();
        baseGrade.setPractitionerId(PRACTITIONER_ID);
        baseGrade.setProfessorId(PROFESSOR_ID);
        baseGrade.setPeriod(PERIOD);
        baseGrade.setTentativeGrade(8.5);
        baseGrade.setFinalGrade(9.0);

        comparedGrade = new PractitionerGrade();
        comparedGrade.setPractitionerId(PRACTITIONER_ID);
        comparedGrade.setProfessorId(PROFESSOR_ID);
        comparedGrade.setPeriod(PERIOD);
        comparedGrade.setTentativeGrade(7.0);
        comparedGrade.setFinalGrade(7.5);
    }

    @Test
    void equals_SamePractitionerAndPeriod_ReturnsTrue() {
        boolean isEqual = baseGrade.equals(comparedGrade);

        assertTrue(isEqual);
    }

    @Test
    void equals_SameInstance_ReturnsTrue() {
        boolean isEqual = baseGrade.equals(baseGrade);

        assertTrue(isEqual);
    }

    @Test
    void equals_SamePeriodDifferentGrades_ReturnsTrue() {
        comparedGrade.setFinalGrade(6.0);

        boolean isEqual = baseGrade.equals(comparedGrade);

        assertTrue(isEqual);
    }

    @Test
    void equals_DifferentPractitioner_ReturnsFalse() {
        comparedGrade.setPractitionerId(999);

        boolean isEqual = baseGrade.equals(comparedGrade);

        assertFalse(isEqual);
    }

    @Test
    void equals_DifferentPeriod_ReturnsFalse() {
        comparedGrade.setPeriod("Enero-Junio 2027");

        boolean isEqual = baseGrade.equals(comparedGrade);

        assertFalse(isEqual);
    }

    @Test
    void equals_NullPeriod_ReturnsFalse() {
        comparedGrade.setPeriod(null);

        boolean isEqual = baseGrade.equals(comparedGrade);

        assertFalse(isEqual);
    }

    @Test
    void equals_ComparedToNull_ReturnsFalse() {
        boolean isEqual = baseGrade.equals(null);

        assertFalse(isEqual);
    }

    @Test
    void equals_ComparedToDifferentClass_ReturnsFalse() {
        boolean isEqual = baseGrade.equals(PERIOD);

        assertFalse(isEqual);
    }

    @Test
    void hashCode_SamePractitionerAndPeriod_ProduceSameHash() {
        int baseHash = baseGrade.hashCode();
        int comparedHash = comparedGrade.hashCode();

        assertEquals(baseHash, comparedHash);
    }

    @Test
    void hashCode_DifferentPeriod_ProduceDifferentHash() {
        comparedGrade.setPeriod("Enero-Junio 2027");

        int baseHash = baseGrade.hashCode();
        int comparedHash = comparedGrade.hashCode();

        assertNotEquals(baseHash, comparedHash);
    }
}
