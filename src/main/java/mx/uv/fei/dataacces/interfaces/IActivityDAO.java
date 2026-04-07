package mx.uv.fei.dataacces.interfaces;

import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.exceptions.DAOException;

public interface IActivityDAO {
    boolean insertActivity(Activity activity) throws DAOException;
}
