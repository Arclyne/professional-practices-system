package mx.uv.fei.presentation.coordinator;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.dto.PracticeGroup;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.domain.enums.PeriodStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.academic.PeriodManager;
import mx.uv.fei.domain.manager.academic.PracticeGroupManager;
import mx.uv.fei.domain.manager.people.ProfessorManager;
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.presentation.components.FormField;
import mx.uv.fei.presentation.shell.ShellNavigator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    private final ShellNavigator shellNavigator;

    private final Map<String, Integer> professorIdByName = new HashMap<>();
    private final Map<Integer, String> professorNameById = new HashMap<>();
    private final Map<String, Integer> periodIdByName = new HashMap<>();
    private final Map<Integer, String> periodNameById = new HashMap<>();

    private PracticeGroup groupBeingEdited;

    @Inject
    public RegisterPracticeGroupController(PracticeGroupManager practiceGroupManager,
                                           ProfessorManager professorManager, PeriodManager periodManager,
                                           ShellNavigator shellNavigator) {
        this.practiceGroupManager = practiceGroupManager;
        this.professorManager = professorManager;
        this.periodManager = periodManager;
        this.shellNavigator = shellNavigator;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadProfessors();
        loadPeriods();

        Object pendingEntity = shellNavigator.consumePendingEntity();
        if (pendingEntity instanceof PracticeGroup) {
            groupBeingEdited = (PracticeGroup) pendingEntity;
            populateFormForEdit(groupBeingEdited);
        } else {
            groupBeingEdited = null;
            saveButton.setText("Guardar Grupo");
        }
    }

    private void loadProfessors() {
        try {
            List<Professor> professors = professorManager.getAllProfessors();
            ObservableList<String> professorOptions = FXCollections.observableArrayList();

            for (Professor professor : professors) {
                String displayName = professor.getName() + " " + professor.getLastName()
                        + " (" + professor.getUserName() + ")";
                professorOptions.add(displayName);
                professorIdByName.put(displayName, professor.getId());
                professorNameById.put(professor.getId(), displayName);
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
                if (isSelectablePeriod(period)) {
                    String displayLabel = buildPeriodDisplayLabel(period);
                    periodOptions.add(displayLabel);
                    periodIdByName.put(displayLabel, period.getPeriodId());
                    periodNameById.put(period.getPeriodId(), displayLabel);
                }
            }
            comboBoxPeriod.setItems(periodOptions);
        } catch (ManagerException e) {
            log.error("Error al cargar los periodos.", e);
            Controller.showAlert("Error de Carga",
                    "No se pudieron cargar los profesores o los periodos disponibles.", AlertType.ERROR);
        }
    }

    private boolean isSelectablePeriod(Period period) {
        PeriodStatus status = PeriodStatus.fromString(period.getPeriodStatus());
        return status == PeriodStatus.UPCOMING || status == PeriodStatus.ACTIVE;
    }

    private String buildPeriodDisplayLabel(Period period) {
        return period.getPeriodName() + " (" + PeriodStatus.fromString(period.getPeriodStatus()).getDisplayLabel() + ")";
    }

    private void populateFormForEdit(PracticeGroup group) {
        saveButton.setText("Guardar cambios");
        fieldSection.setText(group.getSection() != null ? group.getSection() : "");
        comboBoxProfessor.setValue(professorNameById.get(group.getProfessorId()));
        comboBoxPeriod.setValue(periodNameById.get(group.getPeriodId()));
    }

    @FXML
    private void handleActionSaveButton() {
        if (isFormIncomplete()) {
            Controller.showAlert("Campos Incompletos",
                    "Por favor, complete todos los campos para registrar el grupo.", AlertType.WARNING);
        } else if (groupBeingEdited != null) {
            updatePracticeGroup();
        } else {
            registerPracticeGroup();
        }
    }

    private void registerPracticeGroup() {
        try {
            practiceGroupManager.registerNewPracticeGroup(buildPracticeGroupFromForm());
            Controller.showAlert("Registro Exitoso",
                    "El grupo de prácticas se ha creado exitosamente.", AlertType.INFORMATION);
            shellNavigator.returnToList();
        } catch (ManagerException e) {
            Controller.showAlert("Fallo en el Registro", e.getMessage(), AlertType.ERROR);
        }
    }

    private void updatePracticeGroup() {
        try {
            practiceGroupManager.updatePracticeGroup(buildPracticeGroupFromForm(), groupBeingEdited.getGroupId());
            Controller.showAlert("Actualización Exitosa",
                    "El grupo de prácticas se actualizó correctamente.", AlertType.INFORMATION);
            shellNavigator.returnToList();
        } catch (ManagerException e) {
            Controller.showAlert("Error al Actualizar", e.getMessage(), AlertType.ERROR);
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
        practiceGroup.setProfessorId(professorIdByName.get((String) comboBoxProfessor.getValue()));
        practiceGroup.setPeriodId(periodIdByName.get((String) comboBoxPeriod.getValue()));
        return practiceGroup;
    }

    @FXML
    private void handleActionCancelButton() {
        shellNavigator.returnToList();
    }
}
