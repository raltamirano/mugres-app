package mugres.app.control.tracker.call;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import mugres.tracker.Event;
import mugres.tracker.Pattern;
import mugres.tracker.Track;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static mugres.function.Function.LENGTH_PARAMETER;

public class Call extends VBox {
    private static final String FXML = "/mugres/app/control/tracker/call/call.fxml";

    private Model model;

    private FunctionControl functionControl;

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
        functionComboBox.maxWidthProperty().bind(this.maxWidthProperty());
        functionComboBox.minWidthProperty().bind(this.minWidthProperty());
        functionComboBox.prefWidthProperty().bind(this.prefWidthProperty());
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
        if (call != null) {
            functionComboBox.getSelectionModel().select(call.getFunction());
            if (functionControl != null)
                functionControl.setCall(call);
        }
        else
            functionComboBox.getSelectionModel().clearSelection();
    }

    private void onFunctionChanged(final mugres.function.Function<?> function) {
        removeCurrentFunctionControl();
        final Pattern pattern = getModel().pattern();
        functionControl = createFunctionControl(function);
        final mugres.function.Call<List<Event>> call = mugres.function.Call.of(function,
                Map.of(LENGTH_PARAMETER.name(), pattern.measures()));
        pattern.matrix(getModel().track(), call);
        functionControl.setCall(call);
    }

    private void removeCurrentFunctionControl() {
        if (functionControl != null)
            getChildren().remove(functionControl);
        functionControl = null;
    }

    private FunctionControl createFunctionControl(final mugres.function.Function<?> function) {
        if (function instanceof mugres.function.builtin.literal.Literal)
            return new mugres.app.control.tracker.call.Literal();

        // If no ad-hoc control for the given function, handle with the
        // generic function control
        return new mugres.app.control.tracker.call.Generic();
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
