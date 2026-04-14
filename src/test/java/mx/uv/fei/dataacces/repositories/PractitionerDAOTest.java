package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.dataacces.exceptions.DAOException;

@SpringBootTest
@ActiveProfiles("test")
public class PractitionerDAOTest {
    private PractitionerDAO practitionerDAO;
    private Practitioner testPractitioner;

    @Autowired
    public PractitionerDAOTest(PractitionerDAO practitionerDAO, Practitioner testPractitioner) {
        this.practitionerDAO = practitionerDAO;
        this.testPractitioner = testPractitioner;
    }

    @BeforeEach
    void setUp() {

        testPractitioner = new Practitioner();

        testPractitioner.setName("Angel");
        testPractitioner.setLastName("Aguilar");
        testPractitioner.setPassword("practicantePass123");
        testPractitioner.setStatus("activo");
        testPractitioner.setGender("Masculino");

        testPractitioner.setIndigenousLanguage("Náhuatl");
        testPractitioner.setGrade(9.5);
    }

    @Test
    void testInsertPractitionerSuccess() {
        try {
            int resultId = practitionerDAO.insertPractitioner(testPractitioner);
            assertTrue(resultId > 0,
                    "El practicante debió registrarse exitosamente en ambas tablas y devolver un ID mayor a 0");

        } catch (DAOException e) {
            fail("Falló la inserción del practicante: " + e.getMessage());
        }
    }
}