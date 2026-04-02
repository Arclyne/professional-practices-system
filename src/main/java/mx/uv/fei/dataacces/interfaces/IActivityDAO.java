package mx.uv.fei.dataacces.interfaces;

import java.util.List;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.dto.Activity;

public interface IActivityDAO {
    boolean insertActivity(Activity activity) throws DAOException;

    Activity recoverActivity(String activityName, String manager) throws DAOException;

    List<Activity> getAllActivity() throws DAOException;
}
