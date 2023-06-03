package mugres.app.control;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import mugres.common.DataType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class KeyValueEditor extends AnchorPane {
    private Model model;
    @FXML
    private Label titleLabel;
    @FXML
    private TableView propertiesTable;
    @FXML
    private TableColumn keyColumn;
    @FXML
    private TableColumn valueColumn;

    public KeyValueEditor() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/mugres/app/control/kve/key-value-editor.fxml"));
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
        keyColumn.setCellValueFactory(new PropertyValueFactory<>("label"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
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
        return keyColumn.getText();
    }

    public void setKeyHeader(final String text) {
        keyColumn.setText(text);
    }

    public String getValueHeader() {
        return valueColumn.getText();
    }

    public void setValueHeader(final String text) {
        valueColumn.setText(text);
    }

    private void loadModel() {
        propertiesTable.getItems().clear();
        propertiesTable.getItems().addAll(model.properties.values());
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
    }

    public static class Property {
        private final String name;
        private final String label;
        private final DataType type;
        private Object value;

        private Property(final String name, final String label, final DataType type, final Object value) {
            this.name = name;
            this.label = label;
            this.type = type;
            this.value = value;
        }

        public static Property of(final String name, final String label, final DataType type, final Object value) {
            return new Property(name, label, type, value);
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
    }
}
