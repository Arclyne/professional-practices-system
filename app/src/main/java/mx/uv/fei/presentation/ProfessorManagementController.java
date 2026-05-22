package mx.uv.fei.presentation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.ProfessorManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProfessorManagementController {

    @FXML private ListView<String> professorsListView;

    private final ProfessorManager professorManager;
    private final AppStore applicationNavigationStore;

    private final Map<String, Integer> itemToIdentifierMap = new HashMap<>();
    private final Map<String, BooleanProperty> itemSelectionStateMap = new HashMap<>();

    @Inject
    public ProfessorManagementController(ProfessorManager professorManager, AppStore applicationNavigationStore) {
        this.professorManager = professorManager;
        this.applicationNavigationStore = applicationNavigationStore;
    }

    @FXML
    public void initialize() {
        professorsListView.setCellFactory(CheckBoxListCell.forListView(itemString -> itemSelectionStateMap.get(itemString)));
        loadActiveProfessors();
    }

    private void loadActiveProfessors() {
        itemToIdentifierMap.clear();
        itemSelectionStateMap.clear();
        ObservableList<String> displayItemsList = FXCollections.observableArrayList();

        try {
            List<Professor> registeredProfessorsList = professorManager.getAllProfessors();
            for (Professor currentProfessor : registeredProfessorsList) {
                if (currentProfessor.getStatus() != null && currentProfessor.getStatus() != UserStatus.INACTIVE) {
                    String formattedDisplayString = currentProfessor.getName() + " " + currentProfessor.getLastName() + " (" + currentProfessor.getUserName() + ") - " + currentProfessor.getStatus().getDatabaseValue();
                    itemToIdentifierMap.put(formattedDisplayString, currentProfessor.getId());
                    itemSelectionStateMap.put(formattedDisplayString, new SimpleBooleanProperty(false));
                    displayItemsList.add(formattedDisplayString);
                }
            }
        } catch (ManagerException dataRetrievalException) {
            Controller.showAlert("Error de carga", dataRetrievalException.getMessage(), AlertType.ERROR);
        }

        professorsListView.setItems(displayItemsList);
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
            Controller.showAlert("Sin selección", "Debe seleccionar al menos un profesor para inactivar.", AlertType.WARNING);
            return;
        }

        try {
            professorManager.inactivateMultipleProfessors(identifiersToInactivateList);
            Controller.showAlert("Proceso Exitoso", "Los profesores seleccionados han sido inactivados.", AlertType.INFORMATION);
            loadActiveProfessors();
        } catch (ManagerException executionException) {
            Controller.showAlert("Error en la Operación", executionException.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleRegisterNewProfessorAction(ActionEvent userActionEvent) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.REGISTER_PROFESSOR));
    }

    @FXML
    private void handleReturnAction(ActionEvent userActionEvent) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}