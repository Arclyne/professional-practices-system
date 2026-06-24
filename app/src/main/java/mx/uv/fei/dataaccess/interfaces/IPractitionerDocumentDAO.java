package mx.uv.fei.dataaccess.interfaces;

import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.dto.PractitionerDocument;

import java.util.List;

public interface IPractitionerDocumentDAO {

    int insertDocument(PractitionerDocument document) throws DAOException;

    void editDocument(int documentId, String documentName, String storedFileUrl) throws DAOException;

    List<PractitionerDocument> getDocumentsByPractitionerAndCategory(int practitionerId, String category) throws DAOException;

    List<PractitionerDocument> getDocumentsByProfessor(int professorId) throws DAOException;

    void acceptDocument(int documentId) throws DAOException;

    void rejectDocument(int documentId, String reviewComment) throws DAOException;

    boolean documentExistsForType(int practitionerId, String typeCode) throws DAOException;

    boolean areAllDocumentsAccepted(int practitionerId, String category) throws DAOException;
}
