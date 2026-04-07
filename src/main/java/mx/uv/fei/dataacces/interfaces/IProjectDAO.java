package mx.uv.fei.dataacces.interfaces;

import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.exceptions.DAOException;

public interface IProjectDAO {
    boolean insertProject(Project project) throws DAOException;
}
