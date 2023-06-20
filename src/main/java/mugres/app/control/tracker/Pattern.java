package mugres.app.control.tracker;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import mugres.app.control.Properties;
import mugres.app.control.tracker.Song.Model;

import java.io.IOException;

public class Pattern extends VBox {
    private static final String FXML = "/mugres/app/control/tracker/pattern.fxml";
    public static final int DEFAULT_PATTERN_MEASURES = 4;

    private Model model;

    @FXML
    private ComboBox<mugres.tracker.Pattern> patternSelectorComboBox;

    @FXML
    private Properties patternPropertiesEditor;

    public Pattern() {
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
        patternPropertiesEditor.setTitleVisible(false);
        patternSelectorComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(mugres.tracker.Pattern pattern) {
                return pattern != null ? pattern.name() : null;
            }
            @Override
            public mugres.tracker.Pattern fromString(String string) {
                return null;
            }
        });
    }

    @FXML
    public void createPattern(final ActionEvent event) {
        final mugres.tracker.Pattern created = model.getSong().createPattern(DEFAULT_PATTERN_MEASURES);
        model.setCurrentPattern(created);
    }

    @FXML
    public void deletePattern(final ActionEvent event) {
        final mugres.tracker.Pattern currentPattern = model.getCurrentPattern();
        if (currentPattern != null)
            model.getSong().deletePattern(currentPattern.name());
    }

    public Model getModel() {
        return model;
    }

    public void setModel(final Model model) {
        this.model = model;
        loadModel();
    }

    private void loadModel() {
        patternSelectorComboBox.setItems(model.patterns());
        patternSelectorComboBox.valueProperty().bindBidirectional(model.currentPatternProperty());
        patternPropertiesEditor.modelProperty().bind(model.patternPropertiesModelProperty());
    }
}
