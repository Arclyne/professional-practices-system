package mx.uv.fei.presentation.coordinator;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.BatchRegistrationSummary;
import mx.uv.fei.domain.dto.PracticeGroup;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.academic.PracticeGroupManager;
import mx.uv.fei.domain.manager.people.PractitionerManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.presentation.components.FormField;
import mx.uv.fei.presentation.shell.ShellNavigator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

@Component
public class RegisterPractitionerController implements Initializable {

    private static final String DEFAULT_INDIGENOUS_LANGUAGE = "Ninguna";

    @FXML private FormField fieldEnrollment;
    @FXML private FormField fieldName;
    @FXML private FormField fieldLastName;
    @FXML private FormField fieldEmail;
    @FXML private FormComboBox comboBoxGender;
    @FXML private FormComboBox comboBoxPracticeGroup;
    @FXML private FormField fieldIndigenousLanguage;
    @FXML private Button cancelButton;
    @FXML private Button registerButton;
    @FXML private Button uploadCsvButton;

    private final PractitionerManager practitionerManager;
    private final PracticeGroupManager practiceGroupManager;
    private final AppStore store;
    private final ShellNavigator shellNavigator;

    private final Map<String, Integer> groupDisplayToId = new HashMap<>();

    private Practitioner practitionerBeingEdited;

    @Inject
    public RegisterPractitionerController(PractitionerManager practitionerManager,
                                          PracticeGroupManager practiceGroupManager, AppStore store,
                                          ShellNavigator shellNavigator) {
        this.practitionerManager = practitionerManager;
        this.practiceGroupManager = practiceGroupManager;
        this.store = store;
        this.shellNavigator = shellNavigator;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadGenderOptions();
        loadPracticeGroups();

        Object pendingEntity = shellNavigator.consumePendingEntity();
        if (pendingEntity instanceof Practitioner) {
            practitionerBeingEdited = (Practitioner) pendingEntity;
            populateFormForEdit(practitionerBeingEdited);
        } else {
            practitionerBeingEdited = null;
            registerButton.setText("Registrar");
        }
    }

    private void populateFormForEdit(Practitioner practitioner) {
        registerButton.setText("Guardar cambios");
        fieldEnrollment.setText(practitioner.getEnrollment() != null ? practitioner.getEnrollment() : "");
        fieldEnrollment.setDisable(true);
        fieldName.setText(practitioner.getName() != null ? practitioner.getName() : "");
        fieldLastName.setText(practitioner.getLastName() != null ? practitioner.getLastName() : "");
        fieldEmail.setText(practitioner.getEmail() != null ? practitioner.getEmail() : "");
        fieldIndigenousLanguage.setText(practitioner.getIndigenousLanguage() != null ? practitioner.getIndigenousLanguage() : "");

        if (practitioner.getGender() != null) {
            comboBoxGender.setValue(practitioner.getGender().getDisplayValue());
        }
        selectGroupForId(practitioner.getGroupId());
        comboBoxPracticeGroup.setVisible(false);
        comboBoxPracticeGroup.setManaged(false);

        uploadCsvButton.setVisible(false);
        uploadCsvButton.setManaged(false);
    }

    private void selectGroupForId(Integer groupId) {
        if (groupId != null) {
            selectMatchingGroup(groupId);
        }
    }

    private void selectMatchingGroup(Integer groupId) {
        for (Map.Entry<String, Integer> groupEntry : groupDisplayToId.entrySet()) {
            if (groupEntry.getValue().equals(groupId)) {
                comboBoxPracticeGroup.setValue(groupEntry.getKey());
            }
        }
    }

    private void loadGenderOptions() {
        ObservableList<String> genderOptions = FXCollections.observableArrayList(
                Gender.MALE.getDisplayValue(),
                Gender.FEMALE.getDisplayValue(),
                Gender.OTHER.getDisplayValue());
        comboBoxGender.setItems(genderOptions);
    }

    private void loadPracticeGroups() {
        try {
            List<PracticeGroup> practiceGroups = practiceGroupManager.getAllPracticeGroups();
            ObservableList<String> groupOptions = FXCollections.observableArrayList();

            for (PracticeGroup practiceGroup : practiceGroups) {
                String displayText = "Sección " + practiceGroup.getSection()
                        + " (Periodo: " + practiceGroup.getPeriodId() + ")";
                groupOptions.add(displayText);
                groupDisplayToId.put(displayText, practiceGroup.getGroupId());
            }
            comboBoxPracticeGroup.setItems(groupOptions);
        } catch (ManagerException e) {
            Controller.showAlert("Error de Carga",
                    "No se pudieron cargar los grupos de prácticas.", AlertType.ERROR);
        }
    }

