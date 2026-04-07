package mx.uv.fei.dataacces.interfaces;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.dto.Coordinator;

public interface ICoordinatorDAO {
    int insertCoordinator(Coordinator coordinator) throws DAOException;
}