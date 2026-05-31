package mx.uv.fei.domain.manager;

import java.util.List;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.dataaccess.interfaces.IPeriodDAO;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class PeriodManager {

    private static final Logger logger = LoggerFactory.getLogger(PeriodManager.class);

    private static final String MSG_REGISTER_ERROR = "The academic period could not be registered.";
    private static final String MSG_CONNECTION_ERROR = "A connection problem occurred. Please try again later.";
    private static final String MSG_RETRIEVE_ERROR = "An error occurred while retrieving academic periods.";
    private static final String MSG_INVALID_DATA = "The period data is incomplete or invalid.";
    private static final String STATUS_ACTIVE = "Active";

    private final IPeriodDAO periodDAO;

    @Inject
    public PeriodManager(IPeriodDAO periodDAO) {
        this.periodDAO = periodDAO;
    }

    public void registerNewPeriod(Period periodToRegister) throws ManagerException {
        if (periodToRegister.getPeriodName() == null || periodToRegister.getPeriodName().trim().isEmpty() ||
                periodToRegister.getStartDate() == null || periodToRegister.getEndDate() == null) {
            throw new ManagerException(MSG_INVALID_DATA);
        }

        periodToRegister.setPeriodStatus(STATUS_ACTIVE);

        try {
            int resultId = periodDAO.insertPeriod(periodToRegister);

            if (resultId <= 0) {
                throw new ManagerException(MSG_REGISTER_ERROR);
            }
        } catch (DAOException e) {
            logger.error(e.getMessage(), e);
            throw new ManagerException(MSG_CONNECTION_ERROR, e);
        }
    }

    public List<Period> getAllPeriods() throws ManagerException {
        List<Period> periods;
        try {
            periods = periodDAO.getAllPeriods();
        } catch (DAOException exception) {
            throw new ManagerException(MSG_RETRIEVE_ERROR, exception);
        }
        return periods;
    }
}