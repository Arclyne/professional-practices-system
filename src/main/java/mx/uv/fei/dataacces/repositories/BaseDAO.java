package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IInsterGeneric;
import mx.uv.fei.dataacces.interfaces.ISelectedList;

abstract class BaseDAO {
    protected final IDatabaseConnection dbconnection;

    public BaseDAO(IDatabaseConnection dbconnection) {
        this.dbconnection = dbconnection;
    }

    protected <T> List<T> recoverALL(String stament, ISelectedList<T> rowMapper, Object... parameterObjects)
            throws DAOException {
        List<T> results = new ArrayList<T>();

        try (
                Connection connection = dbconnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(stament)) {
            int index = 1;
            for (Object parameters : parameterObjects) {
                statement.setObject(index++, parameters);
            }

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

    protected boolean updateTuple(String sqlStatement, IInsterGeneric insertGeneric)
            throws DAOException {
        try (
                Connection connection = dbconnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sqlStatement)) {

            if (insertGeneric != null) {
                insertGeneric.insertGeneric(statement);
            }

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Error BD ejecutando sentencia: " + e.getMessage(), e);
        }
    }
}
