package mugres.app.control.tracker;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import mugres.app.control.Properties;
import mugres.common.DataType;
import mugres.common.Key;
import mugres.common.TimeSignature;

import java.io.IOException;

import static java.util.Arrays.asList;

public class Song extends BorderPane {
    private static final String FXML = "/mugres/app/control/tracker/song.fxml";
    private static final Object MIN_TEMPO = 1;
    private static final Object MAX_TEMPO = 10000;

    private Model model = new Model();

    @FXML
    private Properties songPropertiesEditor;

    @FXML
    private Patterns patterns;

    @FXML
    private Arrangement arrangement;

    public Song() {
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
                Properties.Model.of(
                        Properties.Property.of("name", "Name", DataType.TEXT,
                                "Untitled"),
                        Properties.Property.of("tempo", "BPM", DataType.INTEGER,
                                120, MIN_TEMPO, MAX_TEMPO),
                        Properties.Property.of("key", "Key", DataType.KEY,
                                Key.C),
                        Properties.Property.of("timeSignature", "Time Sig.", DataType.TIME_SIGNATURE,
                                TimeSignature.TS44)
                );
        model.patternsModel =
                Patterns.Model.of(
                    Properties.Model.of(
                            Properties.Property.of("pattern", "Pattern", DataType.UNKNOWN,
                                    null, asList("A", "B", "C")),
                            Properties.Property.of("tempo", "BPM", DataType.INTEGER,
                                    120, MIN_TEMPO, MAX_TEMPO),
                            Properties.Property.of("key", "Key", DataType.KEY,
                                    Key.C),
                            Properties.Property.of("timeSignature", "Time Sig.", DataType.TIME_SIGNATURE,
                                    TimeSignature.TS44),
                            Properties.Property.of("length", "Measures", DataType.INTEGER,
                                    1, 1, 100000),
                            Properties.Property.of("beastSubdivision", "Beat subdivision", DataType.INTEGER,
                                    0, 0, 128)
                    )
                );
        model.arrangementModel =
                Arrangement.Model.of(

                );

        songPropertiesEditor.setModel(model.songPropertiesModel);
        patterns.setModel(model.patternsModel);
        arrangement.setModel(model.arrangementModel);
    }

    public static class Model {
        private Properties.Model songPropertiesModel = null;
        private Patterns.Model patternsModel = null;
        private Arrangement.Model arrangementModel = null;
    }
}
