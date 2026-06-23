package mx.uv.fei.presentation;

import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.GraphicsEnvironment;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import mx.uv.fei.domain.dto.PracticeGroup;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.PracticeGroupManager;
import mx.uv.fei.domain.manager.PractitionerManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.state.RootState;
import mx.uv.fei.presentation.shell.ShellNavigator;
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.presentation.components.FormField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

public class RegisterPractitionerControllerTest extends ApplicationTest {

    @BeforeAll
    static void requireGraphicalDisplay() {
        assumeFalse(GraphicsEnvironment.isHeadless(),
                "Se omiten las pruebas de interfaz porque no hay un entorno gráfico disponible.");
    }


    private static final String FXML_PATH = "/mx/uv/fei/presentation/registerPractitioner.fxml";
    private static final int STORED_GROUP_ID = 6;
    private static final int STORED_PERIOD_ID = 5;
    private static final String STORED_GROUP_SECTION = "601";
    private static final String GROUP_DISPLAY = "Sección " + STORED_GROUP_SECTION + " (Periodo: " + STORED_PERIOD_ID + ")";
    private static final String GENERATED_PASSWORD = "PracticanteUv2026";

    private final PractitionerManager practitionerManager = mock(PractitionerManager.class);
    private final PracticeGroupManager practiceGroupManager = mock(PracticeGroupManager.class);
    private final AppStore appStore = mock(AppStore.class);
    private final ShellNavigator shellNavigator = mock(ShellNavigator.class);

    @Override
    public void start(Stage stage) throws Exception {
        when(practiceGroupManager.getAllPracticeGroups()).thenReturn(buildStoredGroups());
        when(practitionerManager.registerNewPractitioner(any(Practitioner.class))).thenReturn(GENERATED_PASSWORD);
        when(appStore.getState()).thenReturn(RootState.initialState());

        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
        loader.setControllerFactory(controllerType ->
                new RegisterPractitionerController(practitionerManager, practiceGroupManager, appStore, shellNavigator));
        Parent root = loader.load();

        stage.setScene(new Scene(root));
        stage.show();
    }

    private List<PracticeGroup> buildStoredGroups() {
        PracticeGroup storedGroup = new PracticeGroup();
        storedGroup.setGroupId(STORED_GROUP_ID);
        storedGroup.setSection(STORED_GROUP_SECTION);
        storedGroup.setPeriodId(STORED_PERIOD_ID);
        return List.of(storedGroup);
    }

    private void fillValidForm() {
        interact(() -> {
            lookup("#fieldEnrollment").queryAs(FormField.class).setText("zS25080910");
            lookup("#fieldName").queryAs(FormField.class).setText("Luis Fernando");
            lookup("#fieldLastName").queryAs(FormField.class).setText("Martinez Rivera");
            lookup("#fieldEmail").queryAs(FormField.class).setText("zS25080910@estudiantes.uv.mx");
            lookup("#comboBoxGender").queryAs(FormComboBox.class).valueProperty().set(Gender.MALE.getDisplayValue());
            lookup("#comboBoxPracticeGroup").queryAs(FormComboBox.class).valueProperty().set(GROUP_DISPLAY);
        });
    }

    private void clickRegisterButton() {
        interact(() -> lookup("#registerButton").queryAs(Button.class).fire());
    }

    @Test
    void handleActionRegisterButton_IncompleteForm_DoesNotRegisterPractitioner() throws ManagerException {
        clickRegisterButton();

        verify(practitionerManager, never()).registerNewPractitioner(any(Practitioner.class));
    }

    @Test
    void handleActionRegisterButton_ValidForm_RegistersPractitioner() throws ManagerException {
        fillValidForm();

        clickRegisterButton();

        verify(practitionerManager).registerNewPractitioner(any(Practitioner.class));
    }
}
