package mugres.app.control.tracker.call;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import mugres.app.control.Properties;

import java.io.IOException;
import java.util.stream.Collectors;

public class Function extends VBox {
    private static final String FXML = "/mugres/app/control/tracker/call/function.fxml";

    @FXML
    private Properties functionParametersEditor;

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
        functionParametersEditor.setReadOnly(true);
    }

    private void onFunctionChanged(final mugres.function.Function<?> function) {
        functionParametersEditor.setModel(Properties.Model.of(
                function.parameters().stream()
                        .map(fp -> Properties.PropertyModel.of(fp.name(), fp.label(), fp.order(), fp.dataType(),
                                fp.defaultValue()))
                        .collect(Collectors.toList())));
    }
}
