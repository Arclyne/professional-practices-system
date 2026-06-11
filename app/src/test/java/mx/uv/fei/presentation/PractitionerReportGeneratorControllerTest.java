package mx.uv.fei.presentation;

import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.GraphicsEnvironment;
import java.time.LocalDate;
import java.util.ArrayList;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;

import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.manager.ActivityManager;
import mx.uv.fei.domain.manager.MonthlyReportManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.state.RootState;
import mx.uv.fei.domain.statemachine.state.SessionState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

public class PractitionerReportGeneratorControllerTest extends ApplicationTest {

    private static final String FXML_PATH = "/mx/uv/fei/presentation/practitionerReportGenerator.fxml";
    private static final String CREATE_BUTTON_TEXT = "Generar y Enviar Reporte";
    private static final int PRACTITIONER_ID = 123;
    private static final String REPORT_MONTH = "Junio";
    private static final LocalDate MONTH_START = LocalDate.of(2026, 6, 1);
    private static final LocalDate MONTH_END = LocalDate.of(2026, 6, 30);

    private final MonthlyReportManager reportManager = mock(MonthlyReportManager.class);
    private final ActivityManager activityManager = mock(ActivityManager.class);
    private final AppStore appStore = mock(AppStore.class);

    @BeforeAll
    static void requireGraphicalDisplay() {
        assumeFalse(GraphicsEnvironment.isHeadless(),
                "Se omiten las pruebas de interfaz porque no hay un entorno gráfico disponible.");
    }

    @Override
    public void start(Stage stage) throws Exception {
        User practitioner = new User();
        practitioner.setId(PRACTITIONER_ID);
        when(appStore.getState()).thenReturn(RootState.initialState().withSessionState(new SessionState(practitioner)));
        when(activityManager.getPractitionerLogbook(PRACTITIONER_ID)).thenReturn(new ArrayList<>());

        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
        loader.setControllerFactory(controllerType ->
                new PractitionerReportGeneratorController(reportManager, activityManager, appStore));
        Parent root = loader.load();

        stage.setScene(new Scene(root));
        stage.show();
    }

    @SuppressWarnings("unchecked")
    private void fillReportForm() {
        interact(() -> {
            lookup("#comboMonth").queryAs(ComboBox.class).setValue(REPORT_MONTH);
            lookup("#dateStart").queryAs(DatePicker.class).setValue(MONTH_START);
            lookup("#dateEnd").queryAs(DatePicker.class).setValue(MONTH_END);
        });
    }

    private void clickCreateReportButton() {
        Button createReportButton = lookup((Node node) -> node instanceof Button button
                && button.getText() != null && button.getText().contains(CREATE_BUTTON_TEXT)).queryButton();
        interact(createReportButton::fire);
    }

    @Test
    void handleCreateReportAction_ValidForm_CreatesMonthlyReport() throws Exception {
        fillReportForm();

        clickCreateReportButton();

        verify(reportManager).createReportAndLinkActivities(any(), any());
    }
}
