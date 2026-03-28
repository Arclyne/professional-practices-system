package mx.uv.fei.dataacces.interfaces;

import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.exceptions.DAOException;

public interface CoordinatorDAOInterface {
    boolean insertCoordinador(Coordinator coordinator) throws DAOException;
}