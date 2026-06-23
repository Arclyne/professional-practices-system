package mx.uv.fei.dataaccess.interfaces;

import java.util.List;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.dto.Period;

public interface IPeriodDAO {
    int insertPeriod(Period period) throws DAOException;
    List<Period> getAllPeriods() throws DAOException;
    Period recoverPeriod(int periodId) throws DAOException;
    void updatePeriod(Period period, int periodId) throws DAOException;
    void activatePeriod(int periodId) throws DAOException;
    void deactivatePeriod(int periodId) throws DAOException;
}
