package mugres.pedalboard;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import mugres.core.common.Context;
import mugres.core.common.Pitch;
import mugres.core.common.Played;
import mugres.core.common.Signal;
import mugres.core.common.io.Input;
import mugres.core.common.io.Output;
import mugres.core.function.Function;
import mugres.core.function.builtin.drums.BlastBeat;
import mugres.core.function.builtin.drums.HalfTime;
import mugres.core.function.builtin.drums.PreRecordedDrums;
import mugres.core.live.processors.Processor;
import mugres.core.live.processors.Status;
import mugres.core.live.processors.drummer.Drummer;
import mugres.core.live.processors.drummer.commands.Finish;
import mugres.core.live.processors.drummer.commands.Hit;
import mugres.core.live.processors.drummer.commands.Play;
import mugres.core.live.processors.drummer.commands.Stop;
import mugres.pedalboard.config.DrummerConfig;
import mugres.pedalboard.config.PedalboardConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.currentTimeMillis;


public class PedalboardController {
    @FXML
    private Text statusText;

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
    private Label drummerMainLabel;

    @FXML
    private Label playingFillLabel;

    @FXML
    private Label finishingLabel;

    @FXML
    private AnchorPane topAnchorPane;

    @FXML
    private Label mugresVersionLabel;

    @FXML
    private ComboBox configurationsCombo;

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

        AnchorPane.setLeftAnchor(mugresVersionLabel, 1.0);
        AnchorPane.setRightAnchor(configurationsCombo, 1.0);

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

        loadConfigurations();
    }

    private void loadConfigurations() {
        final List<PedalboardConfig> pedalboards = EntryPoint.getMUGRESApplication()
                .getMUGRESConfig().getPedalboardConfigs();

        configurationsCombo.getItems().clear();
        configurationsCombo.getItems().addAll(pedalboards);

        if (pedalboards.size() == 1) {
            final PedalboardConfig pedalboardConfig = pedalboards.get(0);
            configurationsCombo.setValue(pedalboardConfig);
            loadConfiguration(pedalboardConfig);
        }
    }

    private void loadConfiguration(final PedalboardConfig pedalboardConfig) {
        if (pedalboardConfig.getProcessor() == PedalboardConfig.Processor.DRUMMER) {
            setDrummerButtonPitches();

            final mugres.core.live.processors.drummer.config.Configuration config =
                    new mugres.core.live.processors.drummer.config.Configuration(pedalboardConfig.getName());

            final Context context = Context.createBasicContext();
            for(final DrummerConfig.Button button : pedalboardConfig.getDrummerConfig().getButtons()) {
                switch(button.getCommand()) {
                    case PLAY:
                        final PreRecordedDrums generator;
                        switch(button.getGenerator()){
                            case HALF_TIME:
                                generator = new HalfTime();
                                break;
                            case BLAST_BEAT:
                                generator = new BlastBeat();
                                break;
                            default:
                                throw new RuntimeException("Unknown generator function: " + button.getGenerator());

                        }

                        final Context playContext = Context.ComposableContext.of(context);
                        playContext.setTempo(button.getTempo());
                        playContext.setTimeSignature(button.getTimeSignature());

                        final String grooveName = button.getTitle();
                        config.createGroove(grooveName, playContext,
                                button.getLengthInMeasures(), generator);

                        config.setAction(buttonPitches.get(button.getNumber()).getMidi(),
                                Play.INSTANCE.action(
                                "pattern", grooveName,
                                "switchMode", button.getSwitchMode()));
                        break;

                    case HIT:
                        config.setAction(buttonPitches.get(button.getNumber()).getMidi(),
                                Hit.INSTANCE.action(
                                "options", button.getHitOptions(),
                                "velocity", button.getHitVelocity()));
                        break;
                    case FINISH:
                        config.setAction(buttonPitches.get(button.getNumber()).getMidi(),
                                Finish.INSTANCE.action());
                        break;
                    case STOP:
                        config.setAction(buttonPitches.get(button.getNumber()).getMidi(),
                                Stop.INSTANCE.action());
                        break;
                }
            }

            processor = new Drummer(context,
                    EntryPoint.getMUGRESApplication().getInput(),
                    EntryPoint.getMUGRESApplication().getOutput(),
                    config);

            processor.addStatusListener(this::updateDrummerStatus);
        } else if (pedalboardConfig.getProcessor() == PedalboardConfig.Processor.TRANSFORMER) {
            throw new RuntimeException("Not implemented!");
        } else {
            throw new RuntimeException("Not implemented!");
        }
    }

    private void updateDrummerStatus(final Status<Drummer.Status> status) {
        Platform.runLater(() -> {
            final Drummer.Status ds = status.getData();
            if (ds.isPlaying()) {
                final String mainLine = ds.getPlayingGroove() +
                        (ds.getNextGroove().isEmpty() ? "" : " > " + ds.getNextGroove());
                drummerMainLabel.setText(mainLine);
                playingFillLabel.setText("Playing fill: " + (ds.isPlayingFillNow() ? "Yes" : "No"));
                finishingLabel.setText("Finishing: " + (ds.isFinishing() ? "Yes" : "No"));
            } else {
                drummerMainLabel.setText("");
                playingFillLabel.setText("");
                finishingLabel.setText("");
            }
        });
    }

    private void setDrummerButtonPitches() {
        buttonPitches.clear();
        buttonPitches.put(1, Pitch.of(60));
        buttonPitches.put(2, Pitch.of(61));
        buttonPitches.put(3, Pitch.of(62));
        buttonPitches.put(4, Pitch.of(63));
        buttonPitches.put(5, Pitch.of(64));
    }

    @FXML
    protected void onConfigurationSelected(final ActionEvent event) {
        if (configurationsCombo.getValue() != null)
            loadConfiguration((PedalboardConfig) configurationsCombo.getValue());
    }

    @FXML
    protected void onMainButton(final ActionEvent event) {
        if (processor == null)
            return;

        final Button button = (Button) event.getSource();
        final int buttonNumber = Integer.valueOf(button.getId()
                .replaceAll("[^\\d.]", ""));

        final Played played = Played.of(buttonPitches.get(buttonNumber), velocity);
        final Signal on = Signal.on(currentTimeMillis(), midiChannel, played);
        final Signal off = Signal.off(currentTimeMillis() + 500, midiChannel, played);

        processor.process(on);
        processor.process(off);
    }
}
