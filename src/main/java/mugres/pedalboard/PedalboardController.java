package mugres.pedalboard;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import mugres.core.common.Pitch;
import mugres.core.common.Played;
import mugres.core.common.Signal;
import mugres.core.live.processors.Status;
import mugres.core.live.processors.drummer.Drummer;

import java.util.HashMap;
import java.util.Map;

import static java.lang.System.currentTimeMillis;


public class PedalboardController {
    private boolean drummer = true;

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

    private Pedalboard pedalboard;

    private final Map<Integer, Pitch> buttonPitches = new HashMap<>();

    private int midiChannel = 1;

    private int velocity = 100;

    @FXML
    public void initialize() {
        pedalboard = EntryPoint.getPedalboard();
        pedalboard.getProcessor().addStatusListener(this::updateProcessorStatus);

        HBox.setHgrow(mainButton1, Priority.ALWAYS);
        HBox.setHgrow(mainButton2, Priority.ALWAYS);
        HBox.setHgrow(mainButton3, Priority.ALWAYS);
        HBox.setHgrow(mainButton4, Priority.ALWAYS);
        HBox.setHgrow(mainButton5, Priority.ALWAYS);

        setDrummerButtonPitches();
    }

    private void updateProcessorStatus(final Status status) {
        Platform.runLater(() -> {
            if(isDrummer()) {
                final Drummer.Status ds = (Drummer.Status) status.getData();
                if (ds.isPlaying()) {
                    final String mainLine = ds.getPlayingGroove() +
                            (ds.getNextGroove().isEmpty() ? "" : " -> " + ds.getNextGroove());
                    drummerMainLabel.setText(mainLine);
                    playingFillLabel.setText("Switching: " + (ds.isPlayingFillNow() ? "Yes" : "No"));
                    finishingLabel.setText("Finishing: " + (ds.isFinishing() ? "Yes" : "No"));
                } else {
                    drummerMainLabel.setText("");
                    playingFillLabel.setText("");
                    finishingLabel.setText("");
                }
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
    public void onMainButton(final ActionEvent event) {
        final Button button = (Button) event.getSource();
        final int buttonNumber = Integer.valueOf(button.getId().replaceAll("[^\\d.]", ""));

        final Played played = Played.of(buttonPitches.get(buttonNumber), velocity);
        final Signal on = Signal.on(currentTimeMillis(), midiChannel, played);
        final Signal off = Signal.off(currentTimeMillis() + 500, midiChannel, played);

        pedalboard.getProcessor().process(on);
        pedalboard.getProcessor().process(off);
    }

    public boolean isDrummer() {
        return drummer;
    }
}
