package mugres.app.control.tracker;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import mugres.app.control.Properties;

import java.io.IOException;

public class Patterns extends VBox {
    private static final String FXML = "/mugres/app/control/tracker/patterns.fxml";

    private Model model;

    @FXML
    private Properties patternPropertiesEditor;

    public Patterns() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(FXML));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public Model getModel() {
        return model;
    }

    public void setModel(final Model model) {
        this.model = model;
        loadModel();
    }

    private void loadModel() {
        patternPropertiesEditor.setModel(model.getPatternPropertiesModel());
    }

    public static class Model {
        private final Properties.Model patternPropertiesModel;

        private Model(final Properties.Model patternPropertiesModel) {
            this.patternPropertiesModel = patternPropertiesModel;
        }

        public static Model of(final Properties.Model patternProperties) {
            return new Model(patternProperties);
        }

        public Properties.Model getPatternPropertiesModel() {
            return patternPropertiesModel;
        }
    }
}
