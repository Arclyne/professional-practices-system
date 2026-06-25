package mx.uv.fei.presentation.professor;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.EvaluableReport;
import mx.uv.fei.domain.dto.MonthlyReport;
import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.dto.PracticeGroup;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.dto.ProgressReport;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.enums.ProgressReportType;
import mx.uv.fei.domain.enums.ReportStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.academic.PeriodManager;
import mx.uv.fei.domain.manager.academic.PracticeGroupManager;
import mx.uv.fei.domain.manager.people.PractitionerManager;
import mx.uv.fei.domain.manager.reporting.MonthlyReportManager;
import mx.uv.fei.domain.manager.reporting.ProgressReportManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

@Component
public class ProfessorEvaluateReportController implements Initializable {

    private static final String EVALUATED_PREFIX = "[Evaluado] ";
    private static final String PENDING_PREFIX = "[Pendiente] ";
    private static final String GROUP_FILTER_ALL = "Todos los grupos";
    private static final String NO_VALUE = "—";

    @FXML private ListView<EvaluableReport> reportsListView;
    @FXML private TextField searchTextField;
    @FXML private ComboBox<String> groupFilterComboBox;
    @FXML private VBox evaluationContainer;
    @FXML private Label labelReportKind;
    @FXML private Label labelReportInfo;
    @FXML private Button btnViewPdf;
    @FXML private TextField fieldGrade;
    @FXML private TextArea areaFeedback;
    @FXML private Button btnSaveEvaluation;

    private final MonthlyReportManager monthlyReportManager;
    private final ProgressReportManager progressReportManager;
    private final PeriodManager periodManager;
    private final PracticeGroupManager practiceGroupManager;
    private final PractitionerManager practitionerManager;
    private final AppStore store;

    private final ObservableList<EvaluableReport> allReports = FXCollections.observableArrayList();
    private final Map<Integer, String> enrollmentByPractitionerId = new HashMap<>();
    private final Map<Integer, String> sectionByPractitionerId = new HashMap<>();

    private FilteredList<EvaluableReport> filteredReports;
    private EvaluableReport selectedReport;
    private int professorId;
    private int activePeriodId;

    @Inject
    public ProfessorEvaluateReportController(MonthlyReportManager monthlyReportManager,
                                             ProgressReportManager progressReportManager, PeriodManager periodManager,
                                             PracticeGroupManager practiceGroupManager,
                                             PractitionerManager practitionerManager, AppStore store) {
        this.monthlyReportManager = monthlyReportManager;
        this.progressReportManager = progressReportManager;
        this.periodManager = periodManager;
        this.practiceGroupManager = practiceGroupManager;
        this.practitionerManager = practitionerManager;
        this.store = store;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        showEvaluationContainer(false);
        resolveProfessorContext();
        configureListView();
        loadProfessorGroups();
        loadAllSubmittedReports();
    }

    private void resolveProfessorContext() {
        User currentUser = store.getState().sessionState().currentUserInSession();
        professorId = currentUser != null ? currentUser.getId() : 0;

        try {
            Period activePeriod = periodManager.getActivePeriod();
            activePeriodId = activePeriod != null ? activePeriod.getPeriodId() : 0;
        } catch (ManagerException e) {
            activePeriodId = 0;
            Controller.showAlert("Error de Carga", e.getMessage(), AlertType.WARNING);
        }
    }

    private void loadProfessorGroups() {
        ObservableList<String> groupOptions = FXCollections.observableArrayList(GROUP_FILTER_ALL);

        try {
            List<PracticeGroup> groups = practiceGroupManager.getGroupsByProfessorAndPeriod(professorId, activePeriodId);
            for (PracticeGroup group : groups) {
                groupOptions.add(group.getSection());
                mapGroupPractitioners(group);
            }
        } catch (ManagerException e) {
            Controller.showAlert("Error de Carga", e.getMessage(), AlertType.WARNING);
        }

        groupFilterComboBox.setItems(groupOptions);
        groupFilterComboBox.setValue(GROUP_FILTER_ALL);
    }

    private void mapGroupPractitioners(PracticeGroup group) throws ManagerException {
        List<Practitioner> practitioners = practitionerManager.retrieveEnrolledPractitionersByGroup(group.getGroupId());
        for (Practitioner practitioner : practitioners) {
            enrollmentByPractitionerId.put(practitioner.getId(), practitioner.getEnrollment());
            sectionByPractitionerId.put(practitioner.getId(), group.getSection());
        }
    }

