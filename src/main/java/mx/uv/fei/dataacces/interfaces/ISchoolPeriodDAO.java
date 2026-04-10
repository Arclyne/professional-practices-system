package mx.uv.fei.dataacces.interfaces;

import mx.uv.fei.domain.dto.SchoolPeriod;
import mx.uv.fei.dataacces.exceptions.DAOException;

public interface ISchoolPeriodDAO {
    int insertSchoolPeriod(SchoolPeriod period) throws DAOException;
}