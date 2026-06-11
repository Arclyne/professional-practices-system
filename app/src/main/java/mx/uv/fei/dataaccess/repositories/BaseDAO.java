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

    protected boolean updateTuple(String sqlStatement, IInsterGeneric statementBinder)
            throws DAOException {
        boolean isUpdated = false;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlStatement)) {

            if (statementBinder != null) {
                statementBinder.insertGeneric(statement);
            }

            isUpdated = statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DAOException("Error al ejecutar la sentencia de actualización.", e);
        }

        return isUpdated;
    }

    protected boolean updateTuple(Connection sharedConnection, String sqlStatement, IInsterGeneric statementBinder)
            throws SQLException {
        boolean isUpdated = false;

        try (PreparedStatement statement = sharedConnection.prepareStatement(sqlStatement)) {
            if (statementBinder != null) {
                statementBinder.insertGeneric(statement);
            }
            isUpdated = statement.executeUpdate() > 0;
        }

        return isUpdated;
    }

    private void bindParameters(PreparedStatement statement, Object[] parameters) throws SQLException {
        for (int parameterIndex = 1; parameterIndex <= parameters.length; parameterIndex++) {
            statement.setObject(parameterIndex, parameters[parameterIndex - 1]);
        }
    }
}