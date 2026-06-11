package mx.uv.fei.presentation;

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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ManagerManagementController {

    private static final String MANAGER_DISPLAY_FORMAT = "%s (%s) - %s";

    @FXML private ListView<String> managersListView;

    private final ManagerManager managerManager;
    private final AppStore store;

    private final Map<String, Integer> displayToManagerId = new HashMap<>();
    private final Map<String, BooleanProperty> displaySelectionState = new HashMap<>();

    @Inject
    public ManagerManagementController(ManagerManager managerManager, AppStore store) {
        this.managerManager = managerManager;
        this.store = store;
    }

    @FXML
    public void initialize() {
        Controller.setupCheckBoxListView(managersListView, displaySelectionState);
        loadActiveManagers();
    }

    private void loadActiveManagers() {
        displayToManagerId.clear();
        displaySelectionState.clear();
        ObservableList<String> displayItems = FXCollections.observableArrayList();

        try {
            List<Manager> managers = managerManager.getAllManagers();
            for (Manager manager : managers) {
                if (isManagerActive(manager)) {
                    addManagerToDisplay(manager, displayItems);
                }
            }
        } catch (ManagerException e) {
            Controller.showAlert("Error de carga", e.getMessage(), AlertType.ERROR);
        }

        managersListView.setItems(displayItems);
    }

    private boolean isManagerActive(Manager manager) {
        return manager.getStatus() != null && manager.getStatus() != UserStatus.INACTIVE;
    }

    private void addManagerToDisplay(Manager manager, ObservableList<String> displayItems) {
        String displayText = String.format(MANAGER_DISPLAY_FORMAT,
                manager.getName(), manager.getEmail(), manager.getStatus());
        displayToManagerId.put(displayText, manager.getId());
        displaySelectionState.put(displayText, new SimpleBooleanProperty(false));
        displayItems.add(displayText);
    }

    @FXML
    private void handleInactivateSelectedAction() {
        List<Integer> selectedManagerIds = collectSelectedManagerIds();

        if (selectedManagerIds.isEmpty()) {
            Controller.showAlert("Sin selección",
                    "Debe seleccionar al menos un encargado para inactivar.", AlertType.WARNING);
        } else {
            try {
                managerManager.inactivateMultipleManagers(selectedManagerIds);
                Controller.showInfoAlert("Proceso Exitoso",
                        "Los encargados seleccionados han sido inactivados.");
                loadActiveManagers();
            } catch (ManagerException e) {
                Controller.showErrorAlert("Error en la Operación", e.getMessage());
            }
        }
    }

    private List<Integer> collectSelectedManagerIds() {
        List<Integer> selectedManagerIds = new ArrayList<>();
        for (Map.Entry<String, BooleanProperty> selectionEntry : displaySelectionState.entrySet()) {
            if (selectionEntry.getValue().get()) {
                selectedManagerIds.add(displayToManagerId.get(selectionEntry.getKey()));
            }
        }
        return selectedManagerIds;
    }

    @FXML
    private void handleRegisterNewManagerAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.REGISTER_MANAGER));
    }

    @FXML
    private void handleReturnAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}