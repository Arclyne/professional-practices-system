package mx.uv.fei.dataacces.interfaces;

import java.util.List;
import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.dataacces.exceptions.DAOException;

public interface ICoordinatorDAO {

    int insertCoordinator(Coordinator coordinator) throws DAOException;

    Coordinator recoverCoordinator(int coordinatorId) throws DAOException;

    Coordinator getCurrentCoordinator() throws DAOException;

    List<Coordinator> getAllCoordinators() throws DAOException;

    boolean updateCoordinator(Coordinator coordinatorToUpdate, int id) throws DAOException;
}