package mugres.apps.pedalboard.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;
import mugres.apps.pedalboard.EntryPoint;
import mugres.apps.pedalboard.config.ContextConfig;
import mugres.apps.pedalboard.config.DrummerConfig;
import mugres.apps.pedalboard.config.MUGRESConfig;
import mugres.apps.pedalboard.config.PedalboardConfig;
import mugres.apps.pedalboard.config.TransformerConfig;
import mugres.apps.pedalboard.controls.DrummerEditor;
import mugres.apps.pedalboard.controls.DrummerPlayer;
import mugres.core.MUGRES;
import mugres.core.common.Context;
import mugres.core.common.DrumKit;
import mugres.core.common.Pitch;
import mugres.core.common.Played;
import mugres.core.common.Signal;
import mugres.core.function.Function;
import mugres.core.function.builtin.drums.BlastBeat;
import mugres.core.function.builtin.drums.HalfTime;
import mugres.core.function.builtin.drums.PreRecordedDrums;
import mugres.core.live.processor.Processor;
import mugres.core.live.processor.drummer.Drummer;
import mugres.core.live.processor.drummer.commands.Finish;
import mugres.core.live.processor.drummer.commands.Hit;
import mugres.core.live.processor.drummer.commands.NoOp;
import mugres.core.live.processor.drummer.commands.Play;
import mugres.core.live.processor.drummer.commands.Stop;
import mugres.core.live.processor.transformer.Transformer;
import mugres.core.live.signaler.Signaler;
import mugres.core.live.signaler.config.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.System.currentTimeMillis;


