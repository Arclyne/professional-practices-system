package mx.uv.fei.dataacces.interfaces;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.dto.Project;

public interface IPorjectDAO {
    boolean insert(Project project) throws DAOException;
}
