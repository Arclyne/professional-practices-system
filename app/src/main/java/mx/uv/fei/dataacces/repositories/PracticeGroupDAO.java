package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IPracticeGroupDAO;
import mx.uv.fei.domain.dto.PracticeGroup;

@Component
public class PracticeGroupDAO extends BaseDAO implements IPracticeGroupDAO {

    private static final String SQL_INSERT = "INSERT INTO practice_group (SECTION, ID_PROFESSOR, ID_PERIOD) VALUES (?, ?, ?)";
    private static final String SQL_SELECT_ONE = "SELECT GROUP_INDEX, SECTION, ID_PROFESSOR, ID_PERIOD FROM practice_group WHERE GROUP_INDEX = ?";
    private static final String SQL_SELECT_ALL = "SELECT GROUP_INDEX, SECTION, ID_PROFESSOR, ID_PERIOD FROM practice_group";
    private static final String SQL_UPDATE = "UPDATE practice_group SET SECTION = ?, ID_PROFESSOR = ?, ID_PERIOD = ? WHERE GROUP_INDEX = ?";

    @Inject
    public PracticeGroupDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public int insertPracticeGroup(PracticeGroup group) throws DAOException {
        int generatedIndex = -1;
        try (Connection connection = databaseConnection.getConnection();
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
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ONE)) {
            statement.setInt(1, groupIndex);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    groupToSearch.setGroupIndex(resultSet.getInt("GROUP_INDEX"));
                    groupToSearch.setSection(resultSet.getString("SECTION"));
                    groupToSearch.setProfessorId(resultSet.getInt("ID_PROFESSOR"));
                    groupToSearch.setPeriodId(resultSet.getInt("ID_PERIOD"));
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
            groupRecovered.setGroupIndex(resultSet.getInt("GROUP_INDEX"));
            groupRecovered.setSection(resultSet.getString("SECTION"));
            groupRecovered.setProfessorId(resultSet.getInt("ID_PROFESSOR"));
            groupRecovered.setPeriodId(resultSet.getInt("ID_PERIOD"));
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