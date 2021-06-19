package mugres.apps.pedalboard.config;

import mugres.core.common.Key;
import mugres.core.common.TimeSignature;

public class ContextConfig {
    private int tempo;
    private Key key;
    private TimeSignature timeSignature;

    public int getTempo() {
        return tempo;
    }

    public void setTempo(int tempo) {
        this.tempo = tempo;
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public TimeSignature getTimeSignature() {
        return timeSignature;
    }

    public void setTimeSignature(TimeSignature timeSignature) {
        this.timeSignature = timeSignature;
    }
}
