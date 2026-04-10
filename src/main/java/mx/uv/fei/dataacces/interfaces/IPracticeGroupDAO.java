package mx.uv.fei.dataacces.interfaces;

import mx.uv.fei.domain.dto.PracticeGroup;
import mx.uv.fei.dataacces.exceptions.DAOException;

public interface IPracticeGroupDAO {
    int insertPracticeGroup(PracticeGroup group) throws DAOException;
}