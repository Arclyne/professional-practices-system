package mx.uv.fei.presentation.professor;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.GradingEligibility;
import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.dto.PracticeGroup;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.dto.PractitionerGrade;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.evaluation.GradingEligibilityManager;
import mx.uv.fei.domain.manager.evaluation.GradingManager;
import mx.uv.fei.domain.manager.academic.PeriodManager;
import mx.uv.fei.domain.manager.academic.PracticeGroupManager;
import mx.uv.fei.domain.manager.people.PractitionerManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

@Component
public class GradePractitionerController implements Initializable {

    private static final String NO_ACTIVE_PERIOD_MESSAGE =
            "No hay un periodo académico activo. Solicita al coordinador que active uno.";
    private static final String ALL_GROUPS_OPTION = "Todos mis grupos";
    private static final String GROUP_LABEL_PREFIX = "NRC ";
    private static final String STATUS_FILTER_ALL = "Todos los estados";
    private static final String STATUS_FILTER_READY = "Listos para calificar";
    private static final String STATUS_FILTER_MISSING_REQUIREMENTS = "Faltan requisitos";
    private static final String STATUS_FILTER_GRADED = "Ya calificados";

    @FXML private ComboBox<String> groupFilterComboBox;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private TextField searchTextField;
    @FXML private ListView<Practitioner> practitionersListView;
    @FXML private VBox gradeContainer;
    @FXML private Label labelPractitionerName;
    @FXML private Label labelTentativeGrade;
    @FXML private TextField fieldFinalGrade;
    @FXML private Button saveButton;
    @FXML private Button editButton;
    @FXML private Button updateButton;
    @FXML private Label labelAlreadyGraded;
    @FXML private Label labelGradingRequirements;
    @FXML private Label labelNoPractitioners;

    private final GradingManager gradingManager;
    private final GradingEligibilityManager eligibilityManager;
    private final PractitionerManager practitionerManager;
    private final PracticeGroupManager practiceGroupManager;
    private final PeriodManager periodManager;
    private final AppStore store;

    private final Map<String, Integer> groupLabelToId = new HashMap<>();
    private final Map<Integer, String> gradingBucketByPractitionerId = new HashMap<>();
    private final List<Practitioner> loadedPractitioners = new ArrayList<>();

    private Practitioner selectedPractitioner;
    private boolean selectedPractitionerEligible;
    private int professorId;
    private String activePeriod;
    private int activePeriodId;

    @Inject
    public GradePractitionerController(GradingManager gradingManager, GradingEligibilityManager eligibilityManager,
                                       PractitionerManager practitionerManager, PracticeGroupManager practiceGroupManager,
                                       PeriodManager periodManager, AppStore store) {
        this.gradingManager = gradingManager;
        this.eligibilityManager = eligibilityManager;
        this.practitionerManager = practitionerManager;
        this.practiceGroupManager = practiceGroupManager;
        this.periodManager = periodManager;
        this.store = store;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User currentUser = store.getState().sessionState().currentUserInSession();
        professorId = currentUser != null ? currentUser.getId() : 0;

        gradeContainer.setVisible(false);
        gradeContainer.setManaged(false);
        labelNoPractitioners.setVisible(false);
        labelNoPractitioners.setManaged(false);

        resolveActivePeriod();
        if (activePeriod != null) {
            configurePractitionerList();
            loadStatusFilter();
            loadProfessorGroups();
        }
    }

    private void loadStatusFilter() {
        statusFilterComboBox.setItems(FXCollections.observableArrayList(
                STATUS_FILTER_ALL, STATUS_FILTER_READY, STATUS_FILTER_MISSING_REQUIREMENTS, STATUS_FILTER_GRADED));
        statusFilterComboBox.setValue(STATUS_FILTER_ALL);
        statusFilterComboBox.valueProperty().addListener((_, _, _) -> applyPractitionerFilters());
    }

    @FXML
    private void handleSearchAction() {
        applyPractitionerFilters();
    }

