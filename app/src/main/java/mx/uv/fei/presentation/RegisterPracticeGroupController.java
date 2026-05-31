package mx.uv.fei.presentation;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.PracticeGroup;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.PracticeGroupManager;
import mx.uv.fei.domain.manager.ProfessorManager;
import mx.uv.fei.domain.manager.PeriodManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.presentation.components.FormField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class RegisterPracticeGroupController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(RegisterPracticeGroupController.class);

    private static final String TITLE_SUCCESS = "Registro Exitoso";
    private static final String MSG_SUCCESS = "El grupo de prácticas se ha creado exitosamente.";
    private static final String TITLE_SAVE_ERROR = "Fallo en el Registro";
    private static final String TITLE_LOAD_ERROR = "Error de Carga";
    private static final String MSG_LOAD_ERROR = "No se pudieron cargar los profesores o los periodos disponibles.";
    private static final String TITLE_VALIDATION_ERROR = "Campos Incompletos";
    private static final String MSG_EMPTY_FIELDS = "Por favor, complete todos los campos para registrar el grupo.";

    private final PracticeGroupManager practiceGroupManager;
    private final ProfessorManager professorManager;
    private final PeriodManager periodManager;
    private final AppStore store;

    private final Map<String, Integer> professorMap = new HashMap<>();
    private final Map<String, Integer> periodMap = new HashMap<>();

    @FXML private FormField fieldSection;
    @FXML private FormComboBox comboBoxProfessor;
    @FXML private FormComboBox comboBoxPeriod;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    @Inject
    public RegisterPracticeGroupController(PracticeGroupManager practiceGroupManager,
                                           ProfessorManager professorManager,
                                           PeriodManager periodManager,
                                           AppStore store) {
        this.practiceGroupManager = practiceGroupManager;
        this.professorManager = professorManager;
        this.periodManager = periodManager;
        this.store = store;
    }

    @Override
    public void initialize(URL locationUrl, ResourceBundle resourcesBundle) {
        loadProfessors();
        loadPeriods();
    }

    private void loadProfessors() {
        try {
            List<Professor> professors = professorManager.getAllProfessors();
            ObservableList<String> options = FXCollections.observableArrayList();

            for (Professor prof : professors) {
                String displayName = prof.getName() + " " + prof.getLastName() + " (" + prof.getUserName() + ")";
                options.add(displayName);
                professorMap.put(displayName, prof.getId());
            }
            comboBoxProfessor.setItems(options);
        } catch (ManagerException exception) {
            log.error("Failed to load professors", exception);
            Controller.showAlert(TITLE_LOAD_ERROR, MSG_LOAD_ERROR, AlertType.ERROR);
        }
    }

    private void loadPeriods() {
        try {
            List<Period> periods = periodManager.getAllPeriods();
            ObservableList<String> options = FXCollections.observableArrayList();

            for (Period period : periods) {
                options.add(period.getPeriodName());
                periodMap.put(period.getPeriodName(), period.getPeriodId());
            }
            comboBoxPeriod.setItems(options);
        } catch (ManagerException exception) {
            log.error("Failed to load periods", exception);
            Controller.showAlert(TITLE_LOAD_ERROR, MSG_LOAD_ERROR, AlertType.ERROR);
        }
    }

    @FXML
    private void handleActionSaveButton(ActionEvent event) {
        if (validateFields()) {
            try {
                PracticeGroup newGroup = createPracticeGroup();

                practiceGroupManager.registerNewPracticeGroup(newGroup);

                Controller.showAlert(TITLE_SUCCESS, MSG_SUCCESS, AlertType.INFORMATION);
                store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));

            } catch (ManagerException exception) {
                Controller.showAlert(TITLE_SAVE_ERROR, exception.getMessage(), AlertType.ERROR);
            }
        }
    }

    private boolean validateFields() {
        boolean isValid = true;

        if (fieldSection.getText().trim().isEmpty() ||
                comboBoxProfessor.getValue() == null ||
                comboBoxPeriod.getValue() == null) {

            Controller.showAlert(TITLE_VALIDATION_ERROR, MSG_EMPTY_FIELDS, AlertType.WARNING);
            isValid = false;
        }

        return isValid;
    }

    private PracticeGroup createPracticeGroup() {
        PracticeGroup group = new PracticeGroup();
        group.setSection(fieldSection.getText().trim());

        String selectedProfessor = (String) comboBoxProfessor.getValue();
        group.setProfessorId(professorMap.get(selectedProfessor));

        String selectedPeriod = (String) comboBoxPeriod.getValue();
        group.setPeriodId(periodMap.get(selectedPeriod));

        return group;
    }

    @FXML
    private void handleActionCancelButton(ActionEvent event) {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}