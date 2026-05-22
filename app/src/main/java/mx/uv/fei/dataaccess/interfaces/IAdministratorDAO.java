package mx.uv.fei.dataaccess.interfaces;

import java.util.List;

import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.dto.Administrator;

public interface IAdministratorDAO {
    int insertAdministrator(Administrator Administrator) throws DAOException;

    Administrator recoverAdministrator(int AdministratorId) throws DAOException;

    List<Administrator> getAllAdministrators() throws DAOException;

    boolean updateAdministrator(Administrator AdministratorToUpdate, int id) throws DAOException;

    boolean checkIfAdminExists() throws DAOException;
}