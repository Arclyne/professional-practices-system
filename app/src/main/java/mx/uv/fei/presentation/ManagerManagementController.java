package mx.uv.fei.presentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;


import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Manager;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.ManagerManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;


@Component
public class ManagerManagementController {

    private static final String LOAD_ERROR_TITLE = "Error de carga";
    private static final String NO_SELECTION_TITLE = "Sin selección";
    private static final String NO_SELECTION_MESSAGE = "Debe seleccionar al menos un encargado para inactivar.";
    private static final String SUCCESS_TITLE = "Proceso Exitoso";
    private static final String SUCCESS_MESSAGE = "Los encargados seleccionados han sido inactivados.";
    private static final String OPERATION_ERROR_TITLE = "Error en la Operación";
    private static final String DISPLAY_ITEM_FORMAT = "%s (%s) - %s";

    @FXML
    private ListView<String> managersListView;

    private final ManagerManager managerManager;
    private final AppStore applicationNavigationStore;

    private final Map<String, Integer> itemToIdentifierMap = new HashMap<>();
    private final Map<String, BooleanProperty> itemSelectionStateMap = new HashMap<>();

    @Inject
    public ManagerManagementController(ManagerManager managerManager, AppStore applicationNavigationStore) {
        this.managerManager = managerManager;
        this.applicationNavigationStore = applicationNavigationStore;
    }

    @FXML
    public void initialize() {
        Controller.setupCheckBoxListView(managersListView, itemSelectionStateMap);
        loadActiveManagers();
    }

    private void loadActiveManagers() {
        itemToIdentifierMap.clear();
        itemSelectionStateMap.clear();
        ObservableList<String> displayItemsList = FXCollections.observableArrayList();

        try {
            List<Manager> registeredManagersList = managerManager.getAllManagers();

            for (Manager currentManager : registeredManagersList) {
                if (currentManager.getStatus() != null && currentManager.getStatus() != UserStatus.INACTIVE) {
                    String formattedDisplayString = String.format(DISPLAY_ITEM_FORMAT, currentManager.getName(), currentManager.getEmail(), currentManager.getStatus());
                    itemToIdentifierMap.put(formattedDisplayString, currentManager.getId());
                    itemSelectionStateMap.put(formattedDisplayString, new SimpleBooleanProperty(false));
                    displayItemsList.add(formattedDisplayString);
                }
            }
        } catch (ManagerException exception) {
            Controller.showAlert(LOAD_ERROR_TITLE, exception.getMessage(), AlertType.ERROR);
        }

        managersListView.setItems(displayItemsList);
    }

    @FXML
    private void handleInactivateSelectedAction() {
        List<Integer> identifiersToInactivateList = new ArrayList<>();

        for (Map.Entry<String, BooleanProperty> currentMapEntry : itemSelectionStateMap.entrySet()) {
            if (currentMapEntry.getValue().get()) {
                identifiersToInactivateList.add(itemToIdentifierMap.get(currentMapEntry.getKey()));
            }
        }

        if (identifiersToInactivateList.isEmpty()) {
            Controller.showAlert(NO_SELECTION_TITLE, NO_SELECTION_MESSAGE, AlertType.WARNING);
        } else {
            try {
                managerManager.inactivateMultipleManagers(identifiersToInactivateList);
                Controller.showInfoAlert(SUCCESS_TITLE, SUCCESS_MESSAGE);
                loadActiveManagers();
            } catch (ManagerException exception) {
                Controller.showErrorAlert(OPERATION_ERROR_TITLE, exception.getMessage());
            }
        }
    }

    @FXML
    private void handleRegisterNewManagerAction() {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.REGISTER_MANAGER));
    }

    @FXML
    private void handleReturnAction() {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}