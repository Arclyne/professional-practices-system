package mx.uv.fei.presentation;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.dto.ProgressReport;
import mx.uv.fei.domain.dto.SelfEvaluation;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.PractitionerManager;
import mx.uv.fei.domain.manager.ProgressReportManager;
import mx.uv.fei.domain.manager.SelfEvaluationManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Component
public class ReviewSelfEvaluationController {

    private static final String REPORT_TYPE_FINAL = "Final";
    private static final String STATUS_NOT_DELIVERED = "No entregada";
    private static final String STATUS_REVIEWED = "Revisada";
    private static final String STATUS_PENDING_EVIDENCE = "pendiente";

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

    @FXML private Button viewEvidenceButton;
    @FXML private Button downloadEvidenceButton;
    @FXML private Button approveButton;

    private final SelfEvaluationManager selfEvaluationManager;
    private final ProgressReportManager progressReportManager;
    private final PractitionerManager practitionerManager;
    private final AppStore appStore;

    private SelfEvaluationRow selectedRow;

    @Inject
    public ReviewSelfEvaluationController(SelfEvaluationManager selfEvaluationManager,
                                          ProgressReportManager progressReportManager,
                                          PractitionerManager practitionerManager,
                                          AppStore appStore) {
        this.selfEvaluationManager = selfEvaluationManager;
        this.progressReportManager = progressReportManager;
        this.practitionerManager = practitionerManager;
        this.appStore = appStore;
    }

    @FXML
    public void initialize() {
        studentTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStudentName()));
        groupTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGroupCode()));
        statusTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));

        evaluationsTableView.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> showEvaluationDetails(newValue));

        clearDetails();
        loadStudentsAndEvaluations();
    }

    private void loadStudentsAndEvaluations() {
        try {
            User currentUser = appStore.getState().sessionState().currentUserInSession();
            int academicId = currentUser.getId();

            List<Practitioner> students = practitionerManager.retrievePractitionersByProfessor(academicId);
            List<SelfEvaluationRow> rows = new ArrayList<>();

            for (Practitioner student : students) {
                SelfEvaluationRow evaluationRow = createSelfEvaluationRow(student);
                rows.add(evaluationRow);
            }

            ObservableList<SelfEvaluationRow> data = FXCollections.observableArrayList(rows);
            evaluationsTableView.setItems(data);

        } catch (ManagerException e) {
            Controller.showAlert("Error", "No se pudieron cargar las autoevaluaciones: " + e.getMessage(), AlertType.ERROR);
        }
    }

    private SelfEvaluationRow createSelfEvaluationRow(Practitioner student) throws ManagerException {
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
        return new SelfEvaluationRow(fullName, student.getEnrollment(), status, student, evaluation);
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

            viewEvidenceButton.setDisable(!hasEvidence);
            downloadEvidenceButton.setDisable(!hasEvidence);
            approveButton.setDisable(STATUS_REVIEWED.equals(eval.getStatus()));
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
    private void handleReturnToMenu() {
        appStore.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }

    public static class SelfEvaluationRow {
        private final String studentName;
        private final String groupCode;
        private final String status;
        private final User student;
        private final SelfEvaluation selfEvaluation;

        public SelfEvaluationRow(String studentName, String groupCode, String status, User student, SelfEvaluation selfEvaluation) {
            this.studentName = studentName;
            this.groupCode = groupCode;
            this.status = status;
            this.student = student;
            this.selfEvaluation = selfEvaluation;
        }

        public String getStudentName() { return studentName; }
        public String getGroupCode() { return groupCode; }
        public String getStatus() { return status; }
        public User getStudent() { return student; }
        public SelfEvaluation getSelfEvaluation() { return selfEvaluation; }
    }
}