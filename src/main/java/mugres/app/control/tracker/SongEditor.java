package mugres.app.control.tracker;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import mugres.app.control.KeyValueEditor;
import mugres.common.DataType;
import mugres.common.Key;
import mugres.common.TimeSignature;

import java.io.IOException;

public class SongEditor extends BorderPane {
    @FXML
    private KeyValueEditor songPropertiesEditor;

    public SongEditor() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/mugres/app/tracker/song-editor.fxml"));
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
        final KeyValueEditor.Model songPropertiesModel =
                KeyValueEditor.Model.of(
                        KeyValueEditor.Property.of("name", "Name", DataType.TEXT,
                                "Untitled"),
                        KeyValueEditor.Property.of("tempo", "BPM", DataType.INTEGER,
                                120),
                        KeyValueEditor.Property.of("key", "Key", DataType.KEY,
                                Key.C),
                        KeyValueEditor.Property.of("timeSignature", "Time Sig.", DataType.TIME_SIGNATURE,
                                TimeSignature.TS44)
                );

        songPropertiesEditor.setTitle("Song");
        songPropertiesEditor.setModel(songPropertiesModel);
    }
}
