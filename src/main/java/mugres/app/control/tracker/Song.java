package mugres.app.control.tracker;

import javafx.beans.InvalidationListener;
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
import mugres.common.Party;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static mugres.app.control.tracker.Pattern.DEFAULT_PATTERN_MEASURES;

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
        private final ObservableList<mugres.tracker.Pattern> patterns;
        private final ObjectProperty<Properties.Model> patternPropertiesModel;
        private final ObjectProperty<mugres.tracker.Pattern> currentPattern;
        private final ObservableList<ArrangementEntryModel> arrangementEntryModels;
        private mugres.common.Party currentParty;

        private Model(final mugres.tracker.Song song) {
            this.song = song;

            this.patterns = FXCollections.observableList(song.patterns().stream().collect(Collectors.toList()));
            this.arrangementEntryModels = FXCollections.observableList(mapArrangementEntries());
            this.songPropertiesModel = Properties.Model.of(song);
            this.currentPattern = new SimpleObjectProperty<>();
            this.currentPattern.addListener(createCurrentPatternChangeListener());
            this.patternPropertiesModel = new SimpleObjectProperty<>();

            this.song.addPropertyChangeListener(createSongPropertyChangeListener());
        }

        private ChangeListener<mugres.tracker.Pattern> createCurrentPatternChangeListener() {
            return (source, oldValue, newValue) -> {
                patternPropertiesModel.setValue(newValue != null ? Properties.Model.of(newValue) : null);
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

        public Party getCurrentParty() {
            return currentParty;
        }

        public ObservableList<ArrangementEntryModel> getArrangementEntryModels() {
            return arrangementEntryModels;
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
                        patterns.setAll(song.patterns().stream().collect(Collectors.toList()));
                        final mugres.tracker.Pattern theCurrentPattern = getCurrentPattern();
                        if (patterns.contains(theCurrentPattern))
                            setCurrentPattern(theCurrentPattern);
                        else
                            setCurrentPattern(patterns().stream().findFirst().orElse(null));
                        break;
                    case mugres.tracker.Song.ARRANGEMENT:
                        arrangementEntryModels.setAll(mapArrangementEntries());
                        break;
                }
            };
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
