package mugres.app.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import mugres.app.config.ProcessorConfig;
import mugres.app.control.processor.Processor;
import mugres.app.control.tracker.Song;
import mugres.app.control.tracker.Song.Model;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class MUGRESController {
    @FXML
    private MenuBar mainMenu;

    @FXML
    private TabPane tabs;

    @FXML
    public void initialize() {
        loadMenus();
    }

    private void loadMenus() {
        final Menu fileMenu = new Menu("File");
        mainMenu.getMenus().add(fileMenu);
        final MenuItem exitMenuItem = new MenuItem("Exit");
        fileMenu.getItems().add(exitMenuItem);
        exitMenuItem.setOnAction(this::fileExit);

        final Menu addMenu = new Menu("Add");
        mainMenu.getMenus().add(addMenu);
        final Menu addProcessorMenu = new Menu("Processor");
        addMenu.getItems().add(addProcessorMenu);

        final MenuItem addDrummerMenuItem = new MenuItem("Drummer");
        addProcessorMenu.getItems().add(addDrummerMenuItem);
        addDrummerMenuItem.setUserData(ProcessorConfig.Processor.DRUMMER);
        addDrummerMenuItem.setOnAction(this::addProcessor);
        final MenuItem addSpirographoneMenuItem = new MenuItem("Spirographone");
        addProcessorMenu.getItems().add(addSpirographoneMenuItem);
        addSpirographoneMenuItem.setUserData(ProcessorConfig.Processor.SPIROGRAPHONE);
        addSpirographoneMenuItem.setOnAction(this::addProcessor);
        final MenuItem addTransformerMenuItem = new MenuItem("Transformer");
        addProcessorMenu.getItems().add(addTransformerMenuItem);
        addTransformerMenuItem.setUserData(ProcessorConfig.Processor.TRANSFORMER);
        addTransformerMenuItem.setOnAction(this::addProcessor);

        final MenuItem addTrackerMenuItem = new MenuItem("Tracker");
        addMenu.getItems().add(addTrackerMenuItem);
        addTrackerMenuItem.setOnAction(this::addTracker);

    }

    private void fileExit(final ActionEvent actionEvent) {
        System.exit(0);
    }

    private void addProcessor(final ActionEvent actionEvent) {
        final ProcessorConfig.Processor type = (ProcessorConfig.Processor) ((MenuItem) actionEvent.getSource()).getUserData();
        final Processor processorEditor = new Processor(type);
        final Tab newTab = new Tab(type.label(), processorEditor);
        newTab.setOnClosed(e -> processorEditor.destroy());
        processorEditor.selectedConfigurationNameProperty()
                .addListener((s, o, n) -> newTab.setText(type.label() + ": " + n));
        tabs.getTabs().add(newTab);
        tabs.getSelectionModel().select(newTab);
    }

    private void addTracker(final ActionEvent actionEvent) {
        final Song songEditor = new Song();
        final Model newSong = Model.forNewSong();
        songEditor.setModel(newSong);
        final Tab newTab = new Tab("Tracker: " + newSong.getSong().name(), songEditor);
        newSong.getSong().addPropertyChangeListener(l -> {
            if (l.getPropertyName().equals("name"))
                newTab.setText("Tracker: " + l.getNewValue());
        });
        tabs.getTabs().add(newTab);
        tabs.getSelectionModel().select(newTab);
    }
}
