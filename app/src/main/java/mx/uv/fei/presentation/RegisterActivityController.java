package mx.uv.fei.presentation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.manager.ProjectManager;
import mx.uv.fei.domain.statemachine.Store;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.presentation.components.FormField;
import mx.uv.fei.domain.common.CommonParse;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.CommonControler;

import java.net.URL;
import java.sql.Date;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;

public class RegisterActivityController implements Initializable {

    private final ProjectManager projectManager;
    private final Store store;

    public Button saveButton;
    public Button cancelButton;

    @FXML
    private FormField fieldActivityName;
    @FXML
    private FormComboBox comboBoxOrganization;
    @FXML
    private FormComboBox comboBoxManager;

    @FXML
    private TextField textFieldStartDay;
    @FXML
    private TextField textFieldStartMonth;
    @FXML
    private TextField textFieldStartYear;

    @FXML
    private TextField textFieldDeadlineDay;
    @FXML
    private TextField textFieldDeadlineMonth;
    @FXML
    private TextField textFieldDeadlineYear;

    @FXML
    private TextArea textAreaDescription;

    @FXML
    private Button buttonSave;
    @FXML
    private Button buttonCancel;

    @Inject
    public RegisterActivityController(ProjectManager projectManager, Store store) {

        this.projectManager = projectManager;
        this.store = store;
    }

    @Override
    public void initialize(URL locationUrl, ResourceBundle resourcesBundle) {

        ObservableList<String> organizationOptions = FXCollections.observableArrayList(
                "Organización A", "Organización B", "Organización C");
        comboBoxOrganization.setItems(organizationOptions);

        ObservableList<String> managerOptions = FXCollections.observableArrayList(
                "Juan Pérez", "Ana Gómez", "Luis Martínez");
        comboBoxManager.setItems(managerOptions);
    }

    @FXML
    private void handleActionSaveButton(ActionEvent actionEvent) {

        try {
            Activity activityInformation = new Activity();

            activityInformation.setName(fieldActivityName.getText());
            activityInformation.setDescription(textAreaDescription.getText());
            activityInformation.setManager(comboBoxManager.getValue());

            Date activityStartDate = CommonParse.parseDate(
                    textFieldStartDay.getText(),
                    textFieldStartMonth.getText(),
                    textFieldStartYear.getText());
            Date activityEndDate = CommonParse.parseDate(
                    textFieldDeadlineDay.getText(),
                    textFieldDeadlineMonth.getText(),
                    textFieldDeadlineYear.getText());

            activityInformation.setStartDate(activityStartDate);
            activityInformation.setEndDate(activityEndDate);

            boolean isActivitySavedSuccessfully = projectManager.registerNewActivity(activityInformation);

            if (isActivitySavedSuccessfully) {
                CommonControler.showInfoAlert("Registro Exitoso", "La actividad se ha guardado correctamente.");
            }

        } catch (IllegalArgumentException | DateTimeParseException dateValidationException) {
            CommonControler.showErrorAlert("Error de fecha",
                    "Por favor, introduzca fechas válidas en formato numérico (DD/MM/AAAA).");
        } catch (ManagerException managerException) {
            CommonControler.showErrorAlert("Fallo en el registro", managerException.getMessage());
        }
    }

    @FXML
    private void handleActionCancelButton(ActionEvent event) {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }

}