package mx.uv.fei.presentation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.cell.CheckBoxListCell;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Organization;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.OrganizationManager;
import mx.uv.fei.domain.statemachine.Store;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OrganizationManagementController {

    @FXML private ListView<String> organizationsListView;

    private final OrganizationManager organizationManager;
    private final Store applicationNavigationStore;

    private final Map<String, Integer> itemToIdentifierMap = new HashMap<>();
    private final Map<String, BooleanProperty> itemSelectionStateMap = new HashMap<>();

    @Inject
    public OrganizationManagementController(OrganizationManager organizationManager, Store applicationNavigationStore) {
        this.organizationManager = organizationManager;
        this.applicationNavigationStore = applicationNavigationStore;
    }

    @FXML
    public void initialize() {
        organizationsListView.setCellFactory(CheckBoxListCell.forListView(itemString -> itemSelectionStateMap.get(itemString)));

        organizationsListView.setOnMouseClicked(event -> {
            String selectedItem = organizationsListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                BooleanProperty checkboxState = itemSelectionStateMap.get(selectedItem);
                if (checkboxState != null) {
                    checkboxState.set(!checkboxState.get());
                    organizationsListView.getSelectionModel().clearSelection();
                }
            }
        });

        loadActiveOrganizations();
    }

    private void loadActiveOrganizations() {
        itemToIdentifierMap.clear();
        itemSelectionStateMap.clear();
        ObservableList<String> displayItemsList = FXCollections.observableArrayList();

        try {
            List<Organization> registeredOrganizationsList = organizationManager.getAllOrganizations();
            for (Organization currentOrganization : registeredOrganizationsList) {

                if (currentOrganization.getState() != null && !"No Activo".equalsIgnoreCase(currentOrganization.getState())) {

                    String formattedDisplayString = currentOrganization.getNameOrganization() + " (" + currentOrganization.getCity() + ") - " + currentOrganization.getState();
                    itemToIdentifierMap.put(formattedDisplayString, currentOrganization.getIdOrganization());
                    itemSelectionStateMap.put(formattedDisplayString, new SimpleBooleanProperty(false));
                    displayItemsList.add(formattedDisplayString);
                }
            }
        } catch (ManagerException dataRetrievalException) {
            Controller.showAlert("Error de carga", dataRetrievalException.getMessage(), AlertType.ERROR);
        }

        organizationsListView.setItems(displayItemsList);
    }

    @FXML
    private void handleInactivateSelectedAction(ActionEvent userActionEvent) {
        List<Integer> identifiersToInactivateList = new ArrayList<>();

        for (Map.Entry<String, BooleanProperty> currentMapEntry : itemSelectionStateMap.entrySet()) {
            if (currentMapEntry.getValue().get()) {
                identifiersToInactivateList.add(itemToIdentifierMap.get(currentMapEntry.getKey()));
            }
        }

        if (identifiersToInactivateList.isEmpty()) {
            Controller.showAlert("Sin selección", "Debe seleccionar al menos una organización para inactivar.", AlertType.WARNING);
            return;
        }

        try {
            organizationManager.inactivateMultipleOrganizations(identifiersToInactivateList);
            Controller.showAlert("Proceso Exitoso", "Las organizaciones seleccionadas han sido inactivadas.", AlertType.INFORMATION);
            loadActiveOrganizations();
        } catch (ManagerException executionException) {
            Controller.showAlert("Error en la Operación", executionException.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleRegisterNewOrganizationAction(ActionEvent userActionEvent) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.REGISTER_ORGANIZATION));
    }

    @FXML
    private void handleReturnAction(ActionEvent userActionEvent) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}