package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.MonthlyReport;
import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.dto.ProgressReport;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.enums.ReportStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.MonthlyReportManager;
import mx.uv.fei.domain.manager.PeriodManager;
import mx.uv.fei.domain.manager.ProgressReportManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.state.RootState;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

@Component
public class ProfessorHomeController {

    private static final String DEFAULT_GREETING_NAME = "profesor";

    @FXML private Label greetingLabel;
    @FXML private Label pendingReportsLabel;

    private final AppStore store;
    private final MonthlyReportManager monthlyReportManager;
    private final ProgressReportManager progressReportManager;
    private final PeriodManager periodManager;

    private int professorId;
    private int activePeriodId;

    @Inject
    public ProfessorHomeController(AppStore store, MonthlyReportManager monthlyReportManager,
                                   ProgressReportManager progressReportManager, PeriodManager periodManager) {
        this.store = store;
        this.monthlyReportManager = monthlyReportManager;
        this.progressReportManager = progressReportManager;
        this.periodManager = periodManager;
    }

    @FXML
    public void initialize() {
        resolveProfessorContext();
        populateGreeting();
        populatePendingReports();
    }

    private void resolveProfessorContext() {
        User currentUser = store.getState().sessionState().currentUserInSession();
        professorId = currentUser != null ? currentUser.getId() : 0;

        try {
            Period activePeriod = periodManager.getActivePeriod();
            activePeriodId = activePeriod != null ? activePeriod.getPeriodId() : 0;
        } catch (ManagerException e) {
            activePeriodId = 0;
            Controller.showErrorAlert("Error de carga", e.getMessage());
        }
    }

    private void populateGreeting() {
        String displayName = DEFAULT_GREETING_NAME;
        RootState currentState = store.getState();

        if (currentState != null && currentState.sessionState() != null) {
            User currentUser = currentState.sessionState().currentUserInSession();
            if (currentUser != null) {
                displayName = currentUser.getName() + " " + currentUser.getLastName();
            }
        }

        greetingLabel.setText("Hola, " + displayName);
    }

    private void populatePendingReports() {
        try {
            int pendingCount = countPendingMonthlyReports() + countPendingProgressReports();
            pendingReportsLabel.setText(String.valueOf(pendingCount));
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de carga", e.getMessage());
        }
    }

    private int countPendingMonthlyReports() throws ManagerException {
        int pendingCount = 0;

        for (MonthlyReport report : monthlyReportManager.getReportsForEvaluation(professorId, activePeriodId)) {
            if (isPending(report.getStatus())) {
                pendingCount++;
            }
        }

        return pendingCount;
    }

    private int countPendingProgressReports() throws ManagerException {
        int pendingCount = 0;

        for (ProgressReport report : progressReportManager.getSubmittedProgressReports(professorId, activePeriodId)) {
            if (isPending(report.getStatus())) {
                pendingCount++;
            }
        }

        return pendingCount;
    }

    private boolean isPending(String reportStatus) {
        return !ReportStatus.EVALUATED.getDatabaseValue().equals(reportStatus);
    }
}
