package mugres.pedalboard.controls;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import mugres.pedalboard.config.DrummerConfig;
import mugres.pedalboard.config.DrummerConfig.Control.Command;
import mugres.pedalboard.config.PedalboardConfig;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class DrummerEditor extends VBox implements Initializable {
    private final java.util.List<Listener> listeners = new ArrayList<>();

    private boolean editing;

    private PedalboardConfig model;

    private PedalboardConfig output;

    @FXML
    private TextField control1Title;

    @FXML
    private TextField control2Title;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    public DrummerEditor() {
        final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/mugres/pedalboard/fxml/controls/drummer-editor.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void addListener(final Listener listener) {
        this.listeners.add(listener);
    }

    public boolean isEditing() {
        return editing;
    }

    public PedalboardConfig getModel() {
        return model;
    }

    public PedalboardConfig getOutput() {
        return output;
    }

    public void setModel(final PedalboardConfig model) {
        if (model == null)
            throw new IllegalArgumentException("model");

        this.model = model;
        this.output = model.clone();
        this.editing = true;

        initializeEditControls();
    }

    private void notifyCreate() {
        listeners.forEach(l -> l.onDrummerEditorCreate(this));
    }

    private void notifyUpdate() {
        listeners.forEach(l -> l.onDrummerEditorUpdate(this));
    }

    private void notifyCancel() {
        listeners.forEach(l -> l.onDrummerEditorCancel(this));
    }

    @Override
    public void initialize(final URL url, final ResourceBundle resourceBundle) {
        output = new PedalboardConfig();

        final DrummerConfig drummerConfig = new DrummerConfig();
        final DrummerConfig.Control control1Config = new DrummerConfig.Control();
        control1Config.setNumber(1);
        control1Config.setCommand(Command.NOOP);
        final DrummerConfig.Control control2Config = new DrummerConfig.Control();
        control2Config.setNumber(2);
        control2Config.setCommand(Command.NOOP);
        drummerConfig.getControls().add(control2Config);
        final DrummerConfig.Control control3Config = new DrummerConfig.Control();
        control3Config.setNumber(3);
        control3Config.setCommand(Command.NOOP);
        drummerConfig.getControls().add(control3Config);
        final DrummerConfig.Control control4Config = new DrummerConfig.Control();
        control4Config.setNumber(4);
        control4Config.setCommand(Command.NOOP);
        drummerConfig.getControls().add(control4Config);
        final DrummerConfig.Control control5Config = new DrummerConfig.Control();
        control5Config.setNumber(5);
        control5Config.setCommand(Command.NOOP);
        drummerConfig.getControls().add(control5Config);

        output.setDrummerConfig(drummerConfig);
        initializeEditControls();
    }

    @FXML
    protected void onSave(final ActionEvent event) {
        output.getDrummerConfig().getControls().get(0).setTitle(control1Title.getText());
        output.getDrummerConfig().getControls().get(1).setTitle(control2Title.getText());

        if (isEditing())
            notifyUpdate();
        else
            notifyCreate();
    }

    @FXML
    protected void onCancel(final ActionEvent event) {
        notifyCancel();
    }

    private void initializeEditControls() {
        control1Title.setText(output.getDrummerConfig().getControls().get(0).getTitle());
        control2Title.setText(output.getDrummerConfig().getControls().get(1).getTitle());
    }

    public interface Listener {
        void onDrummerEditorCreate(final DrummerEditor editor);
        void onDrummerEditorUpdate(final DrummerEditor editor);
        void onDrummerEditorCancel(final DrummerEditor editor);
    }
}
