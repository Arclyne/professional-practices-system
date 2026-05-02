package mx.uv.fei.dataacces.interfaces;

import java.util.List;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.dto.Coordinator;

public interface ICoordinatorDAO {
    int insertCoordinator(Coordinator coordinator) throws DAOException;

    Coordinator recoverCoordinator(int coordinatorId) throws DAOException;

    boolean updateCoordinator(Coordinator coordinatorToUpdate, int id) throws DAOException;

    List<Coordinator> getAllCoordinators() throws DAOException;

}