package mx.uv.fei.dataacces.interfaces;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.dto.Profesor;

public interface IProfesorDAO {
    int insertProfesor(Profesor profesor) throws DAOException;
}