package mx.uv.fei.presentation;

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
public class ProfessorManagementController {

    private static final String PROFESSOR_DISPLAY_FORMAT = "%s %s (%s) - %s";

    @FXML private ListView<String> professorsListView;

    private final ProfessorManager professorManager;
    private final AppStore store;

    private final Map<String, Integer> displayToProfessorId = new HashMap<>();
    private final Map<String, BooleanProperty> displaySelectionState = new HashMap<>();

    @Inject
    public ProfessorManagementController(ProfessorManager professorManager, AppStore store) {
        this.professorManager = professorManager;
        this.store = store;
    }

    @FXML
    public void initialize() {
        Controller.setupCheckBoxListView(professorsListView, displaySelectionState);
        loadActiveProfessors();
    }

    private void loadActiveProfessors() {
        displayToProfessorId.clear();
        displaySelectionState.clear();
        ObservableList<String> displayItems = FXCollections.observableArrayList();

        try {
            List<Professor> professors = professorManager.getAllProfessors();
            for (Professor professor : professors) {
                if (isProfessorActive(professor)) {
                    addProfessorToDisplay(professor, displayItems);
                }
            }
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de carga", e.getMessage());
        }

        professorsListView.setItems(displayItems);
    }

    private boolean isProfessorActive(Professor professor) {
        return professor.getStatus() != null && professor.getStatus() != UserStatus.INACTIVE;
    }

    private void addProfessorToDisplay(Professor professor, ObservableList<String> displayItems) {
        String displayText = String.format(PROFESSOR_DISPLAY_FORMAT,
                professor.getName(), professor.getLastName(),
                professor.getUserName(), professor.getStatus().getDatabaseValue());
        displayToProfessorId.put(displayText, professor.getId());
        displaySelectionState.put(displayText, new SimpleBooleanProperty(false));
        displayItems.add(displayText);
    }

    @FXML
    private void handleInactivateSelectedAction() {
        List<Integer> selectedProfessorIds = collectSelectedProfessorIds();

        if (selectedProfessorIds.isEmpty()) {
            Controller.showAlert("Sin selección",
                    "Debe seleccionar al menos un profesor para inactivar.", AlertType.WARNING);
        } else {
            try {
                professorManager.inactivateMultipleProfessors(selectedProfessorIds);
                Controller.showInfoAlert("Proceso Exitoso",
                        "Los profesores seleccionados han sido inactivados.");
                loadActiveProfessors();
            } catch (ManagerException e) {
                Controller.showErrorAlert("Error en la Operación", e.getMessage());
            }
        }
    }

    private List<Integer> collectSelectedProfessorIds() {
        List<Integer> selectedProfessorIds = new ArrayList<>();
        for (Map.Entry<String, BooleanProperty> selectionEntry : displaySelectionState.entrySet()) {
            if (selectionEntry.getValue().get()) {
                selectedProfessorIds.add(displayToProfessorId.get(selectionEntry.getKey()));
            }
        }
        return selectedProfessorIds;
    }

    @FXML
    private void handleRegisterNewProfessorAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.REGISTER_PROFESSOR));
    }

    @FXML
    private void handleReturnAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}