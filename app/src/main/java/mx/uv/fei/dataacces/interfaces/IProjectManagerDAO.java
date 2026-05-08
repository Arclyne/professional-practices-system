package mx.uv.fei.dataacces.interfaces;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.dto.Manager;

import java.util.List;

public interface IProjectManagerDAO {
    List<Manager> getManagersByOrganization(int organizationId) throws DAOException;
}