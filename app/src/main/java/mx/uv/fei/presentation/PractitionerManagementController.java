package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.PractitionerManager;
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
public class PractitionerManagementController {

    private static final String PRACTITIONER_DISPLAY_FORMAT = "%s %s (%s) - %s";

    @FXML private ListView<String> practitionersListView;

    private final PractitionerManager practitionerManager;
    private final AppStore store;

    private final Map<String, Integer> displayToPractitionerId = new HashMap<>();
    private final Map<String, BooleanProperty> displaySelectionState = new HashMap<>();

    @Inject
    public PractitionerManagementController(PractitionerManager practitionerManager, AppStore store) {
        this.practitionerManager = practitionerManager;
        this.store = store;
    }

    @FXML
    public void initialize() {
        Controller.setupCheckBoxListView(practitionersListView, displaySelectionState);
        loadActivePractitioners();
    }

    private void loadActivePractitioners() {
        displayToPractitionerId.clear();
        displaySelectionState.clear();
        ObservableList<String> displayItems = FXCollections.observableArrayList();

        try {
            List<Practitioner> practitioners = practitionerManager.getAllPractitioners();
            for (Practitioner practitioner : practitioners) {
                if (isPractitionerActive(practitioner)) {
                    addPractitionerToDisplay(practitioner, displayItems);
                }
            }
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de carga", e.getMessage());
        }

        practitionersListView.setItems(displayItems);
    }

    private boolean isPractitionerActive(Practitioner practitioner) {
        return practitioner.getStatus() != null && practitioner.getStatus() != UserStatus.INACTIVE;
    }

    private void addPractitionerToDisplay(Practitioner practitioner, ObservableList<String> displayItems) {
        String displayText = String.format(PRACTITIONER_DISPLAY_FORMAT,
                practitioner.getName(), practitioner.getLastName(),
                practitioner.getEnrollment(), practitioner.getStatus().getDatabaseValue());
        displayToPractitionerId.put(displayText, practitioner.getId());
        displaySelectionState.put(displayText, new SimpleBooleanProperty(false));
        displayItems.add(displayText);
    }

    private List<Integer> collectSelectedPractitionerIds() {
        List<Integer> selectedPractitionerIds = new ArrayList<>();
        for (Map.Entry<String, BooleanProperty> selectionEntry : displaySelectionState.entrySet()) {
            if (selectionEntry.getValue().get()) {
                selectedPractitionerIds.add(displayToPractitionerId.get(selectionEntry.getKey()));
            }
        }
        return selectedPractitionerIds;
    }

    @FXML
    private void handleEditSelectedAction() {
        List<Integer> selectedPractitionerIds = collectSelectedPractitionerIds();

        if (selectedPractitionerIds.size() != 1) {
            Controller.showAlert("Selección inválida",
                    "Seleccione exactamente un practicante para editar.", AlertType.WARNING);
        } else {
            store.dispatch(new NavigationAction.ViewEntityDetails(
                    AppSection.REGISTER_PRACTITIONER, String.valueOf(selectedPractitionerIds.getFirst())));
        }
    }

    @FXML
    private void handleRegisterNewPractitionerAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.REGISTER_PRACTITIONER));
    }

    @FXML
    private void handleReturnAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}
