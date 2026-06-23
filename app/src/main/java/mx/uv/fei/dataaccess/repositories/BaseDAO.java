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

abstract class BaseDAO {

    protected final IDatabaseConnection databaseConnection;

    @Inject
    public BaseDAO(IDatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

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

    private void bindParameters(PreparedStatement statement, Object[] parameters) throws SQLException {
        for (int parameterIndex = 1; parameterIndex <= parameters.length; parameterIndex++) {
            statement.setObject(parameterIndex, parameters[parameterIndex - 1]);
        }
    }
}