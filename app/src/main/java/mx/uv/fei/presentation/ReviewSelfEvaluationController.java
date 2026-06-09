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

    @FXML private TableView<SelfEvaluationRow> tvEvaluations;
    @FXML private TableColumn<SelfEvaluationRow, String> tcStudent;
    @FXML private TableColumn<SelfEvaluationRow, String> tcGroup;
    @FXML private TableColumn<SelfEvaluationRow, String> tcStatus;

    @FXML private Label lblStudentName;
    @FXML private Label lblStatus;

    @FXML private TextField tfQ1, tfQ2, tfQ3, tfQ4, tfQ5, tfQ6, tfQ7, tfQ8, tfQ9, tfQ10;

    @FXML private Button btnViewEvidence;
    @FXML private Button btnDownloadEvidence;
    @FXML private Button btnApprove;

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
        tcStudent.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStudentName()));
        tcGroup.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGroupCode()));
        tcStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));

        tvEvaluations.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showEvaluationDetails(newValue));

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
                List<ProgressReport> reports = progressReportManager.getProgressReportsByPractitioner(student.getId());
                ProgressReport finalReport = reports.stream()
                        .filter(r -> "Final".equals(r.getReportType()))
                        .findFirst()
                        .orElse(null);

                String status = "No entregada";
                SelfEvaluation evaluation = null;

                if (finalReport != null) {
                    evaluation = selfEvaluationManager.recoverSelfEvaluation(finalReport.getReportId());
                    if (evaluation != null) {
                        status = evaluation.getStatus();
                    }
                }

                String fullName = student.getName() + " " + student.getLastName();

                rows.add(new SelfEvaluationRow(fullName, student.getEnrollment(), status, student, evaluation));
            }

            ObservableList<SelfEvaluationRow> data = FXCollections.observableArrayList(rows);
            tvEvaluations.setItems(data);

        } catch (ManagerException e) {
            Controller.showAlert("Error", "No se pudieron cargar las autoevaluaciones: " + e.getMessage(), AlertType.ERROR);
        }
    }

    private void showEvaluationDetails(SelfEvaluationRow row) {
        this.selectedRow = row;
        if (row == null || row.getSelfEvaluation() == null) {
            clearDetails();
            if (row != null) {
                lblStudentName.setText("Alumno: " + row.getStudentName());
                lblStatus.setText("Estado: " + row.getStatus());
            }
            return;
        }

        SelfEvaluation eval = row.getSelfEvaluation();
        lblStudentName.setText("Alumno: " + row.getStudentName());
        lblStatus.setText("Estado: " + eval.getStatus());

        tfQ1.setText(String.valueOf(eval.getQ1())); tfQ2.setText(String.valueOf(eval.getQ2()));
        tfQ3.setText(String.valueOf(eval.getQ3())); tfQ4.setText(String.valueOf(eval.getQ4()));
        tfQ5.setText(String.valueOf(eval.getQ5())); tfQ6.setText(String.valueOf(eval.getQ6()));
        tfQ7.setText(String.valueOf(eval.getQ7())); tfQ8.setText(String.valueOf(eval.getQ8()));
        tfQ9.setText(String.valueOf(eval.getQ9())); tfQ10.setText(String.valueOf(eval.getQ10()));

        boolean hasEvidence = eval.getEvidence() != null
                && !eval.getEvidence().isEmpty()
                && !"pendiente".equals(eval.getEvidence());

        btnViewEvidence.setDisable(!hasEvidence);
        btnDownloadEvidence.setDisable(!hasEvidence);
        btnApprove.setDisable("Revisada".equals(eval.getStatus()));
    }

    private void clearDetails() {
        lblStudentName.setText("Alumno: Seleccione un alumno");
        lblStatus.setText("Estado: -");
        TextField[] fields = {tfQ1, tfQ2, tfQ3, tfQ4, tfQ5, tfQ6, tfQ7, tfQ8, tfQ9, tfQ10};
        for (TextField tf : fields) {
            tf.clear();
            tf.setEditable(false);
        }
        btnViewEvidence.setDisable(true);
        btnDownloadEvidence.setDisable(true);
        btnApprove.setDisable(true);
    }

    @FXML
    private void handleViewEvidence() {
        if (selectedRow == null || selectedRow.getSelfEvaluation() == null) return;

        String evidencePath = selectedRow.getSelfEvaluation().getEvidence();
        if (evidencePath == null || evidencePath.isEmpty()) return;

        try {
            java.net.URI uri = new java.io.File(evidencePath).toURI();
            java.awt.Desktop.getDesktop().browse(uri);
        } catch (Exception e) {
            Controller.showAlert("Error de Archivo",
                    "No se pudo abrir la evidencia del alumno: " + e.getMessage(),
                    AlertType.ERROR);
        }
    }

    @FXML
    private void handleDownloadEvidence() {
        if (selectedRow == null || selectedRow.getSelfEvaluation() == null) return;

        SelfEvaluation eval = selectedRow.getSelfEvaluation();
        String remotePath = eval.getEvidence();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Evidencia de Autoevaluación");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"));
        fileChooser.setInitialFileName("Evidencia_" + selectedRow.getStudentName().replace(" ", "_") + ".pdf");

        Stage stage = (Stage) btnDownloadEvidence.getScene().getWindow();
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

    @FXML
    private void handleApproveEvaluation() {
        if (selectedRow == null || selectedRow.getSelfEvaluation() == null) return;

        try {
            selfEvaluationManager.updateSelfEvaluation(selectedRow.getSelfEvaluation(), selectedRow.getSelfEvaluation().getSelfEvalId());
            selfEvaluationManager.updateStatus(selectedRow.getSelfEvaluation().getSelfEvalId(), "Revisada");
            Controller.showAlert("Éxito", "Autoevaluación marcada como 'Revisada' correctamente.", AlertType.INFORMATION);
            loadStudentsAndEvaluations();
            clearDetails();
        } catch (ManagerException e) {
            Controller.showAlert("Error", "No se pudo actualizar el estado: " + e.getMessage(), AlertType.ERROR);
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