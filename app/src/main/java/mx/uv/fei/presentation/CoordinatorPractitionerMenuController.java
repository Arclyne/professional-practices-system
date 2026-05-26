package mx.uv.fei.presentation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.common.PdfService;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.PractitionerManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

@Component
public class CoordinatorPractitionerMenuController {
    private static final String TEMPLATE_PATH = "/templates/formato_practicante.pdf";
    private static final String OUTPUT_NAME_FORMAT = "Documento_%s.pdf";

    private static final String WARNING_TITLE = "Selección Requerida";
    private static final String WARNING_MESSAGE = "Por favor, seleccione un practicante de la lista.";
    private static final String SUCCESS_TITLE = "Documento Generado";
    private static final String SUCCESS_MESSAGE_FORMAT = "El archivo PDF ha sido guardado exitosamente en:\n%s";
    private static final String ERROR_GENERATE_TITLE = "Error al generar documento";
    private static final String ERROR_GENERATE_MESSAGE_FORMAT = "Ocurrió un error creando el directorio o escribiendo el archivo: %s";
    private static final String ERROR_LOAD_TITLE = "Error de Carga";

    private static final String FIELD_NAME = "campoNombre";
    private static final String FIELD_ENROLLMENT = "campoMatricula";
    private static final String DISPLAY_FORMAT = "%s %s - %s";

    @FXML private ListView<Practitioner> practitionersListView;
    @FXML private Button registerNewPractitionerButton;
    @FXML private Button assignProjectsButton;
    @FXML private Button generatePdfButton;
    @FXML private Button returnToDashboardButton;

    private final AppStore applicationNavigationStore;
    private final PractitionerManager practitionerManager;

    @Inject
    public CoordinatorPractitionerMenuController(AppStore applicationNavigationStore, PractitionerManager practitionerManager) {
        this.applicationNavigationStore = applicationNavigationStore;
        this.practitionerManager = practitionerManager;
    }

    @FXML
    public void initialize() {
        configurePractitionerListView();
        loadPractitionersData();
    }

    private void configurePractitionerListView() {
        practitionersListView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Practitioner practitioner, boolean empty) {
                super.updateItem(practitioner, empty);
                if (empty || practitioner == null) {
                    setText(null);
                } else {
                    setText(String.format(DISPLAY_FORMAT, practitioner.getName(), practitioner.getLastName(), practitioner.getEnrollment()));
                }
            }
        });
    }

    private void loadPractitionersData() {
        try {
            List<Practitioner> practitioners = practitionerManager.retrieveAssignedPractitioners();
            ObservableList<Practitioner> observableList = FXCollections.observableArrayList(practitioners);
            practitionersListView.setItems(observableList);
        } catch (ManagerException exception) {
            Controller.showErrorAlert(ERROR_LOAD_TITLE, exception.getMessage());
        }
    }

    @FXML
    private void handleGeneratePdfAction() {
        Practitioner selectedPractitioner = practitionersListView.getSelectionModel().getSelectedItem();

        if (selectedPractitioner == null) {
            Controller.showAlert(WARNING_TITLE, WARNING_MESSAGE, AlertType.WARNING);
        } else {
            generatePractitionerPdf(selectedPractitioner);
        }
    }

    private void generatePractitionerPdf(Practitioner practitioner) {
        final String DIR_APP = "app";
        final String DIR_DOCUMENTS = "documents";
        final String DIR_GENERATED = "generated";

        try {
            Path outputDirectory = Paths.get(DIR_APP,DIR_DOCUMENTS, DIR_GENERATED);

            if (!Files.exists(outputDirectory)) {
                Files.createDirectories(outputDirectory);
            }

            String fileName = String.format(OUTPUT_NAME_FORMAT, practitioner.getEnrollment());
            Path finalOutputPath = outputDirectory.resolve(fileName);

            Map<String, String> fieldData = new HashMap<>();
            fieldData.put(FIELD_NAME, String.format("%s %s", practitioner.getName(), practitioner.getLastName()));
            fieldData.put(FIELD_ENROLLMENT, practitioner.getEnrollment());

            PdfService pdfService = new PdfService();
            pdfService.fillPdfTemplate(TEMPLATE_PATH, finalOutputPath.toString(), fieldData);

            Controller.showInfoAlert(SUCCESS_TITLE, String.format(SUCCESS_MESSAGE_FORMAT, finalOutputPath.toAbsolutePath()));

        } catch (IOException exception) {
            Controller.showErrorAlert(ERROR_GENERATE_TITLE, String.format(ERROR_GENERATE_MESSAGE_FORMAT, exception.getMessage()));
        }
    }

    @FXML
    private void handleRegisterNewPractitionerAction() {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.REGISTER_PRACTITIONER));
    }

    @FXML
    private void handleAssignProjectsAction() {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.PENDING_PRACTITIONER_SELECTION));
    }

    @FXML
    private void handleReturnToDashboardAction() {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}