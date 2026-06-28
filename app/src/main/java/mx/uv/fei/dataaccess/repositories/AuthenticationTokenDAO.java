
package mx.uv.fei.dataaccess.repositories;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IAuthenticationToken;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.AuthenticationToken;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Acceso a datos de los tokens de autenticación de un solo uso.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
@Component
public class AuthenticationTokenDAO extends BaseDAO implements IAuthenticationToken {

    private static final String SQL_INSERT =
            "INSERT INTO access_token (token_value, creation_time, username) VALUES (?, ?, ?)";
    private static final String SQL_SELECT_BY_VALUE =
            "SELECT token_value, creation_time, username FROM access_token WHERE token_value = ?";
    private static final String SQL_SELECT_CREATION_TIME_BY_VALUE_AND_USER =
            "SELECT creation_time FROM access_token WHERE token_value = ? AND username = ?";

    /**
     * Crea el DAO de tokens de autenticación con la fuente de conexiones a la base de datos.
     *
     * @param databaseConnection proveedor de conexiones a la base de datos
     */
    @Inject
    public AuthenticationTokenDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    /**
     * Inserta un token de autenticación con su valor, fecha de creación y usuario asociado.
     *
     * @param tokenToInsert token con los datos a registrar
     * @throws DAOException si ocurre un error al guardar el token
     */
    @Override
    public void insertToken(AuthenticationToken tokenToInsert) throws DAOException {
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT)) {

            statement.setInt(1, tokenToInsert.getValueToken());
            statement.setTimestamp(2, Timestamp.valueOf(tokenToInsert.getTimeCreation()));
            statement.setString(3, tokenToInsert.getUserName());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new DAOException(buildTokenDebugMessage(
                    "Fallo al insertar token [%d] para el usuario '%s'",
                    tokenToInsert.getValueToken(), tokenToInsert.getUserName(), e), e);
        }
    }

    /**
     * Recupera un token de autenticación a partir de su valor.
     *
     * @param tokenValue valor del token a recuperar
     * @return token encontrado, o un {@link AuthenticationToken} vacío si no existe
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public AuthenticationToken recoverToken(int tokenValue) throws DAOException {
        AuthenticationToken recoveredToken = new AuthenticationToken();

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_VALUE)) {

            statement.setInt(1, tokenValue);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    recoveredToken = mapResultSetToAuthenticationToken(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new DAOException(buildTokenDebugMessage(
                    "Fallo al recuperar los datos del token [%d] para el usuario '%s'",
                    tokenValue, null, e), e);
        }

        return recoveredToken;
    }

    /**
     * Obtiene la fecha de creación de un token para un usuario determinado.
     *
     * @param tokenValue valor del token
     * @param userName   nombre de usuario asociado al token
     * @return fecha de creación del token, o {@code null} si no existe la combinación
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public LocalDateTime getTokenCreationTime(int tokenValue, String userName) throws DAOException {
        LocalDateTime creationTime = null;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_CREATION_TIME_BY_VALUE_AND_USER)) {

            statement.setInt(1, tokenValue);
            statement.setString(2, userName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    creationTime = resolveNullableTimestamp(resultSet, "creation_time");
                }
            }
        } catch (SQLException e) {
            throw new DAOException(buildTokenDebugMessage(
                    "Fallo al consultar tiempo de creación para el token [%d] del usuario '%s'",
                    tokenValue, userName, e), e);
        }

        return creationTime;
    }

    /**
     * Construye un token de autenticación con los valores de la fila actual del resultado.
     *
     * @param resultSet resultado posicionado en la fila a mapear
     * @return token con los datos de la fila
     * @throws SQLException si ocurre un error al leer alguna columna
     */
    private AuthenticationToken mapResultSetToAuthenticationToken(ResultSet resultSet) throws SQLException {
        AuthenticationToken token = new AuthenticationToken();
        token.setValueToken(resultSet.getInt("token_value"));
        token.setTimeCreation(resolveNullableTimestamp(resultSet, "creation_time"));
        token.setUserName(resultSet.getString("username"));
        return token;
    }

    /**
     * Obtiene una marca de tiempo como {@link LocalDateTime} tolerando valores nulos.
     *
     * @param resultSet  resultado posicionado en la fila a leer
     * @param columnName nombre de la columna de tipo marca de tiempo
     * @return fecha y hora correspondiente, o {@code null} si la columna es nula
     * @throws SQLException si ocurre un error al leer la columna
     */
    private LocalDateTime resolveNullableTimestamp(ResultSet resultSet, String columnName) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp(columnName);
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    /**
     * Construye un mensaje de error con detalle del token y los códigos de la excepción SQL.
     *
     * @param template plantilla del mensaje con marcadores para el valor del token y el usuario
     * @param tokenValue valor del token involucrado
     * @param userName   nombre de usuario asociado, o {@code null} si no aplica
     * @param e          excepción SQL de la que se extraen el estado y el código de error
     * @return mensaje de error formateado con la información de depuración
     */
    private String buildTokenDebugMessage(String template, int tokenValue, String userName, SQLException e) {
        return String.format(template + ". SQLState: %s, ErrorCode: %d",
                tokenValue, userName != null ? userName : "N/A", e.getSQLState(), e.getErrorCode());
    }
}