    private void configureListView() {
        filteredReports = new FilteredList<>(allReports);
        reportsListView.setItems(filteredReports);

        reportsListView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(EvaluableReport report, boolean isEmpty) {
                super.updateItem(report, isEmpty);
                if (isEmpty || report == null) {
                    setText(null);
                } else {
                    setText(buildReportDisplayText(report));
                }
            }
        });

        reportsListView.getSelectionModel().selectedItemProperty()
                .addListener((_, _, selectedReport) -> {
                    if (selectedReport != null) {
                        showReportDetails(selectedReport);
                    }
                });
    }

    private String buildReportDisplayText(EvaluableReport report) {
        boolean isEvaluated = ReportStatus.EVALUATED.getDatabaseValue().equals(report.getStatus());
        String prefix = isEvaluated ? EVALUATED_PREFIX : PENDING_PREFIX;
        return prefix + report.getDisplayName() + " — Matrícula " + enrollmentOf(report);
    }

    private void loadAllSubmittedReports() {
        allReports.clear();
        loadMonthlyReports();
        loadProgressReports();
        applyGroupFilter();
    }

    private void loadMonthlyReports() {
        try {
            List<MonthlyReport> monthlyReports = monthlyReportManager
                    .getReportsForEvaluation(professorId, activePeriodId);
            for (MonthlyReport report : monthlyReports) {
                allReports.add(enrich(EvaluableReport.fromMonthlyReport(report)));
            }
        } catch (ManagerException e) {
            Controller.showAlert("Error de Carga", "Reportes mensuales: " + e.getMessage(), AlertType.WARNING);
        }
    }

    private void loadProgressReports() {
        try {
            List<ProgressReport> progressReports = progressReportManager
                    .getSubmittedProgressReports(professorId, activePeriodId);
            for (ProgressReport report : progressReports) {
                allReports.add(enrich(EvaluableReport.fromProgressReport(report)));
            }
        } catch (ManagerException e) {
            Controller.showAlert("Error de Carga", "Reportes de avance: " + e.getMessage(), AlertType.WARNING);
        }
    }

    private EvaluableReport enrich(EvaluableReport report) {
        report.setPractitionerEnrollment(enrollmentByPractitionerId.get(report.getPractitionerId()));
        report.setGroupSection(sectionByPractitionerId.get(report.getPractitionerId()));
        return report;
    }

    private void showReportDetails(EvaluableReport report) {
        selectedReport = report;
        showEvaluationContainer(true);

        labelReportKind.setText("Tipo de reporte: " + report.getReportKind());
        labelReportInfo.setText(report.getDisplayName()
                + "\nMatrícula: " + enrollmentOf(report)
                + "\nGrupo: " + sectionOf(report)
                + "\nEstado: " + report.getStatus());

        boolean hasPdf = report.getSignedFileUrl() != null && !report.getSignedFileUrl().isEmpty();
        btnViewPdf.setDisable(!hasPdf);

        fillGradeField(report);
        fillFeedbackArea(report);
    }

    private void fillGradeField(EvaluableReport report) {
        if (report.getGrade() != null) {
            fieldGrade.setText(String.valueOf(report.getGrade()));
        } else {
            fieldGrade.clear();
        }
    }

    private void fillFeedbackArea(EvaluableReport report) {
        if (report.getProfessorFeedback() != null) {
            areaFeedback.setText(report.getProfessorFeedback());
        } else {
            areaFeedback.clear();
        }
    }

    private void showEvaluationContainer(boolean isVisible) {
        evaluationContainer.setVisible(isVisible);
        evaluationContainer.setManaged(isVisible);
    }

    @FXML
    private void handleGroupFilterAction() {
        applyGroupFilter();
    }

    @FXML
    private void handleSearchAction() {
        applyGroupFilter();
    }

    private void applyGroupFilter() {
        filteredReports.setPredicate(this::matchesActiveFilters);
    }

    private boolean matchesActiveFilters(EvaluableReport report) {
        return matchesGroupFilter(report) && matchesEnrollmentSearch(report);
    }

    private boolean matchesGroupFilter(EvaluableReport report) {
        String selectedGroup = groupFilterComboBox.getValue();
        boolean isMatch = selectedGroup == null || GROUP_FILTER_ALL.equals(selectedGroup);

        if (!isMatch) {
            isMatch = selectedGroup.equals(report.getGroupSection());
        }

        return isMatch;
    }

    private boolean matchesEnrollmentSearch(EvaluableReport report) {
        String query = searchTextField.getText() == null ? "" : searchTextField.getText().trim().toLowerCase();
        String enrollment = report.getPractitionerEnrollment() == null
                ? "" : report.getPractitionerEnrollment().toLowerCase();

        return query.isEmpty() || enrollment.contains(query);
    }

    private String enrollmentOf(EvaluableReport report) {
        return report.getPractitionerEnrollment() != null ? report.getPractitionerEnrollment() : NO_VALUE;
    }

    private String sectionOf(EvaluableReport report) {
        return report.getGroupSection() != null ? report.getGroupSection() : NO_VALUE;
    }

    @FXML
    private void handleViewPdfAction() {
        if (selectedReport == null || selectedReport.getSignedFileUrl() == null) {
            return;
        }
        try {
            Desktop.getDesktop().browse(new URI(selectedReport.getSignedFileUrl()));
        } catch (Exception e) {
            Controller.showAlert("Error de Archivo",
                    "No se pudo abrir la evidencia del alumno.", AlertType.ERROR);
        }
    }

    @FXML
    private void handleSaveEvaluationAction() {
        if (selectedReport == null) {
            return;
        }

        String rawGrade = fieldGrade.getText().trim();
        if (rawGrade.isEmpty()) {
            Controller.showAlert("Campo requerido",
                    "Ingresa la calificación antes de guardar.", AlertType.WARNING);
            return;
        }

        String feedback = areaFeedback.getText().trim();
        try {
            double grade = Double.parseDouble(rawGrade);
            saveEvaluation(selectedReport, grade, feedback);
        } catch (NumberFormatException e) {
            Controller.showAlert("Formato inválido",
                    "La calificación debe ser un número decimal (ej. 9.5).", AlertType.WARNING);
        } catch (ManagerException e) {
            Controller.showAlert("Datos inválidos", e.getMessage(), AlertType.WARNING);
        }
    }

    private void saveEvaluation(EvaluableReport report, double grade, String feedback) throws ManagerException {
        if (report.isProgressReport()) {
            ProgressReportType reportType = ProgressReportType.fromString(report.getReportKind());
            progressReportManager.evaluateProgressReport(report.getPractitionerId(), reportType, grade, feedback);
        } else {
            monthlyReportManager.evaluateReport(report.getReportId(), grade, feedback);
        }

        Controller.showAlert("Evaluación Guardada",
                "La evaluación del reporte fue registrada correctamente.", AlertType.INFORMATION);

        showEvaluationContainer(false);
        selectedReport = null;
        loadAllSubmittedReports();
    }

    @FXML
    private void handleReturnAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}
