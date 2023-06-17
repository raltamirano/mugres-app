package mugres.app.control.tracker;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

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
        private final ObservableList<ArrangementEntry> entries;

        private Model(final List<ArrangementEntry> entries) {
            this.entries = FXCollections.observableList(entries != null ? entries : Collections.emptyList());
        }

        public static Model of() {
            return new Model(null);
        }

        public static Model of(final List<ArrangementEntry> entries) {
            return new Model(entries);
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
            private SimpleStringProperty pattern;
            private SimpleIntegerProperty repetitions;

            private ArrangementEntry(final String pattern, final int repetitions) {
                this.pattern = new SimpleStringProperty(pattern);
                this.repetitions = new SimpleIntegerProperty(repetitions);
            }

            public static ArrangementEntry of(final String pattern, final int repetitions) {
                return new ArrangementEntry(pattern, repetitions);
            }

            public String getPattern() {
                return pattern.get();
            }

            public SimpleStringProperty patternProperty() {
                return pattern;
            }

            public void setPattern(String pattern) {
                this.pattern.set(pattern);
            }

            public int getRepetitions() {
                return repetitions.get();
            }

            public SimpleIntegerProperty repetitionsProperty() {
                return repetitions;
            }

            public void setRepetitions(int repetitions) {
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
