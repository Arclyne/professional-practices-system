package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IPeriodDAO;
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

    private void validatePeriodData(Period period) throws ManagerException {
        if (period.getPeriodName() == null || period.getPeriodName().trim().isEmpty()) {
            throw new ManagerException("El nombre del periodo es obligatorio.");
        }
        if (period.getStartDate() == null || period.getEndDate() == null) {
            throw new ManagerException("Las fechas de inicio y fin del periodo son obligatorias.");
        }
    }
}