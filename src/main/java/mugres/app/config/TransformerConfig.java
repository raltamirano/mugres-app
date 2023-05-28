package mugres.app.config;

import mugres.common.Pitch;

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

}
