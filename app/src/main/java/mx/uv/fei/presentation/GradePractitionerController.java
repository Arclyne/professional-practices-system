package mx.uv.fei.presentation;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

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


@Component
public class GradePractitionerController implements Initializable {

    private static final String PERIOD_ACTIVE          = "Junio-Diciembre 2026";
    private static final String MSG_NO_PRACTITIONERS   = "No tienes practicantes asignados en este periodo.";
    private static final String MSG_SELECT_FIRST       = "Selecciona un practicante de la lista para calificarlo.";
    private static final String MSG_GRADE_REQUIRED     = "Campo requerido";
    private static final String MSG_ENTER_GRADE        = "Ingresa la calificación antes de continuar.";
    private static final String MSG_INVALID_FORMAT     = "Formato inválido";
    private static final String MSG_NUMBER_FORMAT      = "La calificación debe ser un número decimal (ej. 9.5).";

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
    public GradePractitionerController(
            GradingManager gradingManager,
            PractitionerManager practitionerManager,
            AppStore store) {
        this.gradingManager = gradingManager;
        this.practitionerManager = practitionerManager;
        this.store = store;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User currentProfessor = store.getState().sessionState().currentUserInSession();
        professorId = currentProfessor != null ? currentProfessor.getId() : 0;

        gradeContainer.setVisible(false);
        gradeContainer.setManaged(false);
        labelNoPractitioners.setVisible(false);
        labelNoPractitioners.setManaged(false);

        configurePractitionerList();
        loadPractitionersForProfessor();
    }

    private void configurePractitionerList() {
        practitionersListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Practitioner practitioner, boolean empty) {
                super.updateItem(practitioner, empty);
                if (empty || practitioner == null) {
                    setText(null);
                } else {
                    setText(practitioner.getName() + " " + practitioner.getLastName()
                            + "  |  " + practitioner.getEnrollment());
                }
            }
        });

        practitionersListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        loadGradePanelForPractitioner(newValue);
                    }
                }
        );
    }

    private void loadPractitionersForProfessor() {
        try {
            List<Practitioner> practitioners = practitionerManager
                    .retrievePractitionersByProfessor(professorId);

            if (practitioners.isEmpty()) {
                labelNoPractitioners.setVisible(true);
                labelNoPractitioners.setManaged(true);
                labelNoPractitioners.setText(MSG_NO_PRACTITIONERS);
            } else {
                practitionersListView.setItems(FXCollections.observableArrayList(practitioners));
            }
        } catch (ManagerException exception) {
            Controller.showAlert("Error de Carga", exception.getMessage(), AlertType.ERROR);
        }
    }

    private void loadGradePanelForPractitioner(Practitioner practitioner) {
        selectedPractitioner = practitioner;

        gradeContainer.setVisible(true);
        gradeContainer.setManaged(true);

        labelPractitionerName.setText(
                practitioner.getName() + " " + practitioner.getLastName());
        labelAlreadyGraded.setVisible(false);
        labelAlreadyGraded.setManaged(false);
        fieldFinalGrade.setDisable(false);
        fieldFinalGrade.clear();

        loadTentativeGradeForPractitioner(practitioner.getId());
        checkIfAlreadyGraded(practitioner.getId());
    }

    private void loadTentativeGradeForPractitioner(int practitionerId) {
        try {
            double tentativeGrade = gradingManager.previewTentativeGrade(practitionerId);
            labelTentativeGrade.setText(
                    String.format("Calificación tentativa del sistema: %.2f / 10.0", tentativeGrade));
        } catch (ManagerException exception) {
            labelTentativeGrade.setText("No se pudo calcular la calificación tentativa.");
        }
    }

    private void checkIfAlreadyGraded(int practitionerId) {
        try {
            boolean alreadyGraded = gradingManager
                    .getGradeByPractitionerAndPeriod(practitionerId, PERIOD_ACTIVE) != null;

            if (alreadyGraded) {
                labelAlreadyGraded.setVisible(true);
                labelAlreadyGraded.setManaged(true);
                fieldFinalGrade.setDisable(true);
            }
        } catch (ManagerException exception) {
            Controller.showAlert("Error", exception.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleSaveGrade(ActionEvent event) {
        if (selectedPractitioner == null) {
            Controller.showAlert(MSG_SELECT_FIRST, MSG_SELECT_FIRST, AlertType.WARNING);
            return;
        }

        String rawGrade = fieldFinalGrade.getText().trim();
        if (rawGrade.isEmpty()) {
            Controller.showAlert(MSG_GRADE_REQUIRED, MSG_ENTER_GRADE, AlertType.WARNING);
            return;
        }

        try {
            double finalGrade = Double.parseDouble(rawGrade);
            gradingManager.registerGrade(
                    selectedPractitioner.getId(), professorId, PERIOD_ACTIVE, finalGrade);

            Controller.showAlert("Calificación Guardada",
                    "Se registró la calificación de "
                            + selectedPractitioner.getName() + " "
                            + selectedPractitioner.getLastName()
                            + " como " + finalGrade + ".",
                    AlertType.INFORMATION);

            loadGradePanelForPractitioner(selectedPractitioner);
        } catch (NumberFormatException exception) {
            Controller.showAlert(MSG_INVALID_FORMAT, MSG_NUMBER_FORMAT, AlertType.WARNING);
        } catch (ManagerException exception) {
            Controller.showAlert("Error al Guardar", exception.getMessage(), AlertType.WARNING);
        }
    }

    @FXML
    private void handleUpdateGrade(ActionEvent event) {
        if (selectedPractitioner == null) {
            Controller.showAlert(MSG_SELECT_FIRST, MSG_SELECT_FIRST, AlertType.WARNING);
            return;
        }

        String rawGrade = fieldFinalGrade.getText().trim();
        if (rawGrade.isEmpty()) {
            Controller.showAlert(MSG_GRADE_REQUIRED, MSG_ENTER_GRADE, AlertType.WARNING);
            return;
        }

        try {
            double newGrade = Double.parseDouble(rawGrade);
            gradingManager.updateFinalGrade(
                    selectedPractitioner.getId(), PERIOD_ACTIVE, newGrade);

            Controller.showAlert("Calificación Actualizada",
                    "La calificación de "
                            + selectedPractitioner.getName() + " "
                            + selectedPractitioner.getLastName()
                            + " fue actualizada a " + newGrade + ".",
                    AlertType.INFORMATION);

            loadGradePanelForPractitioner(selectedPractitioner);
        } catch (NumberFormatException exception) {
            Controller.showAlert(MSG_INVALID_FORMAT, MSG_NUMBER_FORMAT, AlertType.WARNING);
        } catch (ManagerException exception) {
            Controller.showAlert("Error al Actualizar", exception.getMessage(), AlertType.WARNING);
        }
    }

    @FXML
    private void handleReturnAction(ActionEvent event) {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}