public class PedalboardController
    implements DrummerEditor.Listener {
    @FXML
    private BorderPane root;

    @FXML
    private Button mainButton1;

    @FXML
    private Button mainButton2;

    @FXML
    private Button mainButton3;

    @FXML
    private Button mainButton4;

    @FXML
    private Button mainButton5;

    @FXML
    private ComboBox configurationsCombo;

    @FXML
    private Button editConfigurationButton;

    @FXML
    private Button deleteConfigurationButton;

    @FXML
    private HBox configurationControls;

    private Processor processor;

    private final Map<Integer, Pitch> buttonPitches = new HashMap<>();

    private int midiChannel = 1;

    private int velocity = 100;

    @FXML
    public void initialize() {
        HBox.setHgrow(mainButton1, Priority.ALWAYS);
        HBox.setHgrow(mainButton2, Priority.ALWAYS);
        HBox.setHgrow(mainButton3, Priority.ALWAYS);
        HBox.setHgrow(mainButton4, Priority.ALWAYS);
        HBox.setHgrow(mainButton5, Priority.ALWAYS);

        AnchorPane.setRightAnchor(configurationControls, 1.0);

        configurationsCombo.setConverter(new StringConverter<PedalboardConfig>() {
            @Override
            public String toString(final PedalboardConfig pedalboardConfig) {
                return pedalboardConfig != null ? pedalboardConfig.getName() : "";
            }
            @Override
            public PedalboardConfig fromString(final String s) {
                return null;
            }
        });

        loadConfigurations(null);
    }

    private void loadConfigurations(final String selectedConfiguration) {
        final List<PedalboardConfig> pedalboards = EntryPoint.MUGRES()
                .getConfig().getPedalboards();

        editConfigurationButton.setDisable(true);
        deleteConfigurationButton.setDisable(true);
        clearButtonsTooltips();
        processor = null;

        configurationsCombo.getItems().clear();
        configurationsCombo.getItems().addAll(pedalboards);

        if (pedalboards.size() == 1) {
            final PedalboardConfig pedalboardConfig = pedalboards.get(0);
            configurationsCombo.setValue(pedalboardConfig);
            loadConfiguration(pedalboardConfig);
        } else if (selectedConfiguration != null) {
            final PedalboardConfig pedalboardConfig = pedalboards.stream()
                    .filter(c -> c.getName().equals(selectedConfiguration))
                    .findFirst()
                    .orElse(null);
            configurationsCombo.setValue(pedalboardConfig);
            if (pedalboardConfig != null)
                loadConfiguration(pedalboardConfig);
        }
    }

    private void loadConfiguration(final PedalboardConfig pedalboardConfig) {
        editConfigurationButton.setDisable(false);
        deleteConfigurationButton.setDisable(false);

        if (processor != null)
            processor.stop();

        processor = null;
        root.setCenter(null);
        clearButtonsTooltips();

        final Context context = Context.createBasicContext();
        overrideWithContextConfig(context, pedalboardConfig.getMUGRESConfig().getContext());

        if (pedalboardConfig.getProcessor() == PedalboardConfig.Processor.DRUMMER) {
            setDrummerButtonPitches();

            final mugres.core.live.processor.drummer.config.Configuration config =
                    new mugres.core.live.processor.drummer.config.Configuration(pedalboardConfig.getName());

            for(final DrummerConfig.Control control : pedalboardConfig.getDrummer().getControls()) {
                setDrummerButtonLabel(control);

                switch(control.getCommand()) {
                    case PLAY:
                        final PreRecordedDrums generator;
                        switch(control.getGenerator()){
                            case HALF_TIME:
                                generator = Function.forName("halfTime");
                                break;
                            case BLAST_BEAT:
                                generator = Function.forName("blastBeat");
                                break;
                            default:
                                throw new RuntimeException("Unknown generator function: " + control.getGenerator());

                        }

                        final Context playContext = Context.ComposableContext.of(context);
                        playContext.setTempo(control.getTempo());
                        playContext.setTimeSignature(control.getTimeSignature());

                        final String grooveName = control.getTitle();
                        config.createGroove(grooveName, playContext,
                                control.getLengthInMeasures(), generator);

                        config.setAction(buttonPitches.get(control.getNumber()).getMidi(),
                                Play.INSTANCE.action(
                                "pattern", grooveName,
                                "switchMode", control.getSwitchMode()));
                        break;

                    case HIT:
                        config.setAction(buttonPitches.get(control.getNumber()).getMidi(),
                                Hit.INSTANCE.action(
                                "options", control.getHitOptions(),
                                "velocity", control.getHitVelocity()));
                        break;
                    case FINISH:
                        config.setAction(buttonPitches.get(control.getNumber()).getMidi(),
                                Finish.INSTANCE.action());
                        break;
                    case STOP:
                        config.setAction(buttonPitches.get(control.getNumber()).getMidi(),
                                Stop.INSTANCE.action());
                        break;
                    case NOOP:
                        config.setAction(buttonPitches.get(control.getNumber()).getMidi(),
                                NoOp.INSTANCE.action());
                }
            }

            final Drummer drummer = new Drummer(context,
                    MUGRES.input(),
                    MUGRES.output(),
                    config);

            processor = drummer;

            final DrummerPlayer drummerPlayer = new DrummerPlayer();
            drummerPlayer.setDrummer(drummer);
            root.setCenter(drummerPlayer);
        } else if (pedalboardConfig.getProcessor() == PedalboardConfig.Processor.TRANSFORMER) {
            final mugres.core.live.processor.transformer.config.Configuration config =
                    new mugres.core.live.processor.transformer.config.Configuration();

            final Context playContext = Context.ComposableContext.of(context);
            overrideWithContextConfig(playContext, pedalboardConfig.getTransformer().getContext());

            if (pedalboardConfig.getTransformer().getButtons().isEmpty()) {
                setStandardButtonPitches();
                for(int index=1; index<=5; index++)
                    getMainButton(index).setTooltip(new Tooltip(String.valueOf(index)));
            } else {
                setTransformerButtonPitches(pedalboardConfig.getTransformer());
                for (final TransformerConfig.Button button : pedalboardConfig.getTransformer().getButtons())
                    getMainButton(button.getNumber()).setTooltip(new Tooltip(button.getLabel()));
            }

            for(final TransformerConfig.Filter filter : pedalboardConfig.getTransformer().getFilters())
                config.appendFilter(filter.getFilter(), filter.getArgs());

            if (!pedalboardConfig.getTransformer().getSignalers().isEmpty()) {
                for(final TransformerConfig.Signaler s : pedalboardConfig.getTransformer().getSignalers()) {
                    Configuration signalerConfig = new Configuration();
                    Configuration.Frequency frequency = new Configuration.Frequency();
                    frequency.setMode(Configuration.Frequency.Mode.valueOf(s.getFrequency().getMode().toString()));
                    frequency.setValue(s.getFrequency().getValue());
                    signalerConfig.setFrequency(frequency);
                    s.getTags().forEach(signalerConfig.getTags()::add);
                    signalerConfig.setDuration(s.getDuration());
                    config.addSignaler(Signaler.forConfig(signalerConfig));
                }
            }

            processor = new Transformer(playContext,
                    MUGRES.input(),
                    MUGRES.output(),
                    config);
        } else {
            throw new RuntimeException("Not implemented!");
        }

        processor.start();
    }

    private void overrideWithContextConfig(final Context baseContext, final ContextConfig contextConfig) {
        if (contextConfig != null) {
            if (contextConfig.getTempo() > 0)
                baseContext.setTempo(contextConfig.getTempo());
            if (contextConfig.getKey() != null)
                baseContext.setKey(contextConfig.getKey());
            if (contextConfig.getTimeSignature() != null)
                baseContext.setTimeSignature(contextConfig.getTimeSignature());
        }
    }

    private void clearButtonsTooltips() {
        getMainButton(1).setTooltip(null);
        getMainButton(2).setTooltip(null);
        getMainButton(3).setTooltip(null);
        getMainButton(4).setTooltip(null);
        getMainButton(5).setTooltip(null);
    }

    private void setDrummerButtonLabel(final DrummerConfig.Control controlConfig) {
        String label = "";
        switch(controlConfig.getCommand()) {
            case PLAY:
                label = controlConfig.getTitle();
                break;
            case HIT:
                label = "Hit " + controlConfig.getHitOptions().stream().map(DrumKit::label).
                        collect(Collectors.joining(" or "));
                break;
            case FINISH:
                label = "Finish";
                break;
            case STOP:
                label = "Stop now!";
                break;
            case NOOP:
                label = "Does nothing";
                break;
        }

        if (controlConfig.getTitle() != null && !controlConfig.getTitle().trim().isEmpty())
            label = controlConfig.getTitle();

        final Button button = getMainButton(controlConfig.getNumber());
        if (button != null)
            button.setTooltip(new Tooltip(label));
    }

    private Button getMainButton(final int number) {
        switch(number) {
            case 1: return mainButton1;
            case 2: return mainButton2;
            case 3: return mainButton3;
            case 4: return mainButton4;
            case 5: return mainButton5;
        }

        return null;
    }

    private void setDrummerButtonPitches() {
        buttonPitches.clear();
        buttonPitches.put(1, Pitch.of(60));
        buttonPitches.put(2, Pitch.of(61));
        buttonPitches.put(3, Pitch.of(62));
        buttonPitches.put(4, Pitch.of(63));
        buttonPitches.put(5, Pitch.of(64));
    }

    private void setStandardButtonPitches() {
        buttonPitches.clear();
        buttonPitches.put(1, Pitch.of(60));
        buttonPitches.put(2, Pitch.of(61));
        buttonPitches.put(3, Pitch.of(62));
        buttonPitches.put(4, Pitch.of(63));
        buttonPitches.put(5, Pitch.of(64));
    }

    private void setTransformerButtonPitches(final TransformerConfig transformerConfig) {
        buttonPitches.clear();
        buttonPitches.put(1, transformerConfig.getButton(1).getPitch());
        buttonPitches.put(2, transformerConfig.getButton(2).getPitch());
        buttonPitches.put(3, transformerConfig.getButton(3).getPitch());
        buttonPitches.put(4, transformerConfig.getButton(4).getPitch());
        buttonPitches.put(5, transformerConfig.getButton(5).getPitch());
    }

    @FXML
    protected void onConfigurationSelected(final ActionEvent event) {
        final PedalboardConfig pedalboardConfiguration =
                (PedalboardConfig)configurationsCombo.getValue();

        if (pedalboardConfiguration != null)
            loadConfiguration(pedalboardConfiguration);
    }

    @FXML
    protected void onNewConfiguration(final ActionEvent event) {
        final DrummerEditor editor = new DrummerEditor();
        editor.addListener(this);
        root.setCenter(editor);
        configurationControls.setVisible(false);
    }

    @FXML
    protected void onEditConfiguration(final ActionEvent event) {
        final PedalboardConfig pedalboardConfiguration =
                (PedalboardConfig)configurationsCombo.getValue();

        final DrummerEditor editor = new DrummerEditor();
        editor.addListener(this);
        editor.setModel(pedalboardConfiguration);
        root.setCenter(editor);
        configurationControls.setVisible(false);
    }

    @FXML
    protected void onDeleteConfiguration(final ActionEvent event) {
        final PedalboardConfig pedalboardConfiguration =
                (PedalboardConfig)configurationsCombo.getValue();

        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete configuration '" + pedalboardConfiguration.getName() + "'?",
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirm deletion");
        setDefaultButton(alert, ButtonType.NO);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.NO)
            return;

        final MUGRESConfig config = EntryPoint.MUGRES().getConfig();
        config.getPedalboards().removeIf(c -> c.getName().equals(pedalboardConfiguration.getName()));
        config.save();

        loadConfigurations(null);
    }

    private static Alert setDefaultButton(final Alert alert, final ButtonType defaultButton) {
        DialogPane pane = alert.getDialogPane();
        for (final ButtonType t : alert.getButtonTypes())
            ((Button)pane.lookupButton(t)).setDefaultButton( t == defaultButton );
        return alert;
    }

    @FXML
    protected void onMainButton(final ActionEvent event) {
        if (processor == null)
            return;

        final Button button = (Button) event.getSource();
        final int buttonNumber = Integer.valueOf(button.getId()
                .replaceAll("[^\\d.]", ""));

        final Played played = Played.of(buttonPitches.get(buttonNumber), velocity);
        final Signal on = Signal.on(UUID.randomUUID(), currentTimeMillis(), midiChannel, played);
        // FIXME: tie to button's release?
        final Signal off = Signal.off(UUID.randomUUID(), currentTimeMillis() + 500, midiChannel, played);

        MUGRES.input().send(on);
        MUGRES.input().send(off);
    }

    @Override
    public void onDrummerEditorCreate(final DrummerEditor editor) {
        root.setCenter(null);
        configurationControls.setVisible(true);

        final MUGRESConfig config = EntryPoint.MUGRES().getConfig();
        config.getPedalboards().add(editor.getOutput());
        config.save();

        loadConfigurations(editor.getOutput().getName());
        configurationControls.setVisible(true);
    }

    @Override
    public void onDrummerEditorUpdate(final DrummerEditor editor) {
        root.setCenter(null);
        configurationControls.setVisible(true);

        final MUGRESConfig config = EntryPoint.MUGRES().getConfig();
        config.getPedalboards().removeIf(c -> c.getName().equals(editor.getModel().getName()));
        config.getPedalboards().add(editor.getOutput());
        config.save();

        loadConfigurations(editor.getOutput().getName());
    }

    @Override
    public void onDrummerEditorCancel(final DrummerEditor editor) {
        root.setCenter(null);
        configurationControls.setVisible(true);

        if (editor.isEditing())
            loadConfiguration((PedalboardConfig) configurationsCombo.getValue());
    }
}
