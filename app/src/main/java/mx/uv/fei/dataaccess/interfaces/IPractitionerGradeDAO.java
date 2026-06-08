package mx.uv.fei.dataaccess.interfaces;

import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.dto.PractitionerGrade;
import java.util.List;

/**
 * Contrato de acceso a datos para las calificaciones de los practicantes
 * asignadas por el profesor.
 *
 * @author Sistema de Prácticas Profesionales UV-FEI
 */
public interface IPractitionerGradeDAO {

    int insertPractitionerGrade(PractitionerGrade grade) throws DAOException;

    boolean updateFinalGrade(int gradeId, double finalGrade) throws DAOException;

    PractitionerGrade getGradeByPractitionerAndPeriod(int practitionerId, String period) throws DAOException;

    List<PractitionerGrade> getGradesByProfessor(int professorId) throws DAOException;

    double calculateTentativeGrade(int practitionerId) throws DAOException;
}
