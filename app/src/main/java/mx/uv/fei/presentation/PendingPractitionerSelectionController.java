package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.PractitionerManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.util.List;

@Component
public class PendingPractitionerSelectionController {

    @FXML private ListView<Practitioner> pendingPractitionersListView;

    private final PractitionerManager practitionerManager;
    private final AppStore store;
    private final ObservableList<Practitioner> pendingPractitioners = FXCollections.observableArrayList();

    @Inject
    public PendingPractitionerSelectionController(PractitionerManager practitionerManager, AppStore store) {
        this.practitionerManager = practitionerManager;
        this.store = store;
    }

    @FXML
    public void initialize() {
        configurePractitionerListView();
        pendingPractitionersListView.setItems(pendingPractitioners);
        loadPendingPractitioners();
    }

    private void configurePractitionerListView() {
        pendingPractitionersListView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Practitioner practitioner, boolean isEmpty) {
                super.updateItem(practitioner, isEmpty);
                if (isEmpty || practitioner == null) {
                    setText(null);
                } else {
                    setText(practitioner.getEnrollment() + " - "
                            + practitioner.getName() + " " + practitioner.getLastName());
                }
            }
        });
    }

    private void loadPendingPractitioners() {
        try {
            pendingPractitioners.clear();
            List<Practitioner> retrievedPractitioners = practitionerManager.retrievePractitionersPendingAssignment();
            pendingPractitioners.addAll(retrievedPractitioners);
        } catch (ManagerException e) {
            Controller.showAlert("Error de conexión", e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleReviewPractitionerPostulationsAction() {
        Practitioner selectedPractitioner = pendingPractitionersListView.getSelectionModel().getSelectedItem();

        if (selectedPractitioner != null) {
            store.dispatch(new NavigationAction.ViewEntityDetails(
                    AppSection.ASSIGN_PROJECT, String.valueOf(selectedPractitioner.getId())));
        } else {
            Controller.showAlert("Selección requerida",
                    "Seleccione un practicante de la lista para revisar sus postulaciones.", AlertType.WARNING);
        }
    }

    @FXML
    private void handleReturnToCoordinatorMenuAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.COORDINATOR_PRACTITIONER_MENU));
    }
}