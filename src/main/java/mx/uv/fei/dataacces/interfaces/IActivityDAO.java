package mx.uv.fei.dataacces.interfaces;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.dto.Activity;

public interface IActivityDAO {
    boolean insertActivity(Activity activity) throws DAOException;
}
