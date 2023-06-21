package mugres.app.control.tracker;

import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import mugres.app.control.tracker.Song.Model;
import mugres.common.Instrument;
import mugres.tracker.Track;

import java.io.IOException;

import static mugres.common.MIDI.DEFAULT_CHANNEL;
import static mugres.tracker.Track.MIN_BEAT_SUBDIVISION;

public class Tracks extends VBox {
    private static final String FXML = "/mugres/app/control/tracker/tracks.fxml";

    private Model model;

    @FXML
    private TableView<Track> tracksTableView;
    @FXML
    private TableColumn<Track, String> nameColumn;
    @FXML
    private TableColumn<Track, Instrument> instrumentColumn;
    @FXML
    private TableColumn<Track, Integer> channelColumn;
    @FXML
    private TableColumn<Track, Integer> subDivisionColumn;
    @FXML
    private ComboBox<Instrument> instrumentComboBox;

    public Tracks() {
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
        nameColumn.setCellValueFactory(item ->
                new SimpleObjectProperty(item.getValue() != null ? item.getValue().name() : null));
        instrumentColumn.setCellValueFactory(item ->
                new SimpleObjectProperty(item.getValue() != null ? item.getValue().instrument() : null));
        channelColumn.setCellValueFactory(item ->
                new SimpleObjectProperty(item.getValue() != null ? item.getValue().channel() : DEFAULT_CHANNEL));
        subDivisionColumn.setCellValueFactory(item ->
                new SimpleObjectProperty(item.getValue() != null ? item.getValue().beatSubdivision() : MIN_BEAT_SUBDIVISION));
    }

    @FXML
    public void createTrack(final ActionEvent event) {
        model.getSong().createTrack(instrumentComboBox.getSelectionModel().getSelectedItem());
    }

    @FXML
    public void deleteTrack(final ActionEvent event) {
        final Track selectedItem = tracksTableView.getSelectionModel().getSelectedItem();
        if (selectedItem == null)
            return;

        model.getSong().removeTrack(selectedItem);
    }

    public Model getModel() {
        return model;
    }

    public void setModel(final Model model) {
        this.model = model;
        loadModel();
    }

    private void loadModel() {
        tracksTableView.setItems(model.tracks());
        instrumentComboBox.setItems(model.instruments());
        instrumentComboBox.setValue(model.instruments().stream().findFirst().orElse(null));
    }
}
