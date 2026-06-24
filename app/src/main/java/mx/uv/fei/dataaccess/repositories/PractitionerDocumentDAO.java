package mx.uv.fei.dataaccess.repositories;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IPractitionerDocumentDAO;
import mx.uv.fei.domain.dto.PractitionerDocument;
import mx.uv.fei.domain.enums.DocumentStatus;
import mx.uv.fei.domain.enums.DocumentType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Acceso a datos de los documentos del expediente de los practicantes.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @version 1.0
 */
@Component
public class PractitionerDocumentDAO extends BaseDAO implements IPractitionerDocumentDAO {

    private static final String SQL_INSERT_DOCUMENT =
            "INSERT INTO practitioner_document (practitioner_id, document_name, document_type, stored_file_url, status) " +
                    "VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_SELECT_DOCUMENTS_BY_PRACTITIONER =
            "SELECT * FROM practitioner_document WHERE practitioner_id = ? ORDER BY upload_date DESC";
    private static final String SQL_SELECT_DOCUMENTS_BY_PRACTITIONER_AND_TYPE =
            "SELECT * FROM practitioner_document WHERE practitioner_id = ? AND document_type = ? ORDER BY upload_date DESC";
    private static final String SQL_SELECT_DOCUMENTS_BY_PROFESSOR =
            "SELECT d.document_id, d.practitioner_id, d.document_name, d.document_type, d.stored_file_url, d.status, " +
                    "d.review_comment, d.upload_date, d.review_date, u.name, u.last_name, u.username AS matricula " +
                    "FROM practitioner_document d " +
                    "INNER JOIN user u ON d.practitioner_id = u.user_id " +
                    "INNER JOIN practitioner p ON d.practitioner_id = p.practitioner_id " +
                    "INNER JOIN practice_group pg ON p.group_id = pg.group_id " +
                    "WHERE pg.professor_id = ? " +
                    "ORDER BY u.last_name, d.document_type, d.upload_date DESC";
    private static final String SQL_UPDATE_DOCUMENT_ACCEPTED =
            "UPDATE practitioner_document SET status = ?, review_comment = NULL, review_date = CURRENT_TIMESTAMP " +
                    "WHERE document_id = ?";
    private static final String SQL_UPDATE_DOCUMENT_REJECTED =
            "UPDATE practitioner_document SET status = ?, review_comment = ?, review_date = CURRENT_TIMESTAMP " +
                    "WHERE document_id = ?";
    private static final String SQL_COUNT_DOCUMENTS_BY_TYPE =
            "SELECT COUNT(*) FROM practitioner_document WHERE practitioner_id = ? AND document_type = ?";
    private static final String SQL_COUNT_DOCUMENTS_NOT_ACCEPTED =
            "SELECT COUNT(*) FROM practitioner_document WHERE practitioner_id = ? AND document_type = ? AND status <> ?";

    @Inject
    public PractitionerDocumentDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public int insertDocument(PractitionerDocument document) throws DAOException {
        int generatedId = -1;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT_DOCUMENT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, document.getPractitionerId());
            statement.setString(2, document.getDocumentName());
            statement.setString(3, resolveType(document));
            statement.setString(4, document.getStoredFileUrl());
            statement.setString(5, resolveStatus(document));

            if (statement.executeUpdate() > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedId = generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al registrar el documento del practicante.", e);
        }

        return generatedId;
    }

    @Override
    public List<PractitionerDocument> getDocumentsByPractitioner(int practitionerId) throws DAOException {
        return recoverALL(SQL_SELECT_DOCUMENTS_BY_PRACTITIONER, this::mapResultSetToDocument, practitionerId);
    }

    @Override
    public List<PractitionerDocument> getDocumentsByPractitionerAndType(int practitionerId, String documentType) throws DAOException {
        return recoverALL(SQL_SELECT_DOCUMENTS_BY_PRACTITIONER_AND_TYPE, this::mapResultSetToDocument, practitionerId, documentType);
    }

    @Override
    public List<PractitionerDocument> getDocumentsByProfessor(int professorId) throws DAOException {
        return recoverALL(SQL_SELECT_DOCUMENTS_BY_PROFESSOR, this::mapResultSetToDocumentWithPractitioner, professorId);
    }

    @Override
    public void acceptDocument(int documentId) throws DAOException {
        updateTuple(SQL_UPDATE_DOCUMENT_ACCEPTED, statement -> {
            statement.setString(1, DocumentStatus.ACCEPTED.getDatabaseValue());
            statement.setInt(2, documentId);
        });
    }

    @Override
    public void rejectDocument(int documentId, String reviewComment) throws DAOException {
        updateTuple(SQL_UPDATE_DOCUMENT_REJECTED, statement -> {
            statement.setString(1, DocumentStatus.REJECTED.getDatabaseValue());
            statement.setString(2, reviewComment);
            statement.setInt(3, documentId);
        });
    }

    @Override
    public boolean areAllDocumentsAccepted(int practitionerId, String documentType) throws DAOException {
        int totalDocuments = countDocuments(SQL_COUNT_DOCUMENTS_BY_TYPE, practitionerId, documentType, null);
        int pendingDocuments = countDocuments(SQL_COUNT_DOCUMENTS_NOT_ACCEPTED, practitionerId, documentType,
                DocumentStatus.ACCEPTED.getDatabaseValue());

        return totalDocuments > 0 && pendingDocuments == 0;
    }

    private int countDocuments(String sqlStatement, int practitionerId, String documentType, String acceptedStatus)
            throws DAOException {
        int documentCount = 0;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlStatement)) {

            statement.setInt(1, practitionerId);
            statement.setString(2, documentType);
            if (acceptedStatus != null) {
                statement.setString(3, acceptedStatus);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    documentCount = resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al contar los documentos del practicante.", e);
        }

        return documentCount;
    }

    private String resolveStatus(PractitionerDocument document) {
        return document.getStatus() != null ? document.getStatus() : DocumentStatus.PENDING.getDatabaseValue();
    }

    private String resolveType(PractitionerDocument document) {
        return document.getDocumentType() != null ? document.getDocumentType() : DocumentType.INITIAL.getDatabaseValue();
    }

    private PractitionerDocument mapResultSetToDocument(ResultSet resultSet) throws SQLException {
        PractitionerDocument document = new PractitionerDocument();
        document.setDocumentId(resultSet.getInt("document_id"));
        document.setPractitionerId(resultSet.getInt("practitioner_id"));
        document.setDocumentName(resultSet.getString("document_name"));
        document.setDocumentType(resultSet.getString("document_type"));
        document.setStoredFileUrl(resultSet.getString("stored_file_url"));
        document.setStatus(resultSet.getString("status"));
        document.setReviewComment(resultSet.getString("review_comment"));
        document.setUploadDate(resultSet.getTimestamp("upload_date"));
        document.setReviewDate(resultSet.getTimestamp("review_date"));
        return document;
    }

    private PractitionerDocument mapResultSetToDocumentWithPractitioner(ResultSet resultSet) throws SQLException {
        PractitionerDocument document = mapResultSetToDocument(resultSet);
        document.setPractitionerName(resultSet.getString("name") + " " + resultSet.getString("last_name"));
        document.setPractitionerEnrollment(resultSet.getString("matricula"));
        return document;
    }
}
