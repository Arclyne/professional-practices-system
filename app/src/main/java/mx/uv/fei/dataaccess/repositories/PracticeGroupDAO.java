package mx.uv.fei.dataaccess.repositories;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IPracticeGroupDAO;
import mx.uv.fei.domain.dto.PracticeGroup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Acceso a datos de los grupos de práctica.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
@Component
public class PracticeGroupDAO extends BaseDAO implements IPracticeGroupDAO {

    private static final String SQL_INSERT_PRACTICE_GROUP =
            "INSERT INTO practice_group (section, professor_id, period_id) VALUES (?, ?, ?)";
    private static final String SQL_SELECT_PRACTICE_GROUP_BY_ID =
            "SELECT group_id, section, professor_id, period_id FROM practice_group WHERE group_id = ?";
    private static final String SQL_SELECT_ALL_PRACTICE_GROUPS =
            "SELECT group_id, section, professor_id, period_id FROM practice_group";
    private static final String SQL_SELECT_GROUPS_BY_PROFESSOR_AND_PERIOD =
            "SELECT group_id, section, professor_id, period_id FROM practice_group "
                    + "WHERE professor_id = ? AND period_id = ?";
    private static final String SQL_UPDATE_PRACTICE_GROUP =
            "UPDATE practice_group SET section = ?, professor_id = ?, period_id = ? WHERE group_id = ?";

    /**
     * Crea el DAO de grupos de práctica con la fuente de conexiones a la base de datos.
     *
     * @param databaseConnection proveedor de conexiones a la base de datos
     */
    @Inject
    public PracticeGroupDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    /**
     * Inserta un nuevo grupo de práctica y devuelve su identificador generado.
     *
     * @param practiceGroup grupo con los datos a registrar
     * @return identificador generado para el grupo
     * @throws DAOException si no se inserta el grupo o si ocurre un error al guardarlo
     */
    @Override
    public int insertPracticeGroup(PracticeGroup practiceGroup) throws DAOException {
        int generatedId = -1;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT_PRACTICE_GROUP, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, practiceGroup.getSection());
            statement.setInt(2, practiceGroup.getProfessorId());
            statement.setInt(3, practiceGroup.getPeriodId());

            if (statement.executeUpdate() > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedId = generatedKeys.getInt(1);
                    }
                }
            } else {
                throw new DAOException("Duplicate entry");
            }
        } catch (SQLException e) {
            throw new DAOException("Error al guardar el grupo de prácticas en la base de datos.", e);
        }

        return generatedId;
    }

    /**
     * Recupera un grupo de práctica a partir de su identificador.
     *
     * @param groupId identificador del grupo a recuperar
     * @return grupo encontrado, o un {@link PracticeGroup} vacío si no existe
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public PracticeGroup recoverPracticeGroup(int groupId) throws DAOException {
        PracticeGroup recoveredGroup = new PracticeGroup();

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_PRACTICE_GROUP_BY_ID)) {

            statement.setInt(1, groupId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    recoveredGroup = mapResultSetToPracticeGroup(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al recuperar el grupo de prácticas de la base de datos.", e);
        }

        return recoveredGroup;
    }

    /**
     * Recupera todos los grupos de práctica registrados.
     *
     * @return lista con todos los grupos de práctica; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<PracticeGroup> getAllPracticeGroups() throws DAOException {
        return recoverALL(SQL_SELECT_ALL_PRACTICE_GROUPS, this::mapResultSetToPracticeGroup);
    }

    /**
     * Recupera los grupos de práctica de un profesor dentro de un periodo determinado.
     *
     * @param professorId identificador del profesor
     * @param periodId    identificador del periodo escolar
     * @return lista de grupos del profesor en ese periodo; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<PracticeGroup> getPracticeGroupsByProfessorAndPeriod(int professorId, int periodId) throws DAOException {
        return recoverALL(SQL_SELECT_GROUPS_BY_PROFESSOR_AND_PERIOD, this::mapResultSetToPracticeGroup,
                professorId, periodId);
    }

    /**
     * Actualiza los datos de un grupo de práctica existente.
     *
     * @param practiceGroup grupo con los datos modificados
     * @param groupId       identificador del grupo a actualizar
     * @throws DAOException si el grupo no existe o si ocurre un error al actualizar
     */
    @Override
    public void updatePracticeGroup(PracticeGroup practiceGroup, int groupId) throws DAOException {
        updateTuple(SQL_UPDATE_PRACTICE_GROUP, statement -> {
            statement.setString(1, practiceGroup.getSection());
            statement.setInt(2, practiceGroup.getProfessorId());
            statement.setInt(3, practiceGroup.getPeriodId());
            statement.setInt(4, groupId);
        });
    }

    /**
     * Construye un grupo de práctica con los valores de la fila actual del resultado.
     *
     * @param resultSet resultado posicionado en la fila a mapear
     * @return grupo de práctica con los datos de la fila
     * @throws SQLException si ocurre un error al leer alguna columna
     */
    private PracticeGroup mapResultSetToPracticeGroup(ResultSet resultSet) throws SQLException {
        PracticeGroup practiceGroup = new PracticeGroup();
        practiceGroup.setGroupId(resultSet.getInt("group_id"));
        practiceGroup.setSection(resultSet.getString("section"));
        practiceGroup.setProfessorId(resultSet.getInt("professor_id"));
        practiceGroup.setPeriodId(resultSet.getInt("period_id"));
        return practiceGroup;
    }
}