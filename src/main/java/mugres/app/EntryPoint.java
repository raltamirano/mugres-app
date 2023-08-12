package mugres.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class EntryPoint extends Application {
    private static MUGRESApp mugresApp;

    @Override
    public void start(final Stage stage) throws IOException {
        mugresApp = new MUGRESApp();

        final Parent mugresParent = FXMLLoader.load(getClass().getResource("/mugres/app/mugres.fxml"));
        final Scene scene = new Scene(mugresParent);
        stage.setTitle("MUGRES");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.getIcons().add(new Image(getClass().getResource("/mugres/app/image/icon.png").openStream()));
        stage.setOnCloseRequest(this::onCloseRequest);
        stage.show();
    }

    private void onCloseRequest(final WindowEvent windowEvent) {
        System.exit(0);
    }

    public static MUGRESApp MUGRESApp() {
        return mugresApp;
    }

    public static void main(final String[] args) {
        launch(args);
    }
}