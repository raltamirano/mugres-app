package mugres.app.control.misc;

import javafx.scene.control.Button;
import javafx.scene.control.TableCell;

import java.util.function.Consumer;

public class ButtonCell<T> extends TableCell<T, Void> {
    private final Button button;

    public ButtonCell(final String text, final Consumer<T> onAction) {
        this.button = new Button(text);
        this.button.setMaxWidth(Double.MAX_VALUE);
        this.button.setOnAction(e -> onAction.accept(getCurrentItem()));
    }

    @Override
    public void updateItem(final Void item, final boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setGraphic(null);
        } else {
            setGraphic(button);
        }
    }

    public T getCurrentItem() {
        return getTableView().getItems().get(getIndex());
    }
}
