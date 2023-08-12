package mugres.app.control.processor;

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;
import mugres.app.EntryPoint;
import mugres.app.config.ContextConfig;
import mugres.app.config.DrummerConfig;
import mugres.app.config.Filter;
import mugres.app.config.MUGRESConfig;
import mugres.app.config.ProcessorConfig;
import mugres.app.config.TransformerConfig;
import mugres.app.control.drummer.DrummerEditor;
import mugres.app.control.drummer.DrummerPlayer;
import mugres.MUGRES;
import mugres.common.Context;
import mugres.common.DrumKit;
import mugres.common.Note;
import mugres.common.Pitch;
import mugres.common.Scale;
import mugres.common.io.Input;
import mugres.common.io.Output;
import mugres.common.io.ProxyInput;
import mugres.common.io.ProxyOutput;
import mugres.live.Signal;
import mugres.function.Function;
import mugres.function.builtin.drums.PreRecordedDrums;
import mugres.live.processor.drummer.Drummer;
import mugres.live.processor.drummer.commands.Finish;
import mugres.live.processor.drummer.commands.Hit;
import mugres.live.processor.drummer.commands.NoOp;
import mugres.live.processor.drummer.commands.Play;
import mugres.live.processor.drummer.commands.Stop;
import mugres.live.processor.spirographone.Spirographone;
import mugres.live.processor.transformer.Transformer;
import mugres.live.signaler.Signaler;
import mugres.live.signaler.config.Configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static mugres.app.config.ProcessorConfig.Processor.*;


public class Processor extends BorderPane implements DrummerEditor.Listener {
    private static final String FXML = "/mugres/app/control/processor/processor.fxml";

    @FXML
    private Button startProcessorButton;

    @FXML
    private Button stopProcessorButton;

    @FXML
    private CheckBox processorRunningCheckBox;

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
    private ComboBox<ProcessorConfig> configurationsCombo;

    @FXML
    private Button editConfigurationButton;

    @FXML
    private Button deleteConfigurationButton;

    @FXML
    private HBox configurationControls;

    private final ObjectProperty<mugres.live.processor.Processor> processor = new SimpleObjectProperty<>();

    private final Map<Integer, Pitch> buttonPitches = new HashMap<>();

    private int midiChannel = 1;

    private int velocity = 100;

    private final ProcessorConfig.Processor processorType;

    private final StringProperty selectedConfigurationName = new SimpleStringProperty();
    private final mugres.live.processor.Processor.StatusListener processorStatusListener;

    public Processor(final ProcessorConfig.Processor processorType) {
        if (processorType == null)
            throw new IllegalArgumentException("processorType");

        this.processorType = processorType;
        processorStatusListener = status -> {};

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
        HBox.setHgrow(mainButton1, Priority.ALWAYS);
        HBox.setHgrow(mainButton2, Priority.ALWAYS);
        HBox.setHgrow(mainButton3, Priority.ALWAYS);
        HBox.setHgrow(mainButton4, Priority.ALWAYS);
        HBox.setHgrow(mainButton5, Priority.ALWAYS);

        processor.addListener(this::onProcessorChange);

        configurationsCombo.valueProperty()
                .addListener((o, s, n) -> selectedConfigurationNameProperty().set(n != null ? n.getName() : ""));

        AnchorPane.setRightAnchor(configurationControls, 1.0);

        configurationsCombo.setConverter(new StringConverter<ProcessorConfig>() {
            @Override
            public String toString(final ProcessorConfig processorConfig) {
                return processorConfig != null ? processorConfig.getName() : "";
            }
            @Override
            public ProcessorConfig fromString(final String s) {
                return null;
            }
        });

        loadConfigurations(null);
    }

    private void onProcessorChange(final Observable observable,
                                   final mugres.live.processor.Processor oldValue,
                                   final mugres.live.processor.Processor newValue) {
        if (oldValue != null)
            oldValue.removeStatusListener(processorStatusListener);
        if (newValue != null)
            newValue.addStatusListener(processorStatusListener);
    }

    public String getSelectedConfigurationName() {
        return selectedConfigurationName.get();
    }

