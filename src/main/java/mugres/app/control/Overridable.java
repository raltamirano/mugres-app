package mugres.app.control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import mugres.parametrizable.ParametrizableSupport.ChangedValue;

import java.beans.PropertyChangeListener;
import java.io.IOException;

public class Overridable extends HBox {
    private static final String FXML = "/mugres/app/control/overridable.fxml";

    private boolean overridden = false;
    private boolean overridable = false;
    private boolean editControlSet = false;
    private Node editorControl;
    private Properties.PropertyModel propertyModel;
    private final PropertyChangeListener propertyChangeListener;

    public Overridable() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(FXML));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }

        this.propertyChangeListener = createPropertyChangeListener();
    }

    @FXML
    private Button toggleOverrideButton;

    @FXML
    private TextField parentValueText;

    @FXML
    public void toggleOverride(final ActionEvent event) {
        if (!overridable)
            return;

        if (overridden)
            propertyModel.getParametrizable().undoOverride(propertyModel.getName());
        else
            propertyModel.getParametrizable().makeOverride(propertyModel.getName());

        overridden = !overridden;
        updateControlsFromOverrideState();
    }

    public void setEditorControl(final Node editorControl, final Properties.PropertyModel propertyModel) {
        if (editControlSet)
            throw new RuntimeException("Edit control already set!");
        if (!propertyModel.hasParametrizable())
            throw new IllegalArgumentException("Overridable only works with Parametrizable-based Property Models");

        this.editorControl = editorControl;
        this.propertyModel = propertyModel;

        overridable = propertyModel.isOverridable() && propertyModel.getParametrizable().hasParentParameterValueSource();
        overridden = overridable ? propertyModel.getParametrizable().overrides(propertyModel.getName()) : false;
        editControlSet = true;
        propertyModel.getParametrizable().addParameterValueChangeListener(propertyChangeListener);

        getChildren().add(0, editorControl);
        updateControlsFromOverrideState();
    }

    private void updateControlsFromOverrideState() {
        if (overridable) {
            editorControl.setVisible(overridden);
            editorControl.setManaged(overridden);
            parentValueText.setVisible(!overridden);
            parentValueText.setManaged(!overridden);
            final Object value = propertyModel.getParametrizable().parameterValue(propertyModel.getName());
            parentValueText.setText(value != null ? String.valueOf(value) : "");
            toggleOverrideButton.setText(overridden ? "R" : "O");
            toggleOverrideButton.setTooltip(new Tooltip(overridden ? "Reset (clear override)" : "Override"));
        } else {
            editorControl.setVisible(true);
            editorControl.setManaged(true);
            parentValueText.setVisible(false);
            parentValueText.setManaged(false);
            toggleOverrideButton.setVisible(false);
            toggleOverrideButton.setManaged(false);
        }
    }

    private PropertyChangeListener createPropertyChangeListener() {
        return e -> {
            if (!overridable || !e.getPropertyName().equals(propertyModel.getName()))
                return;

            if (e.getNewValue() instanceof ChangedValue) {
                final ChangedValue changedValue = (ChangedValue) e.getNewValue();
                if (changedValue.fromParent())
                    parentValueText.setText(changedValue.value() != null ? String.valueOf(changedValue.value()) : "");
                else
                    setEditorControlValue(changedValue.value());
            } else {
                setEditorControlValue(e.getNewValue());
            }
        };
    }

    private void setEditorControlValue(final Object newValue) {
        if (editorControl instanceof TextField)
            ((TextField)editorControl).setText(newValue != null ? newValue.toString() : null);
        else if (editorControl instanceof ComboBox)
            ((ComboBox)editorControl).setValue(newValue);
        else if (editorControl instanceof CheckBox)
            ((CheckBox)editorControl).setSelected(newValue != null ? Boolean.valueOf(newValue.toString()) : false);
        else if (editorControl instanceof Spinner)
            ((Spinner)editorControl).getValueFactory().setValue(newValue);
        else
            throw new IllegalStateException("Unknown editor control type: " + editorControl.getClass().getSimpleName());
    }
}
