package mx.uv.fei.presentation;

import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.GraphicsEnvironment;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import mx.uv.fei.domain.dto.GradingEligibility;
import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.manager.GradingEligibilityManager;
import mx.uv.fei.domain.manager.GradingManager;
import mx.uv.fei.domain.manager.PeriodManager;
import mx.uv.fei.domain.manager.PracticeGroupManager;
import mx.uv.fei.domain.manager.PractitionerManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.state.RootState;
import mx.uv.fei.domain.statemachine.state.SessionState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

public class GradePractitionerControllerTest extends ApplicationTest {

    private static final String FXML_PATH = "/mx/uv/fei/presentation/gradePractitioner.fxml";
    private static final String ACTIVE_PERIOD = "Junio-Diciembre 2026";
    private static final String SAVE_BUTTON_TEXT = "Guardar Calificación";
    private static final int PROFESSOR_ID = 68;
    private static final int ACTIVE_PERIOD_ID = 5;
    private static final int PRACTITIONER_ID = 123;
    private static final double TENTATIVE_GRADE = 7.5;
    private static final double FINAL_GRADE = 9.5;

    private final GradingManager gradingManager = mock(GradingManager.class);
    private final GradingEligibilityManager eligibilityManager = mock(GradingEligibilityManager.class);
    private final PractitionerManager practitionerManager = mock(PractitionerManager.class);
    private final PracticeGroupManager practiceGroupManager = mock(PracticeGroupManager.class);
    private final PeriodManager periodManager = mock(PeriodManager.class);
    private final AppStore appStore = mock(AppStore.class);

    @BeforeAll
    static void requireGraphicalDisplay() {
        assumeFalse(GraphicsEnvironment.isHeadless(),
                "Se omiten las pruebas de interfaz porque no hay un entorno gráfico disponible.");
    }

    @Override
    public void start(Stage stage) throws Exception {
        User professor = new User();
        professor.setId(PROFESSOR_ID);
        when(appStore.getState()).thenReturn(RootState.initialState().withSessionState(new SessionState(professor)));
        when(periodManager.getActivePeriod()).thenReturn(buildActivePeriod());
        when(practiceGroupManager.getAllPracticeGroups()).thenReturn(List.of());
        when(practitionerManager.retrievePractitionersByProfessorAndPeriod(PROFESSOR_ID, ACTIVE_PERIOD_ID))
                .thenReturn(buildAssignedPractitioners());
        when(gradingManager.previewTentativeGrade(PRACTITIONER_ID)).thenReturn(TENTATIVE_GRADE);
        when(gradingManager.getGradeByPractitionerAndPeriod(PRACTITIONER_ID, ACTIVE_PERIOD)).thenReturn(null);
        when(eligibilityManager.evaluateEligibility(PRACTITIONER_ID))
                .thenReturn(new GradingEligibility(true, true, true));

        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
        loader.setControllerFactory(controllerType ->
                new GradePractitionerController(gradingManager, eligibilityManager, practitionerManager,
                        practiceGroupManager, periodManager, appStore));
        Parent root = loader.load();

        stage.setScene(new Scene(root));
        stage.show();
    }

    private Period buildActivePeriod() {
        Period activePeriod = new Period();
        activePeriod.setPeriodId(ACTIVE_PERIOD_ID);
        activePeriod.setPeriodName(ACTIVE_PERIOD);
        return activePeriod;
    }

    private List<Practitioner> buildAssignedPractitioners() {
        Practitioner assignedPractitioner = new Practitioner();
        assignedPractitioner.setId(PRACTITIONER_ID);
        assignedPractitioner.setName("Angel Gabriel");
        assignedPractitioner.setLastName("Aguilar Hernandez");
        assignedPractitioner.setEnrollment("S24242424");
        return List.of(assignedPractitioner);
    }

    private void selectFirstPractitioner() {
        interact(() -> lookup("#practitionersListView").queryAs(ListView.class).getSelectionModel().select(0));
    }

    private void writeFinalGrade(String grade) {
        interact(() -> lookup("#fieldFinalGrade").queryAs(TextField.class).setText(grade));
    }

    private void clickSaveGradeButton() {
        Button saveGradeButton = lookup((Node node) -> node instanceof Button button
                && button.getText() != null && button.getText().contains(SAVE_BUTTON_TEXT)).queryButton();
        interact(saveGradeButton::fire);
    }

    @Test
    void handleSaveGrade_ValidGrade_RegistersGrade() throws Exception {
        selectFirstPractitioner();
        writeFinalGrade(String.valueOf(FINAL_GRADE));

        clickSaveGradeButton();

        verify(gradingManager).registerGrade(PRACTITIONER_ID, PROFESSOR_ID, ACTIVE_PERIOD, FINAL_GRADE);
    }

    @Test
    void handleSaveGrade_NoPractitionerSelected_DoesNotRegisterGrade() throws Exception {
        clickSaveGradeButton();

        verify(gradingManager, never()).registerGrade(anyInt(), anyInt(), anyString(), anyDouble());
    }
}
