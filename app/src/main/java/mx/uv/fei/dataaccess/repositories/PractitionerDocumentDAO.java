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
 * Acceso a datos de los documentos del expediente de los practicantes.
 *
 * Cada documento referencia un tipo del catálogo {@code document_type} mediante llave foránea.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @version 1.0
 */
@Component
public class PractitionerDocumentDAO extends BaseDAO implements IPractitionerDocumentDAO {

    private static final String SQL_INSERT_DOCUMENT =
            "INSERT INTO practitioner_document (practitioner_id, document_type_id, document_name, stored_file_url, status) " +
                    "VALUES (?, (SELECT document_type_id FROM document_type WHERE type_code = ?), ?, ?, ?)";
    private static final String SQL_EDIT_DOCUMENT =
            "UPDATE practitioner_document SET document_name = ?, stored_file_url = ?, status = ?, " +
                    "review_comment = NULL, review_date = NULL, upload_date = CURRENT_TIMESTAMP WHERE document_id = ?";
    private static final String SQL_SELECT_DOCUMENTS_BY_PRACTITIONER_AND_CATEGORY =
            "SELECT d.document_id, d.practitioner_id, d.document_name, d.stored_file_url, d.status, d.review_comment, " +
                    "d.upload_date, d.review_date, t.type_code, t.type_name, t.category " +
                    "FROM practitioner_document d " +
                    "INNER JOIN document_type t ON d.document_type_id = t.document_type_id " +
                    "WHERE d.practitioner_id = ? AND t.category = ? " +
                    "ORDER BY t.document_type_id";
    private static final String SQL_SELECT_DOCUMENTS_BY_PROFESSOR =
            "SELECT DISTINCT d.document_id, d.practitioner_id, d.document_name, d.stored_file_url, d.status, " +
                    "d.review_comment, d.upload_date, d.review_date, t.type_code, t.type_name, t.category, t.document_type_id, " +
                    "u.name, u.last_name, u.username AS matricula " +
                    "FROM practitioner_document d " +
                    "INNER JOIN document_type t ON d.document_type_id = t.document_type_id " +
                    "INNER JOIN user u ON d.practitioner_id = u.user_id " +
                    "INNER JOIN group_enrollment ge ON d.practitioner_id = ge.practitioner_id " +
                    "INNER JOIN practice_group pg ON ge.group_id = pg.group_id " +
                    "WHERE pg.professor_id = ? " +
                    "ORDER BY u.last_name, t.category, t.document_type_id";
    private static final String SQL_UPDATE_DOCUMENT_ACCEPTED =
            "UPDATE practitioner_document SET status = ?, review_comment = NULL, review_date = CURRENT_TIMESTAMP " +
                    "WHERE document_id = ?";
    private static final String SQL_UPDATE_DOCUMENT_REJECTED =
            "UPDATE practitioner_document SET status = ?, review_comment = ?, review_date = CURRENT_TIMESTAMP " +
                    "WHERE document_id = ?";
    private static final String SQL_COUNT_DOCUMENTS_FOR_TYPE =
            "SELECT COUNT(*) FROM practitioner_document d " +
                    "INNER JOIN document_type t ON d.document_type_id = t.document_type_id " +
                    "WHERE d.practitioner_id = ? AND t.type_code = ?";
    private static final String SQL_COUNT_MISSING_ACCEPTED_DOCUMENTS =
            "SELECT COUNT(*) FROM document_type t " +
                    "WHERE t.category = ? AND NOT EXISTS (" +
                    "SELECT 1 FROM practitioner_document d " +
                    "WHERE d.practitioner_id = ? AND d.document_type_id = t.document_type_id AND d.status = ?)";

