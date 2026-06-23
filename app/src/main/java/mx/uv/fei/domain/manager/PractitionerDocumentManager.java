package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IPractitionerDocumentDAO;
import mx.uv.fei.domain.dto.PractitionerDocument;
import mx.uv.fei.domain.enums.DocumentStatus;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.io.File;
import java.util.List;

@Component
public class PractitionerDocumentManager {

    private final IPractitionerDocumentDAO documentDAO;
    private final CloudStorageManager cloudStorageManager;

    @Inject
    public PractitionerDocumentManager(IPractitionerDocumentDAO documentDAO, CloudStorageManager cloudStorageManager) {
        this.documentDAO = documentDAO;
        this.cloudStorageManager = cloudStorageManager;
    }

    public void uploadDocument(int practitionerId, File file) throws ManagerException {
        String storedFileUrl = cloudStorageManager.uploadEvidenceFile(file);
        PractitionerDocument document = buildPendingDocument(practitionerId, file.getName(), storedFileUrl);

        try {
            int generatedId = documentDAO.insertDocument(document);
            if (generatedId <= 0) {
                throw new ManagerException("No se pudo registrar el documento subido.");
            }
        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un error al guardar el documento.", e);
        }
    }

    public List<PractitionerDocument> getPractitionerDocuments(int practitionerId) throws ManagerException {
        try {
            return documentDAO.getDocumentsByPractitioner(practitionerId);
        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un error al cargar los documentos.", e);
        }
    }

    public List<PractitionerDocument> getAllDocuments() throws ManagerException {
        try {
            return documentDAO.getAllDocuments();
        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un error al cargar los documentos de los practicantes.", e);
        }
    }

    public void markDocumentAsReviewed(int documentId) throws ManagerException {
        try {
            documentDAO.markDocumentAsReviewed(documentId);
        } catch (DAOException e) {
            throw new ManagerException("No se pudo marcar el documento como revisado.", e);
        }
    }

    private PractitionerDocument buildPendingDocument(int practitionerId, String documentName, String storedFileUrl) {
        PractitionerDocument document = new PractitionerDocument();
        document.setPractitionerId(practitionerId);
        document.setDocumentName(documentName);
        document.setStoredFileUrl(storedFileUrl);
        document.setStatus(DocumentStatus.PENDING.getDatabaseValue());
        return document;
    }
}
