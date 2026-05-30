package mx.uv.fei.presentation;

import java.net.URL;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;


import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.common.Parse;
import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.dto.Manager;
import mx.uv.fei.domain.dto.Organization;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.ActivityManager;
import mx.uv.fei.domain.manager.ManagerManager;
import mx.uv.fei.domain.manager.OrganizationManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.presentation.components.FormField;


@Component
public class RegisterActivityController implements Initializable {

    private static final String SUCCESS_TITLE = "Registro Exitoso";
    private static final String SUCCESS_MESSAGE = "La actividad se ha guardado correctamente.";
    private static final String DATE_ERROR_TITLE = "Error de fecha";
    private static final String DATE_ERROR_MESSAGE = "Por favor, introduzca fechas válidas en formato numérico (DD/MM/AAAA).";
    private static final String SAVE_ERROR_TITLE = "Fallo en el registro";
    private static final String LOAD_ERROR_TITLE = "Error de Carga";

    private final ActivityManager activityManager;
    private final OrganizationManager organizationManager;
    private final ManagerManager managerManager;
    private final AppStore store;

    private final Map<String, Integer> organizationMap = new HashMap<>();

    @FXML private FormField fieldActivityName;
    @FXML private FormComboBox comboBoxOrganization;
    @FXML private FormComboBox comboBoxManager;
    @FXML private TextField textFieldStartDay;
    @FXML private TextField textFieldStartMonth;
    @FXML private TextField textFieldStartYear;
    @FXML private TextField textFieldDeadlineDay;
    @FXML private TextField textFieldDeadlineMonth;
    @FXML private TextField textFieldDeadlineYear;
    @FXML private TextArea textAreaDescription;

    @Inject
    public RegisterActivityController(ActivityManager activityManager,
                                      OrganizationManager organizationManager,
                                      ManagerManager managerManager,
                                      AppStore store) {
        this.activityManager = activityManager;
        this.organizationManager = organizationManager;
        this.managerManager = managerManager;
        this.store = store;
    }

    @Override
    public void initialize(URL locationUrl, ResourceBundle resourcesBundle) {
        loadOrganizations();

        comboBoxOrganization.valueProperty().addListener((_, _, newValue) -> {
            if (newValue != null) {
                loadManagersByOrganization(organizationMap.get(newValue));
            }
        });
    }

    private void loadOrganizations() {
        try {
            List<Organization> organizations = organizationManager.getAllOrganizations();
            ObservableList<String> options = FXCollections.observableArrayList();

            for (Organization org : organizations) {
                options.add(org.getNameOrganization());
                organizationMap.put(org.getNameOrganization(), org.getIdOrganization());
            }
            comboBoxOrganization.setItems(options);
        } catch (ManagerException exception) {
            Controller.showErrorAlert(LOAD_ERROR_TITLE, exception.getMessage());
        }
    }

    private void loadManagersByOrganization(int orgId) {
        try {
            List<Manager> managers = managerManager.getManagersByOrganization(orgId);
            ObservableList<String> managerOptions = FXCollections.observableArrayList();

            for (Manager mgr : managers) {
                managerOptions.add(mgr.getName());
            }
            comboBoxManager.setItems(managerOptions);
            comboBoxManager.setDisable(managerOptions.isEmpty());
        } catch (ManagerException exception) {
            Controller.showErrorAlert(LOAD_ERROR_TITLE, exception.getMessage());
        }
    }

    @FXML
    private void handleActionSaveButton() {
        try {
            Activity activity = createActivity();

            activityManager.registerNewActivity(activity);
            Controller.showInfoAlert(SUCCESS_TITLE, SUCCESS_MESSAGE);
            store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));

        } catch (IllegalArgumentException | DateTimeParseException _) {
            Controller.showErrorAlert(DATE_ERROR_TITLE, DATE_ERROR_MESSAGE);
        } catch (ManagerException exception) {
            Controller.showErrorAlert(SAVE_ERROR_TITLE, exception.getMessage());
        }
    }

    private Activity createActivity() {
        Activity activity = new Activity();
        activity.setName(fieldActivityName.getText());
        activity.setDescription(textAreaDescription.getText());
        activity.setManager((String) comboBoxManager.getValue());
        activity.setStartDate(Parse.parseDate(textFieldStartDay.getText(), textFieldStartMonth.getText(), textFieldStartYear.getText()));
        activity.setEndDate(Parse.parseDate(textFieldDeadlineDay.getText(), textFieldDeadlineMonth.getText(), textFieldDeadlineYear.getText()));
        return activity;
    }

    @FXML
    private void handleActionCancelButton() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}