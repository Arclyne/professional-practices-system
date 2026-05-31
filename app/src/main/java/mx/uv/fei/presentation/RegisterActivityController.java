package mx.uv.fei.presentation;

import java.net.URL;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.common.Parse;
import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.dto.PracticeGroup;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.ActivityManager;
import mx.uv.fei.domain.manager.PracticeGroupManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.presentation.components.FormField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class RegisterActivityController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(RegisterActivityController.class);

    private static final String TITLE_SUCCESS = "Registro Exitoso";
    private static final String MSG_SUCCESS = "La actividad se ha guardado correctamente.";
    private static final String TITLE_DATE_ERROR = "Error de Fecha";
    private static final String MSG_DATE_ERROR = "Por favor, introduzca fechas válidas en formato numérico (DD/MM/AAAA).";
    private static final String TITLE_SAVE_ERROR = "Fallo en el Registro";

    private static final String TITLE_GROUP_ERROR = "Sin Grupo Asignado";
    private static final String MSG_GROUP_ERROR = "No tiene un grupo de prácticas asignado para este periodo. No puede crear actividades.";

    private static final String TITLE_VALIDATION_ERROR = "Campos Inválidos";
    private static final String MSG_EMPTY_FIELDS = "Por favor, llene todos los campos obligatorios.";

    private static final String TITLE_DB_ERROR = "Error de Base de Datos";
    private static final String MSG_DB_ERROR = "No se pudo verificar su grupo asignado.";

    private final ActivityManager activityManager;
    private final PracticeGroupManager practiceGroupManager;
    private final AppStore store;

    private int assignedGroupId = -1;

    @FXML private FormField fieldActivityName;
    @FXML private TextField textFieldStartDay;
    @FXML private TextField textFieldStartMonth;
    @FXML private TextField textFieldStartYear;
    @FXML private TextField textFieldDeadlineDay;
    @FXML private TextField textFieldDeadlineMonth;
    @FXML private TextField textFieldDeadlineYear;
    @FXML private TextArea textAreaDescription;
    @FXML private Button saveButton;

    @Inject
    public RegisterActivityController(ActivityManager activityManager,
                                      PracticeGroupManager practiceGroupManager,
                                      AppStore store) {
        this.activityManager = activityManager;
        this.practiceGroupManager = practiceGroupManager;
        this.store = store;
    }

    @Override
    public void initialize(URL locationUrl, ResourceBundle resourcesBundle) {
        loadProfessorGroup();
    }

    private void loadProfessorGroup() {
        try {
            User currentUser = store.getState().sessionState().currentUserInSession();
            int currentProfessorId = currentUser != null ? currentUser.getId() : 0;

            List<PracticeGroup> groupsList = practiceGroupManager.getAllPracticeGroups();

            for (PracticeGroup group : groupsList) {
                if (group.getProfessorId() == currentProfessorId) {
                    assignedGroupId = group.getGroupId();
                    break;
                }
            }

            if (assignedGroupId == -1) {
                saveButton.setDisable(true);
                Controller.showAlert(TITLE_GROUP_ERROR, MSG_GROUP_ERROR, AlertType.WARNING);
            }

        } catch (ManagerException exception) {
            log.error("Error verifying professor's group", exception);
            saveButton.setDisable(true);
            Controller.showAlert(TITLE_DB_ERROR, MSG_DB_ERROR, AlertType.ERROR);
        }
    }

    @FXML
    private void handleActionSaveButton(ActionEvent event) {
        if (validateFields()) {
            try {
                Activity newActivity = createActivity();

                activityManager.registerNewActivity(newActivity);

                Controller.showAlert(TITLE_SUCCESS, MSG_SUCCESS, AlertType.INFORMATION);
                store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));

            } catch (IllegalArgumentException | DateTimeParseException dateException) {
                Controller.showAlert(TITLE_DATE_ERROR, MSG_DATE_ERROR, AlertType.WARNING);
            } catch (ManagerException exception) {
                Controller.showAlert(TITLE_SAVE_ERROR, exception.getMessage(), AlertType.ERROR);
            }
        }
    }

    private boolean validateFields() {
        boolean isValid = true;

        if (fieldActivityName.getText().trim().isEmpty() ||
                textAreaDescription.getText().trim().isEmpty() ||
                textFieldStartDay.getText().trim().isEmpty() ||
                textFieldDeadlineDay.getText().trim().isEmpty()) {

            Controller.showAlert(TITLE_VALIDATION_ERROR, MSG_EMPTY_FIELDS, AlertType.WARNING);
            isValid = false;
        } else if (assignedGroupId == -1) {
            Controller.showAlert(TITLE_GROUP_ERROR, MSG_GROUP_ERROR, AlertType.WARNING);
            isValid = false;
        }

        return isValid;
    }

    private Activity createActivity() {
        Activity activity = new Activity();
        activity.setName(fieldActivityName.getText().trim());
        activity.setDescription(textAreaDescription.getText().trim());

        activity.setGroupId(assignedGroupId);

        activity.setStartDate(Parse.parseDate(textFieldStartDay.getText().trim(), textFieldStartMonth.getText().trim(), textFieldStartYear.getText().trim()));
        activity.setEndDate(Parse.parseDate(textFieldDeadlineDay.getText().trim(), textFieldDeadlineMonth.getText().trim(), textFieldDeadlineYear.getText().trim()));

        return activity;
    }

    @FXML
    private void handleActionCancelButton(ActionEvent event) {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}