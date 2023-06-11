package mugres.app.control.tracker;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import mugres.common.Instrument;

import java.io.IOException;

public class Matrix extends ScrollPane {
    private static final String FXML = "/mugres/app/control/tracker/matrix.fxml";

    @FXML
    private GridPane callsMatrix;

    @FXML
    private ComboBox<Instrument> instrumentComboBox;

    public Matrix() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(FXML));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @FXML
    public void initialize() {
        instrumentComboBox.setItems(FXCollections.observableArrayList(mugres.common.Instrument.values()).sorted());
        instrumentComboBox.valueProperty().addListener((observable, oldValue, newValue) -> onFunctionChanged(newValue));
    }

    private void onFunctionChanged(final Instrument instrument) {

    }
}
