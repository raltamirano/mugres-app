package mugres.apps.pedalboard.controls;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class DrummerControlEditor extends VBox {
    public DrummerControlEditor() {
        final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/mugres/apps/pedalboard/fxml/controls/drummer-control-editor.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
