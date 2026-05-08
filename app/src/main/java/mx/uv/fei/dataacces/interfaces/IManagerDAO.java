package mx.uv.fei.dataacces.interfaces;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.dto.Manager;

import java.util.List;

public interface IManagerDAO {
    List<Manager> getManagersByOrganization(int organizationId) throws DAOException;
    boolean insertManager(Manager manager) throws DAOException;

}