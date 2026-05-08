package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IPractitionerDAO;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.util.List;

@Component
public class PendingPractitionerManager {

    private final IPractitionerDAO practitionerDataAccessObject;

    @Inject
    public PendingPractitionerManager(IPractitionerDAO practitionerDataAccessObject) {
        this.practitionerDataAccessObject = practitionerDataAccessObject;
    }

    public List<Practitioner> retrievePractitionersPendingAssignment() throws ManagerException {
        List<Practitioner> retrievedPendingList;

        try {
            retrievedPendingList = practitionerDataAccessObject.retrievePractitionersPendingAssignment();
        } catch (DAOException dataAccessObjectException) {
            throw new ManagerException("Ocurrio un error al intentar recuperar la lista de practicantes pendientes.", dataAccessObjectException);
        }

        return retrievedPendingList;
    }
}