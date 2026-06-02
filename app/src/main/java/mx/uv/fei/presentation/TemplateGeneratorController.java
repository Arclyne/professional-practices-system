package mx.uv.fei.presentation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.domain.common.Controller;

@Component
public class TemplateGeneratorController {

    @FXML private ComboBox<String> documentTypeComboBox;
    @FXML private ListView<TemplateFragment> templatePartsListView;
    @FXML private TextArea documentPreviewTextArea;

    // Mapa para guardar las listas de fragmentos según el tipo de documento
    private final Map<String, List<TemplateFragment>> documentTemplates = new HashMap<>();

    @FXML
    public void initialize() {
        loadTemplateData();

        // Cargar opciones en el ComboBox
        documentTypeComboBox.setItems(FXCollections.observableArrayList(
                "Oficio de Aceptación",
                "Bitácora de Reporte"
        ));

        // Escuchar cambios en el ComboBox para actualizar la lista de piezas
        documentTypeComboBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> updateListViewForDocument(newValue)
        );

        // Permitir doble clic en la lista para insertar la pieza rápidamente
        templatePartsListView.setOnMouseClicked(this::handleListDoubleClick);

        // Seleccionar el primer elemento por defecto
        documentTypeComboBox.getSelectionModel().selectFirst();
    }

    private void loadTemplateData() {
        String commonHeader = """
                UNIVERSIDAD VERACRUZANA
                Facultad de Estadística e Informática
                Sistema de Gestión de Prácticas Profesionales
                Xalapa, Veracruz.
                
                """;

        String commonSignatures = """
                
                _________________________________
                Firma del Coordinador de Prácticas
                
                _________________________________
                Firma del Profesor / Responsable
                """;

        // Configurar piezas para Oficio de Aceptación
        documentTemplates.put("Oficio de Aceptación", List.of(
                new TemplateFragment("1. Encabezado Institucional UV", commonHeader),
                new TemplateFragment("2. Cuerpo de Aceptación", """
                   Por medio de la presente, se hace constar la ACEPTACIÓN oficial del practicante
                   C. %PRACTICANTE_NOMBRE%, con matrícula %PRACTICANTE_MATRICULA%, para colaborar 
                   en el proyecto denominado "%PROYECTO_NOMBRE%".
                   
                   Este documento certifica que el estudiante cumple con los requisitos establecidos 
                   por la coordinación para el periodo %PERIODO_ESCOLAR%.
                   
                   """),
                new TemplateFragment("3. Firmas de Autorización", commonSignatures)
        ));

        // Configurar piezas para Bitácora de Reporte
        documentTemplates.put("Bitácora de Reporte", List.of(
                new TemplateFragment("1. Encabezado Institucional UV", commonHeader),
                new TemplateFragment("2. Detalles del Reporte", """
                   REPORTE DE ACTIVIDADES
                   --------------------------------------------------
                   Nombre del Practicante: %PRACTICANTE_NOMBRE%
                   Fecha de Reporte: %FECHA_ACTUAL%
                   
                   Actividades realizadas:
                   1. %ACTIVIDAD_1%
                   2. %ACTIVIDAD_2%
                   
                   Horas cubiertas en este reporte: %HORAS_REPORTADAS%
                   Horas acumuladas totales: %HORAS_TOTALES%
                   
                   """),
                new TemplateFragment("3. Firmas de Revisión", commonSignatures),
                new TemplateFragment("+. Observaciones Adicionales", """
                   Observaciones del Responsable:
                   %OBSERVACIONES%
                   
                   """)
        ));
    }

    private void updateListViewForDocument(String documentType) {
        if (documentType != null && documentTemplates.containsKey(documentType)) {
            templatePartsListView.setItems(FXCollections.observableArrayList(documentTemplates.get(documentType)));
        } else {
            templatePartsListView.getItems().clear();
        }
    }

    @FXML
    private void handleInsertSelectedPart(ActionEvent event) {
        TemplateFragment selectedFragment = templatePartsListView.getSelectionModel().getSelectedItem();
        if (selectedFragment != null) {
            documentPreviewTextArea.appendText(selectedFragment.getContent());
        }
    }

    private void handleListDoubleClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            handleInsertSelectedPart(null);
        }
    }

    @FXML
    private void handleClear(ActionEvent event) {
        documentPreviewTextArea.clear();
    }

    @FXML
    private void handleSaveDocument(ActionEvent event) {
        String content = documentPreviewTextArea.getText();
        if (content == null || content.trim().isEmpty()) {
            Controller.showAlert("Documento Vacío", "No puedes guardar un documento sin contenido.", AlertType.WARNING);
            return;
        }

        Window currentWindow = documentPreviewTextArea.getScene().getWindow();

        // Cambiamos DirectoryChooser por FileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Plantilla de Documento");

        // Filtro para asegurar que se guarde como .txt
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos de Texto (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);

        // Sugerir un nombre de archivo por defecto basado en el tipo de documento seleccionado
        String docType = documentTypeComboBox.getValue().replace(" ", "_");
        fileChooser.setInitialFileName(docType + "_Plantilla.txt");

        // Mostrar la ventana nativa de "Guardar como..."
        File fileToSave = fileChooser.showSaveDialog(currentWindow);

        // Si el usuario no canceló la ventana
        if (fileToSave != null) {
            try (FileWriter writer = new FileWriter(fileToSave)) {
                writer.write(content);
                Controller.showAlert("Éxito", "El documento se guardó correctamente en:\n" + fileToSave.getAbsolutePath(), AlertType.INFORMATION);
            } catch (IOException e) {
                Controller.showAlert("Error al Guardar", "Ocurrió un problema al intentar escribir el archivo.", AlertType.ERROR);
            }
        }
    }

    /**
     * Clase auxiliar para representar una pieza de la plantilla.
     * Al sobrescribir el método toString(), el ListView de JavaFX
     * mostrará automáticamente el nombre (title) en la interfaz.
     */
    private static class TemplateFragment {
        private final String title;
        private final String content;

        public TemplateFragment(String title, String content) {
            this.title = title;
            this.content = content;
        }

        public String getContent() {
            return content;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}