    public StringProperty selectedConfigurationNameProperty() {
        return selectedConfigurationName;
    }

    private void loadConfigurations(final String selectedConfiguration) {
        final List<ProcessorConfig> processorConfigs = EntryPoint.MUGRESApp()
                .getConfig().getProcessors()
                .stream().filter(p -> p.getProcessor() == processorType)
                .collect(Collectors.toList());

        editConfigurationButton.setDisable(true);
        deleteConfigurationButton.setDisable(true);
        clearButtonsTooltips();
        processor.set(null);

        configurationsCombo.getItems().clear();
        configurationsCombo.getItems().addAll(processorConfigs);

        if (processorConfigs.size() == 1) {
            final ProcessorConfig processorConfig = processorConfigs.get(0);
            configurationsCombo.setValue(processorConfig);
            loadConfiguration(processorConfig);
        } else if (selectedConfiguration != null) {
            final ProcessorConfig processorConfig = processorConfigs.stream()
                    .filter(c -> c.getName().equals(selectedConfiguration))
                    .findFirst()
                    .orElse(null);
            configurationsCombo.setValue(processorConfig);
            if (processorConfig != null)
                loadConfiguration(processorConfig);
        }
    }

    private void loadConfiguration(final ProcessorConfig processorConfig) {
        editConfigurationButton.setDisable(false);
        deleteConfigurationButton.setDisable(false);

        if (processor.get() != null)
            processor.get().stop();

        processor.set(null);
        setCenter(null);
        clearButtonsTooltips();

        final Context context = Context.basicContext();
        overrideWithContextConfig(context, processorConfig.getMUGRESConfig().getContext());

        if (processorConfig.getProcessor() == ProcessorConfig.Processor.DRUMMER) {
            setDrummerButtonPitches();

            final mugres.live.processor.drummer.config.Configuration config =
                    new mugres.live.processor.drummer.config.Configuration(processorConfig.getName());

            for(final DrummerConfig.Control control : processorConfig.getDrummer().getControls()) {
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
                        playContext.tempo(control.getTempo());
                        playContext.timeSignature(control.getTimeSignature());

                        final String grooveName = control.getTitle();
                        config.createGroove(grooveName, playContext,
                                control.getLengthInMeasures(), generator);

                        config.setAction(buttonPitches.get(control.getNumber()).midi(),
                                Play.INSTANCE.action(
                                "pattern", grooveName,
                                "switchMode", control.getSwitchMode()));
                        break;

                    case HIT:
                        config.setAction(buttonPitches.get(control.getNumber()).midi(),
                                Hit.INSTANCE.action(
                                "options", control.getHitOptions(),
                                "velocity", control.getHitVelocity()));
                        break;
                    case FINISH:
                        config.setAction(buttonPitches.get(control.getNumber()).midi(),
                                Finish.INSTANCE.action());
                        break;
                    case STOP:
                        config.setAction(buttonPitches.get(control.getNumber()).midi(),
                                Stop.INSTANCE.action());
                        break;
                    case NOOP:
                        config.setAction(buttonPitches.get(control.getNumber()).midi(),
                                NoOp.INSTANCE.action());
                }
            }

            final Drummer drummer = new Drummer(context,
                    createProcessorInput(processorConfig),
                    createProcessorOutput(processorConfig),
                    config);

            processor.set(drummer);

            final DrummerPlayer drummerPlayer = new DrummerPlayer();
            drummerPlayer.setDrummer(drummer);
            setCenter(drummerPlayer);
        } else if (processorConfig.getProcessor() == ProcessorConfig.Processor.TRANSFORMER) {
            final mugres.live.processor.transformer.config.Configuration config =
                    new mugres.live.processor.transformer.config.Configuration();

            final Context playContext = Context.ComposableContext.of(context);
            overrideWithContextConfig(playContext, processorConfig.getTransformer().getContext());

            if (processorConfig.getTransformer().getButtons().isEmpty()) {
                setStandardButtonPitches();
                for(int index=1; index<=5; index++)
                    getMainButton(index).setTooltip(new Tooltip(String.valueOf(index)));
            } else {
                setTransformerButtonPitches(processorConfig.getTransformer());
                for (final TransformerConfig.Button button : processorConfig.getTransformer().getButtons())
                    getMainButton(button.getNumber()).setTooltip(new Tooltip(button.getLabel()));
            }

            for(final Filter filter : processorConfig.getTransformer().getFilters())
                config.appendFilter(filter.getFilter(), filter.getArgs());

            if (!processorConfig.getTransformer().getSignalers().isEmpty()) {
                for(final mugres.app.config.Signaler s : processorConfig.getTransformer().getSignalers()) {
                    Configuration signalerConfig = new Configuration();
                    Configuration.Frequency frequency = new Configuration.Frequency();
                    frequency.mode(Configuration.Frequency.Mode.valueOf(s.getFrequency().getMode().toString()));
                    frequency.value(s.getFrequency().getValue());
                    signalerConfig.frequency(frequency);
                    s.getTags().forEach(signalerConfig.tags()::add);
                    signalerConfig.duration(s.getDuration());
                    config.addSignaler(Signaler.forConfig(signalerConfig));
                }
            }

            processor.set(new Transformer(playContext,
                    createProcessorInput(processorConfig),
                    createProcessorOutput(processorConfig),
                    config));
        } else if (processorConfig.getProcessor() == SPIROGRAPHONE) {
            final mugres.live.processor.spirographone.config.Configuration config =
                    new mugres.live.processor.spirographone.config.Configuration();
            final Context playContext = Context.ComposableContext.of(context);
            overrideWithContextConfig(playContext, processorConfig.getSpirographone().getContext());

            config.setOutputChannel(processorConfig.getSpirographone().getOutputChannel());
            config.setAutoStart(processorConfig.getSpirographone().isAutoStart());
            config.setExternalCircleRadius(processorConfig.getSpirographone().getExternalCircleRadius());
            config.setInternalCircleRadius(processorConfig.getSpirographone().getInternalCircleRadius());
            config.setOffsetOnInternalCircle(processorConfig.getSpirographone().getOffsetOnInternalCircle());
            config.setIterationDelta(processorConfig.getSpirographone().getIterationDelta());
            config.setSpaceMillis(processorConfig.getSpirographone().getSpaceMillis());
            config.setMinOctave(processorConfig.getSpirographone().getMinOctave());
            config.setMaxOctave(processorConfig.getSpirographone().getMaxOctave());
            final Note root = processorConfig.getSpirographone().getRoot() != null ?
                    processorConfig.getSpirographone().getRoot() : playContext.key().root();
            config.setRoot(root);
            final Scale scale = processorConfig.getSpirographone().getScale() != null ?
                    processorConfig.getSpirographone().getScale() : playContext.key().defaultScale();
            config.setScale(scale);

            processor.set(new Spirographone(playContext,
                    createProcessorInput(processorConfig),
                    createProcessorOutput(processorConfig),
                    config));
        } else {
            throw new RuntimeException("Not implemented!");
        }

