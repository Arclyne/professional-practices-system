package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.dto.PracticeGroup;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.PeriodManager;
import mx.uv.fei.domain.manager.PracticeGroupManager;
import mx.uv.fei.domain.manager.ProfessorManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.presentation.components.FormField;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

@Component
public class RegisterPracticeGroupController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(RegisterPracticeGroupController.class);

    @FXML private FormField fieldSection;
    @FXML private FormComboBox comboBoxProfessor;
    @FXML private FormComboBox comboBoxPeriod;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final PracticeGroupManager practiceGroupManager;
    private final ProfessorManager professorManager;
    private final PeriodManager periodManager;
    private final AppStore store;

    private final Map<String, Integer> professorNameToId = new HashMap<>();
    private final Map<String, Integer> periodNameToId = new HashMap<>();

    @Inject
    public RegisterPracticeGroupController(PracticeGroupManager practiceGroupManager,
                                           ProfessorManager professorManager, PeriodManager periodManager, AppStore store) {
        this.practiceGroupManager = practiceGroupManager;
        this.professorManager = professorManager;
        this.periodManager = periodManager;
        this.store = store;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadProfessors();
        loadPeriods();
    }

    private void loadProfessors() {
        try {
            List<Professor> professors = professorManager.getAllProfessors();
            ObservableList<String> professorOptions = FXCollections.observableArrayList();

            for (Professor professor : professors) {
                String displayName = professor.getName() + " " + professor.getLastName()
                        + " (" + professor.getUserName() + ")";
                professorOptions.add(displayName);
                professorNameToId.put(displayName, professor.getId());
            }
            comboBoxProfessor.setItems(professorOptions);
        } catch (ManagerException e) {
            log.error("Error al cargar los profesores.", e);
            Controller.showAlert("Error de Carga",
                    "No se pudieron cargar los profesores o los periodos disponibles.", AlertType.ERROR);
        }
    }

    private void loadPeriods() {
        try {
            List<Period> periods = periodManager.getAllPeriods();
            ObservableList<String> periodOptions = FXCollections.observableArrayList();

            for (Period period : periods) {
                periodOptions.add(period.getPeriodName());
                periodNameToId.put(period.getPeriodName(), period.getPeriodId());
            }
            comboBoxPeriod.setItems(periodOptions);
        } catch (ManagerException e) {
            log.error("Error al cargar los periodos.", e);
            Controller.showAlert("Error de Carga",
                    "No se pudieron cargar los profesores o los periodos disponibles.", AlertType.ERROR);
        }
    }

    @FXML
    private void handleActionSaveButton() {
        if (isFormIncomplete()) {
            Controller.showAlert("Campos Incompletos",
                    "Por favor, complete todos los campos para registrar el grupo.", AlertType.WARNING);
            return;
        }

        try {
            PracticeGroup practiceGroup = buildPracticeGroupFromForm();
            practiceGroupManager.registerNewPracticeGroup(practiceGroup);
            Controller.showAlert("Registro Exitoso",
                    "El grupo de prácticas se ha creado exitosamente.", AlertType.INFORMATION);
            store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
        } catch (ManagerException e) {
            Controller.showAlert("Fallo en el Registro", e.getMessage(), AlertType.ERROR);
        }
    }

    private boolean isFormIncomplete() {
        return fieldSection.getText().trim().isEmpty()
                || comboBoxProfessor.getValue() == null
                || comboBoxPeriod.getValue() == null;
    }

    private PracticeGroup buildPracticeGroupFromForm() {
        PracticeGroup practiceGroup = new PracticeGroup();
        practiceGroup.setSection(fieldSection.getText().trim());
        practiceGroup.setProfessorId(professorNameToId.get((String) comboBoxProfessor.getValue()));
        practiceGroup.setPeriodId(periodNameToId.get((String) comboBoxPeriod.getValue()));
        return practiceGroup;
    }

    @FXML
    private void handleActionCancelButton() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}