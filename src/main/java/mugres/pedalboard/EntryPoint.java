package mugres.pedalboard;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class EntryPoint extends Application {
    private static MUGRESApplication mugresApplication;

    @Override
    public void start(final Stage stage) throws IOException {
        mugresApplication = new MUGRESApplication();

        final URL resource = getClass().getResource("/mugres/pedalboard/fxml/pedalboard.fxml");
        final Parent root = FXMLLoader.load(resource);
        root.getStylesheets().add(getClass().getResource("/mugres/pedalboard/fxml/css/pedalboard.css").toExternalForm());


        final Scene scene = new Scene(root, 1000, 700);
        stage.setTitle("MUGRES Pedalboard");
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResource("/mugres/pedalboard/fxml/images/icon.png").openStream()));
        stage.show();
    }

    public static MUGRESApplication getMUGRESApplication() {
        return mugresApplication;
    }

    public static void main(final String[] args) {
        launch(args);
    }
}