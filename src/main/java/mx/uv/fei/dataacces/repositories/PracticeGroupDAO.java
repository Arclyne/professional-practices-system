package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IPracticeGroupDAO;
import mx.uv.fei.domain.dto.PracticeGroup;

@Repository
public class PracticeGroupDAO extends BaseDAO implements IPracticeGroupDAO {

    private static final String SQL_INSERT = "INSERT INTO GRUPO_PRACTICAS (SECCION, ID_PROFESOR, ID_PERIODO) VALUES (?, ?, ?)";
    private static final String SQL_SELECT_ONE = "SELECT INDEX_GRUPO, SECCION, ID_PROFESOR, ID_PERIODO FROM GRUPO_PRACTICAS WHERE INDEX_GRUPO = ?";
    private static final String SQL_SELECT_ALL = "SELECT INDEX_GRUPO, SECCION, ID_PROFESOR, ID_PERIODO FROM GRUPO_PRACTICAS";
    private static final String SQL_UPDATE = "UPDATE GRUPO_PRACTICAS SET SECCION = ?, ID_PROFESOR = ?, ID_PERIODO = ? WHERE INDEX_GRUPO = ?";

    @Autowired
    public PracticeGroupDAO(IDatabaseConnection dbConnection) {
        super(dbConnection);
    }

    @Override
    public int insertPracticeGroup(PracticeGroup group) throws DAOException {
        int generatedIndex = -1;

        try (
                Connection connection = dbConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setString(1, group.getSection());
            statement.setInt(2, group.getProfessorId());
            statement.setInt(3, group.getPeriodId());

            if (statement.executeUpdate() > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedIndex = generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error saving the practice group to the database. Ensure Professor ID and Period ID exist.", e);
        }

        return generatedIndex;
    }

    @Override
    public PracticeGroup recoverPracticeGroup(int groupIndex) throws DAOException {
        PracticeGroup groupToSearch = new PracticeGroup();
        
        try (
                Connection connection = dbConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ONE)) {
            
            statement.setInt(1, groupIndex);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    groupToSearch.setGroupIndex(resultSet.getInt("INDEX_GRUPO"));
                    groupToSearch.setSection(resultSet.getString("SECCION"));
                    groupToSearch.setProfessorId(resultSet.getInt("ID_PROFESOR"));
                    groupToSearch.setPeriodId(resultSet.getInt("ID_PERIODO"));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error recovering the practice group from the database.", e);
        }
        return groupToSearch;
    }

    @Override
    public List<PracticeGroup> getAllPracticeGroups() throws DAOException {
        return recoverALL(SQL_SELECT_ALL, resultSet -> {
            PracticeGroup groupRecovered = new PracticeGroup();
            
            groupRecovered.setGroupIndex(resultSet.getInt("INDEX_GRUPO"));
            groupRecovered.setSection(resultSet.getString("SECCION"));
            groupRecovered.setProfessorId(resultSet.getInt("ID_PROFESOR"));
            groupRecovered.setPeriodId(resultSet.getInt("ID_PERIODO"));

            return groupRecovered;
        });
    }

    @Override
    public boolean updatePracticeGroup(PracticeGroup group, int groupIndex) throws DAOException {
        return updateTuple(SQL_UPDATE, statement -> {
            statement.setString(1, group.getSection());
            statement.setInt(2, group.getProfessorId());
            statement.setInt(3, group.getPeriodId());
            statement.setInt(4, groupIndex);
        });
    }
}