        configureControls(processorConfig);
        processor.get().start();
        processorRunningCheckBox.setSelected(true);
    }

    private Input createProcessorInput(final ProcessorConfig processorConfig) {
        List<mugres.filter.Filter> filters = new ArrayList<>();
        if (processorConfig.getInputFilters() != null)
            for(final Filter filter : processorConfig.getInputFilters())
                filters.add(mugres.filter.Filter.of(filter.getFilter(), filter.getArgs()));

        return ProxyInput.of(MUGRES.input(), filters);
    }

    private Output createProcessorOutput(final ProcessorConfig processorConfig) {
        List<mugres.filter.Filter> filters = new ArrayList<>();
        if (processorConfig.getOutputFilters() != null)
            for(final Filter filter : processorConfig.getOutputFilters())
                filters.add(mugres.filter.Filter.of(filter.getFilter(), filter.getArgs()));

        return ProxyOutput.of(MUGRES.output(), filters);
    }

    private void configureControls(final ProcessorConfig processorConfig) {
        if (processorConfig.getControls() == null)
            return;

        processorConfig.getControls()
                .forEach(c -> processor.get().mapParameterToControlChange(c.getParameter(), c.getControlChange()));
    }

    private void overrideWithContextConfig(final Context baseContext, final ContextConfig contextConfig) {
        if (contextConfig != null) {
            if (contextConfig.getTempo() > 0)
                baseContext.tempo(contextConfig.getTempo());
            if (contextConfig.getKey() != null)
                baseContext.key(contextConfig.getKey());
            if (contextConfig.getTimeSignature() != null)
                baseContext.timeSignature(contextConfig.getTimeSignature());
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
    protected void startProcessor(final ActionEvent event) {
        final mugres.live.processor.Processor theProcessor = processor.get();
        if (theProcessor != null) {
            theProcessor.start();
            processorRunningCheckBox.setSelected(true);
        }
    }

    @FXML
    protected void stopProcessor(final ActionEvent event) {
        final mugres.live.processor.Processor theProcessor = processor.get();
        if (theProcessor != null) {
            theProcessor.stop();
            processorRunningCheckBox.setSelected(false);
        }
    }

    @FXML
    protected void onConfigurationSelected(final ActionEvent event) {
        final ProcessorConfig processorConfiguration =
                (ProcessorConfig)configurationsCombo.getValue();

        if (processorConfiguration != null)
            loadConfiguration(processorConfiguration);
    }

    @FXML
    protected void onNewConfiguration(final ActionEvent event) {
        final DrummerEditor editor = new DrummerEditor();
        editor.addListener(this);
        setCenter(editor);
        configurationControls.setVisible(false);
    }

    @FXML
    protected void onEditConfiguration(final ActionEvent event) {
        final ProcessorConfig processorConfiguration =
                (ProcessorConfig)configurationsCombo.getValue();

        final DrummerEditor editor = new DrummerEditor();
        editor.addListener(this);
        editor.setModel(processorConfiguration);
        setCenter(editor);
        configurationControls.setVisible(false);
    }

    @FXML
    protected void onDeleteConfiguration(final ActionEvent event) {
        final ProcessorConfig processorConfiguration =
                (ProcessorConfig)configurationsCombo.getValue();

        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete configuration '" + processorConfiguration.getName() + "'?",
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirm deletion");
        setDefaultButton(alert, ButtonType.NO);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.NO)
            return;

        final MUGRESConfig config = EntryPoint.MUGRESApp().getConfig();
        config.getProcessors().removeIf(c -> c.getName().equals(processorConfiguration.getName()));
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

        final Signal on = Signal.on(midiChannel, buttonPitches.get(buttonNumber), velocity);
        // FIXME: tie to button's release?
        final Signal off = Signal.off(midiChannel, buttonPitches.get(buttonNumber));

        MUGRES.input().receive(on);
        MUGRES.input().receive(off);
    }

    @Override
    public void onDrummerEditorCreate(final DrummerEditor editor) {
        setCenter(null);
        configurationControls.setVisible(true);

        final MUGRESConfig config = EntryPoint.MUGRESApp().getConfig();
        config.getProcessors().add(editor.getOutput());
        config.save();

        loadConfigurations(editor.getOutput().getName());
        configurationControls.setVisible(true);
    }

    @Override
    public void onDrummerEditorUpdate(final DrummerEditor editor) {
        setCenter(null);
        configurationControls.setVisible(true);

        final MUGRESConfig config = EntryPoint.MUGRESApp().getConfig();
        config.getProcessors().removeIf(c -> c.getName().equals(editor.getModel().getName()));
        config.getProcessors().add(editor.getOutput());
        config.save();

        loadConfigurations(editor.getOutput().getName());
    }

    @Override
    public void onDrummerEditorCancel(final DrummerEditor editor) {
        setCenter(null);
        configurationControls.setVisible(true);

        if (editor.isEditing())
            loadConfiguration((ProcessorConfig) configurationsCombo.getValue());
    }

    public void destroy() {
        final mugres.live.processor.Processor theProcessor = processor.get();
        if (theProcessor != null)
            theProcessor.stop();
        processor.set(null);
    }
}
