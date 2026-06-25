package mx.uv.fei.presentation.coordinator;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.PracticeGroup;
import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.academic.PeriodManager;
import mx.uv.fei.domain.manager.academic.PracticeGroupManager;
import mx.uv.fei.domain.manager.people.ProfessorManager;
import mx.uv.fei.presentation.shell.ShellNavigator;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CoordinatorGroupsController {

    private static final String UNKNOWN_VALUE = "—";
    private static final String REGISTER_FORM_VIEW = "/mx/uv/fei/presentation/registerPracticeGroup.fxml";

    @FXML private TextField searchTextField;
    @FXML private TableView<PracticeGroup> groupsTableView;
    @FXML private TableColumn<PracticeGroup, String> sectionColumn;
    @FXML private TableColumn<PracticeGroup, String> periodColumn;
    @FXML private TableColumn<PracticeGroup, String> professorColumn;

    private final PracticeGroupManager practiceGroupManager;
    private final PeriodManager periodManager;
    private final ProfessorManager professorManager;
    private final ShellNavigator shellNavigator;
    private final ObservableList<PracticeGroup> allGroups = FXCollections.observableArrayList();

    private FilteredList<PracticeGroup> filteredGroups;
    private Map<Integer, String> periodNamesById = new HashMap<>();
    private Map<Integer, String> professorNamesById = new HashMap<>();

    @Inject
    public CoordinatorGroupsController(PracticeGroupManager practiceGroupManager, PeriodManager periodManager,
            ProfessorManager professorManager, ShellNavigator shellNavigator) {
        this.practiceGroupManager = practiceGroupManager;
        this.periodManager = periodManager;
        this.professorManager = professorManager;
        this.shellNavigator = shellNavigator;
    }

    @FXML
    public void initialize() {
        setupColumns();
        bindFilteredTable();
        loadGroups();
    }

    private void setupColumns() {
        sectionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSection()));
        periodColumn.setCellValueFactory(data -> new SimpleStringProperty(periodNameOf(data.getValue())));
        professorColumn.setCellValueFactory(data -> new SimpleStringProperty(professorNameOf(data.getValue())));
    }

    private void bindFilteredTable() {
        filteredGroups = new FilteredList<>(allGroups);
        groupsTableView.setItems(filteredGroups);
    }

    private void loadGroups() {
        try {
            mapPeriods(periodManager.getAllPeriods());
            mapProfessors(professorManager.getAllProfessors());
            allGroups.setAll(practiceGroupManager.getAllPracticeGroups());
            applyFilter();
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de carga", e.getMessage());
        }
    }

    private void mapPeriods(List<Period> periods) {
        periodNamesById = new HashMap<>();
        for (Period period : periods) {
            periodNamesById.put(period.getPeriodId(), period.getPeriodName());
        }
    }

    private void mapProfessors(List<Professor> professors) {
        professorNamesById = new HashMap<>();
        for (Professor professor : professors) {
            professorNamesById.put(professor.getId(), professor.getName() + " " + professor.getLastName());
        }
    }

    @FXML
    private void handleSearchAction() {
        applyFilter();
    }

    @FXML
    private void handleRefreshAction() {
        loadGroups();
    }

    @FXML
    private void handleEditAction() {
        PracticeGroup selectedGroup = groupsTableView.getSelectionModel().getSelectedItem();
        if (selectedGroup == null) {
            Controller.showInfoAlert("Selección requerida", "Selecciona un grupo de la lista para editarlo.");
        } else {
            shellNavigator.openForm(REGISTER_FORM_VIEW, selectedGroup);
        }
    }

    @FXML
    private void handleRegisterAction() {
        shellNavigator.openForm(REGISTER_FORM_VIEW);
    }

    private void applyFilter() {
        filteredGroups.setPredicate(this::matchesSearchText);
    }

    private boolean matchesSearchText(PracticeGroup group) {
        String query = searchTextField.getText() == null ? "" : searchTextField.getText().trim().toLowerCase();
        String searchableText = (group.getSection() + " " + periodNameOf(group) + " "
                + professorNameOf(group)).toLowerCase();

        return query.isEmpty() || searchableText.contains(query);
    }

    private String periodNameOf(PracticeGroup group) {
        return periodNamesById.getOrDefault(group.getPeriodId(), UNKNOWN_VALUE);
    }

    private String professorNameOf(PracticeGroup group) {
        return professorNamesById.getOrDefault(group.getProfessorId(), UNKNOWN_VALUE);
    }
}
