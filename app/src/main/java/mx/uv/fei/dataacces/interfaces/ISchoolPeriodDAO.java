package mx.uv.fei.dataacces.interfaces;

import mx.uv.fei.domain.dto.SchoolPeriod;

import java.util.List;

import mx.uv.fei.dataacces.exceptions.DAOException;

public interface ISchoolPeriodDAO {
    int insertSchoolPeriod(SchoolPeriod period) throws DAOException;

    SchoolPeriod recoverSchoolPeriod(int periodId) throws DAOException;

    boolean updateSchoolPeriod(SchoolPeriod period, int id) throws DAOException;

    List<SchoolPeriod> getAllSchoolPeriods() throws DAOException;
}