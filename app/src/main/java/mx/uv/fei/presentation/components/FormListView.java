package mx.uv.fei.presentation.components;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class FormListView extends VBox {
    @FXML
    private Label labelTitle;

    @FXML
    private ListView<String> listViewItems;

    public FormListView() {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/mx/uv/fei/presentation/components/FormListView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setLabelText(String labelValue) {
        labelTitle.setText(labelValue);
    }

    public String getLabelText() {
        return labelTitle.getText();
    }

    public void setItems(ObservableList<String> listItems) {
        listViewItems.setItems(listItems);
    }

    public String getSelectedItem() {
        return listViewItems.getSelectionModel().getSelectedItem();
    }

    public void clearSelection() {
        if (listViewItems != null) {
            listViewItems.getSelectionModel().clearSelection();
        }
    }
}
