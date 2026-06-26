package mx.uv.fei.presentation.professor;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.dto.PracticeGroup;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.dto.ProgressReport;
import mx.uv.fei.domain.dto.SelfEvaluation;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.enums.SelfEvaluationStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.academic.PeriodManager;
import mx.uv.fei.domain.manager.academic.PracticeGroupManager;
import mx.uv.fei.domain.manager.people.PractitionerManager;
import mx.uv.fei.domain.manager.reporting.ProgressReportManager;
import mx.uv.fei.domain.manager.evaluation.SelfEvaluationManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ReviewSelfEvaluationController {

    private static final String REPORT_TYPE_FINAL = "Final";
    private static final String STATUS_NOT_DELIVERED = "No entregada";
    private static final String STATUS_REVIEWED = SelfEvaluationStatus.REVIEWED.getDatabaseValue();
    private static final String STATUS_PENDING_EVIDENCE = "pendiente";
    private static final String ALL_GROUPS_OPTION = "Todos mis grupos";
    private static final String GROUP_LABEL_PREFIX = "NRC ";
    private static final String STATUS_FILTER_ALL = "Todas";
    private static final String STATUS_FILTER_PENDING = "Por revisar";
    private static final String STATUS_FILTER_REVIEWED = "Revisadas";
    private static final String STATUS_FILTER_REJECTED = "Rechazadas";
    private static final String STATUS_FILTER_NOT_DELIVERED = "No entregadas";

    @FXML private ComboBox<String> groupFilterComboBox;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private TextField searchTextField;
    @FXML private TableView<SelfEvaluationRow> evaluationsTableView;
    @FXML private TableColumn<SelfEvaluationRow, String> studentTableColumn;
    @FXML private TableColumn<SelfEvaluationRow, String> groupTableColumn;
    @FXML private TableColumn<SelfEvaluationRow, String> statusTableColumn;

    @FXML private Label studentNameLabel;
    @FXML private Label statusLabel;

    @FXML private TextField q1TextField;
    @FXML private TextField q2TextField;
    @FXML private TextField q3TextField;
    @FXML private TextField q4TextField;
    @FXML private TextField q5TextField;
    @FXML private TextField q6TextField;
    @FXML private TextField q7TextField;
    @FXML private TextField q8TextField;
    @FXML private TextField q9TextField;
    @FXML private TextField q10TextField;

    @FXML private TextField rejectReasonTextField;

    @FXML private Button viewEvidenceButton;
    @FXML private Button downloadEvidenceButton;
    @FXML private Button approveButton;
    @FXML private Button rejectButton;

    private final SelfEvaluationManager selfEvaluationManager;
    private final ProgressReportManager progressReportManager;
    private final PractitionerManager practitionerManager;
    private final PracticeGroupManager practiceGroupManager;
    private final PeriodManager periodManager;
    private final AppStore appStore;

    private final Map<String, PracticeGroup> groupByLabel = new HashMap<>();
    private final List<SelfEvaluationRow> allEvaluationRows = new ArrayList<>();

    private SelfEvaluationRow selectedRow;
    private int professorId;
    private int activePeriodId;

    @Inject
    public ReviewSelfEvaluationController(SelfEvaluationManager selfEvaluationManager,
                                          ProgressReportManager progressReportManager,
                                          PractitionerManager practitionerManager,
                                          PracticeGroupManager practiceGroupManager,
                                          PeriodManager periodManager,
                                          AppStore appStore) {
        this.selfEvaluationManager = selfEvaluationManager;
        this.progressReportManager = progressReportManager;
        this.practitionerManager = practitionerManager;
        this.practiceGroupManager = practiceGroupManager;
        this.periodManager = periodManager;
        this.appStore = appStore;
    }

    @FXML
    public void initialize() {
        studentTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStudentName()));
        groupTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGroupCode()));
        statusTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));

        evaluationsTableView.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> showEvaluationDetails(newValue));

        clearDetails();
        resolveProfessorContext();
        loadProfessorGroups();
        loadStatusFilter();
        loadStudentsAndEvaluations();
    }

    private void loadStatusFilter() {
        statusFilterComboBox.setItems(FXCollections.observableArrayList(
                STATUS_FILTER_ALL, STATUS_FILTER_PENDING, STATUS_FILTER_REVIEWED,
                STATUS_FILTER_REJECTED, STATUS_FILTER_NOT_DELIVERED));
        statusFilterComboBox.setValue(STATUS_FILTER_ALL);
        statusFilterComboBox.valueProperty().addListener((_, _, _) -> applyStatusFilter());
    }

    private void resolveProfessorContext() {
        User currentUser = appStore.getState().sessionState().currentUserInSession();
        professorId = currentUser != null ? currentUser.getId() : 0;

        try {
            Period activePeriod = periodManager.getActivePeriod();
            activePeriodId = activePeriod != null ? activePeriod.getPeriodId() : 0;
        } catch (ManagerException e) {
            activePeriodId = 0;
            Controller.showAlert("Error", e.getMessage(), AlertType.ERROR);
        }
    }

    private void loadProfessorGroups() {
        ObservableList<String> groupOptions = FXCollections.observableArrayList(ALL_GROUPS_OPTION);

        try {
            List<PracticeGroup> groups = practiceGroupManager.getGroupsByProfessorAndPeriod(professorId, activePeriodId);
            for (PracticeGroup group : groups) {
                String groupLabel = GROUP_LABEL_PREFIX + group.getSection();
                groupOptions.add(groupLabel);
                groupByLabel.put(groupLabel, group);
            }
        } catch (ManagerException e) {
            Controller.showAlert("Error", e.getMessage(), AlertType.ERROR);
        }

        groupFilterComboBox.setItems(groupOptions);
        groupFilterComboBox.setValue(ALL_GROUPS_OPTION);
        groupFilterComboBox.valueProperty().addListener((_, _, _) -> loadStudentsAndEvaluations());
    }

    private void loadStudentsAndEvaluations() {
        try {
            allEvaluationRows.clear();
            for (PracticeGroup group : groupsForSelectedFilter()) {
                appendRowsForGroup(group, allEvaluationRows);
            }
            applyStatusFilter();
        } catch (ManagerException e) {
            Controller.showAlert("Error", "No se pudieron cargar las autoevaluaciones: " + e.getMessage(), AlertType.ERROR);
        }
    }

    private void applyStatusFilter() {
        String selectedStatus = statusFilterComboBox.getValue();
        List<SelfEvaluationRow> visibleRows = new ArrayList<>();
        for (SelfEvaluationRow row : allEvaluationRows) {
            if (matchesStatusFilter(row, selectedStatus) && matchesEnrollmentSearch(row)) {
                visibleRows.add(row);
            }
        }
        evaluationsTableView.setItems(FXCollections.observableArrayList(visibleRows));
    }

    private boolean matchesStatusFilter(SelfEvaluationRow row, String selectedStatus) {
        boolean isMatch = selectedStatus == null || STATUS_FILTER_ALL.equals(selectedStatus);

        if (!isMatch) {
            isMatch = expectedStatusForFilter(selectedStatus).equals(row.getStatus());
        }

        return isMatch;
    }

    private String expectedStatusForFilter(String filterOption) {
        String expectedStatus;
        if (STATUS_FILTER_PENDING.equals(filterOption)) {
            expectedStatus = SelfEvaluationStatus.PENDING.getDatabaseValue();
        } else if (STATUS_FILTER_REVIEWED.equals(filterOption)) {
            expectedStatus = SelfEvaluationStatus.REVIEWED.getDatabaseValue();
        } else if (STATUS_FILTER_REJECTED.equals(filterOption)) {
            expectedStatus = SelfEvaluationStatus.REJECTED.getDatabaseValue();
        } else {
            expectedStatus = STATUS_NOT_DELIVERED;
        }
        return expectedStatus;
    }

    private boolean matchesEnrollmentSearch(SelfEvaluationRow row) {
        String query = searchTextField.getText() == null ? "" : searchTextField.getText().trim().toLowerCase();
        String enrollment = row.getEnrollment() == null ? "" : row.getEnrollment().toLowerCase();
        return query.isEmpty() || enrollment.contains(query);
    }

    @FXML
    private void handleSearchAction() {
        applyStatusFilter();
    }

    private List<PracticeGroup> groupsForSelectedFilter() {
        String selectedGroup = groupFilterComboBox.getValue();
        List<PracticeGroup> groups;

        if (selectedGroup == null || ALL_GROUPS_OPTION.equals(selectedGroup)) {
            groups = new ArrayList<>(groupByLabel.values());
        } else {
            groups = List.of(groupByLabel.get(selectedGroup));
        }

        return groups;
    }

    private void appendRowsForGroup(PracticeGroup group, List<SelfEvaluationRow> rows) throws ManagerException {
        List<Practitioner> students = practitionerManager.retrieveEnrolledPractitionersByGroup(group.getGroupId());
        for (Practitioner student : students) {
            rows.add(createSelfEvaluationRow(student, group.getSection()));
        }
    }

    private SelfEvaluationRow createSelfEvaluationRow(Practitioner student, String groupSection) throws ManagerException {
        List<ProgressReport> reports = progressReportManager.getProgressReportsByPractitioner(student.getId());
        ProgressReport finalReport = reports.stream()
                .filter(r -> REPORT_TYPE_FINAL.equals(r.getReportType()))
                .findFirst()
                .orElse(null);

        String status = STATUS_NOT_DELIVERED;
        SelfEvaluation evaluation = null;

        if (finalReport != null) {
            evaluation = selfEvaluationManager.recoverSelfEvaluation(finalReport.getReportId());
            if (evaluation != null) {
                status = evaluation.getStatus();
            }
        }

        String fullName = student.getName() + " " + student.getLastName();
        return new SelfEvaluationRow(fullName, groupSection, student.getEnrollment(), status, student, evaluation);
    }

    private void showEvaluationDetails(SelfEvaluationRow row) {
        this.selectedRow = row;
        if (row == null || row.getSelfEvaluation() == null) {
            clearDetails();
            if (row != null) {
                studentNameLabel.setText("Alumno: " + row.getStudentName());
                statusLabel.setText("Estado: " + row.getStatus());
            }
        } else {
            SelfEvaluation eval = row.getSelfEvaluation();
            studentNameLabel.setText("Alumno: " + row.getStudentName());
            statusLabel.setText("Estado: " + eval.getStatus());

            q1TextField.setText(String.valueOf(eval.getQ1()));
            q2TextField.setText(String.valueOf(eval.getQ2()));
            q3TextField.setText(String.valueOf(eval.getQ3()));
            q4TextField.setText(String.valueOf(eval.getQ4()));
            q5TextField.setText(String.valueOf(eval.getQ5()));
            q6TextField.setText(String.valueOf(eval.getQ6()));
            q7TextField.setText(String.valueOf(eval.getQ7()));
            q8TextField.setText(String.valueOf(eval.getQ8()));
            q9TextField.setText(String.valueOf(eval.getQ9()));
            q10TextField.setText(String.valueOf(eval.getQ10()));

            boolean hasEvidence = eval.getEvidence() != null
                    && !eval.getEvidence().isEmpty()
                    && !STATUS_PENDING_EVIDENCE.equals(eval.getEvidence());

            boolean isReviewed = STATUS_REVIEWED.equals(eval.getStatus());
            viewEvidenceButton.setDisable(!hasEvidence);
            downloadEvidenceButton.setDisable(!hasEvidence);
            approveButton.setDisable(isReviewed);
            rejectButton.setDisable(isReviewed);
            rejectReasonTextField.setDisable(isReviewed);
        }
    }

    private void clearDetails() {
        studentNameLabel.setText("Alumno: Seleccione un alumno");
        statusLabel.setText("Estado: -");
        TextField[] fields = {q1TextField, q2TextField, q3TextField, q4TextField, q5TextField, q6TextField, q7TextField, q8TextField, q9TextField, q10TextField};
        for (TextField tf : fields) {
            tf.clear();
            tf.setEditable(false);
        }
        viewEvidenceButton.setDisable(true);
        downloadEvidenceButton.setDisable(true);
        approveButton.setDisable(true);
        rejectButton.setDisable(true);
        rejectReasonTextField.clear();
    }

    @FXML
    private void handleViewEvidence() {
        if (selectedRow != null && selectedRow.getSelfEvaluation() != null) {
            String evidencePath = selectedRow.getSelfEvaluation().getEvidence();
            if (evidencePath != null && !evidencePath.isEmpty()) {
                try {
                    java.net.URI uri = new java.io.File(evidencePath).toURI();
                    java.awt.Desktop.getDesktop().browse(uri);
                } catch (IOException e) {
                    Controller.showAlert("Error de Archivo",
                            "No se pudo abrir la evidencia del alumno: " + e.getMessage(),
                            AlertType.ERROR);
                }
            }
        }
    }

    @FXML
    private void handleDownloadEvidence() {
        if (selectedRow != null && selectedRow.getSelfEvaluation() != null) {
            SelfEvaluation eval = selectedRow.getSelfEvaluation();
            String remotePath = eval.getEvidence();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Evidencia de Autoevaluación");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"));
            fileChooser.setInitialFileName("Evidencia_" + selectedRow.getStudentName().replace(" ", "_") + ".pdf");

            Stage stage = (Stage) downloadEvidenceButton.getScene().getWindow();
            File destination = fileChooser.showSaveDialog(stage);

            if (destination != null) {
                try {
                    Files.copy(Paths.get(remotePath), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    Controller.showAlert("Éxito", "Archivo de evidencia descargado correctamente.", AlertType.INFORMATION);
                } catch (IOException e) {
                    Controller.showAlert("Error", "No se pudo descargar el archivo de evidencia: " + e.getMessage(), AlertType.ERROR);
                }
            }
        }
    }

    @FXML
    private void handleApproveEvaluation() {
        if (selectedRow != null && selectedRow.getSelfEvaluation() != null) {
            try {
                selfEvaluationManager.updateSelfEvaluation(selectedRow.getSelfEvaluation(), selectedRow.getSelfEvaluation().getSelfEvalId());
                selfEvaluationManager.updateSelfEvaluationStatus(selectedRow.getSelfEvaluation().getSelfEvalId(), STATUS_REVIEWED);
                Controller.showAlert("Éxito", "Autoevaluación marcada como 'Revisada' correctamente.", AlertType.INFORMATION);
                loadStudentsAndEvaluations();
                clearDetails();
            } catch (ManagerException e) {
                Controller.showAlert("Error", "No se pudo actualizar el estado: " + e.getMessage(), AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleRejectEvaluation() {
        if (selectedRow != null && selectedRow.getSelfEvaluation() != null) {
            try {
                selfEvaluationManager.rejectSelfEvaluation(
                        selectedRow.getSelfEvaluation().getSelfEvalId(), rejectReasonTextField.getText());
                Controller.showAlert("Éxito", "Autoevaluación rechazada correctamente.", AlertType.INFORMATION);
                loadStudentsAndEvaluations();
                clearDetails();
            } catch (ManagerException e) {
                Controller.showAlert("Error", "No se pudo rechazar la autoevaluación: " + e.getMessage(), AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleReturnToMenu() {
        appStore.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }

    public static class SelfEvaluationRow {
        private final String studentName;
        private final String groupCode;
        private final String enrollment;
        private final String status;
        private final User student;
        private final SelfEvaluation selfEvaluation;

        public SelfEvaluationRow(String studentName, String groupCode, String enrollment, String status,
                                 User student, SelfEvaluation selfEvaluation) {
            this.studentName = studentName;
            this.groupCode = groupCode;
            this.enrollment = enrollment;
            this.status = status;
            this.student = student;
            this.selfEvaluation = selfEvaluation;
        }

        public String getStudentName() { return studentName; }
        public String getGroupCode() { return groupCode; }
        public String getEnrollment() { return enrollment; }
        public String getStatus() { return status; }
        public User getStudent() { return student; }
        public SelfEvaluation getSelfEvaluation() { return selfEvaluation; }
    }
}
