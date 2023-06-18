package mugres.app.controller;

import javafx.fxml.FXML;
import mugres.app.control.tracker.Song;
import mugres.app.control.tracker.Song.Model;

public class MUGRESController {
    @FXML
    private Song songEditor;

    @FXML
    public void initialize() {
        songEditor.setModel(Model.forNewSong());
    }
}
