package mugres.app.control.tracker;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.adapter.JavaBeanIntegerPropertyBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
        System.out.println(model.getSong());
    }

    @FXML
    public void playPattern(final ActionEvent event) {

    }

    @FXML
    public void stopPlaying(final ActionEvent event) {

    }

    private void loadModel() {
        properties.setModel(model.getSongPropertiesModel());
        pattern.setModel(model);
        arrangement.setModel(model);
    }

    public static class Model {
        private final mugres.tracker.Song song;
        private final Properties.Model songPropertiesModel;
        private final Properties.Model patternPropertiesModel;
        private mugres.tracker.Pattern currentPattern;
        private final ObservableList<ArrangementEntryModel> arrangementEntryModels;
        private mugres.common.Party currentParty;

        private Model(final mugres.tracker.Song song) {
            this.song = song;

            this.arrangementEntryModels = FXCollections.observableList(song.arrangement().entries().stream()
                    .map(e -> ArrangementEntryModel.of(e))
                    .collect(Collectors.toList())
            );

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

            patternPropertiesModel =
                    Properties.Model.of(
                            Properties.Property.of("name", "Name", DataType.TEXT,
                                    currentPattern != null ? currentPattern.name() : "Untitled"),
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
                    );
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

        public Properties.Model getPatternPropertiesModel() {
            return patternPropertiesModel;
        }

        public mugres.tracker.Pattern getCurrentPattern() {
            return currentPattern;
        }

        public Party getCurrentParty() {
            return currentParty;
        }

        public ObservableList<ArrangementEntryModel> getArrangementEntryModels() {
            return arrangementEntryModels;
        }

        private int readBeatSubdivision(final mugres.tracker.Song song, final mugres.tracker.Pattern currentPattern) {
            final EditorMetadata editorMetadata = song.metadataAs(EditorMetadata.class);
            return editorMetadata != null && editorMetadata.getPatternBeatSubdivision() != null && editorMetadata.getPatternBeatSubdivision().containsKey(currentPattern.name()) ?
                    editorMetadata.getPatternBeatSubdivision().get(currentPattern.name()) : 0;
        }
    }

    public static class ArrangementEntryModel {
        private final mugres.tracker.Arrangement.Entry entry;
        private ObjectProperty<mugres.tracker.Pattern> pattern;
        private IntegerProperty repetitions;

        private ArrangementEntryModel(final mugres.tracker.Arrangement.Entry entry) {
            this.entry = entry;
            this.pattern = new SimpleObjectProperty(entry.pattern());
            try {
                this.repetitions = JavaBeanIntegerPropertyBuilder.create()
                        .bean(entry)
                        .name("repetitions")
                        .setter("repetitions")
                        .getter("repetitions")
                        .build();
            } catch (final NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        public static ArrangementEntryModel of(final mugres.tracker.Arrangement.Entry entry) {
            return new ArrangementEntryModel(entry);
        }

        public final mugres.tracker.Pattern getPattern() {
            return pattern.get();
        }

        public ObjectProperty patternProperty() {
            return pattern;
        }

        public final void setPattern(mugres.tracker.Pattern pattern) {
            this.pattern.set(pattern);
        }

        public final int getRepetitions() {
            return repetitions.get();
        }

        public IntegerProperty repetitionsProperty() {
            return repetitions;
        }

        public final void setRepetitions(int repetitions) {
            this.repetitions.set(repetitions);
        }

        @Override
        public String toString() {
            return "ArrangementEntryModel{" +
                    "pattern=" + pattern +
                    ", repetitions=" + repetitions +
                    '}';
        }
    }
}
