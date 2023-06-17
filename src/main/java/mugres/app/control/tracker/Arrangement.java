package mugres.app.control.tracker;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.adapter.JavaBeanIntegerPropertyBuilder;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import mugres.tracker.Pattern;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class Arrangement extends VBox {
    private static final String FXML = "/mugres/app/control/tracker/arrangement.fxml";

    private Model model;

    @FXML
    private TableView<Model.ArrangementEntry> entriesTableView;
    @FXML
    private TableColumn<Model.ArrangementEntry, String> patternColumn;
    @FXML
    private TableColumn<Model.ArrangementEntry, Number> repetitionsColumn;

    public Arrangement() {
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
        patternColumn.setCellValueFactory(item -> item.getValue().patternProperty());
        repetitionsColumn.setCellValueFactory(item -> item.getValue().repetitionsProperty());
    }

    @FXML
    public void onKeyPressedTableView(final KeyEvent keyEvent) {
        final Model.ArrangementEntry selectedArrangementEntry = entriesTableView.getSelectionModel().getSelectedItem();
        if (keyEvent.getCode() == KeyCode.ADD) {
            if (selectedArrangementEntry != null)
                selectedArrangementEntry.setRepetitions(selectedArrangementEntry.getRepetitions() + 1);
        } else if (keyEvent.getCode() == KeyCode.SUBTRACT) {
            if (selectedArrangementEntry != null && selectedArrangementEntry.getRepetitions() > 1)
                selectedArrangementEntry.setRepetitions(selectedArrangementEntry.getRepetitions() - 1);
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
        entriesTableView.setItems(model.getEntries());
    }

    public static class Model {
        private final mugres.tracker.Song song;
        private final ObservableList<ArrangementEntry> entries;

        private Model(final mugres.tracker.Song song, final List<ArrangementEntry> entries) {
            this.song = song;
            this.entries = FXCollections.observableList(entries != null ? entries : Collections.emptyList());
        }

        public static Model of(final mugres.tracker.Song song) {
            return new Model(song, null);
        }

        public static Model of(final mugres.tracker.Song song, final List<ArrangementEntry> entries) {
            return new Model(song, entries);
        }

        public mugres.tracker.Song getSong() {
            return song;
        }

        public ObservableList<ArrangementEntry> getEntries() {
            return entries;
        }

        @Override
        public String toString() {
            return "Model{" +
                    "entries=" + entries +
                    '}';
        }

        public static class ArrangementEntry {
            private final mugres.tracker.Arrangement.Entry entry;
            private ObjectProperty<Pattern> pattern;
            private IntegerProperty repetitions;

            private ArrangementEntry(final mugres.tracker.Arrangement.Entry entry) {
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

            public static ArrangementEntry of(final mugres.tracker.Arrangement.Entry entry) {
                return new ArrangementEntry(entry);
            }

            public final Pattern getPattern() {
                return pattern.get();
            }

            public ObjectProperty patternProperty() {
                return pattern;
            }

            public final void setPattern(Pattern pattern) {
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
                return "ArrangementEntry{" +
                        "pattern=" + pattern +
                        ", repetitions=" + repetitions +
                        '}';
            }
        }
    }
}
