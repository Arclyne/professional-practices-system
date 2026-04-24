package mx.uv.fei.domain.manager;

import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.dataacces.interfaces.IActivityDAO;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.repositories.ActivityDAO;
import mx.uv.fei.dataacces.exceptions.DAOException;

public class ProyectManager {

    private final IActivityDAO activityDAO;

    public ProyectManager(IDatabaseConnection databaseConnection) {
        this.activityDAO = new ActivityDAO(databaseConnection);
    }

    public boolean registerNewActivity(Activity activityToRegister) throws DAOException {
        validateActivityData(activityToRegister);
        return activityDAO.insertActivity(activityToRegister);
    }

    private void validateActivityData(Activity activityToValidate) {
        if (activityToValidate.getName() == null || activityToValidate.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la actividad es obligatorio.");
        }

        if (activityToValidate.getManager() == null || activityToValidate.getManager().trim().isEmpty()) {
            throw new IllegalArgumentException("El encargado de la actividad es obligatorio.");
        }

        if (activityToValidate.getStartDate() != null && activityToValidate.getEndDate() != null) {
            if (activityToValidate.getEndDate().before(activityToValidate.getStartDate())) {
                throw new IllegalArgumentException("La fecha de término no puede ser anterior a la fecha de inicio.");
            }
        }
    }
}