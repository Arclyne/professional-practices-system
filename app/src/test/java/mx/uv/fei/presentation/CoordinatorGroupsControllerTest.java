package mx.uv.fei.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.GraphicsEnvironment;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.dto.PracticeGroup;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.domain.manager.PeriodManager;
import mx.uv.fei.domain.manager.PracticeGroupManager;
import mx.uv.fei.domain.manager.ProfessorManager;
import mx.uv.fei.presentation.shell.ShellNavigator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

public class CoordinatorGroupsControllerTest extends ApplicationTest {

    private static final String FXML_PATH = "/mx/uv/fei/presentation/coordinatorGroups.fxml";
    private static final int PERIOD_COLUMN_INDEX = 1;
    private static final int PROFESSOR_COLUMN_INDEX = 2;

    private final PracticeGroupManager practiceGroupManager = mock(PracticeGroupManager.class);
    private final PeriodManager periodManager = mock(PeriodManager.class);
    private final ProfessorManager professorManager = mock(ProfessorManager.class);
    private final ShellNavigator shellNavigator = mock(ShellNavigator.class);

    @BeforeAll
    static void requireGraphicalDisplay() {
        assumeFalse(GraphicsEnvironment.isHeadless(),
                "Se omiten las pruebas de interfaz porque no hay un entorno gráfico disponible.");
    }

    @Override
    public void start(Stage stage) throws Exception {
        when(practiceGroupManager.getAllPracticeGroups()).thenReturn(List.of(buildGroup()));
        when(periodManager.getAllPeriods()).thenReturn(List.of(buildPeriod()));
        when(professorManager.getAllProfessors()).thenReturn(List.of(buildProfessor()));

        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
        loader.setControllerFactory(controllerType -> new CoordinatorGroupsController(
                practiceGroupManager, periodManager, professorManager, shellNavigator));
        Parent root = loader.load();

        stage.setScene(new Scene(root));
        stage.show();
    }

    private PracticeGroup buildGroup() {
        PracticeGroup group = new PracticeGroup();
        group.setGroupId(1);
        group.setSection("Grupo 1");
        group.setPeriodId(5);
        group.setProfessorId(68);

        return group;
    }

    private Period buildPeriod() {
        Period period = new Period();
        period.setPeriodId(5);
        period.setPeriodName("Junio-Diciembre 2026");

        return period;
    }

    private Professor buildProfessor() {
        Professor professor = new Professor();
        professor.setId(68);
        professor.setName("Jose Eduardo");
        professor.setLastName("Prior Hernandez");

        return professor;
    }

    private TableView<PracticeGroup> groupsTable() {
        return lookup("#groupsTableView").queryAs(TableView.class);
    }

    @Test
    void initialize_GroupWithPeriod_ResolvesPeriodName() {
        TableColumn<PracticeGroup, String> periodColumn =
                (TableColumn<PracticeGroup, String>) groupsTable().getColumns().get(PERIOD_COLUMN_INDEX);

        assertEquals("Junio-Diciembre 2026", periodColumn.getCellData(0));
    }

    @Test
    void initialize_GroupWithProfessor_ResolvesProfessorName() {
        TableColumn<PracticeGroup, String> professorColumn =
                (TableColumn<PracticeGroup, String>) groupsTable().getColumns().get(PROFESSOR_COLUMN_INDEX);

        assertEquals("Jose Eduardo Prior Hernandez", professorColumn.getCellData(0));
    }

    @Test
    void search_NonMatchingQuery_HidesGroupFromTable() {
        applySearchQuery("inexistente-zzz");

        assertEquals(0, groupsTable().getItems().size());
    }

    private void applySearchQuery(String query) {
        TextField searchTextField = lookup("#searchTextField").queryAs(TextField.class);
        interact(() -> {
            searchTextField.setText(query);
            searchTextField.fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED,
                    "", "", KeyCode.UNDEFINED, false, false, false, false));
        });
    }
}
