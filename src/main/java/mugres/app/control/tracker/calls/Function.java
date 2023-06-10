package mugres.app.control.tracker.calls;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import mugres.app.control.kveditor.KeyValueEditor;
import mugres.parametrizable.Parameter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Function extends VBox {
    private static final String FXML = "/mugres/app/control/tracker/calls/function.fxml";

    @FXML
    private ComboBox<mugres.function.Function> functionComboBox;

    @FXML
    private KeyValueEditor functionParametersEditor;

    public Function() {
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
        functionParametersEditor.setTitleVisible(false);
        functionComboBox.setItems(FXCollections.observableArrayList(mugres.function.Function.allFunctions()).sorted());
        functionComboBox.valueProperty().addListener((observable, oldValue, newValue) -> onFunctionChanged(newValue));
    }

    private void onFunctionChanged(final mugres.function.Function<?> function) {
        functionParametersEditor.setModel(KeyValueEditor.Model.of(
                function.parameters().stream()
                        .map(fp -> KeyValueEditor.Property.of(fp.name(), fp.name(), fp.dataType(), fp.defaultValue()))
                        .collect(Collectors.toList())));
    }
}
