package mx.uv.fei.presentation.components;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class FormField extends VBox {
    @FXML private Label label;
    @FXML private TextField textField;

    public FormField() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FormField.fxml"));
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

    public void setPromptText(String value) {
        textField.setPromptText(value);
    }
    public String getPromptText() {
        return textField.getPromptText();
    }

    public String getText() {
        return textField.getText();
    }

    public void setText(String text) {
        textField.setText(text);
    }
}
