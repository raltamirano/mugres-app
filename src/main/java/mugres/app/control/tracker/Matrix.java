package mugres.app.control.tracker;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import mugres.tracker.Track;
import mugres.function.Function;

import java.io.IOException;

public class Matrix extends ScrollPane {
    private static final String FXML = "/mugres/app/control/tracker/matrix.fxml";

    private Song.Model model;

    @FXML
    private GridPane matrix;

    @FXML
    private ComboBox<Track> trackComboBox;

    @FXML
    private ComboBox<Function.EventsFunction> functionComboBox;

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
        trackComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(final Track Track) {
                return Track != null ? Track.name() : "";
            }

            @Override
            public Track fromString(final String string) {
                return null;
            }
        });
    }

    public Song.Model getModel() {
        return model;
    }

    public void setModel(final Song.Model model) {
        this.model = model;
        loadModel();
    }


    @FXML
    protected void setCall(final ActionEvent event) {
        final Track currentTrack = model.getCurrentTrack();
        if (currentTrack == null)
            return;
    }

    @FXML
    protected void clearCall(final ActionEvent event) {
        final Track currentTrack = model.getCurrentTrack();
        if (currentTrack == null)
            return;
    }

    private void loadModel() {
        trackComboBox.setItems(model.tracks());
        functionComboBox.setItems(model.eventsFunctions());
    }
}
