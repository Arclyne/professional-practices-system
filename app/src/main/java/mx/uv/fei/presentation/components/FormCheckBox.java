package mx.uv.fei.presentation.components;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class FormCheckBox extends VBox {
    @FXML private CheckBox checkBox;

    public FormCheckBox() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/mx/uv/fei/presentation/components/FormCheckBox.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void setText(String value) { checkBox.setText(value); }
    public String getText() { return checkBox.getText(); }

    public boolean isSelected() { return checkBox.isSelected(); }
    public void setSelected(boolean value) { checkBox.setSelected(value); }
}