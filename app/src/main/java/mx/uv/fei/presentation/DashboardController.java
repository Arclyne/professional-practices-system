package mx.uv.fei.presentation;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.statemachine.Store;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.domain.statemachine.state.RootState;

@Component
public class DashboardController {

    @FXML
    private Label labelUserName;
    @FXML
    private Label labelUserRole;
    @FXML
    private VBox containerAdminActions;
    @FXML
    private StackPane dashboardContent;

    @FXML
    private Button btnRegistrarPracticante;
    @FXML
    private Button btnRegistrarProfesor;
    @FXML
    private Button btnRegistrarProyecto;
    @FXML
    private Button btnRegistrarActividad;

    private final Store store;

    @Inject
    public DashboardController(Store store) {
        this.store = store;
    }

    @FXML
    public void initialize() {
        // Obtenemos el estado desde el store inyectado
        RootState currentState = store.getState();
        User currentUser = currentState.sessionState().currentUserInSession(); // Corregido para buscar en sessionState

        if (currentUser != null) {
            labelUserName.setText(currentUser.getName() + " " + currentUser.getLastName());
            labelUserRole.setText("Rol: " + currentUser.getRole());
            configurePermissions(currentUser.getRole());
        }
    }

    private void configurePermissions(String role) {
        containerAdminActions.setVisible(false);
        containerAdminActions.setManaged(false);

        btnRegistrarPracticante.setVisible(false);
        btnRegistrarPracticante.setManaged(false);
        btnRegistrarProfesor.setVisible(false);
        btnRegistrarProfesor.setManaged(false);
        btnRegistrarProyecto.setVisible(false);
        btnRegistrarProyecto.setManaged(false);
        btnRegistrarActividad.setVisible(false);
        btnRegistrarActividad.setManaged(false);

        if ("Administrador".equalsIgnoreCase(role)) {
            containerAdminActions.setVisible(true);
            containerAdminActions.setManaged(true);
            btnRegistrarPracticante.setVisible(true);
            btnRegistrarPracticante.setManaged(true);
            btnRegistrarProfesor.setVisible(true);
            btnRegistrarProfesor.setManaged(true);
            btnRegistrarProyecto.setVisible(true);
            btnRegistrarProyecto.setManaged(true);
            btnRegistrarActividad.setVisible(true);
            btnRegistrarActividad.setManaged(true);
        } else if ("Coordinador".equalsIgnoreCase(role)) {
            btnRegistrarPracticante.setVisible(true);
            btnRegistrarPracticante.setManaged(true);
            btnRegistrarProfesor.setVisible(true);
            btnRegistrarProfesor.setManaged(true);
            btnRegistrarProyecto.setVisible(true);
            btnRegistrarProyecto.setManaged(true);
        } else if ("Profesor".equalsIgnoreCase(role)) {
            btnRegistrarActividad.setVisible(true);
            btnRegistrarActividad.setManaged(true);
        }
    }

    @FXML
    private void handleRegistrarPracticante() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.REGISTER_PRACTITIONER));
    }

    @FXML
    private void handleLogout() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.LOGIN));
    }
}