    /**
     * Crea el DAO de documentos del expediente con la fuente de conexiones a la base de datos.
     *
     * @param databaseConnection proveedor de conexiones a la base de datos
     */
    @Inject
    public PractitionerDocumentDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    /**
     * Registra un documento del expediente y devuelve su identificador generado.
     *
     * @param document documento con los datos a registrar
     * @return identificador generado para el documento, o {@code -1} si no se generó
     * @throws DAOException si ocurre un error al guardar el documento
     */
    @Override
    public int insertDocument(PractitionerDocument document) throws DAOException {
        int generatedId = -1;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT_DOCUMENT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, document.getPractitionerId());
            statement.setString(2, document.getDocumentTypeCode());
            statement.setString(3, document.getDocumentName());
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

    /**
     * Reemplaza el archivo de un documento y reinicia su revisión, dejándolo en estado pendiente.
     *
     * @param documentId    identificador del documento a editar
     * @param documentName  nuevo nombre del documento
     * @param storedFileUrl nueva URL del archivo almacenado
     * @throws DAOException si el documento no existe o si ocurre un error al actualizar
     */
    @Override
    public void editDocument(int documentId, String documentName, String storedFileUrl) throws DAOException {
        updateTuple(SQL_EDIT_DOCUMENT, statement -> {
            statement.setString(1, documentName);
            statement.setString(2, storedFileUrl);
            statement.setString(3, DocumentStatus.PENDING.getDatabaseValue());
            statement.setInt(4, documentId);
        });
    }

    /**
     * Recupera los documentos de un practicante pertenecientes a una categoría.
     *
     * @param practitionerId identificador del practicante
     * @param category       categoría de documentos a recuperar
     * @return lista de documentos de la categoría; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<PractitionerDocument> getDocumentsByPractitionerAndCategory(int practitionerId, String category) throws DAOException {
        return recoverALL(SQL_SELECT_DOCUMENTS_BY_PRACTITIONER_AND_CATEGORY, this::mapResultSetToDocument, practitionerId, category);
    }

    /**
     * Recupera los documentos de los practicantes a cargo de un profesor, con sus datos de identificación.
     *
     * @param professorId identificador del profesor
     * @return lista de documentos junto con el nombre y matrícula del practicante; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<PractitionerDocument> getDocumentsByProfessor(int professorId) throws DAOException {
        return recoverALL(SQL_SELECT_DOCUMENTS_BY_PROFESSOR, this::mapResultSetToDocumentWithPractitioner, professorId);
    }

    /**
     * Marca un documento como aceptado, limpiando el comentario de revisión y fechando la revisión.
     *
     * @param documentId identificador del documento a aceptar
     * @throws DAOException si el documento no existe o si ocurre un error al actualizar
     */
    @Override
    public void acceptDocument(int documentId) throws DAOException {
        updateTuple(SQL_UPDATE_DOCUMENT_ACCEPTED, statement -> {
            statement.setString(1, DocumentStatus.ACCEPTED.getDatabaseValue());
            statement.setInt(2, documentId);
        });
    }

    /**
     * Marca un documento como rechazado, guardando el comentario de revisión y fechando la revisión.
     *
     * @param documentId    identificador del documento a rechazar
     * @param reviewComment comentario que explica el motivo del rechazo
     * @throws DAOException si el documento no existe o si ocurre un error al actualizar
     */
    @Override
    public void rejectDocument(int documentId, String reviewComment) throws DAOException {
        updateTuple(SQL_UPDATE_DOCUMENT_REJECTED, statement -> {
            statement.setString(1, DocumentStatus.REJECTED.getDatabaseValue());
            statement.setString(2, reviewComment);
            statement.setInt(3, documentId);
        });
    }

