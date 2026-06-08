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

    private static final String PERIOD_ACTIVE = "Junio-Diciembre 2026";

    @FXML private ListView<Practitioner> practitionersListView;
    @FXML private VBox gradeContainer;
    @FXML private Label labelPractitionerName;
    @FXML private Label labelTentativeGrade;
    @FXML private TextField fieldFinalGrade;
    @FXML private Label labelAlreadyGraded;

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

        configurePractitionerList();
        loadPractitioners();
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
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        loadPractitionerGradePanel(newVal);
                    }
                }
        );
    }

    private void loadPractitioners() {
        try {
            List<Practitioner> practitioners = practitionerManager.retrieveAssignedPractitioners();
            practitionersListView.setItems(FXCollections.observableArrayList(practitioners));
        } catch (ManagerException exception) {
            Controller.showAlert("Error de Carga", exception.getMessage(), AlertType.ERROR);
        }
    }

    private void loadPractitionerGradePanel(Practitioner practitioner) {
        selectedPractitioner = practitioner;

        gradeContainer.setVisible(true);
        gradeContainer.setManaged(true);

        labelPractitionerName.setText(practitioner.getName() + " " + practitioner.getLastName());
        labelAlreadyGraded.setVisible(false);
        labelAlreadyGraded.setManaged(false);
        fieldFinalGrade.setDisable(false);
        fieldFinalGrade.clear();

        try {
            double tentative = gradingManager.previewTentativeGrade(practitioner.getId());
            labelTentativeGrade.setText(String.format("Calificación tentativa del sistema: %.2f", tentative));

            boolean alreadyGraded = gradingManager.getGradesByProfessor(professorId).stream()
                    .anyMatch(g -> g.getPractitionerId() == practitioner.getId()
                            && PERIOD_ACTIVE.equals(g.getPeriod()));

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
            return;
        }

        String rawGrade = fieldFinalGrade.getText().trim();
        if (rawGrade.isEmpty()) {
            Controller.showAlert("Campo requerido",
                    "Ingresa la calificación final antes de guardar.", AlertType.WARNING);
            return;
        }

        try {
            double finalGrade = Double.parseDouble(rawGrade);
            gradingManager.registerGrade(
                    selectedPractitioner.getId(), professorId, PERIOD_ACTIVE, finalGrade);

            Controller.showAlert("Calificación Guardada",
                    "Se registró la calificación de "
                    + selectedPractitioner.getName() + " " + selectedPractitioner.getLastName()
                    + " como " + finalGrade + ".",
                    AlertType.INFORMATION);

            loadPractitionerGradePanel(selectedPractitioner);
        } catch (NumberFormatException exception) {
            Controller.showAlert("Formato Inválido",
                    "La calificación debe ser un número (ej. 9.5).", AlertType.WARNING);
        } catch (ManagerException exception) {
            Controller.showAlert("Error al Guardar", exception.getMessage(), AlertType.WARNING);
        }
    }

    @FXML
    private void handleUpdateGrade(ActionEvent event) {
        if (selectedPractitioner == null) {
            return;
        }

        String rawGrade = fieldFinalGrade.getText().trim();
        if (rawGrade.isEmpty()) {
            Controller.showAlert("Campo requerido",
                    "Ingresa la nueva calificación.", AlertType.WARNING);
            return;
        }

        try {
            double newGrade = Double.parseDouble(rawGrade);
            gradingManager.updateFinalGrade(selectedPractitioner.getId(), PERIOD_ACTIVE, newGrade);

            Controller.showAlert("Calificación Actualizada",
                    "La calificación de "
                    + selectedPractitioner.getName() + " " + selectedPractitioner.getLastName()
                    + " fue actualizada a " + newGrade + ".",
                    AlertType.INFORMATION);

            loadPractitionerGradePanel(selectedPractitioner);
        } catch (NumberFormatException exception) {
            Controller.showAlert("Formato Inválido",
                    "La calificación debe ser un número.", AlertType.WARNING);
        } catch (ManagerException exception) {
            Controller.showAlert("Error al Actualizar", exception.getMessage(), AlertType.WARNING);
        }
    }

    @FXML
    private void handleReturnAction(ActionEvent event) {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}
