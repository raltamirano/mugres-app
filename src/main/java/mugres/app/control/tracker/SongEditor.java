package mugres.app.control.tracker;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import mugres.app.control.KeyValueEditor;
import mugres.common.DataType;
import mugres.common.Key;
import mugres.common.TimeSignature;

import java.io.IOException;

public class SongEditor extends BorderPane {
    private static final String FXML = "/mugres/app/tracker/song-editor.fxml";
    private static final Object MIN_TEMPO = 1;
    private static final Object MAX_TEMPO = 10000;

    @FXML
    private KeyValueEditor songPropertiesEditor;

    KeyValueEditor.Model songPropertiesModel = null;

    public SongEditor() {
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
        songPropertiesModel =
                KeyValueEditor.Model.of(
                        KeyValueEditor.Property.of("name", "Name", DataType.TEXT,
                                "Untitled"),
                        KeyValueEditor.Property.of("tempo", "BPM", DataType.INTEGER,
                                120, MIN_TEMPO, MAX_TEMPO),
                        KeyValueEditor.Property.of("key", "Key", DataType.KEY,
                                Key.C),
                        KeyValueEditor.Property.of("timeSignature", "Time Sig.", DataType.TIME_SIGNATURE,
                                TimeSignature.TS44)
                );

        songPropertiesEditor.setTitle("Song");
        songPropertiesEditor.setModel(songPropertiesModel);
    }
}
