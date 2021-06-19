package mugres.apps.pedalboard.config;

import mugres.core.common.Pitch;

import java.util.*;

public class TransformerConfig {
    private List<Button> buttons = new ArrayList<>();
    private List<Filter> filters = new ArrayList<>();
    private final List<Signaler> signalers = new ArrayList<>();
    private ContextConfig context;

    public List<Button> getButtons() {
        return buttons;
    }

    public void setButtons(List<Button> buttons) {
        this.buttons = buttons;
    }

    public Button getButton(final int number) {
        return buttons.stream().filter(b -> b.number == number).findFirst().orElse(null);
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    public List<Signaler> getSignalers() {
        return signalers;
    }

    public ContextConfig getContext() {
        return context;
    }

    public void setContext(ContextConfig context) {
        this.context = context;
    }

    public static class Button {
        private int number;
        private int midi;
        private String label;

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        public int getMidi() {
            return midi;
        }

        public void setMidi(int midi) {
            this.midi = midi;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public Pitch getPitch() {
            return Pitch.of(midi);
        }
    }

    public static class Filter {
        private String filter;
        private Map<String, Object> args = new HashMap<>();

        public String getFilter() {
            return filter;
        }

        public void setFilter(String filter) {
            this.filter = filter;
        }

        public Map<String, Object> getArgs() {
            return args;
        }

        public void setArgs(Map<String, Object> args) {
            this.args = args;
        }
    }

    public static class Signaler {
        private Frequency frequency;
        private Set<String> tags = new HashSet<>();
        private String duration;

        public Frequency getFrequency() {
            return frequency;
        }

        public void setFrequency(Frequency frequency) {
            this.frequency = frequency;
        }

        public Set<String> getTags() {
            return tags;
        }

        public void setTags(Set<String> tags) {
            this.tags = tags;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public static class Frequency {
            private Mode mode;
            private Object value;

            public Mode getMode() {
                return mode;
            }

            public void setMode(Mode mode) {
                this.mode = mode;
            }

            public Object getValue() {
                return value;
            }

            public void setValue(Object value) {
                this.value = value;
            }

            public enum Mode {
                FIXED;
            }
        }
    }
}
