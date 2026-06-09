package mx.uv.fei.domain.manager;

import java.util.List;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IPractitionerGradeDAO;
import mx.uv.fei.domain.dto.PractitionerGrade;
import mx.uv.fei.domain.exceptions.ManagerException;

@Component
public class GradingManager {

    private static final double MIN_GRADE = 0.0;
    private static final double MAX_GRADE = 10.0;

    private static final String MSG_INVALID_GRADE   = "La calificación debe ser un valor entre 0.0 y 10.0.";
    private static final String MSG_PERIOD_REQUIRED = "El periodo escolar es obligatorio para registrar la calificación.";
    private static final String MSG_ALREADY_GRADED  = "Este practicante ya tiene una calificación registrada para el periodo '%s'.";
    private static final String MSG_INSERT_ERROR    = "No se pudo registrar la calificación en el sistema.";
    private static final String MSG_UPDATE_ERROR    = "No se pudo actualizar la calificación final.";
    private static final String MSG_RETRIEVE_ERROR  = "Error al recuperar las calificaciones.";
    private static final String MSG_NOT_FOUND       = "No se encontró una calificación para el practicante en el periodo especificado.";

    private final IPractitionerGradeDAO practitionerGradeDAO;

    @Inject
    public GradingManager(IPractitionerGradeDAO practitionerGradeDAO) {
        this.practitionerGradeDAO = practitionerGradeDAO;
    }

    public PractitionerGrade registerGrade(
            int practitionerId,
            int professorId,
            String period,
            double professorGrade) throws ManagerException {

        validateGrade(professorGrade);
        validatePeriod(period);
        validateNotAlreadyGraded(practitionerId, period);

        double tentativeGrade = computeTentativeGrade(practitionerId);

        PractitionerGrade gradeRecord = buildGradeRecord(
                practitionerId, professorId, period, tentativeGrade, professorGrade);

        int generatedId = persistGrade(gradeRecord);
        gradeRecord.setGradeId(generatedId);

        return gradeRecord;
    }

    private double computeTentativeGrade(int practitionerId) throws ManagerException {
        double tentativeGrade;

        try {
            tentativeGrade = practitionerGradeDAO.calculateTentativeGrade(practitionerId);
        } catch (DAOException e) {
            throw new ManagerException(MSG_RETRIEVE_ERROR, e);
        }

        return tentativeGrade;
    }

    private void validateNotAlreadyGraded(int practitionerId, String period) throws ManagerException {
        try {
            PractitionerGrade existing = practitionerGradeDAO
                    .getGradeByPractitionerAndPeriod(practitionerId, period);

            if (existing != null) {
                throw new ManagerException(String.format(MSG_ALREADY_GRADED, period));
            }
        } catch (DAOException e) {
            throw new ManagerException(MSG_RETRIEVE_ERROR, e);
        }
    }

    private PractitionerGrade buildGradeRecord(
            int practitionerId,
            int professorId,
            String period,
            double tentativeGrade,
            double finalGrade) {

        PractitionerGrade grade = new PractitionerGrade();
        grade.setPractitionerId(practitionerId);
        grade.setProfessorId(professorId);
        grade.setPeriod(period);
        grade.setTentativeGrade(tentativeGrade);
        grade.setFinalGrade(finalGrade);

        return grade;
    }

    private int persistGrade(PractitionerGrade grade) throws ManagerException {
        int generatedId;

        try {
            generatedId = practitionerGradeDAO.insertPractitionerGrade(grade);
            if (generatedId <= 0) {
                throw new ManagerException(MSG_INSERT_ERROR);
            }
        } catch (DAOException e) {
            throw new ManagerException(MSG_INSERT_ERROR + " Causa: " + e.getMessage(), e);
        }

        return generatedId;
    }

    public void updateFinalGrade(int practitionerId, String period, double newFinalGrade) throws ManagerException {
        validateGrade(newFinalGrade);

        PractitionerGrade existing = getExistingGrade(practitionerId, period);

        try {
            boolean isUpdated = practitionerGradeDAO.updateFinalGrade(existing.getGradeId(), newFinalGrade);
            if (!isUpdated) {
                throw new ManagerException(MSG_UPDATE_ERROR);
            }
        } catch (DAOException e) {
            throw new ManagerException(MSG_UPDATE_ERROR + " Causa: " + e.getMessage(), e);
        }
    }

    private PractitionerGrade getExistingGrade(int practitionerId, String period) throws ManagerException {
        PractitionerGrade grade;

        try {
            grade = practitionerGradeDAO.getGradeByPractitionerAndPeriod(practitionerId, period);
        } catch (DAOException e) {
            throw new ManagerException(MSG_RETRIEVE_ERROR, e);
        }

        if (grade == null) {
            throw new ManagerException(MSG_NOT_FOUND);
        }

        return grade;
    }

    public List<PractitionerGrade> getGradesByProfessor(int professorId) throws ManagerException {
        List<PractitionerGrade> grades;

        try {
            grades = practitionerGradeDAO.getGradesByProfessor(professorId);
        } catch (DAOException e) {
            throw new ManagerException(MSG_RETRIEVE_ERROR, e);
        }

        return grades;
    }

    public double previewTentativeGrade(int practitionerId) throws ManagerException {
        return computeTentativeGrade(practitionerId);
    }

    public PractitionerGrade getGradeByPractitionerAndPeriod(int practitionerId, String period) throws ManagerException {
        PractitionerGrade grade;

        try {
            grade = practitionerGradeDAO.getGradeByPractitionerAndPeriod(practitionerId, period);
        } catch (DAOException e) {
            throw new ManagerException(MSG_RETRIEVE_ERROR, e);
        }

        return grade;
    }

    private void validateGrade(double grade) throws ManagerException {
        boolean isValidGrade = grade >= MIN_GRADE && grade <= MAX_GRADE;

        if (!isValidGrade) {
            throw new ManagerException(MSG_INVALID_GRADE);
        }
    }

    private void validatePeriod(String period) throws ManagerException {
        boolean isPeriodEmpty = (period == null || period.trim().isEmpty());

        if (isPeriodEmpty) {
            throw new ManagerException(MSG_PERIOD_REQUIRED);
        }
    }
}
