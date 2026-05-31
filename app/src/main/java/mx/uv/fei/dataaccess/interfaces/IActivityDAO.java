package mx.uv.fei.dataaccess.interfaces;

import java.util.List;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.dto.Activity;

public interface IActivityDAO {
    boolean insertActivity(Activity activity) throws DAOException;

    Activity recoverActivity(String activityName, int groupId) throws DAOException;

    List<Activity> getAllActivities() throws DAOException;

    boolean updateActivity(Activity activity, int activityId) throws DAOException;
}