package mx.uv.fei.dataaccess.interfaces;

import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.dto.Activity;
import java.util.List;

public interface IActivityDAO {
    int insertActivity(Activity activity) throws DAOException;
    void updateActivity(Activity activity, int activityId) throws DAOException;
    List<Activity> getActivitiesByPractitioner(int practitionerId) throws DAOException;
    List<Activity> getActivitiesByReport(int reportId) throws DAOException;

    void assignActivityToReport(int activityId, int reportId) throws DAOException;
    void removeActivityFromReport(int activityId) throws DAOException;
}
