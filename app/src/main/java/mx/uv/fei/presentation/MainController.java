package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.core.DependencyInjector;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.AdminManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.domain.statemachine.state.RootState;
import mx.uv.fei.presentation.interfaces.IDisposable;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

@Component
public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    private static final String VIEW_BASE_PATH = "/mx/uv/fei/presentation/";
    private static final Map<AppSection, String> VIEW_PATHS = buildViewPaths();

    private static final double DASHBOARD_WIDTH = 1100;
    private static final double DASHBOARD_HEIGHT = 750;
    private static final double FORM_WIDTH = 500;
    private static final double FORM_HEIGHT = 450;
    private static final double SPLASH_DELAY_SECONDS = 2;

    @FXML private StackPane contentArea;

    private final DependencyInjector dependencyInjector;
    private final AppStore store;
    private final AdminManager administratorManager;

    private Runnable unsubscribe;
    private AppSection activeSection;
    private Object currentViewController;

    @Inject
    public MainController(DependencyInjector dependencyInjector, AppStore store, AdminManager administratorManager) {
        this.dependencyInjector = dependencyInjector;
        this.store = store;
        this.administratorManager = administratorManager;
    }

    @FXML
    public void initialize() {
        unsubscribe = store.subscribe(this::handleApplicationStateChange);
        startAppFlow();
    }

    public void dispose() {
        if (unsubscribe != null) {
            unsubscribe.run();
        }
    }

    private static Map<AppSection, String> buildViewPaths() {
        Map<AppSection, String> paths = new EnumMap<>(AppSection.class);
        paths.put(AppSection.SPLASH_SCREEN, VIEW_BASE_PATH + "loading.fxml");
        paths.put(AppSection.ADMIN_REGISTRATION, VIEW_BASE_PATH + "registerAdmin.fxml");
        paths.put(AppSection.LOGIN, VIEW_BASE_PATH + "startSession.fxml");
        paths.put(AppSection.DASHBOARD, VIEW_BASE_PATH + "dashboard.fxml");
        paths.put(AppSection.PASSWORD_RESET, VIEW_BASE_PATH + "passwordReset.fxml");
        paths.put(AppSection.TOKEN_VERIFICATION, VIEW_BASE_PATH + "tokenVerification.fxml");
        paths.put(AppSection.REGISTER_PRACTITIONER, VIEW_BASE_PATH + "registerPractitioner.fxml");
        paths.put(AppSection.REGISTER_PROFESSOR, VIEW_BASE_PATH + "registerProfessor.fxml");
        paths.put(AppSection.REGISTER_PROJECT, VIEW_BASE_PATH + "projectForm.fxml");
        paths.put(AppSection.REGISTER_COORDINATOR, VIEW_BASE_PATH + "registerCoordinator.fxml");
        paths.put(AppSection.COORDINATOR_DETAILS, VIEW_BASE_PATH + "coordinatorDetails.fxml");
        paths.put(AppSection.PRIORITIZE_PROJECTS, VIEW_BASE_PATH + "prioritizeProjects.fxml");
        paths.put(AppSection.VIEW_PRACTITIONER_PRIORITIES, VIEW_BASE_PATH + "viewPractitionerPriorities.fxml");
        paths.put(AppSection.COORDINATOR_PRACTITIONER_MENU, VIEW_BASE_PATH + "coordinatorPractitionerMenu.fxml");
        paths.put(AppSection.PENDING_PRACTITIONER_SELECTION, VIEW_BASE_PATH + "pendingPractitionerSelection.fxml");
        paths.put(AppSection.REGISTER_MANAGER, VIEW_BASE_PATH + "registerManager.fxml");
        paths.put(AppSection.PROFESSOR_MANAGEMENT_MENU, VIEW_BASE_PATH + "managerProfessor.fxml");
        paths.put(AppSection.ASSIGN_PROJECT, VIEW_BASE_PATH + "assignProject.fxml");
        paths.put(AppSection.PROJECT_MANAGEMENT_MENU, VIEW_BASE_PATH + "manageProjects.fxml");
        paths.put(AppSection.MANAGER_MANAGEMENT_MENU, VIEW_BASE_PATH + "manageManagers.fxml");
        paths.put(AppSection.ORGANIZATION_MANAGEMENT_MENU, VIEW_BASE_PATH + "manageOrganizations.fxml");
        paths.put(AppSection.REGISTER_ORGANIZATION, VIEW_BASE_PATH + "registerOrganization.fxml");
        paths.put(AppSection.MESSAGES, VIEW_BASE_PATH + "MessageView.fxml");
        paths.put(AppSection.REGISTER_PERIOD, VIEW_BASE_PATH + "registerPeriod.fxml");
        paths.put(AppSection.REGISTER_PRACTICE_GROUP, VIEW_BASE_PATH + "registerPracticeGroup.fxml");
        paths.put(AppSection.TEMPLATE_GENERATOR, VIEW_BASE_PATH + "templateGenerator.fxml");
        paths.put(AppSection.PRACTITIONER_LOGBOOK, VIEW_BASE_PATH + "practitionerLogbook.fxml");
        paths.put(AppSection.PRACTITIONER_REPORT_GENERATOR, VIEW_BASE_PATH + "practitionerReportGenerator.fxml");
        paths.put(AppSection.PRACTITIONER_REPORTS_LIST, VIEW_BASE_PATH + "practitionerReportsList.fxml");
        paths.put(AppSection.PROFESSOR_EVALUATE_REPORT, VIEW_BASE_PATH + "professorEvaluateReport.fxml");
        paths.put(AppSection.PROGRESS_REPORT_GENERATOR, VIEW_BASE_PATH + "progressReportGenerator.fxml");
        paths.put(AppSection.GRADE_PRACTITIONER, VIEW_BASE_PATH + "gradePractitioner.fxml");
        paths.put(AppSection.PRACTITIONER_GRADE_VIEW, VIEW_BASE_PATH + "practitionerGradeView.fxml");
        paths.put(AppSection.PROFESSOR_REVIEW_SELF_EVALUATION, VIEW_BASE_PATH + "ReviewSelfEvaluation.fxml");
        paths.put(AppSection.PRACTITIONER_SELF_EVALUATION, VIEW_BASE_PATH + "PractitionerSelfEvaluation.fxml");
        return paths;
    }

    private Stage getStage() {
        return (Stage) contentArea.getScene().getWindow();
    }

    private void handleApplicationStateChange(RootState previousState, RootState newState) {
        AppSection targetSection = newState.navigationState().currentSection();
        Platform.runLater(() -> renderSection(targetSection));
    }

    private void renderSection(AppSection targetSection) {
        if (activeSection == targetSection) {
            return;
        }
        activeSection = targetSection;
        adjustStageSize(targetSection);

        String viewPath = VIEW_PATHS.get(targetSection);
        if (viewPath != null) {
            loadView(viewPath);
        } else {
            Controller.showAlert("Error de Navegación",
                    "No se encontró la ruta para la sección solicitada en el sistema.", AlertType.ERROR);
        }
    }

    private void adjustStageSize(AppSection targetSection) {
        Stage stage = getStage();
        if (targetSection == AppSection.DASHBOARD) {
            stage.setWidth(DASHBOARD_WIDTH);
            stage.setHeight(DASHBOARD_HEIGHT);
            stage.centerOnScreen();
        } else if (targetSection == AppSection.LOGIN || targetSection == AppSection.ADMIN_REGISTRATION) {
            stage.setWidth(FORM_WIDTH);
            stage.setHeight(FORM_HEIGHT);
            stage.centerOnScreen();
        }
    }

    private void loadView(String fxmlPath) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new IllegalArgumentException("Ruta FXML no encontrada: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            loader.setControllerFactory(dependencyInjector::retrieveInstance);
            Parent view = loader.load();

            disposeCurrentController();
            currentViewController = loader.getController();
            bindViewToContentArea(view);
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            log.error("Error de E/S al cargar la vista FXML: {}.", fxmlPath, e);
            Controller.showAlert("Error de Interfaz",
                    "No se pudo cargar el archivo visual.", AlertType.ERROR);
        } catch (IllegalArgumentException e) {
            log.error("Ruta de vista inválida: {}.", fxmlPath, e);
            Controller.showAlert("Error de Interfaz",
                    "La ruta solicitada no existe en el sistema.", AlertType.ERROR);
        }
    }

    private void disposeCurrentController() {
        if (currentViewController instanceof IDisposable disposableController) {
            disposableController.dispose();
        }
    }

    private void bindViewToContentArea(Parent view) {
        if (view instanceof Region regionView) {
            regionView.prefWidthProperty().bind(contentArea.widthProperty());
            regionView.prefHeightProperty().bind(contentArea.heightProperty());
        }
    }

    private void startAppFlow() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.SPLASH_SCREEN));
        PauseTransition pause = new PauseTransition(Duration.seconds(SPLASH_DELAY_SECONDS));
        pause.setOnFinished(_ -> resolveInitialSection());
        pause.play();
    }

    private void resolveInitialSection() {
        try {
            boolean adminExists = administratorManager.checkSystemHasAdmin();
            AppSection initialSection = adminExists ? AppSection.LOGIN : AppSection.ADMIN_REGISTRATION;
            store.dispatch(new NavigationAction.GoToSection(initialSection));
        } catch (ManagerException e) {
            log.error("El sistema falló al verificar el estado inicial.", e);
            Controller.showAlert("Error de Arranque",
                    "El sistema falló al iniciar.", AlertType.ERROR);
        }
    }
}