package mx.uv.fei.dataaccess.interfaces;

import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.dto.PractitionerDocument;

import java.util.List;

public interface IPractitionerDocumentDAO {

    int insertDocument(PractitionerDocument document) throws DAOException;

    List<PractitionerDocument> getDocumentsByPractitioner(int practitionerId) throws DAOException;

    List<PractitionerDocument> getAllDocuments() throws DAOException;

    void markDocumentAsReviewed(int documentId) throws DAOException;
}
