package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.GradingManager;
import mx.uv.fei.domain.manager.PractitionerManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Component
public class GradePractitionerController implements Initializable {

    private static final String ACTIVE_PERIOD = "Junio-Diciembre 2026";

    @FXML private ListView<Practitioner> practitionersListView;
    @FXML private VBox gradeContainer;
    @FXML private Label labelPractitionerName;
    @FXML private Label labelTentativeGrade;
    @FXML private TextField fieldFinalGrade;
    @FXML private Label labelAlreadyGraded;
    @FXML private Label labelNoPractitioners;

    private final GradingManager gradingManager;
    private final PractitionerManager practitionerManager;
    private final AppStore store;

    private Practitioner selectedPractitioner;
    private int professorId;

    @Inject
    public GradePractitionerController(GradingManager gradingManager,
                                       PractitionerManager practitionerManager, AppStore store) {
        this.gradingManager = gradingManager;
        this.practitionerManager = practitionerManager;
        this.store = store;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User currentUser = store.getState().sessionState().currentUserInSession();
        professorId = currentUser != null ? currentUser.getId() : 0;

        gradeContainer.setVisible(false);
        gradeContainer.setManaged(false);
        labelNoPractitioners.setVisible(false);
        labelNoPractitioners.setManaged(false);

        configurePractitionerList();
        loadPractitionersForProfessor();
    }

    private void configurePractitionerList() {
        practitionersListView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Practitioner practitioner, boolean isEmpty) {
                super.updateItem(practitioner, isEmpty);
                if (isEmpty || practitioner == null) {
                    setText(null);
                } else {
                    setText(practitioner.getName() + " " + practitioner.getLastName()
                            + "  |  " + practitioner.getEnrollment());
                }
            }
        });

        practitionersListView.getSelectionModel().selectedItemProperty()
                .addListener((_, _, selectedPractitioner) -> {
                    if (selectedPractitioner != null) {
                        loadGradePanelForPractitioner(selectedPractitioner);
                    }
                });
    }

    private void loadPractitionersForProfessor() {
        try {
            List<Practitioner> practitioners = practitionerManager
                    .retrievePractitionersByProfessor(professorId);

            if (practitioners.isEmpty()) {
                labelNoPractitioners.setVisible(true);
                labelNoPractitioners.setManaged(true);
                labelNoPractitioners.setText("No tienes practicantes asignados en este periodo.");
            } else {
                practitionersListView.setItems(FXCollections.observableArrayList(practitioners));
            }
        } catch (ManagerException e) {
            Controller.showAlert("Error de Carga", e.getMessage(), AlertType.ERROR);
        }
    }

    private void loadGradePanelForPractitioner(Practitioner practitioner) {
        selectedPractitioner = practitioner;
        gradeContainer.setVisible(true);
        gradeContainer.setManaged(true);
        labelPractitionerName.setText(practitioner.getName() + " " + practitioner.getLastName());
        labelAlreadyGraded.setVisible(false);
        labelAlreadyGraded.setManaged(false);
        fieldFinalGrade.setDisable(false);
        fieldFinalGrade.clear();

        loadTentativeGrade(practitioner.getId());
        checkIfAlreadyGraded(practitioner.getId());
    }

    private void loadTentativeGrade(int practitionerId) {
        try {
            double tentativeGrade = gradingManager.previewTentativeGrade(practitionerId);
            labelTentativeGrade.setText(
                    String.format("Calificación tentativa del sistema: %.2f / 10.0", tentativeGrade));
        } catch (ManagerException e) {
            labelTentativeGrade.setText("No se pudo calcular la calificación tentativa.");
        }
    }

    private void checkIfAlreadyGraded(int practitionerId) {
        try {
            boolean isAlreadyGraded = gradingManager
                    .getGradeByPractitionerAndPeriod(practitionerId, ACTIVE_PERIOD) != null;
            if (isAlreadyGraded) {
                labelAlreadyGraded.setVisible(true);
                labelAlreadyGraded.setManaged(true);
                fieldFinalGrade.setDisable(true);
            }
        } catch (ManagerException e) {
            Controller.showAlert("Error", e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleSaveGrade() {
        if (selectedPractitioner == null) {
            Controller.showAlert("Selección requerida",
                    "Selecciona un practicante de la lista para calificarlo.", AlertType.WARNING);
            return;
        }
        String rawGrade = fieldFinalGrade.getText().trim();
        if (rawGrade.isEmpty()) {
            Controller.showAlert("Campo requerido",
                    "Ingresa la calificación antes de continuar.", AlertType.WARNING);
            return;
        }
        try {
            double finalGrade = Double.parseDouble(rawGrade);
            gradingManager.registerGrade(selectedPractitioner.getId(), professorId, ACTIVE_PERIOD, finalGrade);
            Controller.showAlert("Calificación Guardada",
                    "Se registró la calificación de " + selectedPractitioner.getName()
                            + " " + selectedPractitioner.getLastName() + " como " + finalGrade + ".",
                    AlertType.INFORMATION);
            loadGradePanelForPractitioner(selectedPractitioner);
        } catch (NumberFormatException e) {
            Controller.showAlert("Formato inválido",
                    "La calificación debe ser un número decimal (ej. 9.5).", AlertType.WARNING);
        } catch (ManagerException e) {
            Controller.showAlert("Error al Guardar", e.getMessage(), AlertType.WARNING);
        }
    }

    @FXML
    private void handleUpdateGrade() {
        if (selectedPractitioner == null) {
            Controller.showAlert("Selección requerida",
                    "Selecciona un practicante de la lista para calificarlo.", AlertType.WARNING);
            return;
        }
        String rawGrade = fieldFinalGrade.getText().trim();
        if (rawGrade.isEmpty()) {
            Controller.showAlert("Campo requerido",
                    "Ingresa la calificación antes de continuar.", AlertType.WARNING);
            return;
        }
        try {
            double newGrade = Double.parseDouble(rawGrade);
            gradingManager.updateFinalGrade(selectedPractitioner.getId(), ACTIVE_PERIOD, newGrade);
            Controller.showAlert("Calificación Actualizada",
                    "La calificación de " + selectedPractitioner.getName()
                            + " " + selectedPractitioner.getLastName() + " fue actualizada a " + newGrade + ".",
                    AlertType.INFORMATION);
            loadGradePanelForPractitioner(selectedPractitioner);
        } catch (NumberFormatException e) {
            Controller.showAlert("Formato inválido",
                    "La calificación debe ser un número decimal (ej. 9.5).", AlertType.WARNING);
        } catch (ManagerException e) {
            Controller.showAlert("Error al Actualizar", e.getMessage(), AlertType.WARNING);
        }
    }

    @FXML
    private void handleReturnAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}