package mx.uv.fei.presentation.coordinator;

import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.awt.GraphicsEnvironment;
import java.time.LocalDate;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;

import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.academic.PeriodManager;
import mx.uv.fei.presentation.components.FormField;
import mx.uv.fei.presentation.shell.ShellNavigator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

public class RegisterPeriodControllerTest extends ApplicationTest {

    private static final String FXML_PATH = "/mx/uv/fei/presentation/registerPeriod.fxml";
    private static final String PERIOD_NAME = "Agosto 2027 - Enero 2028";
    private static final LocalDate PERIOD_START = LocalDate.of(2027, 8, 1);
    private static final LocalDate PERIOD_END = LocalDate.of(2028, 1, 31);

    private final PeriodManager periodManager = mock(PeriodManager.class);
    private final ShellNavigator shellNavigator = mock(ShellNavigator.class);

    @BeforeAll
    static void requireGraphicalDisplay() {
        assumeFalse(GraphicsEnvironment.isHeadless(),
                "Se omiten las pruebas de interfaz porque no hay un entorno gráfico disponible.");
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
        loader.setControllerFactory(controllerType -> new RegisterPeriodController(periodManager, shellNavigator));
        Parent root = loader.load();

        stage.setScene(new Scene(root));
        stage.show();
    }

    private void fillPeriodForm() {
        interact(() -> {
            lookup("#fieldPeriodName").queryAs(FormField.class).setText(PERIOD_NAME);
            lookup("#startDatePicker").queryAs(DatePicker.class).setValue(PERIOD_START);
            lookup("#endDatePicker").queryAs(DatePicker.class).setValue(PERIOD_END);
        });
    }

    private void clickSaveButton() {
        interact(() -> lookup("#saveButton").queryAs(Button.class).fire());
    }

    @Test
    void handleActionSaveButton_CompleteForm_RegistersPeriod() throws ManagerException {
        fillPeriodForm();

        clickSaveButton();

        verify(periodManager).registerNewPeriod(any(Period.class));
    }

    @Test
    void handleActionSaveButton_MissingDates_DoesNotRegisterPeriod() throws ManagerException {
        interact(() -> lookup("#fieldPeriodName").queryAs(FormField.class).setText(PERIOD_NAME));

        clickSaveButton();

        verify(periodManager, never()).registerNewPeriod(any(Period.class));
    }
}
