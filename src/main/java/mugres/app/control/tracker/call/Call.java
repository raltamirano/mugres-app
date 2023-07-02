package mugres.app.control.tracker.call;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import mugres.app.control.Properties;
import mugres.tracker.Event;
import mugres.tracker.Pattern;
import mugres.tracker.Track;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class Call extends VBox {
    private static final String FXML = "/mugres/app/control/tracker/call/call.fxml";

    private Model model;

    @FXML
    private ComboBox<mugres.function.Function> functionComboBox;

    public Call() {
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
        functionComboBox.setItems(FXCollections.observableArrayList(mugres.function.Function.allFunctions()).sorted());
        functionComboBox.valueProperty().addListener((observable, oldValue, newValue) -> onFunctionChanged(newValue));
    }

    public Model getModel() {
        return model;
    }

    public void setModel(final Model model) {
        if (model == null)
            throw new IllegalArgumentException("model");

        this.model = model;
        loadModel();
    }

    private void loadModel() {
        final mugres.function.Call<List<Event>> call = model.pattern().matrix(model.track());
        if (call != null)
            functionComboBox.getSelectionModel().select(call.getFunction());
        else
            functionComboBox.getSelectionModel().clearSelection();
    }

    private void onFunctionChanged(final mugres.function.Function<?> function) {

    }

    public static class Model {
        private final Pattern pattern;
        private final Track track;

        private Model(final Pattern pattern, final Track track) {
            if (pattern == null)
                throw new IllegalArgumentException("pattern");
            if (track == null)
                throw new IllegalArgumentException("track");

            this.pattern = pattern;
            this.track = track;
        }

        public static Model of(final Pattern pattern, final Track track) {
            return new Model(pattern, track);
        }

        public Pattern pattern() {
            return pattern;
        }

        public Track track() {
            return track;
        }
    }
}
