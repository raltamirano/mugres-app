package mugres.app.control.tracker.call;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import mugres.app.control.Properties;
import mugres.function.Call;
import mugres.tracker.Event;

import java.io.IOException;
import java.util.List;

public class Generic extends VBox implements FunctionControl {
    private static final String FXML = "/mugres/app/control/tracker/call/generic.fxml";

    @FXML
    private Properties functionParameters;

    public Generic() {
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
        functionParameters.setTitleVisible(false);
        functionParameters.setReadOnly(true);
    }

    @Override
    public void setCall(final Call<List<Event>> call) {
        if (call == null) {
            functionParameters.setModel(Properties.Model.EMPTY);
        } else {
            functionParameters.setModel(Properties.Model.of(call));
        }
    }
}
