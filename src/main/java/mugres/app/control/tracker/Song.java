package mugres.app.control.tracker;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.adapter.JavaBeanIntegerPropertyBuilder;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import mugres.app.control.Properties;
import mugres.common.Context;
import mugres.common.Instrument;
import mugres.tracker.Track;
import mugres.function.Function;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static mugres.app.control.tracker.Pattern.DEFAULT_PATTERN_MEASURES;
import static mugres.common.Context.PATTERN_LENGTH;

public class Song extends BorderPane {
    private static final String FXML = "/mugres/app/control/tracker/song.fxml";

    private Model model;

    @FXML
    private Properties properties;

    @FXML
    private Pattern pattern;

    @FXML
    private Arrangement arrangement;

    @FXML
    private Tracks tracks;

    @FXML
    private CheckBox loopPattern;

    @FXML
    private Matrix matrix;

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
        model.getSong().play();
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
        tracks.setModel(model);
        matrix.setModel(model);
    }

    public static class Model {
        private final mugres.tracker.Song song;
        private final Properties.Model songPropertiesModel;
        private final ObservableList<mugres.tracker.Pattern> patterns;
        private final ObjectProperty<Properties.Model> patternPropertiesModel;
        private final ObjectProperty<mugres.tracker.Pattern> currentPattern;
        private final ObservableList<ArrangementEntryModel> arrangementEntryModels;
        private final ObjectProperty<Track> currentTrack;
        private final ObservableList<Function.EventsFunction> eventsFunctions;
        private final ObservableList<Instrument> instruments;
        private final ObservableList<Track> tracks;

        private Model(final mugres.tracker.Song song) {
            this.song = song;

            this.songPropertiesModel = Properties.Model.of(song);
            this.patterns = FXCollections.observableList(song.patterns().stream().sorted().collect(Collectors.toList()));
            this.currentPattern = new SimpleObjectProperty<>();
            this.currentPattern.addListener(createCurrentPatternChangeListener());
            this.patternPropertiesModel = new SimpleObjectProperty<>();
            if (!patterns().isEmpty()) setCurrentPattern(patterns().get(0));
            this.arrangementEntryModels = FXCollections.observableList(mapArrangementEntries());
            this.currentTrack = new SimpleObjectProperty<>();
            this.eventsFunctions = FXCollections.unmodifiableObservableList(FXCollections.observableList(
                    mugres.function.Function.allEventsFunctions().stream().collect(Collectors.toList())).sorted());
            this.instruments = FXCollections.unmodifiableObservableList(FXCollections.observableList(
                    asList(mugres.common.Instrument.values())).sorted());
            this.tracks = FXCollections.observableList(song.tracks().stream().collect(Collectors.toList()));
            this.song.addPropertyChangeListener(createSongPropertyChangeListener());
        }

        private ChangeListener<mugres.tracker.Pattern> createCurrentPatternChangeListener() {
            return (source, oldValue, newValue) -> {
                if (newValue != null) {
                    patternPropertiesModel.setValue(Properties.Model.of(newValue));
                    newValue.addPropertyChangeListener(this::onPatternPropertyChangeListener);
                } else {
                    patternPropertiesModel.setValue(null);
                }

            };
        }

        public static Model forSong(final mugres.tracker.Song song) {
            if (song == null)
                throw new IllegalArgumentException("song");
            return new Model(song);
        }

        public static Model forNewSong() {
            final mugres.tracker.Song song = mugres.tracker.Song.of("Masterpiece", Context.basicContext());
            final mugres.tracker.Pattern pattern = song.createPattern(DEFAULT_PATTERN_MEASURES);
            song.arrangement().append(pattern, 1);
            return new Model(song);
        }

        public mugres.tracker.Song getSong() {
            return song;
        }

        public ObservableList<mugres.tracker.Pattern> patterns() {
            return patterns;
        }

        public Properties.Model getSongPropertiesModel() {
            return songPropertiesModel;
        }

        public Properties.Model getPatternPropertiesModel() {
            return patternPropertiesModel.get();
        }

        public ObjectProperty<Properties.Model> patternPropertiesModelProperty() {
            return patternPropertiesModel;
        }

        public mugres.tracker.Pattern getCurrentPattern() {
            return currentPattern.get();
        }

        public ObjectProperty<mugres.tracker.Pattern> currentPatternProperty() {
            return currentPattern;
        }

        public void setCurrentPattern(final mugres.tracker.Pattern currentPattern) {
            this.currentPattern.set(currentPattern);
        }

        public Track getCurrentTrack() {
            return currentTrack.get();
        }

        public ObjectProperty<Track> currentTrackProperty() {
            return currentTrack;
        }

        public void setCurrentTrack(Track currentTrack) {
            this.currentTrack.set(currentTrack);
        }

        public ObservableList<ArrangementEntryModel> getArrangementEntryModels() {
            return arrangementEntryModels;
        }

        public ObservableList<Function.EventsFunction> eventsFunctions() {
            return eventsFunctions;
        }

        public ObservableList<Instrument> instruments() {
            return instruments;
        }

        public ObservableList<Track> tracks() {
            return tracks;
        }

        private List<ArrangementEntryModel> mapArrangementEntries() {
            return song.arrangement().entries().stream()
                    .map(e -> ArrangementEntryModel.of(e))
                    .collect(Collectors.toList());
        }

        private PropertyChangeListener createSongPropertyChangeListener() {
            return e -> {
                switch (e.getPropertyName()) {
                    case mugres.tracker.Song.PATTERNS:
                        patterns.setAll(song.patterns().stream().sorted().collect(Collectors.toList()));
                        final mugres.tracker.Pattern theCurrentPattern = getCurrentPattern();
                        if (patterns.contains(theCurrentPattern))
                            setCurrentPattern(theCurrentPattern);
                        else
                            setCurrentPattern(patterns().stream().findFirst().orElse(null));
                        break;
                    case mugres.tracker.Song.TRACKS:
                        tracks.setAll(song.tracks().stream().sorted().collect(Collectors.toList()));
                        final Track theCurrentTrack = getCurrentTrack();
                        if (tracks.contains(theCurrentTrack))
                            setCurrentTrack(theCurrentTrack);
                        else
                            setCurrentTrack(tracks.stream().findFirst().orElse(null));
                        break;
                    case mugres.tracker.Song.ARRANGEMENT:
                        arrangementEntryModels.setAll(mapArrangementEntries());
                        break;
                }
            };
        }

        private void onPatternPropertyChangeListener(final PropertyChangeEvent propertyChangeEvent) {
            switch (propertyChangeEvent.getPropertyName()) {
                case PATTERN_LENGTH:
                    final int newLengthInMeasures = (int) propertyChangeEvent.getNewValue();
                    getCurrentPattern().matrix().values()
                            .forEach(l -> l.forEach(c ->
                                    c.parameterValue(Function.LENGTH_PARAMETER.name(), newLengthInMeasures)));
                    break;
                default:
                    // do nothing
                    break;
            }
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
