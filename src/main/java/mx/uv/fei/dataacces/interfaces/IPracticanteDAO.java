package mx.uv.fei.dataacces.interfaces;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.dto.Practicante;

public interface IPracticanteDAO {
    int insertPracticante(Practicante practicante) throws DAOException;
}