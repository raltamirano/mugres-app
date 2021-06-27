package mugres.apps.pedalboard.controls;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import mugres.core.live.processor.Status;
import mugres.core.live.processor.drummer.Drummer;

import java.io.IOException;

public class DrummerPlayer extends VBox {
    private Drummer drummer;

    public DrummerPlayer() {
        final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/mugres/apps/pedalboard/fxml/controls/drummer-player.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @FXML
    private Label drummerMainLabel;

    @FXML
    private Label playingFillLabel;

    @FXML
    private Label finishingLabel;

    public void setDrummer(final Drummer drummer) {
        this.drummer = drummer;

        this.drummer.addStatusListener(this::updateDrummerStatus);
    }

    private void updateDrummerStatus(final Status<Drummer.Status> status) {
        Platform.runLater(() -> {
            final Drummer.Status ds = status.data();
            if (ds.isPlaying()) {
                final String mainLine = ds.playingGroove() +
                        (ds.nextGroove().isEmpty() ? "" : " > " + ds.nextGroove());
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
}
