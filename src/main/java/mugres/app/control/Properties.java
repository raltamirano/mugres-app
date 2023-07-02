package mugres.app.control;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static mugres.parametrizable.ParametrizableSupport.ChangedValue.unwrap;

public class Properties extends VBox {
    private static final String FXML = "/mugres/app/control/properties.fxml";

    private final ObjectProperty<Model> model;

    @FXML
    private Label titleLabel;

    @FXML
    private GridPane propertiesGrid;

    private final BooleanProperty readOnly = new SimpleBooleanProperty(false);

    public Properties() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(FXML));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }

        model = new SimpleObjectProperty<>();
        model.addListener((source, oldValue, newValue) -> loadModel());
    }

    public Model getModel() {
        return model.get();
    }

    public ObjectProperty<Model> modelProperty() {
        return model;
    }

    public void setModel(final Model model) {
        this.model.set(model);
    }

    public String getTitle() {
        return titleLabel.getText();
    }

    public void setTitle(final String title) {
        titleLabel.setText(title);
    }

    public boolean isReadOnly() {
        return readOnly.get();
    }

    public BooleanProperty readOnlyProperty() {
        return readOnly;
    }

    public void setReadOnly(final boolean readOnly) {
        this.readOnly.set(readOnly);
    }

    public void setTitleVisible(final boolean value) {
        titleLabel.setVisible(value);
        titleLabel.setManaged(value);
    }

    private void loadModel() {
        propertiesGrid.getChildren().clear();

        final Model currentModel = getModel();
        if (currentModel == null)
            return;

        final boolean readOnlyMode = isReadOnly();
        int rowIndex = 0;
        for(PropertyModel property : currentModel.properties.values().stream().sorted().collect(Collectors.toList())) {
            if (readOnlyMode)
                createViewer(rowIndex, property);
            else
                createEditor(rowIndex, property);
            rowIndex++;
        }
    }

    private void createViewer(final int rowIndex, final PropertyModel property) {
        final Label viewer = new Label();
        final Object value;
        if (property.hasParametrizable()) {
            value = property.getParametrizable().parameterValue(property.getName());
            property.getParametrizable().addParameterValueChangeListener(e -> {
                if (e.getPropertyName().equals(property.getName()))
                    viewer.setText(e.getNewValue() != null ? e.getNewValue().toString() : "");
            });
        } else {
            value = property.getValue();
        }

        viewer.setText(value != null ? value.toString() : "");
        viewer.setUserData(property.name);
        viewer.minWidth(200.0);
        propertiesGrid.addRow(rowIndex, new Label(property.label), viewer);
    }

    private void createEditor(final int rowIndex, final PropertyModel property) {
        Node editor = null;
        switch (property.type) {
            case VALUE:
                editor = getComboBox(property, Value.values());
                break;
            case NOTE:
                editor = getComboBox(property, Note.values());
                break;
            case SCALE:
                editor = getComboBox(property, Scale.values());
                break;
            case KEY:
                editor = getComboBox(property, Key.values());
                break;
            case TIME_SIGNATURE:
                editor = getComboBox(property, TimeSignature.commonTimeSignatures());
                break;
            case TEXT:
                editor = getTextField(property);
                break;
            case INTEGER:
                editor = getIntegerSpinner(property);
                break;
            case DRUM_KIT:
                editor = getComboBox(property, DrumKit.values());
                break;
            case VARIANT:
                editor = getComboBox(property, Variant.values());
                break;
            case BOOLEAN:
                editor = getCheckBox(property);
                break;
            case UNKNOWN:
                if (property.isDomainBased())
                    editor = getComboBox(property, property.domain);
                else
                    throw new IllegalArgumentException("No editor for data type: " + property.type);
                break;
            case PITCH:
            case LENGTH:
            case EUCLIDEAN_PATTERN:
            default:
                throw new IllegalArgumentException("No editor for data type: " + property.type);
        }
        editor.setUserData(property.name);
        editor.minWidth(200.0);
        if (property.hasParametrizable()) {
            final Overridable overridable = new Overridable();
            overridable.setEditorControl(editor, property);
            propertiesGrid.addRow(rowIndex, new Label(property.label), overridable);
        } else {
            propertiesGrid.addRow(rowIndex, new Label(property.label), editor);
        }
    }

    private ComboBox getComboBox(final PropertyModel propertyModel, final Collection<Object> values) {
        return getComboBox(propertyModel, values.toArray());
    }

    private ComboBox getComboBox(final PropertyModel propertyModel, final Object[] values) {
        final ComboBox comboBox = new ComboBox(FXCollections.observableArrayList(values));
        if (propertyModel.getValue() != null)
            comboBox.setValue(propertyModel.getValue());
        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            getModel().getProperties().get(comboBox.getUserData()).setValue(unwrap(newValue));
        });

        return comboBox;
    }

    private CheckBox getCheckBox(final PropertyModel propertyModel) {
        final CheckBox checkBox = new CheckBox();
        if (propertyModel.getValue() != null)
            checkBox.setSelected(Boolean.parseBoolean(propertyModel.getValue().toString()));
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            getModel().getProperties().get(checkBox.getUserData()).setValue(unwrap(newValue));
        });

        return checkBox;
    }

    private TextField getTextField(final PropertyModel propertyModel) {
        final TextField textField = new TextField();
        if (propertyModel.getValue() != null)
            textField.setText(propertyModel.getValue().toString());
        textField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            getModel().getProperties().get(textField.getUserData()).setValue(unwrap(newValue));
        });
        return textField;
    }

    private Spinner getIntegerSpinner(final PropertyModel propertyModel) {
        final int min = propertyModel.min instanceof Integer ? (int) propertyModel.min : Integer.MIN_VALUE;
        final int max = propertyModel.max instanceof Integer ? (int) propertyModel.max : Integer.MAX_VALUE;
        final int initial = propertyModel.getValue() instanceof Integer ? (int) propertyModel.getValue() :
                (0 >= min && 0 <= max) ? 0 : min;
        final Spinner spinner = new Spinner(min, max, initial);
        spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            getModel().getProperties().get(spinner.getUserData()).setValue(unwrap(newValue));
        });
        return spinner;
    }

    public static class Model {
        private final Map<String, PropertyModel> properties = new HashMap<>();

        private Model() {
        }

        public static Model of(final Parametrizable parametrizable) {
            final Model model = new Model();
            parametrizable.parameters().forEach(p -> model.properties.put(p.name(), PropertyModel.of(parametrizable, p)));
            return model;
        }

        public static Model of(final Collection<PropertyModel> properties) {
            final Model model = new Model();
            properties.forEach(p -> model.properties.put(p.name, p));
            return model;
        }

        public static Model of(final PropertyModel... properties) {
          return of(asList(properties));
        }

        public Map<String, PropertyModel> getProperties() {
            return properties;
        }

        @Override
        public String toString() {
            return "Model{" +
                    "properties=" + properties +
                    '}';
        }
    }

    public static class PropertyModel implements Comparable<PropertyModel> {
        private final String name;
        private final String label;
        private final int order;
        private final DataType type;
        private Object value;
        private final Object min;
        private final Object max;
        private final Collection<Object> domain;
        private final Parametrizable parametrizable;
        private final boolean overridable;

        private PropertyModel(final String name, final String label, final int order, final DataType type,
                              final Object value, final Object min, final Object max, final Collection<Object> domain,
                              final Parametrizable parametrizable, final boolean overridable) {
            this.name = name;
            this.label = label;
            this.order = order;
            this.type = type;
            this.value = value;
            this.min = min;
            this.max = max;
            this.domain = domain;
            this.parametrizable = parametrizable;
            this.overridable = overridable;
        }

        public static PropertyModel of(final String name, final String label, final int order, final DataType type,
                                       final Object value, final Collection<Object> domain) {
            return new PropertyModel(name, label, order,  type, value, null, null, domain, null,
                    false);
        }

        public static PropertyModel of(final String name, final String label, final int order, final DataType type,
                                       final Object value) {
            return new PropertyModel(name, label, order, type, value, null, null, null,
                    null, false);
        }

        public static PropertyModel of(final String name, final String label, final int order, final DataType type,
                                       final Object value, final Object min, final Object max) {
            return new PropertyModel(name, label, order, type, value, min, max, null, null,
                    false);
        }

        public static PropertyModel of(final Parametrizable parametrizable, final Parameter parameter) {
            if (parametrizable == null)
                throw new IllegalArgumentException("parametrizable");

            return new PropertyModel(parameter.name(), parameter.label(), parameter.order(), parameter.dataType(),
                    parameter.defaultValue(), parameter.min(), parameter.max(), parameter.domain(), parametrizable,
                    parameter.isOverridable());
        }

        public String getName() {
            return name;
        }

        public String getLabel() {
            return label;
        }

        public int getOrder() {
            return order;
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

        public Parametrizable getParametrizable() {
            return parametrizable;
        }

        public boolean hasParametrizable() {
            return parametrizable != null;
        }

        public boolean isOverridable() {
            return overridable;
        }

        @Override
        public String toString() {
            return "Property{" +
                    "name='" + name + '\'' +
                    ", label='" + label + '\'' +
                    ", order=" + order +
                    ", type=" + type +
                    ", value=" + value +
                    ", min=" + min +
                    ", max=" + max +
                    ", domain=" + domain +
                    ", overridable=" + overridable +
                    '}';
        }

        @Override
        public int compareTo(PropertyModel o) {
            return Integer.compare(this.order, o.order);
        }
    }
}
