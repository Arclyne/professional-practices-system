package mx.uv.fei.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.GraphicsEnvironment;
import java.sql.Date;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.manager.PeriodManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

public class CoordinatorPeriodsControllerTest extends ApplicationTest {

    private static final String FXML_PATH = "/mx/uv/fei/presentation/coordinatorPeriods.fxml";
    private static final int STATUS_COLUMN_INDEX = 3;

    private final PeriodManager periodManager = mock(PeriodManager.class);

    @BeforeAll
    static void requireGraphicalDisplay() {
        assumeFalse(GraphicsEnvironment.isHeadless(),
                "Se omiten las pruebas de interfaz porque no hay un entorno gráfico disponible.");
    }

    @Override
    public void start(Stage stage) throws Exception {
        when(periodManager.getAllPeriods()).thenReturn(List.of(buildPeriod()));

        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
        loader.setControllerFactory(controllerType -> new CoordinatorPeriodsController(periodManager));
        Parent root = loader.load();

        stage.setScene(new Scene(root));
        stage.show();
    }

    private Period buildPeriod() {
        Period period = new Period();
        period.setPeriodId(5);
        period.setPeriodName("Junio-Diciembre 2026");
        period.setStartDate(Date.valueOf("2026-06-01"));
        period.setEndDate(Date.valueOf("2026-12-12"));
        period.setPeriodStatus("Active");

        return period;
    }

    private TableView<Period> periodsTable() {
        return lookup("#periodsTableView").queryAs(TableView.class);
    }

    @Test
    void initialize_ActivePeriod_ShowsActivoStatusLabel() {
        TableColumn<Period, String> statusColumn =
                (TableColumn<Period, String>) periodsTable().getColumns().get(STATUS_COLUMN_INDEX);

        assertEquals("Activo", statusColumn.getCellData(0));
    }

    @Test
    void initialize_LoadsPeriods_ShowsListAndHidesForm() {
        VBox listPane = lookup("#listPane").queryAs(VBox.class);
        VBox formPane = lookup("#formPane").queryAs(VBox.class);

        assertTrue(listPane.isVisible());
        assertFalse(formPane.isVisible());
        assertEquals(1, periodsTable().getItems().size());
    }

    @Test
    void search_NonMatchingQuery_HidesPeriodFromTable() {
        applySearchQuery("inexistente-zzz");

        assertEquals(0, periodsTable().getItems().size());
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
