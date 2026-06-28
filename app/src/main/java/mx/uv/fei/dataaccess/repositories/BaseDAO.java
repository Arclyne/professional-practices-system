package mx.uv.fei.dataaccess.repositories;

import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IInsterGeneric;
import mx.uv.fei.dataaccess.interfaces.ISelectedList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Base abstracta de los DAOs con operaciones genéricas de inserción, actualización y consulta.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
abstract class BaseDAO {

    protected final IDatabaseConnection databaseConnection;

    /**
     * Crea la base con la fuente de conexiones que utilizarán los DAOs derivados.
     *
     * @param databaseConnection proveedor de conexiones a la base de datos
     */
    @Inject
    public BaseDAO(IDatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    /**
     * Ejecuta una consulta de selección y transforma cada fila del resultado en un objeto.
     *
     * @param <T>          tipo de objeto que produce el mapeador de filas
     * @param sqlStatement sentencia SQL de consulta a ejecutar
     * @param rowMapper    función que convierte una fila del {@link ResultSet} en un objeto
     * @param parameters   parámetros posicionales que se enlazan a la sentencia
     * @return lista con los objetos mapeados; vacía si no hay resultados
     * @throws DAOException si ocurre un error al ejecutar la consulta
     */
    protected <T> List<T> recoverALL(String sqlStatement, ISelectedList<T> rowMapper, Object... parameters)
            throws DAOException {
        List<T> results = new ArrayList<>();

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlStatement)) {

            bindParameters(statement, parameters);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al ejecutar la consulta genérica.", e);
        }

        return results;
    }

    /**
     * Ejecuta una inserción y recupera la llave primaria autogenerada.
     *
     * @param sqlStatement   sentencia SQL de inserción a ejecutar
     * @param statementBinder función que enlaza los valores a la sentencia preparada
     * @return identificador generado por la inserción, o {@code -1} si no se generó ninguno
     * @throws DAOException si ocurre un error al ejecutar la inserción
     */
    protected int insertTuple(String sqlStatement, IInsterGeneric statementBinder)
            throws DAOException {
        int generatedId = -1;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlStatement, Statement.RETURN_GENERATED_KEYS)) {

            if (statementBinder != null) {
                statementBinder.insertGeneric(statement);
            }

            if (statement.executeUpdate() > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedId = generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al ejecutar la inserción en la base de datos.", e);
        }

        return generatedId;
    }

    /**
     * Ejecuta una actualización abriendo una conexión propia y verifica que afecte algún registro.
     *
     * @param sqlStatement   sentencia SQL de actualización a ejecutar
     * @param statementBinder función que enlaza los valores a la sentencia preparada
     * @throws DAOException si no se modifica ningún registro o si ocurre un error al ejecutar la sentencia
     */
    protected void updateTuple(String sqlStatement, IInsterGeneric statementBinder)
            throws DAOException {
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlStatement)) {

            if (statementBinder != null) {
                statementBinder.insertGeneric(statement);
            }

            if (statement.executeUpdate() == 0) {
                throw new DAOException("No se encontró el registro que se intentó modificar.");
            }

        } catch (SQLException e) {
            throw new DAOException("Error al ejecutar la sentencia de actualización.", e);
        }
    }

    /**
     * Ejecuta una actualización sobre una conexión compartida, útil dentro de transacciones.
     *
     * @param sharedConnection conexión compartida que controla la transacción en curso
     * @param sqlStatement     sentencia SQL de actualización a ejecutar
     * @param statementBinder  función que enlaza los valores a la sentencia preparada
     * @throws SQLException si no se modifica ningún registro o si ocurre un error al ejecutar la sentencia
     */
    protected void updateTuple(Connection sharedConnection, String sqlStatement, IInsterGeneric statementBinder)
            throws SQLException {
        try (PreparedStatement statement = sharedConnection.prepareStatement(sqlStatement)) {
            if (statementBinder != null) {
                statementBinder.insertGeneric(statement);
            }
            if (statement.executeUpdate() == 0) {
                throw new SQLException("No se encontró el registro que se intentó modificar.");
            }
        }
    }

    /**
     * Enlaza de forma posicional los parámetros recibidos a la sentencia preparada.
     *
     * @param statement  sentencia preparada sobre la que se enlazan los parámetros
     * @param parameters valores a asignar en el orden de los marcadores de la sentencia
     * @throws SQLException si ocurre un error al asignar algún parámetro
     */
    private void bindParameters(PreparedStatement statement, Object[] parameters) throws SQLException {
        for (int parameterIndex = 1; parameterIndex <= parameters.length; parameterIndex++) {
            statement.setObject(parameterIndex, parameters[parameterIndex - 1]);
        }
    }
}