    @FXML
    private void handleActionRegisterButton() {
        if (isFormIncomplete()) {
            Controller.showAlert("Campos Incompletos",
                    "Por favor, llene todos los campos obligatorios.", AlertType.WARNING);
        } else if (practitionerBeingEdited != null) {
            updatePractitioner();
        } else {
            registerSinglePractitioner();
        }
    }

    private void updatePractitioner() {
        try {
            registerButton.setDisable(true);
            applyEditableFields(practitionerBeingEdited);
            practitionerManager.updatePractitioner(practitionerBeingEdited, practitionerBeingEdited.getId());
            Controller.showAlert("Actualización Exitosa",
                    "La información del practicante se actualizó correctamente.", AlertType.INFORMATION);
            shellNavigator.returnToList();
        } catch (ManagerException e) {
            Controller.showAlert("Error al Actualizar", e.getMessage(), AlertType.ERROR);
        } finally {
            registerButton.setDisable(false);
        }
    }

    private void applyEditableFields(Practitioner practitioner) {
        practitioner.setName(fieldName.getText().trim());
        practitioner.setLastName(fieldLastName.getText().trim());
        practitioner.setEmail(fieldEmail.getText().trim());
        practitioner.setGender(Gender.fromDisplayValue((String) comboBoxGender.getValue()));
        practitioner.setGroupId(groupDisplayToId.get((String) comboBoxPracticeGroup.getValue()));
        practitioner.setIndigenousLanguage(resolveIndigenousLanguage());
    }

    private void registerSinglePractitioner() {
        Practitioner practitioner = buildPractitionerFromForm();
        try {
            registerButton.setDisable(true);
            String generatedPassword = practitionerManager.registerNewPractitioner(practitioner);
            Controller.showAlert("Registro Exitoso",
                    "Practicante registrado exitosamente.\nContraseña: " + generatedPassword,
                    AlertType.INFORMATION);
            clearForm();
        } catch (ManagerException e) {
            Controller.showAlert("Error en el Registro", e.getMessage(), AlertType.ERROR);
        } finally {
            registerButton.setDisable(false);
        }
    }

    private boolean isFormIncomplete() {
        return fieldEnrollment.getText().trim().isEmpty()
                || fieldName.getText().trim().isEmpty()
                || fieldLastName.getText().trim().isEmpty()
                || fieldEmail.getText().trim().isEmpty()
                || comboBoxGender.getValue() == null
                || comboBoxPracticeGroup.getValue() == null;
    }

    private Practitioner buildPractitionerFromForm() {
        Practitioner practitioner = new Practitioner();
        practitioner.setEnrollment(fieldEnrollment.getText().trim());
        practitioner.setName(fieldName.getText().trim());
        practitioner.setLastName(fieldLastName.getText().trim());
        practitioner.setEmail(fieldEmail.getText().trim());
        practitioner.setGender(Gender.fromDisplayValue((String) comboBoxGender.getValue()));
        practitioner.setGroupId(groupDisplayToId.get((String) comboBoxPracticeGroup.getValue()));
        practitioner.setIndigenousLanguage(resolveIndigenousLanguage());
        return practitioner;
    }

    private String resolveIndigenousLanguage() {
        String language = fieldIndigenousLanguage.getText().trim();
        return language.isEmpty() ? DEFAULT_INDIGENOUS_LANGUAGE : language;
    }

    @FXML
    private void handleActionUploadCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File selectedFile = fileChooser.showOpenDialog(fieldEnrollment.getScene().getWindow());

        if (selectedFile != null) {
            registerPractitionerBatch(selectedFile);
        }
    }

    private void registerPractitionerBatch(File selectedFile) {
        try {
            User currentUser = store.getState().sessionState().currentUserInSession();
            String coordinatorName = currentUser.getName() + "_" + currentUser.getLastName();
            BatchRegistrationSummary summary = practitionerManager
                    .registerPractitionerBatch(selectedFile, coordinatorName);

            Controller.showAlert("Resumen de Registro",
                    "Exitosos: " + summary.getSuccessfulRegistrations()
                            + "\nFallidos: " + summary.getFailedRegistrations(),
                    AlertType.INFORMATION);
            shellNavigator.returnToList();
        } catch (ManagerException e) {
            Controller.showAlert("Error en el Registro", e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleActionCancelButton() {
        shellNavigator.returnToList();
    }

    private void clearForm() {
        fieldEnrollment.setText("");
        fieldName.setText("");
        fieldLastName.setText("");
        fieldEmail.setText("");
        fieldIndigenousLanguage.setText("");
        comboBoxGender.clearSelection();
        comboBoxPracticeGroup.clearSelection();
    }
}