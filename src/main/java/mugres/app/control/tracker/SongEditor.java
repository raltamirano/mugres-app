package mugres.app.control.tracker;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import mugres.app.control.kveditor.KeyValueEditor;
import mugres.common.DataType;
import mugres.common.Key;
import mugres.common.TimeSignature;

import java.io.IOException;

import static java.util.Arrays.asList;

public class SongEditor extends BorderPane {
    private static final String FXML = "/mugres/app/control/tracker/song-editor.fxml";
    private static final Object MIN_TEMPO = 1;
    private static final Object MAX_TEMPO = 10000;

    private Model model = new Model();

    @FXML
    private KeyValueEditor songPropertiesEditor;

    @FXML
    private KeyValueEditor patternPropertiesEditor;

    @FXML
    private ArrangementEditor arrangementEditor;

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
        model.songPropertiesModel =
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
        model.patternPropertiesModel =
                KeyValueEditor.Model.of(
                        KeyValueEditor.Property.of("pattern", "Pattern", DataType.UNKNOWN,
                                null, asList("A", "B", "C")),
                        KeyValueEditor.Property.of("tempo", "BPM", DataType.INTEGER,
                                120, MIN_TEMPO, MAX_TEMPO),
                        KeyValueEditor.Property.of("key", "Key", DataType.KEY,
                                Key.C),
                        KeyValueEditor.Property.of("timeSignature", "Time Sig.", DataType.TIME_SIGNATURE,
                                TimeSignature.TS44),
                        KeyValueEditor.Property.of("length", "Measures", DataType.INTEGER,
                                1, 1, 100000),
                        KeyValueEditor.Property.of("beastSubdivision", "Beat subdivision", DataType.INTEGER,
                                0, 0, 128)
                );

        model.arrangementModel =
                ArrangementEditor.Model.of(

                );

        arrangementEditor.setModel(model.arrangementModel);
        songPropertiesEditor.setModel(model.songPropertiesModel);
        patternPropertiesEditor.setModel(model.patternPropertiesModel);
    }

    public static class Model {
        private int currentPattern = -1;
        private KeyValueEditor.Model songPropertiesModel = null;
        private KeyValueEditor.Model patternPropertiesModel = null;
        private ArrangementEditor.Model arrangementModel = null;
    }
}
