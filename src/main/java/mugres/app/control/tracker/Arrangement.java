package mugres.app.control.tracker;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import mugres.app.control.misc.IntegerSpinnerCell;
import mugres.app.control.tracker.Song.ArrangementEntryModel;
import mugres.app.control.tracker.Song.Model;
import mugres.tracker.Pattern;

import java.io.IOException;

public class Arrangement extends VBox {
    private static final String FXML = "/mugres/app/control/tracker/arrangement.fxml";

    private Model model;

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
        patternColumn.setCellValueFactory(item ->
                Bindings.createStringBinding(() ->
                                item.getValue().getPattern() != null ? item.getValue().getPattern().name() : "",
                                item.getValue().patternProperty())
        );
        repetitionsColumn.setCellValueFactory(item -> item.getValue().repetitionsProperty());
        repetitionsColumn.setCellFactory(c -> new IntegerSpinnerCell<>(1, Integer.MAX_VALUE));
    }

    @FXML
    public void createEntry(final ActionEvent event) {
        final Pattern currentPattern = model.getCurrentPattern();
        if (currentPattern != null)
            model.getSong().arrangement().append(currentPattern, 1);
    }

    @FXML
    public void deleteEntry(final ActionEvent event) {
        final int selectedIndex = entriesTableView.getSelectionModel().getSelectedIndex();
        if (selectedIndex == -1)
            return;

        model.getSong().arrangement().remove(selectedIndex);
    }

    @FXML
    public void moveEntryDown(final ActionEvent event) {
        final int selectedIndex = entriesTableView.getSelectionModel().getSelectedIndex();
        if (selectedIndex == -1)
            return;

        model.getSong().arrangement().moveForward(selectedIndex);
    }

    @FXML
    public void moveEntryUp(final ActionEvent event) {
        final int selectedIndex = entriesTableView.getSelectionModel().getSelectedIndex();
        if (selectedIndex == -1)
            return;

        model.getSong().arrangement().moveBack(selectedIndex);
    }

    public Model getModel() {
        return model;
    }

    public void setModel(final Model model) {
        this.model = model;
        loadModel();
    }

    private void loadModel() {
        entriesTableView.setItems(model.getArrangementEntryModels());
    }
}
