package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.CoordinatorManager;
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
public class CoordinatorManagementController {

    private static final String COORDINATOR_DISPLAY_FORMAT = "%s %s (%s) - %s";

    @FXML private ListView<String> coordinatorsListView;

    private final CoordinatorManager coordinatorManager;
    private final AppStore store;

    private final Map<String, Integer> displayToCoordinatorId = new HashMap<>();
    private final Map<String, BooleanProperty> displaySelectionState = new HashMap<>();

    @Inject
    public CoordinatorManagementController(CoordinatorManager coordinatorManager, AppStore store) {
        this.coordinatorManager = coordinatorManager;
        this.store = store;
    }

    @FXML
    public void initialize() {
        Controller.setupCheckBoxListView(coordinatorsListView, displaySelectionState);
        loadActiveCoordinators();
    }

    private void loadActiveCoordinators() {
        displayToCoordinatorId.clear();
        displaySelectionState.clear();
        ObservableList<String> displayItems = FXCollections.observableArrayList();

        try {
            List<Coordinator> coordinators = coordinatorManager.getAllCoordinators();
            for (Coordinator coordinator : coordinators) {
                if (isCoordinatorActive(coordinator)) {
                    addCoordinatorToDisplay(coordinator, displayItems);
                }
            }
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de carga", e.getMessage());
        }

        coordinatorsListView.setItems(displayItems);
    }

    private boolean isCoordinatorActive(Coordinator coordinator) {
        return coordinator.getStatus() != null && coordinator.getStatus() != UserStatus.INACTIVE;
    }

    private void addCoordinatorToDisplay(Coordinator coordinator, ObservableList<String> displayItems) {
        String displayText = String.format(COORDINATOR_DISPLAY_FORMAT,
                coordinator.getName(), coordinator.getLastName(),
                coordinator.getUserName(), coordinator.getStatus().getDatabaseValue());
        displayToCoordinatorId.put(displayText, coordinator.getId());
        displaySelectionState.put(displayText, new SimpleBooleanProperty(false));
        displayItems.add(displayText);
    }

    private List<Integer> collectSelectedCoordinatorIds() {
        List<Integer> selectedCoordinatorIds = new ArrayList<>();
        for (Map.Entry<String, BooleanProperty> selectionEntry : displaySelectionState.entrySet()) {
            if (selectionEntry.getValue().get()) {
                selectedCoordinatorIds.add(displayToCoordinatorId.get(selectionEntry.getKey()));
            }
        }
        return selectedCoordinatorIds;
    }

    @FXML
    private void handleEditSelectedAction() {
        List<Integer> selectedCoordinatorIds = collectSelectedCoordinatorIds();

        if (selectedCoordinatorIds.size() != 1) {
            Controller.showAlert("Selección inválida",
                    "Seleccione exactamente un coordinador para editar.", AlertType.WARNING);
        } else {
            store.dispatch(new NavigationAction.ViewEntityDetails(
                    AppSection.REGISTER_COORDINATOR, String.valueOf(selectedCoordinatorIds.getFirst())));
        }
    }

    @FXML
    private void handleRegisterNewCoordinatorAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.REGISTER_COORDINATOR));
    }

    @FXML
    private void handleReturnAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}
