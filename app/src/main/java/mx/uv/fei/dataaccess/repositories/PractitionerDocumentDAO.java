package mx.uv.fei.dataaccess.repositories;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IPractitionerDocumentDAO;
import mx.uv.fei.domain.dto.PractitionerDocument;
import mx.uv.fei.domain.enums.DocumentStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Acceso a datos de los documentos generales subidos por los practicantes.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @version 1.0
 */
@Component
public class PractitionerDocumentDAO extends BaseDAO implements IPractitionerDocumentDAO {

    private static final String SQL_INSERT_DOCUMENT =
            "INSERT INTO practitioner_document (practitioner_id, document_name, stored_file_url, status) " +
                    "VALUES (?, ?, ?, ?)";
    private static final String SQL_SELECT_DOCUMENTS_BY_PRACTITIONER =
            "SELECT * FROM practitioner_document WHERE practitioner_id = ? ORDER BY upload_date DESC";
    private static final String SQL_SELECT_ALL_DOCUMENTS =
            "SELECT d.document_id, d.practitioner_id, d.document_name, d.stored_file_url, d.status, " +
                    "d.upload_date, d.review_date, u.name, u.last_name, u.username AS matricula " +
                    "FROM practitioner_document d " +
                    "INNER JOIN user u ON d.practitioner_id = u.user_id " +
                    "ORDER BY d.upload_date DESC";
    private static final String SQL_UPDATE_DOCUMENT_REVIEWED =
            "UPDATE practitioner_document SET status = ?, review_date = CURRENT_TIMESTAMP WHERE document_id = ?";

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
            statement.setString(3, document.getStoredFileUrl());
            statement.setString(4, resolveStatus(document));

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
    public List<PractitionerDocument> getAllDocuments() throws DAOException {
        return recoverALL(SQL_SELECT_ALL_DOCUMENTS, this::mapResultSetToDocumentWithPractitioner);
    }

    @Override
    public void markDocumentAsReviewed(int documentId) throws DAOException {
        updateTuple(SQL_UPDATE_DOCUMENT_REVIEWED, statement -> {
            statement.setString(1, DocumentStatus.REVIEWED.getDatabaseValue());
            statement.setInt(2, documentId);
        });
    }

    private String resolveStatus(PractitionerDocument document) {
        return document.getStatus() != null ? document.getStatus() : DocumentStatus.PENDING.getDatabaseValue();
    }

    private PractitionerDocument mapResultSetToDocument(ResultSet resultSet) throws SQLException {
        PractitionerDocument document = new PractitionerDocument();
        document.setDocumentId(resultSet.getInt("document_id"));
        document.setPractitionerId(resultSet.getInt("practitioner_id"));
        document.setDocumentName(resultSet.getString("document_name"));
        document.setStoredFileUrl(resultSet.getString("stored_file_url"));
        document.setStatus(resultSet.getString("status"));
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
