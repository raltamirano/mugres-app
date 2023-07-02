package mugres.app.control.tracker;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import mugres.app.control.misc.ButtonCell;
import mugres.common.TimeSignature;
import mugres.function.Call;
import mugres.function.builtin.literal.Literal;
import mugres.tracker.Event;
import mugres.tracker.Pattern;
import mugres.tracker.Track;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static mugres.utils.Maths.lcm;

public class Matrix extends ScrollPane {
    private static final String FXML = "/mugres/app/control/tracker/matrix.fxml";
    private static final double ROW_HEIGHT = 40.0;

    private final ObjectProperty<Song.Model> model;
    private final ObservableList<RowModel> items = FXCollections.observableList(new ArrayList<>());
    @FXML
    private TableView<RowModel> matrix;

    public Matrix() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(FXML));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }

        model = new SimpleObjectProperty<>();
        model.addListener((source, oldValue, newValue) -> loadModel());
    }

    @FXML
    public void initialize() {

    }

    public Song.Model getModel() {
        return model.get();
    }

    public ObjectProperty<Song.Model> modelProperty() {
        return model;
    }

    public void setModel(final Song.Model model) {
        this.model.set(model);
    }

    private void loadModel() {
        getModel().tracks().addListener((ListChangeListener<? super Track>) c -> doDefineTracks());
        getModel().currentPatternProperty().addListener(c -> doDefineTracks());
        doDefineTracks();
    }

    private void doDefineTracks() {
        matrix.getColumns().clear();
        matrix.getColumns().add(createFixedColumn());
        matrix.getColumns().add(createChordEventColumn());

        for(final Track track : getModel().tracks())
            matrix.getColumns().add(createTrackColumn(track));

        refreshItems();
        matrix.setItems(items);
        matrix.setPrefHeight(ROW_HEIGHT * items.size());
    }

    private void refreshItems() {
        items.clear();

        final Pattern pattern = getModel().getCurrentPattern();
        final TimeSignature ts = pattern.context().timeSignature();
        final int totalRows = pattern.measures() * ts.numerator() * lcmLiteralsSubdivisions();

        for(int row = 0; row < totalRows; row++) {
            final Map<String, Call> trackCalls = new HashMap<>();
            if (row == 0) {
                for(final Track track : getModel().tracks()) {
                    final Call<List<Event>> call = pattern.matrix(track);
                    if (call!= null)
                        trackCalls.put(track.name(), call);
                }
            }
            items.add(new RowModel(row + 1, trackCalls));
        }
    }

    private TableColumn<RowModel, String> createFixedColumn() {
        final TableColumn<RowModel, String> rowNumberColumn = new TableColumn<>();
        rowNumberColumn.setText("#");
        rowNumberColumn.setMinWidth(40.0);
        rowNumberColumn.setMaxWidth(40.0);
        rowNumberColumn.setPrefWidth(40.0);

        rowNumberColumn.setCellValueFactory(item -> new SimpleStringProperty(
                String.format("%04d", item.getValue().row())));

        return rowNumberColumn;
    }

    private TableColumn<RowModel, Void> createChordEventColumn() {
        final TableColumn<RowModel, Void> chordEventColumn = new TableColumn<>();
        chordEventColumn.setText("Chords");
        chordEventColumn.setMinWidth(60.0);
        chordEventColumn.setMaxWidth(60.0);
        chordEventColumn.setPrefWidth(60.0);

        chordEventColumn.setCellFactory(item -> new ButtonCell<>("...",
                e -> System.out.println(e.row())));

        return chordEventColumn;
    }

    private TableColumn<RowModel, Object> createTrackColumn(final Track track) {
        final TableColumn<RowModel, Object> column = new TableColumn<>();
        column.setText(track.name());
        column.setMinWidth(200.0);
        column.setMaxWidth(200.0);
        column.setPrefWidth(200.0);
        return column;
    }

    private int lcmLiteralsSubdivisions() {
        final int[] allSubdivisions = getModel().getCurrentPattern().matrix().values()
                .stream()
                .flatMap(List::stream)
                .filter(e -> e.getFunction() instanceof Literal)
                .mapToInt(e -> (int)e.parameterValue("beatSubdivision"))
                .toArray();
        return lcm(allSubdivisions);
    }

    private static class RowModel {
        private final int row;
        private final Map<String, Call> trackCalls;

        public RowModel(final int row, final Map<String, Call> trackCalls) {
            this.row = row;
            this.trackCalls = trackCalls;
        }

        public int row() {
            return row;
        }

        public Map<String, Call> trackCalls() {
            return trackCalls;
        }
    }
}
