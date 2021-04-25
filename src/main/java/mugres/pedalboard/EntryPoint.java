package mugres.pedalboard;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class EntryPoint extends Application {
    private static MUGRESApplication mugresApplication;

    @Override
    public void start(final Stage stage) throws IOException {
        mugresApplication = new MUGRESApplication();

        final Parent pedalboard = FXMLLoader.load(getClass().getResource("/mugres/pedalboard/fxml/pedalboard.fxml"));
        pedalboard.getStylesheets().add(getClass().getResource("/mugres/pedalboard/fxml/css/pedalboard.css").toExternalForm());


        final Scene scene = new Scene(pedalboard, 1000, 700);
        stage.setTitle("MUGRES Pedalboard");
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResource("/mugres/pedalboard/fxml/images/icon.png").openStream()));
        stage.show();
    }

    public static MUGRESApplication MUGRES() {
        return mugresApplication;
    }

    public static void main(final String[] args) {
        launch(args);
    }
}