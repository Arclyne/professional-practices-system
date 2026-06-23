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
import javafx.scene.control.Label;
import javafx.stage.Stage;

import mx.uv.fei.domain.dto.Organization;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.manager.OrganizationManager;
import mx.uv.fei.domain.manager.PractitionerManager;
import mx.uv.fei.domain.manager.ProfessorManager;
import mx.uv.fei.domain.manager.ProjectManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

public class CoordinatorHomeControllerTest extends ApplicationTest {

    private static final String FXML_PATH = "/mx/uv/fei/presentation/coordinatorHome.fxml";

    private final PractitionerManager practitionerManager = mock(PractitionerManager.class);
    private final ProjectManager projectManager = mock(ProjectManager.class);
    private final OrganizationManager organizationManager = mock(OrganizationManager.class);
    private final ProfessorManager professorManager = mock(ProfessorManager.class);

    @BeforeAll
    static void requireGraphicalDisplay() {
        assumeFalse(GraphicsEnvironment.isHeadless(),
                "Se omiten las pruebas de interfaz porque no hay un entorno gráfico disponible.");
    }

    @Override
    public void start(Stage stage) throws Exception {
        when(practitionerManager.getAllPractitioners()).thenReturn(List.of(
                practitionerWithStatus(UserStatus.ACTIVE),
                practitionerWithStatus(UserStatus.ACTIVE),
                practitionerWithStatus(UserStatus.PENDING)));
        when(projectManager.getAllProjects()).thenReturn(List.of(new Project(), new Project()));
        when(organizationManager.getAllOrganizations()).thenReturn(List.of(new Organization()));
        when(professorManager.getAllProfessors()).thenReturn(List.of(new Professor(), new Professor()));

        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
        loader.setControllerFactory(controllerType -> new CoordinatorHomeController(
                practitionerManager, projectManager, organizationManager, professorManager));
        Parent root = loader.load();

        stage.setScene(new Scene(root));
        stage.show();
    }

    private Practitioner practitionerWithStatus(UserStatus status) {
        Practitioner practitioner = new Practitioner();
        practitioner.setStatus(status);

        return practitioner;
    }

    @Test
    void initialize_MixedData_ShowsSummaryCounts() {
        assertEquals("3", lookup("#practitionersCountLabel").queryAs(Label.class).getText());
        assertEquals("2", lookup("#projectsCountLabel").queryAs(Label.class).getText());
        assertEquals("1", lookup("#organizationsCountLabel").queryAs(Label.class).getText());
        assertEquals("2", lookup("#professorsCountLabel").queryAs(Label.class).getText());
    }

    @Test
    void initialize_MixedData_ShowsPractitionerStatusDistribution() {
        assertEquals("2", lookup("#activePractitionersLabel").queryAs(Label.class).getText());
        assertEquals("0", lookup("#inactivePractitionersLabel").queryAs(Label.class).getText());
        assertEquals("1", lookup("#pendingPractitionersLabel").queryAs(Label.class).getText());
    }
}
