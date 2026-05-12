package mx.uv.fei.presentation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.PendingPractitionerManager;
import mx.uv.fei.domain.statemachine.Store;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

import java.util.List;

@Component
public class PendingPractitionerSelectionController {

    @FXML
    private ListView<Practitioner> pendingPractitionersListView;

    private final PendingPractitionerManager pendingPractitionerManager;
    private final Store applicationNavigationStore;
    private final ObservableList<Practitioner> pendingPractitionersObservableList = FXCollections.observableArrayList();

    @Inject
    public PendingPractitionerSelectionController(PendingPractitionerManager pendingPractitionerManager, Store applicationNavigationStore) {
        this.pendingPractitionerManager = pendingPractitionerManager;
        this.applicationNavigationStore = applicationNavigationStore;
    }

    @FXML
    public void initialize() {
        configurePractitionerListViewDisplay();
        pendingPractitionersListView.setItems(pendingPractitionersObservableList);
        loadPendingPractitioners();
    }

    private void configurePractitionerListViewDisplay() {
        pendingPractitionersListView.setCellFactory(parameter -> new ListCell<>() {
            @Override
            protected void updateItem(Practitioner currentPractitionerItem, boolean isItemEmpty) {
                super.updateItem(currentPractitionerItem, isItemEmpty);
                if (isItemEmpty || currentPractitionerItem == null) {
                    setText(null);
                } else {
                    String formattedDisplayString = currentPractitionerItem.getEnrollment() + " - " + currentPractitionerItem.getName() + " " + currentPractitionerItem.getLastName();
                    setText(formattedDisplayString);
                }
            }
        });
    }

    private void loadPendingPractitioners() {
        try {
            pendingPractitionersObservableList.clear();
            List<Practitioner> retrievedPendingList = pendingPractitionerManager.retrievePractitionersPendingAssignment();
            pendingPractitionersObservableList.addAll(retrievedPendingList);
        } catch (ManagerException retrievalException) {
            Controller.showAlert("Error de conexion", retrievalException.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleReviewPractitionerPostulationsAction(ActionEvent userActionEvent) {
        Practitioner selectedPractitionerToReview = pendingPractitionersListView.getSelectionModel().getSelectedItem();

        if (selectedPractitionerToReview != null) {
            String targetPractitionerIdentifier = String.valueOf(selectedPractitionerToReview.getId());
            applicationNavigationStore.dispatch(new NavigationAction.ViewEntityDetails( AppSection.ASSIGN_PROJECT, targetPractitionerIdentifier));
        } else {
            Controller.showAlert("Seleccion requerida", "Seleccione un practicante de la lista para revisar sus postulaciones.", AlertType.WARNING);
        }
    }

    @FXML
    private void handleReturnToCoordinatorMenuAction(ActionEvent userActionEvent) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.COORDINATOR_PRACTITIONER_MENU));
    }
}