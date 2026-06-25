package mx.uv.fei.domain.manager.academic;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IPeriodDAO;
import mx.uv.fei.domain.common.validators.BaseValidator;
import mx.uv.fei.domain.common.validators.FieldLengthLimits;
import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.exceptions.ManagerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Component
public class PeriodManager {

    private static final Logger log = LoggerFactory.getLogger(PeriodManager.class);
    private static final String PERIOD_STATUS_ACTIVE = "Active";

    private final IPeriodDAO periodDAO;

    @Inject
    public PeriodManager(IPeriodDAO periodDAO) {
        this.periodDAO = periodDAO;
    }

    public void registerNewPeriod(Period period) throws ManagerException {
        validatePeriodData(period);
        period.setPeriodStatus(PERIOD_STATUS_ACTIVE);

        try {
            int generatedId = periodDAO.insertPeriod(period);
            if (generatedId <= 0) {
                throw new ManagerException("No se pudo registrar el periodo académico.");
            }
        } catch (DAOException e) {
            log.error("Error al insertar el periodo académico.", e);
            throw new ManagerException("Ocurrió un problema de conexión. Por favor, intente más tarde.", e);
        }
    }

    public List<Period> getAllPeriods() throws ManagerException {
        try {
            return periodDAO.getAllPeriods();
        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un error al recuperar los periodos académicos.", e);
        }
    }

    public Period getActivePeriod() throws ManagerException {
        try {
            return periodDAO.getActivePeriod();
        } catch (DAOException e) {
            throw new ManagerException("No se pudo recuperar el periodo académico activo.", e);
        }
    }

    public Period getPeriodById(int periodId) throws ManagerException {
        try {
            return periodDAO.recoverPeriod(periodId);
        } catch (DAOException e) {
            throw new ManagerException("No se pudo recuperar la información del periodo académico.", e);
        }
    }

    public void updatePeriod(Period period, int periodId) throws ManagerException {
        validatePeriodData(period);

        try {
            periodDAO.updatePeriod(period, periodId);
        } catch (DAOException e) {
            log.error("Error al actualizar el periodo académico.", e);
            throw new ManagerException("Ocurrió un problema de conexión. Por favor, intente más tarde.", e);
        }
    }

    public void activatePeriod(int periodId) throws ManagerException {
        try {
            periodDAO.activatePeriod(periodId);
        } catch (DAOException e) {
            log.error("Error al activar el periodo académico con ID: {}.", periodId, e);
            throw new ManagerException("No se pudo activar el periodo académico.", e);
        }
    }

    public void inactivatePeriod(int periodId) throws ManagerException {
        try {
            periodDAO.deactivatePeriod(periodId);
        } catch (DAOException e) {
            log.error("Error al inactivar el periodo académico con ID: {}.", periodId, e);
            throw new ManagerException("No se pudo inactivar el periodo académico.", e);
        }
    }

    private void validatePeriodData(Period period) throws ManagerException {
        BaseValidator.validateString(period.getPeriodName(),
                "El nombre del periodo es obligatorio.");
        BaseValidator.validateMaxLength(period.getPeriodName(), FieldLengthLimits.PERIOD_NAME_MAX,
                "El nombre del periodo no puede exceder " + FieldLengthLimits.PERIOD_NAME_MAX + " caracteres.");
        if (period.getStartDate() == null || period.getEndDate() == null) {
            throw new ManagerException("Las fechas de inicio y fin del periodo son obligatorias.");
        }
        BaseValidator.validateStartBeforeEnd(period.getStartDate(), period.getEndDate(),
                "La fecha de inicio debe ser anterior a la fecha de fin del periodo.");
    }
}