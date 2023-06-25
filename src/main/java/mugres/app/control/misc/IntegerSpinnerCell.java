package mugres.app.control.misc;

import javafx.beans.value.WritableValue;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableCell;

public class IntegerSpinnerCell<T> extends TableCell<T, Number> {
    private final int min;
    private final int max;
    private final Spinner<Number> spinner;
    private boolean ignoreUpdate;

    public IntegerSpinnerCell(final int min, final int max) {
        this.min = min;
        this.max = max;
        spinner = new Spinner(min, max, min, 1);
        spinner.valueProperty().addListener((o, oldValue, newValue) -> {
            if (!ignoreUpdate) {
                ignoreUpdate = true;
                final WritableValue<Number> property = (WritableValue<Number>) getTableColumn()
                        .getCellObservableValue((T) getTableRow().getItem());
                property.setValue(newValue);
                ignoreUpdate = false;
            }
        });
    }

    @Override
    protected void updateItem(Number item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
        } else {
            ignoreUpdate = true;
            spinner.getValueFactory().setValue(item.intValue());
            setGraphic(spinner);
            ignoreUpdate = false;
        }
    }
}
