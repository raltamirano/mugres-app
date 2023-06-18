package mugres.app.control.tracker;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import mugres.app.control.Properties;
import mugres.app.control.tracker.Song.Model;

import java.io.IOException;
import java.util.stream.Collectors;

public class Pattern extends VBox {
    private static final String FXML = "/mugres/app/control/tracker/pattern.fxml";

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
                return pattern.name();
            }
            @Override
            public mugres.tracker.Pattern fromString(String string) {
                return null;
            }
        });
    }

    public Model getModel() {
        return model;
    }

    public void setModel(final Model model) {
        this.model = model;
        loadModel();
    }

    private void loadModel() {
        patternSelectorComboBox.setItems(FXCollections.observableList(model.getSong().patterns().stream().collect(Collectors.toList())));
        patternSelectorComboBox.setValue(model.getCurrentPattern());
        patternPropertiesEditor.setModel(model.getPatternPropertiesModel());
    }
}
