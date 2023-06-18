package mugres.app.control;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import mugres.common.DataType;
import mugres.common.DrumKit;
import mugres.common.Key;
import mugres.common.Note;
import mugres.common.Scale;
import mugres.common.TimeSignature;
import mugres.common.Value;
import mugres.common.Variant;
import mugres.parametrizable.Parameter;
import mugres.parametrizable.Parametrizable;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;

public class Properties extends VBox {
    private static final String FXML = "/mugres/app/control/properties.fxml";

    private Model model;

    @FXML
    private Label titleLabel;

    @FXML
    private GridPane propertiesGrid;

    public Properties() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(FXML));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
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

    public void setTitleVisible(final boolean value) {
        titleLabel.setVisible(value);
        titleLabel.setManaged(value);
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
        if (property.getValue() != null)
            comboBox.setValue(property.getValue());
        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            model.getProperties().get(comboBox.getUserData()).setValue(newValue);
        });

        return comboBox;
    }

    private CheckBox getCheckBox(final Property property) {
        final CheckBox checkBox = new CheckBox();
        if (property.getValue() != null)
            checkBox.setSelected(Boolean.parseBoolean(property.getValue().toString()));
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            model.getProperties().get(checkBox.getUserData()).setValue(newValue);
        });

        return checkBox;
    }

    private TextField getTextField(final Property property) {
        final TextField textField = new TextField();
        if (property.getValue() != null)
            textField.setText(property.getValue().toString());
        textField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            model.getProperties().get(textField.getUserData()).setValue(newValue);
        });
        return textField;
    }

    private Spinner getIntegerSpinner(final Property property) {
        final int min = property.min instanceof Integer ? (int)property.min : Integer.MIN_VALUE;
        final int max = property.max instanceof Integer ? (int)property.max : Integer.MAX_VALUE;
        final int initial = property.getValue() instanceof Integer ? (int)property.getValue() :
                (0 >= min && 0 <= max) ? 0 : min;
        final Spinner spinner = new Spinner(min, max, initial);
        spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            model.getProperties().get(spinner.getUserData()).setValue(newValue);
        });
        return spinner;
    }

    public static class Model {
        private final Map<String, Property> properties = new HashMap<>();

        private Model() {
        }

        public static Model of(final Parametrizable parametrizable) {
            final Model model = new Model();
            parametrizable.parameters().forEach(p -> model.properties.put(p.name(), Property.of(parametrizable, p)));
            return model;
        }

        public static Model of(final Collection<Property> properties) {
            final Model model = new Model();
            properties.forEach(p -> model.properties.put(p.name, p));
            return model;
        }

        public static Model of(final Property... properties) {
          return of(asList(properties));
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
        private final Object min;
        private final Object max;
        private final Collection<Object> domain;
        private final Parametrizable parametrizable;

        private Property(final String name, final String label, final DataType type, final Object value,
                         final Object min, final Object max, final Collection<Object> domain,
                         final Parametrizable parametrizable) {
            this.name = name;
            this.label = label;
            this.type = type;
            this.value = value;
            this.min = min;
            this.max = max;
            this.domain = domain;
            this.parametrizable = parametrizable;
        }

        public static Property of(final String name, final String label, final DataType type,
                                      final Object value, final Collection<Object> domain) {
            return new Property(name, label, type, value, null, null, domain, null);
        }

        public static Property of(final String name, final String label, final DataType type, final Object value) {
            return new Property(name, label, type, value, null, null, null, null);
        }

        public static Property of(final String name, final String label, final DataType type, final Object value,
                final Object min, final Object max) {
            return new Property(name, label, type, value, min, max, null, null);
        }

        public static Property of(final Parametrizable parametrizable, final Parameter parameter) {
            if (parametrizable == null)
                throw new IllegalArgumentException("parametrizable");

            return new Property(parameter.name(), parameter.name(), parameter.dataType(), parameter.defaultValue(),
                    parameter.min(), parameter.max(), parameter.domain(), parametrizable);
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
            if (parametrizable != null)
                return parametrizable.parameterValue(name);
            else
                return value;
        }

        public void setValue(final Object value) {
            if (parametrizable != null)
                parametrizable.parameterValue(name, value);
            else
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
