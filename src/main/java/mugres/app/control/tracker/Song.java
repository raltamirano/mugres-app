package mugres.app.control.tracker;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import mugres.app.control.Properties;
import mugres.app.control.tracker.storage.EditorMetadata;
import mugres.common.Context;
import mugres.common.DataType;
import mugres.common.Party;

import java.io.IOException;
import java.util.stream.Collectors;

public class Song extends BorderPane {
    private static final String FXML = "/mugres/app/control/tracker/song.fxml";
    private static final Object MIN_TEMPO = 1;
    private static final Object MAX_TEMPO = 10000;

    private Model model;

    @FXML
    private Properties properties;

    @FXML
    private Pattern pattern;

    @FXML
    private Arrangement arrangement;

    @FXML
    private CheckBox loopPattern;

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

    public Model getModel() {
        return model;
    }

    public void setModel(final Model model) {
        if (model == null)
            throw new IllegalArgumentException("model");
        this.model = model;

        loadModel();
    }

    @FXML
    public void playSong(final ActionEvent event) {

    }

    @FXML
    public void playPattern(final ActionEvent event) {

    }

    @FXML
    public void stopPlaying(final ActionEvent event) {

    }

    private void loadModel() {
        properties.setModel(model.getSongPropertiesModel());
        pattern.setModel(model.getPatternModel());
        arrangement.setModel(model.getArrangementModel());
    }

    public static class Model {
        private final mugres.tracker.Song song;

        private final Properties.Model songPropertiesModel;
        private final Pattern.Model patternModel;
        private final Arrangement.Model arrangementModel;
        private mugres.tracker.Pattern currentPattern;
        private mugres.common.Party currentParty;

        private Model(final mugres.tracker.Song song) {
            this.song = song;

            songPropertiesModel =
                    Properties.Model.of(
                            Properties.Property.of("name", "Name", DataType.TEXT,
                                    song.title()),
                            Properties.Property.of("tempo", "BPM", DataType.INTEGER,
                                    song.context().tempo(), MIN_TEMPO, MAX_TEMPO),
                            Properties.Property.of("key", "Key", DataType.KEY,
                                    song.context().key()),
                            Properties.Property.of("timeSignature", "Time Sig.", DataType.TIME_SIGNATURE,
                                    song.context().timeSignature())
                    );

            if (!song.patterns().isEmpty())
                currentPattern = song.patterns().iterator().next();

            patternModel =
                    Pattern.Model.of(
                            Properties.Model.of(
                                    Properties.Property.of("pattern", "Pattern", DataType.UNKNOWN,
                                            currentPattern, song.patterns().stream().collect(Collectors.toList())),
                                    Properties.Property.of("tempo", "BPM", DataType.INTEGER,
                                            currentPattern != null ? currentPattern.context().tempo() : song.context().tempo(),
                                            MIN_TEMPO, MAX_TEMPO),
                                    Properties.Property.of("key", "Key", DataType.KEY,
                                            currentPattern != null ? currentPattern.context().key() : song.context().key()),
                                    Properties.Property.of("timeSignature", "Time Sig.", DataType.TIME_SIGNATURE,
                                            currentPattern != null ? currentPattern.context().timeSignature() :  song.context().timeSignature()),
                                    Properties.Property.of("length", "Measures", DataType.INTEGER,
                                            currentPattern != null ? currentPattern.measures() : 0, 1, 100000),
                                    Properties.Property.of("beatSubdivision", "Beat subdivision", DataType.INTEGER,
                                            currentPattern != null ? readBeatSubdivision(song, currentPattern) : 0, 0, 128)
                            )
                    );

            arrangementModel = Arrangement.Model.of(song.arrangement().entries().stream()
                    .map(e -> Arrangement.Model.ArrangementEntry.of(e.pattern().name(), e.repetitions()))
                    .collect(Collectors.toList()));
        }

        public static Model forSong(final mugres.tracker.Song song) {
            if (song == null)
                throw new IllegalArgumentException("song");
            return new Model(song);
        }

        public static Model forNewSong() {
            final mugres.tracker.Song song = mugres.tracker.Song.of("Masterpiece", Context.basicContext());
            final mugres.tracker.Pattern pattern = song.createPattern("A", 4);
            song.arrangement().append(pattern, 1);
            return new Model(song);
        }

        public mugres.tracker.Song getSong() {
            return song;
        }

        public Properties.Model getSongPropertiesModel() {
            return songPropertiesModel;
        }

        public Pattern.Model getPatternModel() {
            return patternModel;
        }

        public Arrangement.Model getArrangementModel() {
            return arrangementModel;
        }

        public mugres.tracker.Pattern getCurrentPattern() {
            return currentPattern;
        }

        public Party getCurrentParty() {
            return currentParty;
        }

        private int readBeatSubdivision(final mugres.tracker.Song song, final mugres.tracker.Pattern currentPattern) {
            final EditorMetadata editorMetadata = song.metadataAs(EditorMetadata.class);
            return editorMetadata != null && editorMetadata.getPatternBeatSubdivision() != null && editorMetadata.getPatternBeatSubdivision().containsKey(currentPattern.name()) ?
                    editorMetadata.getPatternBeatSubdivision().get(currentPattern.name()) : 0;
        }
    }
}
