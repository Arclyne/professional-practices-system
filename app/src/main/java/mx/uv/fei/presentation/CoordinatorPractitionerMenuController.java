package mx.uv.fei.presentation;

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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CoordinatorPractitionerMenuController {

    private static final String TEMPLATE_PATH = "/templates/formato_practicante.pdf";
    private static final String OUTPUT_DIRECTORY = "app/documents/generated";
    private static final String OUTPUT_NAME_FORMAT = "Documento_%s.pdf";
    private static final String PRACTITIONER_DISPLAY_FORMAT = "%s %s - %s";
    private static final String PDF_FIELD_NAME = "campoNombre";
    private static final String PDF_FIELD_ENROLLMENT = "campoMatricula";

    @FXML private ListView<Practitioner> practitionersListView;
    @FXML private Button registerNewPractitionerButton;
    @FXML private Button assignProjectsButton;
    @FXML private Button generatePdfButton;
    @FXML private Button returnToDashboardButton;

    private final AppStore store;
    private final PractitionerManager practitionerManager;

    @Inject
    public CoordinatorPractitionerMenuController(AppStore store, PractitionerManager practitionerManager) {
        this.store = store;
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
            protected void updateItem(Practitioner practitioner, boolean isEmpty) {
                super.updateItem(practitioner, isEmpty);
                if (isEmpty || practitioner == null) {
                    setText(null);
                } else {
                    setText(String.format(PRACTITIONER_DISPLAY_FORMAT,
                            practitioner.getName(), practitioner.getLastName(), practitioner.getEnrollment()));
                }
            }
        });
    }

    private void loadPractitionersData() {
        try {
            List<Practitioner> practitioners = practitionerManager.retrieveAssignedPractitioners();
            ObservableList<Practitioner> practitionerItems = FXCollections.observableArrayList(practitioners);
            practitionersListView.setItems(practitionerItems);
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de Carga", e.getMessage());
        }
    }

    @FXML
    private void handleGeneratePdfAction() {
        Practitioner selectedPractitioner = practitionersListView.getSelectionModel().getSelectedItem();
        if (selectedPractitioner == null) {
            Controller.showAlert("Selección Requerida",
                    "Por favor, seleccione un practicante de la lista.", AlertType.WARNING);
        } else {
            generatePractitionerPdf(selectedPractitioner);
        }
    }

    private void generatePractitionerPdf(Practitioner practitioner) {
        try {
            Path outputDirectory = Paths.get(OUTPUT_DIRECTORY);
            if (!Files.exists(outputDirectory)) {
                Files.createDirectories(outputDirectory);
            }

            String fileName = String.format(OUTPUT_NAME_FORMAT, practitioner.getEnrollment());
            Path outputFilePath = outputDirectory.resolve(fileName);

            Map<String, String> fieldData = new HashMap<>();
            fieldData.put(PDF_FIELD_NAME,
                    String.format("%s %s", practitioner.getName(), practitioner.getLastName()));
            fieldData.put(PDF_FIELD_ENROLLMENT, practitioner.getEnrollment());

            PdfService pdfService = new PdfService();
            pdfService.fillPdfTemplate(TEMPLATE_PATH, outputFilePath.toString(), fieldData);

            Controller.showInfoAlert("Documento Generado",
                    String.format("El archivo PDF ha sido guardado exitosamente en:\n%s",
                            outputFilePath.toAbsolutePath()));
        } catch (IOException e) {
            Controller.showErrorAlert("Error al generar documento",
                    String.format("Ocurrió un error creando el directorio o escribiendo el archivo: %s",
                            e.getMessage()));
        }
    }

    @FXML
    private void handleRegisterNewPractitionerAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.REGISTER_PRACTITIONER));
    }

    @FXML
    private void handleAssignProjectsAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.PENDING_PRACTITIONER_SELECTION));
    }

    @FXML
    private void handleReturnToDashboardAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}