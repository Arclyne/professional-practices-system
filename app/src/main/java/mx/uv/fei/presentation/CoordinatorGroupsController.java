package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.PracticeGroup;
import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.PeriodManager;
import mx.uv.fei.domain.manager.PracticeGroupManager;
import mx.uv.fei.domain.manager.ProfessorManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

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

    @FXML private TextField searchTextField;
    @FXML private TableView<PracticeGroup> groupsTableView;
    @FXML private TableColumn<PracticeGroup, String> sectionColumn;
    @FXML private TableColumn<PracticeGroup, String> periodColumn;
    @FXML private TableColumn<PracticeGroup, String> professorColumn;

    private final PracticeGroupManager practiceGroupManager;
    private final PeriodManager periodManager;
    private final ProfessorManager professorManager;
    private final AppStore store;
    private final ObservableList<PracticeGroup> allGroups = FXCollections.observableArrayList();

    private FilteredList<PracticeGroup> filteredGroups;
    private Map<Integer, String> periodNamesById = new HashMap<>();
    private Map<Integer, String> professorNamesById = new HashMap<>();

    @Inject
    public CoordinatorGroupsController(PracticeGroupManager practiceGroupManager, PeriodManager periodManager,
            ProfessorManager professorManager, AppStore store) {
        this.practiceGroupManager = practiceGroupManager;
        this.periodManager = periodManager;
        this.professorManager = professorManager;
        this.store = store;
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
            periodNamesById = mapPeriodNames(periodManager.getAllPeriods());
            professorNamesById = mapProfessorNames(professorManager.getAllProfessors());
            allGroups.setAll(practiceGroupManager.getAllPracticeGroups());
            applyFilter();
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de carga", e.getMessage());
        }
    }

    private Map<Integer, String> mapPeriodNames(List<Period> periods) {
        Map<Integer, String> namesById = new HashMap<>();
        for (Period period : periods) {
            namesById.put(period.getPeriodId(), period.getPeriodName());
        }

        return namesById;
    }

    private Map<Integer, String> mapProfessorNames(List<Professor> professors) {
        Map<Integer, String> namesById = new HashMap<>();
        for (Professor professor : professors) {
            namesById.put(professor.getId(), professor.getName() + " " + professor.getLastName());
        }

        return namesById;
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
    private void handleRegisterAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.REGISTER_PRACTICE_GROUP));
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