    private void resolveActivePeriod() {
        try {
            Period period = periodManager.getActivePeriod();
            activePeriod = period != null ? period.getPeriodName() : null;
            activePeriodId = period != null ? period.getPeriodId() : 0;
            if (activePeriod == null) {
                labelNoPractitioners.setVisible(true);
                labelNoPractitioners.setManaged(true);
                labelNoPractitioners.setText(NO_ACTIVE_PERIOD_MESSAGE);
            }
        } catch (ManagerException e) {
            Controller.showAlert("Error de Carga", e.getMessage(), AlertType.ERROR);
        }
    }

    private void configurePractitionerList() {
        practitionersListView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Practitioner practitioner, boolean isEmpty) {
                super.updateItem(practitioner, isEmpty);
                if (isEmpty || practitioner == null) {
                    setText(null);
                } else {
                    setText(practitioner.getName() + " " + practitioner.getLastName()
                            + "  |  " + practitioner.getEnrollment());
                }
            }
        });

        practitionersListView.getSelectionModel().selectedItemProperty()
                .addListener((_, _, selectedPractitioner) -> {
                    if (selectedPractitioner != null) {
                        loadGradePanelForPractitioner(selectedPractitioner);
                    }
                });

        groupFilterComboBox.valueProperty().addListener((_, _, _) -> reloadPractitioners());
    }

    private void loadProfessorGroups() {
        try {
            List<PracticeGroup> professorGroups = filterProfessorGroupsForActivePeriod(
                    practiceGroupManager.getAllPracticeGroups());
            ObservableList<String> groupOptions = FXCollections.observableArrayList(ALL_GROUPS_OPTION);

            for (PracticeGroup professorGroup : professorGroups) {
                String groupLabel = GROUP_LABEL_PREFIX + professorGroup.getSection();
                groupOptions.add(groupLabel);
                groupLabelToId.put(groupLabel, professorGroup.getGroupId());
            }

            groupFilterComboBox.setItems(groupOptions);
            groupFilterComboBox.setValue(ALL_GROUPS_OPTION);
        } catch (ManagerException e) {
            Controller.showAlert("Error de Carga", e.getMessage(), AlertType.ERROR);
        }
    }

    private List<PracticeGroup> filterProfessorGroupsForActivePeriod(List<PracticeGroup> allGroups) {
        List<PracticeGroup> professorGroups = new ArrayList<>();

        for (PracticeGroup group : allGroups) {
            if (group.getProfessorId() == professorId && group.getPeriodId() == activePeriodId) {
                professorGroups.add(group);
            }
        }

        return professorGroups;
    }

    private void reloadPractitioners() {
        try {
            loadedPractitioners.clear();
            loadedPractitioners.addAll(retrievePractitionersForSelectedFilter());
            loadGradingStatuses(loadedPractitioners);
            applyPractitionerFilters();
        } catch (ManagerException e) {
            Controller.showAlert("Error de Carga", e.getMessage(), AlertType.ERROR);
        }
    }

    private void loadGradingStatuses(List<Practitioner> practitioners) {
        gradingBucketByPractitionerId.clear();
        for (Practitioner practitioner : practitioners) {
            gradingBucketByPractitionerId.put(practitioner.getId(), resolveGradingBucket(practitioner.getId()));
        }
    }

    private String resolveGradingBucket(int practitionerId) {
        String bucket;

        try {
            if (gradingManager.getGradeByPractitionerAndPeriod(practitionerId, activePeriod) != null) {
                bucket = STATUS_FILTER_GRADED;
            } else if (eligibilityManager.evaluateEligibility(practitionerId).isEligible()) {
                bucket = STATUS_FILTER_READY;
            } else {
                bucket = STATUS_FILTER_MISSING_REQUIREMENTS;
            }
        } catch (ManagerException e) {
            bucket = STATUS_FILTER_MISSING_REQUIREMENTS;
        }

        return bucket;
    }

    private void applyPractitionerFilters() {
        List<Practitioner> visiblePractitioners = new ArrayList<>();
        for (Practitioner practitioner : loadedPractitioners) {
            if (matchesStatusFilter(practitioner) && matchesEnrollmentSearch(practitioner)) {
                visiblePractitioners.add(practitioner);
            }
        }
        updatePractitionerList(visiblePractitioners);
    }

    private boolean matchesStatusFilter(Practitioner practitioner) {
        String selectedStatus = statusFilterComboBox.getValue();
        String bucket = gradingBucketByPractitionerId.getOrDefault(
                practitioner.getId(), STATUS_FILTER_MISSING_REQUIREMENTS);
        return selectedStatus == null || STATUS_FILTER_ALL.equals(selectedStatus) || selectedStatus.equals(bucket);
    }

    private boolean matchesEnrollmentSearch(Practitioner practitioner) {
        String query = searchTextField.getText() == null ? "" : searchTextField.getText().trim().toLowerCase();
        String enrollment = practitioner.getEnrollment() == null ? "" : practitioner.getEnrollment().toLowerCase();
        return query.isEmpty() || enrollment.contains(query);
    }

    private List<Practitioner> retrievePractitionersForSelectedFilter() throws ManagerException {
        String selectedGroup = groupFilterComboBox.getValue();
        List<Practitioner> practitioners;

        if (selectedGroup == null || ALL_GROUPS_OPTION.equals(selectedGroup)) {
            practitioners = retrievePractitionersFromAllGroups();
        } else {
            practitioners = practitionerManager.retrieveEnrolledPractitionersByGroup(groupLabelToId.get(selectedGroup));
        }

        return practitioners;
    }

    private List<Practitioner> retrievePractitionersFromAllGroups() throws ManagerException {
        List<Practitioner> practitioners = new ArrayList<>();

        for (int groupId : groupLabelToId.values()) {
            practitioners.addAll(practitionerManager.retrieveEnrolledPractitionersByGroup(groupId));
        }

        return practitioners;
    }

    private void updatePractitionerList(List<Practitioner> practitioners) {
        boolean hasPractitioners = !practitioners.isEmpty();

        practitionersListView.setItems(FXCollections.observableArrayList(practitioners));
        labelNoPractitioners.setText("No hay practicantes asignados para el filtro seleccionado.");
        labelNoPractitioners.setVisible(!hasPractitioners);
        labelNoPractitioners.setManaged(!hasPractitioners);
    }

    private void loadGradePanelForPractitioner(Practitioner practitioner) {
        selectedPractitioner = practitioner;
        gradeContainer.setVisible(true);
        gradeContainer.setManaged(true);
        labelPractitionerName.setText(practitioner.getName() + " " + practitioner.getLastName());

        loadTentativeGrade(practitioner.getId());
        PractitionerGrade existingGrade = recoverExistingGrade(practitioner.getId());
        selectedPractitionerEligible = resolveEligibility(practitioner.getId());
        applyGradeFormState(existingGrade);
    }

    private void loadTentativeGrade(int practitionerId) {
        try {
            double tentativeGrade = gradingManager.previewTentativeGrade(practitionerId);
            labelTentativeGrade.setText(
                    String.format("Calificación tentativa del sistema: %.2f / 10.0", tentativeGrade));
        } catch (ManagerException e) {
            labelTentativeGrade.setText("No se pudo calcular la calificación tentativa.");
        }
    }

    private PractitionerGrade recoverExistingGrade(int practitionerId) {
        PractitionerGrade existingGrade = null;
        try {
            existingGrade = gradingManager.getGradeByPractitionerAndPeriod(practitionerId, activePeriod);
        } catch (ManagerException e) {
            Controller.showAlert("Error", e.getMessage(), AlertType.ERROR);
        }
        return existingGrade;
    }

    private boolean resolveEligibility(int practitionerId) {
        boolean isEligible;
        try {
            GradingEligibility eligibility = eligibilityManager.evaluateEligibility(practitionerId);
            isEligible = eligibility.isEligible();
            labelGradingRequirements.setText(
                    isEligible ? "" : eligibilityManager.buildPendingRequirementsMessage(eligibility));
            setNodeVisible(labelGradingRequirements, !isEligible);
        } catch (ManagerException e) {
            isEligible = false;
            Controller.showAlert("Error de verificación", e.getMessage(), AlertType.ERROR);
        }
        return isEligible;
    }

    private void applyGradeFormState(PractitionerGrade existingGrade) {
        boolean isGraded = existingGrade != null;
        setNodeVisible(labelAlreadyGraded, isGraded);

        if (isGraded) {
            fieldFinalGrade.setText(String.valueOf(existingGrade.getFinalGrade()));
            fieldFinalGrade.setDisable(true);
        } else {
            fieldFinalGrade.clear();
            fieldFinalGrade.setDisable(!selectedPractitionerEligible);
        }

        setNodeVisible(saveButton, !isGraded && selectedPractitionerEligible);
        setNodeVisible(editButton, isGraded);
        setNodeVisible(updateButton, false);
    }

    @FXML
    private void handleEditGrade() {
        fieldFinalGrade.setDisable(false);
        setNodeVisible(saveButton, false);
        setNodeVisible(editButton, false);
        setNodeVisible(updateButton, true);
    }

    private void setNodeVisible(Node node, boolean isVisible) {
        node.setVisible(isVisible);
        node.setManaged(isVisible);
    }

    @FXML
    private void handleSaveGrade() {
        if (canRegisterGrade()) {
            registerGradeFromForm();
        }
    }

    private boolean canRegisterGrade() {
        boolean canRegister = true;

        if (selectedPractitioner == null) {
            Controller.showAlert("Selección requerida",
                    "Selecciona un practicante de la lista para calificarlo.", AlertType.WARNING);
            canRegister = false;
        } else if (!selectedPractitionerEligible) {
            Controller.showAlert("Requisitos pendientes",
                    "El practicante aún no cumple los requisitos para ser calificado.", AlertType.WARNING);
            canRegister = false;
        } else if (fieldFinalGrade.getText().trim().isEmpty()) {
            Controller.showAlert("Campo requerido",
                    "Ingresa la calificación antes de continuar.", AlertType.WARNING);
            canRegister = false;
        }

        return canRegister;
    }

    private void registerGradeFromForm() {
        try {
            double finalGrade = Double.parseDouble(fieldFinalGrade.getText().trim());
            gradingManager.registerGrade(selectedPractitioner.getId(), professorId, activePeriod, finalGrade);
            Controller.showAlert("Calificación Guardada",
                    "Se registró la calificación de " + selectedPractitioner.getName()
                            + " " + selectedPractitioner.getLastName() + " como " + finalGrade + ".",
                    AlertType.INFORMATION);
            loadGradePanelForPractitioner(selectedPractitioner);
        } catch (NumberFormatException e) {
            Controller.showAlert("Formato inválido",
                    "La calificación debe ser un número decimal (ej. 9.5).", AlertType.WARNING);
        } catch (ManagerException e) {
            Controller.showAlert("Error al Guardar", e.getMessage(), AlertType.WARNING);
        }
    }

    @FXML
    private void handleUpdateGrade() {
        if (canUpdateGrade()) {
            updateGradeFromForm();
        }
    }

    private boolean canUpdateGrade() {
        boolean canUpdate = true;

        if (selectedPractitioner == null) {
            Controller.showAlert("Selección requerida",
                    "Selecciona un practicante de la lista para calificarlo.", AlertType.WARNING);
            canUpdate = false;
        } else if (fieldFinalGrade.getText().trim().isEmpty()) {
            Controller.showAlert("Campo requerido",
                    "Ingresa la calificación antes de continuar.", AlertType.WARNING);
            canUpdate = false;
        }

        return canUpdate;
    }

    private void updateGradeFromForm() {
        try {
            double newGrade = Double.parseDouble(fieldFinalGrade.getText().trim());
            gradingManager.updateFinalGrade(selectedPractitioner.getId(), activePeriod, newGrade);
            Controller.showAlert("Calificación Actualizada",
                    "La calificación de " + selectedPractitioner.getName()
                            + " " + selectedPractitioner.getLastName() + " fue actualizada a " + newGrade + ".",
                    AlertType.INFORMATION);
            loadGradePanelForPractitioner(selectedPractitioner);
        } catch (NumberFormatException e) {
            Controller.showAlert("Formato inválido",
                    "La calificación debe ser un número decimal (ej. 9.5).", AlertType.WARNING);
        } catch (ManagerException e) {
            Controller.showAlert("Error al Actualizar", e.getMessage(), AlertType.WARNING);
        }
    }

    @FXML
    private void handleReturnAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}