package mugres.app.control.tracker;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ArrangementEditor extends VBox {
    private static final String FXML = "/mugres/app/control/tracker/arrangement-editor.fxml";

    private Model model;

    @FXML
    private TableView entriesTableView;

    public ArrangementEditor() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(FXML));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (final IOException exception) {
            System.out.println(exception);
            throw new RuntimeException(exception);
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

    }

    public static class Model {
        private final List<ArrangementEntry> entries = new ArrayList<>();

        private Model(final List<ArrangementEntry> entries) {
            if (entries != null)
                this.entries.addAll(entries);
        }

        public static Model of() {
            return new Model(null);
        }

        public static Model of(final List<ArrangementEntry> entries) {
            return new Model(entries);
        }

        public List<ArrangementEntry> getEntries() {
            return entries;
        }

        public static class ArrangementEntry {
            private String pattern;
            private int repetitions;

            private ArrangementEntry(final String pattern, final int repetitions) {
                this.pattern = pattern;
                this.repetitions = repetitions;
            }

            public static ArrangementEntry of(final String pattern, final int repetitions) {
                return new ArrangementEntry(pattern, repetitions);
            }

            public String getPattern() {
                return pattern;
            }

            public void setPattern(String pattern) {
                this.pattern = pattern;
            }

            public int getRepetitions() {
                return repetitions;
            }

            public void setRepetitions(int repetitions) {
                this.repetitions = repetitions;
            }
        }
    }
}
