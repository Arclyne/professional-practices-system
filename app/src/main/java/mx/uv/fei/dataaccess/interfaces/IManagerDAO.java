package mx.uv.fei.dataaccess.interfaces;

import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.dto.Manager;

import java.util.List;

public interface IManagerDAO {
    List<Manager> getManagersByOrganization(int organizationId) throws DAOException;
    int insertManager(Manager manager) throws DAOException;
    List<Manager> getAllManagers() throws DAOException;
    Manager recoverManager(int managerId) throws DAOException;
    void updateManager(Manager manager, int managerId) throws DAOException;
    void deactivateMultipleManagers(List<Integer> managerIdentifiersList) throws DAOException;
    void activateManager(int managerId) throws DAOException;
}
