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

    private static final String SQL_INSERT = "INSERT INTO practice_group (section, professor_id, period_id) VALUES (?, ?, ?)";
    private static final String SQL_SELECT_ONE = "SELECT group_id, section, professor_id, period_id FROM practice_group WHERE group_id = ?";
    private static final String SQL_SELECT_ALL = "SELECT group_id, section, professor_id, period_id FROM practice_group";
    private static final String SQL_UPDATE = "UPDATE practice_group SET section = ?, professor_id = ?, period_id = ? WHERE group_id = ?";

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
                    groupToSearch.setGroupIndex(resultSet.getInt("group_id"));
                    groupToSearch.setSection(resultSet.getString("section"));
                    groupToSearch.setProfessorId(resultSet.getInt("professor_id"));
                    groupToSearch.setPeriodId(resultSet.getInt("period_id"));
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
            groupRecovered.setGroupIndex(resultSet.getInt("group_id"));
            groupRecovered.setSection(resultSet.getString("section"));
            groupRecovered.setProfessorId(resultSet.getInt("professor_id"));
            groupRecovered.setPeriodId(resultSet.getInt("period_id"));
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