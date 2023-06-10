package mugres.app.control.kveditor;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import mugres.common.DataType;
import mugres.common.DrumKit;
import mugres.common.Key;
import mugres.common.Note;
import mugres.common.Scale;
import mugres.common.TimeSignature;
import mugres.common.Value;
import mugres.common.Variant;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class KeyValueEditor extends AnchorPane {
    private static final String FXML = "/mugres/app/control/kveditor/key-value-editor.fxml";

    private Model model;

    @FXML
    private Label titleLabel;

    @FXML
    private GridPane propertiesGrid;

    public KeyValueEditor() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(FXML));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @FXML
    public void initialize() {
    }

    public Model getModel() {
        return model;
    }

    public void setModel(final Model model) {
        this.model = model;
        loadModel();
    }

    public String getTitle() {
        return titleLabel.getText();
    }

    public void setTitle(final String title) {
        titleLabel.setText(title);
    }

    public String getKeyHeader() {
        return "";
    }

    public void setKeyHeader(final String text) {
    }

    public String getValueHeader() {
        return "";
    }

    public void setValueHeader(final String text) {
    }

    private void loadModel() {
        int rowIndex = 0;
        for(Property p : model.properties.values()) {
            Node editor = null;
            switch (p.type) {
                case VALUE:
                    editor = getComboBox(p, Value.values());
                    break;
                case NOTE:
                    editor = getComboBox(p, Note.values());
                    break;
                case SCALE:
                    editor = getComboBox(p, Scale.values());
                    break;
                case KEY:
                    editor = getComboBox(p, Key.values());
                    break;
                case TIME_SIGNATURE:
                    editor = getComboBox(p, TimeSignature.commonTimeSignatures());
                    break;
                case TEXT:
                    editor = getTextField(p);
                    break;
                case INTEGER:
                    editor = getIntegerSpinner(p);
                    break;
                case DRUM_KIT:
                    editor = getComboBox(p, DrumKit.values());
                    break;
                case VARIANT:
                    editor = getComboBox(p, Variant.values());
                    break;
                case BOOLEAN:
                    editor = getCheckBox(p);
                    break;
                case UNKNOWN:
                    if (p.isDomainBased())
                        editor = getComboBox(p, p.domain);
                    else
                        throw new IllegalArgumentException("No editor for data type: " + p.type);
                    break;
                case PITCH:
                case LENGTH:
                case EUCLIDEAN_PATTERN:
                default:
                    throw new IllegalArgumentException("No editor for data type: " + p.type);
            }
            editor.setUserData(p.name);
            editor.minWidth(200.0);

            propertiesGrid.addRow(rowIndex++, new Label(p.label), editor);
        }
    }

    private ComboBox getComboBox(final Property property, final Collection<Object> values) {
        return getComboBox(property, values.toArray());
    }

    private ComboBox getComboBox(final Property property, final Object[] values) {
        final ComboBox comboBox = new ComboBox(FXCollections.observableArrayList(values));
        if (property.value != null)
            comboBox.setValue(property.value);
        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            model.getProperties().get(comboBox.getUserData()).value = newValue;
        });

        return comboBox;
    }

    private CheckBox getCheckBox(final Property property) {
        final CheckBox checkBox = new CheckBox();
        if (property.value != null)
            checkBox.setSelected(Boolean.parseBoolean(property.value.toString()));
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            model.getProperties().get(checkBox.getUserData()).value = newValue;
        });

        return checkBox;
    }

    private TextField getTextField(final Property property) {
        final TextField textField = new TextField();
        if (property.value != null)
            textField.setText(property.value.toString());
        textField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            model.getProperties().get(textField.getUserData()).value = newValue;
        });
        return textField;
    }

    private Spinner getIntegerSpinner(final Property property) {
        final int min = property.min instanceof Integer ? (int)property.min : Integer.MIN_VALUE;
        final int max = property.max instanceof Integer ? (int)property.max : Integer.MAX_VALUE;
        final int initial = property.value instanceof Integer ? (int)property.value :
                (0 >= min && 0 <= max) ? 0 : min;
        final Spinner spinner = new Spinner(min, max, initial);
        spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            model.getProperties().get(spinner.getUserData()).value = newValue;
        });
        return spinner;
    }

    public static class Model {
        private final Map<String, Property> properties = new HashMap<>();

        private Model() {
        }

        public static Model of(final Property... properties) {
            final Model model = new Model();
            for(Property p : properties)
                model.properties.put(p.name, p);
            return model;
        }

        public Map<String, Property> getProperties() {
            return properties;
        }

        @Override
        public String toString() {
            return "Model{" +
                    "properties=" + properties +
                    '}';
        }
    }

    public static class Property {
        private final String name;
        private final String label;
        private final DataType type;
        private Object value;
        private Object min;
        private Object max;
        private Collection<Object> domain;

        private Property(final String name, final String label, final DataType type, final Object value,
                         final Object min, final Object max, final Collection<Object> domain) {
            this.name = name;
            this.label = label;
            this.type = type;
            this.value = value;
            this.min = min;
            this.max = max;
            this.domain = domain;
        }

        public static Property of(final String name, final String label, final DataType type,
                                      final Object value, final Collection<Object> domain) {
            return new Property(name, label, type, value, null, null, domain);
        }

        public static Property of(final String name, final String label, final DataType type, final Object value) {
            return new Property(name, label, type, value, null, null, null);
        }

        public static Property of(final String name, final String label, final DataType type, final Object value,
                final Object min, final Object max) {
            return new Property(name, label, type, value, min, max, null);
        }

        public String getName() {
            return name;
        }

        public String getLabel() {
            return label;
        }

        public DataType getType() {
            return type;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(final Object value) {
            this.value = value;
        }

        public Object getMin() {
            return min;
        }

        public Object getMax() {
            return max;
        }

        public Collection<Object> getDomain() {
            return domain;
        }

        public boolean isDomainBased() {
            return domain != null;
        }

        @Override
        public String toString() {
            return "Property{" +
                    "name='" + name + '\'' +
                    ", label='" + label + '\'' +
                    ", type=" + type +
                    ", value=" + value +
                    ", min=" + min +
                    ", max=" + max +
                    ", domain=" + domain +
                    '}';
        }
    }
}
