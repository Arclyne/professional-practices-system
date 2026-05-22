package mx.uv.fei.dataaccess.interfaces;

import mx.uv.fei.domain.dto.PracticeGroup;

import java.util.List;

import mx.uv.fei.dataaccess.exceptions.DAOException;

public interface IPracticeGroupDAO {
    int insertPracticeGroup(PracticeGroup group) throws DAOException;

    PracticeGroup recoverPracticeGroup(int groupIndex) throws DAOException;

    boolean updatePracticeGroup(PracticeGroup group, int groupIndex) throws DAOException;

    List<PracticeGroup> getAllPracticeGroups() throws DAOException;
}