package mx.uv.fei.presentation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataacces.repositories.OrganizationDAO;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Manager;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.statemachine.Store;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ManagerManagementController {

    @FXML private ListView<String> managersListView;

    private final OrganizationDAO managerManager;
    private final Store applicationNavigationStore;

    private final Map<String, Integer> itemToIdentifierMap = new HashMap<>();
    private final Map<String, BooleanProperty> itemSelectionStateMap = new HashMap<>();

    @Inject
    public ManagerManagementController(OrganizationDAO managerManager, Store applicationNavigationStore) {
        this.managerManager = managerManager;
        this.applicationNavigationStore = applicationNavigationStore;
    }

    @FXML
    public void initialize() {
        Controller.setupCheckBoxListViewUX(managersListView, itemSelectionStateMap);
        loadActiveManagers();
    }

    private void loadActiveManagers() {
        itemToIdentifierMap.clear();
        itemSelectionStateMap.clear();
        ObservableList<String> displayItemsList = FXCollections.observableArrayList();

        try {
            List<Manager> registeredManagersList = managerManager.getAllManagers();
            for (Manager currentManager : registeredManagersList) {

                if (currentManager.getStatus() != null && !"No Activo".equalsIgnoreCase(currentManager.getStatus())) {

                    String formattedDisplayString = currentManager.getName() + " (" + currentManager.getEmail() + ") - " + currentManager.getStatus();
                    itemToIdentifierMap.put(formattedDisplayString, currentManager.getId());
                    itemSelectionStateMap.put(formattedDisplayString, new SimpleBooleanProperty(false));
                    displayItemsList.add(formattedDisplayString);
                }
            }
        } catch (ManagerException dataRetrievalException) {
            Controller.showAlert("Error de carga", dataRetrievalException.getMessage(), AlertType.ERROR);
        }

        managersListView.setItems(displayItemsList);
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
            Controller.showAlert("Sin selección", "Debe seleccionar al menos un encargado para inactivar.", AlertType.WARNING);
            return;
        }

        try {
            managerManager.inactivateMultipleManagers(identifiersToInactivateList);
            Controller.showAlert("Proceso Exitoso", "Los encargados seleccionados han sido inactivados.", AlertType.INFORMATION);
            loadActiveManagers();
        } catch (ManagerException executionException) {
            Controller.showAlert("Error en la Operación", executionException.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleRegisterNewManagerAction(ActionEvent userActionEvent) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.REGISTER_MANAGER));
    }

    @FXML
    private void handleReturnAction(ActionEvent userActionEvent) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}