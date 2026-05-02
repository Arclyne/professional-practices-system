package mx.uv.fei.presentation.components;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class FormPasswordField extends VBox {

    @FXML
    private Label labelTitle;

    @FXML
    private PasswordField passwordFieldInput;

    public FormPasswordField() {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/mx/uv/fei/presentation/components/FormPasswordField.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException loaderException) {
            throw new RuntimeException(loaderException);
        }
    }

    public void setLabelText(String labelValue) {
        labelTitle.setText(labelValue);
    }

    public String getLabelText() {
        return labelTitle.getText();
    }

    public String getPasswordText() {
        return passwordFieldInput.getText();
    }

    public void setPasswordText(String passwordValue) {
        passwordFieldInput.setText(passwordValue);
    }

    public void clearPassword() {
        if (passwordFieldInput != null) {
            passwordFieldInput.clear();
        }
    }
}