package mx.uv.fei.presentation.components;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class FormComboBox extends VBox {
    @FXML
    private Label label;
    @FXML
    private ComboBox<String> comboBox;

    public FormComboBox() {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/mx/uv/fei/presentation/components/FormComboBox.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setLabelText(String value) {
        label.setText(value);
    }

    public String getLabelText() {
        return label.getText();
    }

    public void setItems(ObservableList<String> items) {
        comboBox.setItems(items);
    }

    public String getValue() {
        return comboBox.getValue();
    }

    public void clearSelection() {
        if (comboBox != null) {
            comboBox.getSelectionModel().clearSelection();
        }
    }

    public ObjectProperty<String> valueProperty() {
        return comboBox.valueProperty();
    }

    public ObservableList<String> getItems() {
        return comboBox.getItems();
    }

    public void setPromptText(String promptText) {
        comboBox.setPromptText(promptText);
    }
}
