package mx.uv.fei.presentation;

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

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.dto.PracticeGroup;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.dto.BatchRegistrationSummary;
import mx.uv.fei.presentation.components.FormField;
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.domain.manager.PractitionerManager;
import mx.uv.fei.domain.manager.PracticeGroupManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.common.Controller;

@Component
public class RegisterPractitionerController implements Initializable {

    private static final String TITLE_INCOMPLETE_FIELDS = "Campos Incompletos";
    private static final String MSG_INCOMPLETE_FIELDS = "Por favor, llene todos los campos obligatorios.";
    private static final String TITLE_SUCCESS = "Registro Exitoso";
    private static final String MSG_SUCCESS = "Practicante registrado exitosamente.\nContraseña: ";
    private static final String TITLE_ERROR = "Error en el Registro";
    private static final String TITLE_BATCH_SUMMARY = "Resumen de Registro";
    private static final String MSG_BATCH_SUCCESS = "Exitosos: ";
    private static final String MSG_BATCH_FAILED = "\nFallidos: ";
    private static final String TITLE_LOAD_GROUPS_ERROR = "Error de Carga";
    private static final String MSG_LOAD_GROUPS_ERROR = "No se pudieron cargar los grupos de prácticas.";
    private static final String DEFAULT_LANGUAGE = "Ninguna";

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
    private final AppStore applicationNavigationStore;

    private final Map<String, Integer> groupMap = new HashMap<>();

    @Inject
    public RegisterPractitionerController(PractitionerManager practitionerManager,
                                          PracticeGroupManager practiceGroupManager,
                                          AppStore applicationNavigationStore) {
        this.practitionerManager = practitionerManager;
        this.practiceGroupManager = practiceGroupManager;
        this.applicationNavigationStore = applicationNavigationStore;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadGenderOptions();
        loadPracticeGroups();
    }

    private void loadGenderOptions() {
        ObservableList<String> genderOptions = FXCollections.observableArrayList(
                Gender.MALE.getDisplayValue(),
                Gender.FEMALE.getDisplayValue(),
                Gender.OTHER.getDisplayValue()
        );
        comboBoxGender.setItems(genderOptions);
    }

    private void loadPracticeGroups() {
        try {
            List<PracticeGroup> groupsList = practiceGroupManager.getAllPracticeGroups();
            ObservableList<String> groupOptions = FXCollections.observableArrayList();

            for (PracticeGroup group : groupsList) {
                String displayString = "Sección " + group.getSection() + " (Periodo: " + group.getPeriodId() + ")";
                groupOptions.add(displayString);
                groupMap.put(displayString, group.getGroupId());
            }

            comboBoxPracticeGroup.setItems(groupOptions);

        } catch (ManagerException e) {
            Controller.showAlert(TITLE_LOAD_GROUPS_ERROR, MSG_LOAD_GROUPS_ERROR, AlertType.ERROR);
        }
    }

    @FXML
    private void handleActionRegisterButton(ActionEvent event) {
        if (validateFields()) {
            Practitioner newPractitioner = createPractitioner();

            try {
                if (registerButton != null) {
                    registerButton.setDisable(true);
                }

                String generatedPassword = practitionerManager.registerNewPractitioner(newPractitioner);
                Controller.showAlert(TITLE_SUCCESS, MSG_SUCCESS + generatedPassword, AlertType.INFORMATION);
                clearForm();

            } catch (ManagerException e) {
                Controller.showAlert(TITLE_ERROR, e.getMessage(), AlertType.ERROR);
            } finally {
                if (registerButton != null) {
                    registerButton.setDisable(false);
                }
            }
        }
    }

    private boolean validateFields() {
        boolean isValid = true;

        if (fieldEnrollment.getText().trim().isEmpty() ||
                fieldName.getText().trim().isEmpty() ||
                fieldLastName.getText().trim().isEmpty() ||
                fieldEmail.getText().trim().isEmpty() ||
                comboBoxGender.getValue() == null ||
                comboBoxPracticeGroup.getValue() == null) {

            Controller.showAlert(TITLE_INCOMPLETE_FIELDS, MSG_INCOMPLETE_FIELDS, AlertType.WARNING);
            isValid = false;
        }

        return isValid;
    }

    private Practitioner createPractitioner() {
        Practitioner newPractitioner = new Practitioner();
        newPractitioner.setEnrollment(fieldEnrollment.getText().trim());
        newPractitioner.setName(fieldName.getText().trim());
        newPractitioner.setLastName(fieldLastName.getText().trim());
        newPractitioner.setEmail(fieldEmail.getText().trim());

        String selectedGender = (String) comboBoxGender.getValue();
        newPractitioner.setGender(Gender.fromDisplayValue(selectedGender));

        String selectedGroup = (String) comboBoxPracticeGroup.getValue();
        newPractitioner.setGroupId(groupMap.get(selectedGroup));

        String language = fieldIndigenousLanguage.getText().trim().isEmpty() ? DEFAULT_LANGUAGE : fieldIndigenousLanguage.getText().trim();
        newPractitioner.setIndigenousLanguage(language);

        return newPractitioner;
    }

    @FXML
    private void handleActionUploadCSV(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));

        File selectedFile = fileChooser.showOpenDialog(fieldEnrollment.getScene().getWindow());

        if (selectedFile != null) {
            try {
                User currentUser = applicationNavigationStore.getState().sessionState().currentUserInSession();
                String coordinatorName = currentUser.getName() + "_" + currentUser.getLastName();

                BatchRegistrationSummary summary = practitionerManager.registerPractitionerBatch(selectedFile, coordinatorName);
                String resultMessage = MSG_BATCH_SUCCESS + summary.getSuccessfulRegistrations() + MSG_BATCH_FAILED + summary.getFailedRegistrations();

                Controller.showAlert(TITLE_BATCH_SUMMARY, resultMessage, AlertType.INFORMATION);
                applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));

            } catch (ManagerException e) {
                Controller.showAlert(TITLE_ERROR, e.getMessage(), AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleActionCancelButton(ActionEvent event) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }

    private void clearForm() {
        if (fieldEnrollment != null) fieldEnrollment.setText("");
        if (fieldName != null) fieldName.setText("");
        if (fieldLastName != null) fieldLastName.setText("");
        if (fieldEmail != null) fieldEmail.setText("");
        if (fieldIndigenousLanguage != null) fieldIndigenousLanguage.setText("");
        if (comboBoxGender != null) comboBoxGender.clearSelection();
        if (comboBoxPracticeGroup != null) comboBoxPracticeGroup.clearSelection();
    }
}
