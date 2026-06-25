package mx.uv.fei.presentation.coordinator;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.dto.PracticeGroup;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.academic.GroupEnrollmentManager;
import mx.uv.fei.domain.manager.academic.PeriodManager;
import mx.uv.fei.domain.manager.academic.PracticeGroupManager;
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.presentation.shell.ShellNavigator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

@Component
public class RegisterEnrollmentController implements Initializable {

    private static final String UNKNOWN_PERIOD = "Periodo desconocido";
    private static final String GROUP_LABEL_PREFIX = "NRC ";
    private static final String GROUP_PERIOD_SEPARATOR = " — ";

    @FXML private Label practitionerNameLabel;
    @FXML private FormComboBox groupFormComboBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final GroupEnrollmentManager groupEnrollmentManager;
    private final PracticeGroupManager practiceGroupManager;
    private final PeriodManager periodManager;
    private final ShellNavigator shellNavigator;

    private final Map<String, Integer> groupLabelToId = new HashMap<>();
    private final Map<Integer, String> periodNamesById = new HashMap<>();

    private Practitioner practitionerToEnroll;

    @Inject
    public RegisterEnrollmentController(GroupEnrollmentManager groupEnrollmentManager,
                                        PracticeGroupManager practiceGroupManager, PeriodManager periodManager,
                                        ShellNavigator shellNavigator) {
        this.groupEnrollmentManager = groupEnrollmentManager;
        this.practiceGroupManager = practiceGroupManager;
        this.periodManager = periodManager;
        this.shellNavigator = shellNavigator;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Object pendingEntity = shellNavigator.consumePendingEntity();
        if (pendingEntity instanceof Practitioner practitioner) {
            practitionerToEnroll = practitioner;
            practitionerNameLabel.setText(practitioner.getName() + " " + practitioner.getLastName()
                    + "  |  " + practitioner.getEnrollment());
        }
        loadGroups();
    }

    private void loadGroups() {
        try {
            mapPeriods(periodManager.getAllPeriods());
            ObservableList<String> groupOptions = FXCollections.observableArrayList();

            for (PracticeGroup practiceGroup : practiceGroupManager.getAllPracticeGroups()) {
                String groupLabel = buildGroupLabel(practiceGroup);
                groupOptions.add(groupLabel);
                groupLabelToId.put(groupLabel, practiceGroup.getGroupId());
            }

            groupFormComboBox.setItems(groupOptions);
        } catch (ManagerException e) {
            Controller.showAlert("Error de Carga",
                    "No se pudieron cargar los grupos de prácticas.", AlertType.ERROR);
        }
    }

    private String buildGroupLabel(PracticeGroup practiceGroup) {
        String periodName = periodNamesById.getOrDefault(practiceGroup.getPeriodId(), UNKNOWN_PERIOD);
        return GROUP_LABEL_PREFIX + practiceGroup.getSection() + GROUP_PERIOD_SEPARATOR + periodName;
    }

    private void mapPeriods(List<Period> periods) {
        periodNamesById.clear();
        for (Period period : periods) {
            periodNamesById.put(period.getPeriodId(), period.getPeriodName());
        }
    }

    @FXML
    private void handleActionSaveButton() {
        if (practitionerToEnroll == null || groupFormComboBox.getValue() == null) {
            Controller.showAlert("Datos incompletos",
                    "Selecciona un practicante y un grupo para reinscribir.", AlertType.WARNING);
            return;
        }

        try {
            int groupId = groupLabelToId.get((String) groupFormComboBox.getValue());
            groupEnrollmentManager.enrollPractitionerInGroup(practitionerToEnroll.getId(), groupId);
            Controller.showAlert("Reinscripción exitosa",
                    "El practicante fue inscrito en el grupo seleccionado.", AlertType.INFORMATION);
            shellNavigator.returnToList();
        } catch (ManagerException e) {
            Controller.showAlert("Error en la reinscripción", e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleActionCancelButton() {
        shellNavigator.returnToList();
    }
}
