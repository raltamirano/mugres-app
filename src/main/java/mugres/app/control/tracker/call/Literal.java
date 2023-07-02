package mugres.app.control.tracker.call;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import mugres.function.Call;
import mugres.tracker.Event;

import java.io.IOException;
import java.util.List;

public class Literal extends VBox implements FunctionControl {
    private static final String FXML = "/mugres/app/control/tracker/call/literal.fxml";

    public Literal() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(FXML));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void setCall(Call<List<Event>> call) {
        throw new RuntimeException("Not implemented!");
    }
}
