package mugres.pedalboard.config;

import mugres.core.common.Pitch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransformerConfig {
    private List<Button> buttons = new ArrayList<>();
    private List<Filter> filters = new ArrayList<>();

    public List<Button> getButtons() {
        return buttons;
    }

    public void setButtons(List<Button> buttons) {
        this.buttons = buttons;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    public static class Button {
        private int number;
        private Pitch pitch;
        private int octave;
        private String label;

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        public Pitch getPitch() {
            return pitch;
        }

        public void setPitch(Pitch pitch) {
            this.pitch = pitch;
        }

        public int getOctave() {
            return octave;
        }

        public void setOctave(int octave) {
            this.octave = octave;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }
    public static class Filter {
        private String filter;
        private Map<String, Object> arguments = new HashMap<>();

        public String getFilter() {
            return filter;
        }

        public void setFilter(String filter) {
            this.filter = filter;
        }

        public Map<String, Object> getArguments() {
            return arguments;
        }

        public void setArguments(Map<String, Object> arguments) {
            this.arguments = arguments;
        }
    }
}
