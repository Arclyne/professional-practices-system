package mx.uv.fei.domain.manager.evaluation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IPractitionerGradeDAO;
import mx.uv.fei.domain.common.validators.BaseValidator;
import mx.uv.fei.domain.common.validators.FieldLengthLimits;
import mx.uv.fei.domain.dto.PractitionerGrade;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.util.List;

@Component
public class GradingManager {

    private static final double MINIMUM_GRADE = 0.0;
    private static final double MAXIMUM_GRADE = 10.0;

    private final IPractitionerGradeDAO practitionerGradeDAO;

    @Inject
    public GradingManager(IPractitionerGradeDAO practitionerGradeDAO) {
        this.practitionerGradeDAO = practitionerGradeDAO;
    }

    public PractitionerGrade registerGrade(int practitionerId, int professorId, String period, double finalGrade) throws ManagerException {
        validateGrade(finalGrade);
        validatePeriod(period);
        validateNotAlreadyGraded(practitionerId, period);

        double tentativeGrade = computeTentativeGrade(practitionerId);
        PractitionerGrade gradeRecord = buildGradeRecord(practitionerId, professorId, period, tentativeGrade, finalGrade);
        int generatedId = persistGrade(gradeRecord);
        gradeRecord.setGradeId(generatedId);

        return gradeRecord;
    }

    public void updateFinalGrade(int practitionerId, String period, double newFinalGrade) throws ManagerException {
        validateGrade(newFinalGrade);
        PractitionerGrade existingGrade = getExistingGrade(practitionerId, period);

        try {
            practitionerGradeDAO.updateFinalGrade(existingGrade.getGradeId(), newFinalGrade);
        } catch (DAOException e) {
            throw new ManagerException("No se pudo actualizar la calificación final.", e);
        }
    }

    public List<PractitionerGrade> getGradesByProfessor(int professorId) throws ManagerException {
        try {
            return practitionerGradeDAO.getGradesByProfessor(professorId);
        } catch (DAOException e) {
            throw new ManagerException("Error al recuperar las calificaciones.", e);
        }
    }

    public double previewTentativeGrade(int practitionerId) throws ManagerException {
        return computeTentativeGrade(practitionerId);
    }

    public PractitionerGrade getGradeByPractitionerAndPeriod(int practitionerId, String period) throws ManagerException {
        try {
            return practitionerGradeDAO.getGradeByPractitionerAndPeriod(practitionerId, period);
        } catch (DAOException e) {
            throw new ManagerException("Error al recuperar las calificaciones.", e);
        }
    }

    private double computeTentativeGrade(int practitionerId) throws ManagerException {
        try {
            return practitionerGradeDAO.calculateTentativeGrade(practitionerId);
        } catch (DAOException e) {
            throw new ManagerException("Error al recuperar las calificaciones.", e);
        }
    }

    private void validateNotAlreadyGraded(int practitionerId, String period) throws ManagerException {
        try {
            PractitionerGrade existingGrade = practitionerGradeDAO.getGradeByPractitionerAndPeriod(practitionerId, period);
            if (existingGrade != null) {
                throw new ManagerException(String.format(
                        "Este practicante ya tiene una calificación registrada para el periodo '%s'.", period));
            }
        } catch (DAOException e) {
            throw new ManagerException("Error al recuperar las calificaciones.", e);
        }
    }

    private PractitionerGrade buildGradeRecord(int practitionerId, int professorId, String period, double tentativeGrade, double finalGrade) {
        PractitionerGrade practitionerGrade = new PractitionerGrade();
        practitionerGrade.setPractitionerId(practitionerId);
        practitionerGrade.setProfessorId(professorId);
        practitionerGrade.setPeriod(period);
        practitionerGrade.setTentativeGrade(tentativeGrade);
        practitionerGrade.setFinalGrade(finalGrade);
        return practitionerGrade;
    }

    private int persistGrade(PractitionerGrade practitionerGrade) throws ManagerException {
        try {
            int generatedId = practitionerGradeDAO.insertPractitionerGrade(practitionerGrade);
            if (generatedId <= 0) {
                throw new ManagerException("No se pudo registrar la calificación en el sistema.");
            }
            return generatedId;
        } catch (DAOException e) {
            throw new ManagerException("No se pudo registrar la calificación en el sistema.", e);
        }
    }

    private PractitionerGrade getExistingGrade(int practitionerId, String period) throws ManagerException {
        try {
            PractitionerGrade existingGrade = practitionerGradeDAO.getGradeByPractitionerAndPeriod(practitionerId, period);
            if (existingGrade == null) {
                throw new ManagerException("No se encontró una calificación para el practicante en el periodo especificado.");
            }
            return existingGrade;
        } catch (DAOException e) {
            throw new ManagerException("Error al recuperar las calificaciones.", e);
        }
    }

    private void validateGrade(double grade) throws ManagerException {
        if (grade < MINIMUM_GRADE || grade > MAXIMUM_GRADE) {
            throw new ManagerException("La calificación debe ser un valor entre 0.0 y 10.0.");
        }
    }

    private void validatePeriod(String period) throws ManagerException {
        BaseValidator.validateString(period,
                "El periodo escolar es obligatorio para registrar la calificación.");
        BaseValidator.validateMaxLength(period, FieldLengthLimits.GRADE_PERIOD_MAX,
                "El periodo escolar no puede exceder " + FieldLengthLimits.GRADE_PERIOD_MAX + " caracteres.");
    }
}