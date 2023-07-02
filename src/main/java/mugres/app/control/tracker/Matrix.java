package mugres.app.control.tracker;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

import static javafx.geometry.Pos.CENTER;
import static mugres.utils.Maths.lcm;

public class Matrix extends ScrollPane {
    private static final String FXML = "/mugres/app/control/tracker/matrix.fxml";
    private static final double ROW_HEIGHT = 40.0;

    private final ObjectProperty<Song.Model> model;
    private final ObservableList<RowModel> items = FXCollections.observableList(new ArrayList<>());
    @FXML
    private HBox tracks;

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
        refreshItems();

        tracks.getChildren().clear();
        tracks.getChildren().add(createRowNumberColumn());
        tracks.getChildren().add(createChordEventColumn());

        for(final Track track : getModel().tracks())
            tracks.getChildren().add(createTrackColumn(track));
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

    private Node createRowNumberColumn() {
        final VBox rowNumberColumn = new VBox();
        final double width = 32.0;
        rowNumberColumn.setPrefWidth(width);
        rowNumberColumn.setMaxWidth(width);
        rowNumberColumn.setPrefWidth(width);

        rowNumberColumn.getChildren().add(createHeader("#", width));
        rowNumberColumn.getChildren().add(spacer());

        items.forEach(item -> {
            final TextField text = new TextField(String.format("%04d", item.row()));
            text.setDisable(true);
            text.setPadding(new Insets(3.0, 0.0, 3.0, 1.0));
            text.minWidth(width);
            text.maxWidth(width);
            text.prefWidth(width);
            text.minHeight(rowHeight());
            text.maxHeight(rowHeight());
            text.prefHeight(rowHeight());
            rowNumberColumn.getChildren().add(text);
        });

        return rowNumberColumn;
    }

    private Node createChordEventColumn() {
        final VBox chordEventColumn = new VBox();
        final double width = 60.0;
        chordEventColumn.setMinWidth(width);
        chordEventColumn.setMaxWidth(width);
        chordEventColumn.setPrefWidth(width);

        chordEventColumn.getChildren().add(createHeader("Chords", width));
        chordEventColumn.getChildren().add(spacer());

        items.forEach(item -> {
            final Button button = new Button("...");
            button.setMinWidth(width);
            button.setMaxWidth(width);
            button.setPrefWidth(width);
            button.setMinHeight(rowHeight());
            button.setMaxHeight(rowHeight());
            button.setPrefHeight(rowHeight());
            button.setUserData(item);
            button.setOnAction(this::onChordEventAction);
            chordEventColumn.getChildren().add(button);
        });

        return chordEventColumn;
    }

    private Node spacer() {
        final ComboBox comboBox = new ComboBox();
        comboBox.setVisible(false);
        comboBox.setManaged(true);
        comboBox.setMinWidth(1.0);
        comboBox.setMaxWidth(1.0);
        comboBox.setPrefWidth(1.0);
        return comboBox;
    }


    private Node createTrackColumn(final Track track) {
        final VBox trackColumn = new VBox();
        final double width = 200.0;

        trackColumn.getChildren().add(createHeader(track.name(), width));

        final mugres.app.control.tracker.call.Call callEditor = new mugres.app.control.tracker.call.Call();
        final double editorHeight = rowHeight() * items.size();

        callEditor.setMinWidth(width);
        callEditor.setMaxWidth(width);
        callEditor.setPrefWidth(width);
        callEditor.setMinHeight(editorHeight);
        callEditor.setMaxHeight(editorHeight);
        callEditor.setPrefHeight(editorHeight);

        trackColumn.getChildren().add(callEditor);

        return trackColumn;
    }

    private TextField createHeader(final String title, final double width) {
        final TextField textField = new TextField(title);
        textField.setDisable(true);
        textField.setAlignment(CENTER);
        textField.minWidth(width);
        textField.maxWidth(width);
        textField.prefWidth(width);
        textField.minHeight(rowHeight());
        textField.maxHeight(rowHeight());
        textField.prefHeight(rowHeight());
        return textField;
    }

    private void onChordEventAction(final ActionEvent actionEvent) {
        final RowModel row = (RowModel) ((Node) actionEvent.getSource()).getUserData();
        System.out.println("ChordEvent for " + row.row());
    }

    private double rowHeight() {
        return 25.0;
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
