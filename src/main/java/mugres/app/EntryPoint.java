package mugres.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class EntryPoint extends Application {
    private static MUGRESApp mugresApp;

    @Override
    public void start(final Stage stage) throws IOException {
        mugresApp = new MUGRESApp();

        final Parent mugresParent = FXMLLoader.load(getClass().getResource("/mugres/app/fxml/mugres.fxml"));
        final Scene scene = new Scene(mugresParent);
        stage.setTitle("MUGRES");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.getIcons().add(new Image(getClass().getResource("/mugres/app/fxml/images/icon.png").openStream()));
        stage.show();
    }

    public static MUGRESApp MUGRESApp() {
        return mugresApp;
    }

    public static void main(final String[] args) {
        launch(args);
    }
}