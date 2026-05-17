package mx.uv.fei.presentation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button; // <-- Importación necesaria
import javafx.stage.FileChooser;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.dto.BatchRegistrationSummary;
import mx.uv.fei.presentation.components.FormField;
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.domain.manager.PractitionerManager;
import mx.uv.fei.domain.statemachine.Store;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.common.Controller;

@Component
public class RegisterPractitionerController implements Initializable {

    @FXML private FormField fieldMatricula;
    @FXML private FormField fieldNombre;
    @FXML private FormField fieldApellido;
    @FXML private FormField fieldCorreo;
    @FXML private FormComboBox comboBoxSexo;
    @FXML private FormField fieldLengua;
    @FXML private Button cancelButton;
    @FXML private Button registerButton;
    @FXML private Button uploadCsvButton;

    private final PractitionerManager practitionerManager;
    private final Store applicationNavigationStore;

    @Inject
    public RegisterPractitionerController(PractitionerManager practitionerManager, Store applicationNavigationStore) {
        this.practitionerManager = practitionerManager;
        this.applicationNavigationStore = applicationNavigationStore;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ObservableList<String> opcionesSexo = FXCollections.observableArrayList("Masculino", "Femenino", "Otro");
        comboBoxSexo.setItems(opcionesSexo);
    }

    @FXML
    private void handleActionRegisterButton(ActionEvent event) {
        Practitioner newPractitioner = createPractitioner();
        String indigenousLanguage = fieldLengua.getText().isEmpty() ? "Ninguna" : fieldLengua.getText();
        newPractitioner.setIndigenousLanguage(indigenousLanguage);

        try {
            if (registerButton != null) registerButton.setDisable(true);

            String generatedPassword = practitionerManager.registerNewPractitioner(newPractitioner);
            Controller.showAlert("Registro Exitoso", "Practicante registrado.\nContraseña: " + generatedPassword, AlertType.INFORMATION);
            clearForm();
        } catch (ManagerException exception) {
            Controller.showAlert("Error en el Registro", exception.getMessage(), AlertType.ERROR);
        } finally {
            if (registerButton != null) registerButton.setDisable(false);
        }
    }

    @FXML
    private void handleActionUploadCSV(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));

        File selectedFile = fileChooser.showOpenDialog(fieldMatricula.getScene().getWindow());

        if (selectedFile != null) {
            try {
                User currentUser = applicationNavigationStore.getState().sessionState().currentUserInSession();
                String coordinatorName = currentUser.getName() + "_" + currentUser.getLastName();

                BatchRegistrationSummary summary = practitionerManager.registerPractitionerBatch(selectedFile, coordinatorName);
                String resultMessage = "Exitosos: " + summary.getSuccessfulRegistrations() + "\nFallidos: " + summary.getFailedRegistrations();

                Controller.showAlert("Resumen", resultMessage, AlertType.INFORMATION);
                applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));

            } catch (ManagerException exception) {
                Controller.showAlert("Error", exception.getMessage(), AlertType.ERROR);
            }
        }
    }

    private Practitioner createPractitioner() {
        Practitioner newPractitioner = new Practitioner();
        newPractitioner.setEnrollment(fieldMatricula.getText());
        newPractitioner.setName(fieldNombre.getText());
        newPractitioner.setLastName(fieldApellido.getText());
        newPractitioner.setEmail(fieldCorreo.getText());
        newPractitioner.setGender((String) comboBoxSexo.getValue());
        return newPractitioner;
    }

    @FXML
    private void handleActionCancelButton(ActionEvent event) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }

    private void clearForm() {
        if (fieldMatricula != null) fieldMatricula.setText("");
        if (fieldNombre != null) fieldNombre.setText("");
        if (fieldApellido != null) fieldApellido.setText("");
        if (fieldCorreo != null) fieldCorreo.setText("");
        if (fieldLengua != null) fieldLengua.setText("");
    }
}