package mugres.pedalboard;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import mugres.core.MUGRES;

import java.io.IOException;
import java.net.URL;

public class EntryPoint extends Application {
    private static Pedalboard pedalboard;

    @Override
    public void start(final Stage stage) throws IOException {
        pedalboard = new Pedalboard();

        final URL resource = getClass().getResource("/mugres/pedalboard/fxml/pedalboard.fxml");
        final Parent root = FXMLLoader.load(resource);
        root.getStylesheets().add(getClass().getResource("/mugres/pedalboard/fxml/css/pedalboard.css").toExternalForm());


        final Scene scene = new Scene(root, 800, 600);
        stage.setTitle("MUGRES Pedalboard");
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResource("/mugres/pedalboard/fxml/images/icon.png").openStream()));
        stage.show();
    }

    public static Pedalboard getPedalboard() {
        return pedalboard;
    }

    public static void main(final String[] args) {
        MUGRES.useMidiInputPort(System.getenv("mugres.inputPort"));
        MUGRES.useMidiOutputPort(System.getenv("mugres.outputPort"));

        launch(args);
    }
}