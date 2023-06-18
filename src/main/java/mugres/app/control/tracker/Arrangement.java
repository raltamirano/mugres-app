package mugres.app.control.tracker;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import mugres.app.control.tracker.Song.ArrangementEntryModel;
import mugres.app.control.tracker.Song.SongModel;

import java.io.IOException;

public class Arrangement extends VBox {
    private static final String FXML = "/mugres/app/control/tracker/arrangement.fxml";

    private SongModel model;

    @FXML
    private TableView<ArrangementEntryModel> entriesTableView;
    @FXML
    private TableColumn<ArrangementEntryModel, String> patternColumn;
    @FXML
    private TableColumn<ArrangementEntryModel, Number> repetitionsColumn;

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
        final ArrangementEntryModel selectedArrangementEntry = entriesTableView.getSelectionModel().getSelectedItem();
        if (keyEvent.getCode() == KeyCode.ADD) {
            if (selectedArrangementEntry != null)
                selectedArrangementEntry.setRepetitions(selectedArrangementEntry.getRepetitions() + 1);
        } else if (keyEvent.getCode() == KeyCode.SUBTRACT) {
            if (selectedArrangementEntry != null && selectedArrangementEntry.getRepetitions() > 1)
                selectedArrangementEntry.setRepetitions(selectedArrangementEntry.getRepetitions() - 1);
        }
    }

    public SongModel getModel() {
        return model;
    }

    public void setModel(final SongModel model) {
        this.model = model;
        loadModel();
    }

    private void loadModel() {
        entriesTableView.setItems(model.getArrangementEntryModels());
    }
}
