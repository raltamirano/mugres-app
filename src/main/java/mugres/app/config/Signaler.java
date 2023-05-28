package mugres.app.config;

import java.util.HashSet;
import java.util.Set;

public class Signaler {
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
        private Frequency.Mode mode;
        private Object value;

        public Frequency.Mode getMode() {
            return mode;
        }

        public void setMode(Frequency.Mode mode) {
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