    /**
     * Indica si el practicante ya tiene registrado un documento de un tipo determinado.
     *
     * @param practitionerId identificador del practicante
     * @param typeCode       código del tipo de documento
     * @return {@code true} si existe al menos un documento de ese tipo; {@code false} en caso contrario
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public boolean documentExistsForType(int practitionerId, String typeCode) throws DAOException {
        return countDocumentsForType(practitionerId, typeCode) > 0;
    }

    /**
     * Indica si el practicante tiene aceptados todos los documentos exigidos por una categoría.
     *
     * @param practitionerId identificador del practicante
     * @param category       categoría de documentos a verificar
     * @return {@code true} si no falta ningún documento aceptado; {@code false} en caso contrario
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public boolean areAllDocumentsAccepted(int practitionerId, String category) throws DAOException {
        int missingDocuments = countMissingAcceptedDocuments(practitionerId, category);
        return missingDocuments == 0;
    }

    /**
     * Cuenta los tipos de documento de una categoría que el practicante aún no tiene aceptados.
     *
     * @param practitionerId identificador del practicante
     * @param category       categoría de documentos a verificar
     * @return número de documentos requeridos que faltan por aceptar
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    private int countMissingAcceptedDocuments(int practitionerId, String category) throws DAOException {
        int missingDocuments = 0;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_COUNT_MISSING_ACCEPTED_DOCUMENTS)) {

            statement.setString(1, category);
            statement.setInt(2, practitionerId);
            statement.setString(3, DocumentStatus.ACCEPTED.getDatabaseValue());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    missingDocuments = resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al verificar los documentos aceptados del practicante.", e);
        }

        return missingDocuments;
    }

    /**
     * Cuenta cuántos documentos de un tipo determinado tiene registrados el practicante.
     *
     * @param practitionerId identificador del practicante
     * @param typeCode       código del tipo de documento
     * @return número de documentos del tipo indicado
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    private int countDocumentsForType(int practitionerId, String typeCode) throws DAOException {
        int rowCount = 0;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_COUNT_DOCUMENTS_FOR_TYPE)) {

            statement.setInt(1, practitionerId);
            statement.setString(2, typeCode);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    rowCount = resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al consultar los documentos del practicante.", e);
        }

        return rowCount;
    }

    /**
     * Determina el estado con el que se guardará un documento, usando "pendiente" por defecto.
     *
     * @param document documento del que se obtiene el estado
     * @return estado del documento, o el valor por defecto si no tiene uno asignado
     */
    private String resolveStatus(PractitionerDocument document) {
        return document.getStatus() != null ? document.getStatus() : DocumentStatus.PENDING.getDatabaseValue();
    }

    /**
     * Construye un documento con los valores de la fila actual del resultado.
     *
     * @param resultSet resultado posicionado en la fila a mapear
     * @return documento con los datos de la fila
     * @throws SQLException si ocurre un error al leer alguna columna
     */
    private PractitionerDocument mapResultSetToDocument(ResultSet resultSet) throws SQLException {
        PractitionerDocument document = new PractitionerDocument();
        document.setDocumentId(resultSet.getInt("document_id"));
        document.setPractitionerId(resultSet.getInt("practitioner_id"));
        document.setDocumentName(resultSet.getString("document_name"));
        document.setDocumentTypeCode(resultSet.getString("type_code"));
        document.setDocumentTypeName(resultSet.getString("type_name"));
        document.setCategory(resultSet.getString("category"));
        document.setStoredFileUrl(resultSet.getString("stored_file_url"));
        document.setStatus(resultSet.getString("status"));
        document.setReviewComment(resultSet.getString("review_comment"));
        document.setUploadDate(resultSet.getTimestamp("upload_date"));
        document.setReviewDate(resultSet.getTimestamp("review_date"));
        return document;
    }

    /**
     * Construye un documento e incorpora los datos de identificación del practicante asociado.
     *
     * @param resultSet resultado posicionado en la fila a mapear
     * @return documento con los datos de la fila y del practicante
     * @throws SQLException si ocurre un error al leer alguna columna
     */
    private PractitionerDocument mapResultSetToDocumentWithPractitioner(ResultSet resultSet) throws SQLException {
        PractitionerDocument document = mapResultSetToDocument(resultSet);
        document.setPractitionerName(resultSet.getString("name") + " " + resultSet.getString("last_name"));
        document.setPractitionerEnrollment(resultSet.getString("matricula"));
        return document;
    }
}