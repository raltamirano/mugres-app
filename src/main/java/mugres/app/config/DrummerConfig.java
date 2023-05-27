package mugres.app.config;

import mugres.common.DrumKit;
import mugres.common.TimeSignature;
import mugres.common.Variant;
import mugres.live.processor.drummer.Drummer;

import java.util.ArrayList;
import java.util.List;

public class DrummerConfig {
    private List<Control> controls = new ArrayList<>();

    public List<Control> getControls() {
        return controls;
    }

    public static class Control {
        private String title;
        private int number;
        private Command command;
        private Generator generator;
        private int lengthInMeasures;
        private int tempo;
        private TimeSignature timeSignature;
        private Drummer.SwitchMode switchMode;
        private List<Variant> mainVariants = new ArrayList<>();
        private List<Variant> fillVariants = new ArrayList<>();
        private List<DrumKit> hitOptions = new ArrayList<>();
        private int hitVelocity;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        public Command getCommand() {
            return command;
        }

        public void setCommand(Command command) {
            this.command = command;
        }

        public Generator getGenerator() {
            return generator;
        }

        public void setGenerator(Generator generator) {
            this.generator = generator;
        }

        public int getLengthInMeasures() {
            return lengthInMeasures;
        }

        public void setLengthInMeasures(int lengthInMeasures) {
            this.lengthInMeasures = lengthInMeasures;
        }

        public int getTempo() {
            return tempo;
        }

        public void setTempo(int tempo) {
            this.tempo = tempo;
        }

        public TimeSignature getTimeSignature() {
            return timeSignature;
        }

        public void setTimeSignature(TimeSignature timeSignature) {
            this.timeSignature = timeSignature;
        }

        public Drummer.SwitchMode getSwitchMode() {
            return switchMode;
        }

        public void setSwitchMode(Drummer.SwitchMode switchMode) {
            this.switchMode = switchMode;
        }

        public List<Variant> getMainVariants() {
            return mainVariants;
        }

        public List<Variant> getFillVariants() {
            return fillVariants;
        }

        public List<DrumKit> getHitOptions() {
            return hitOptions;
        }

        public int getHitVelocity() {
            return hitVelocity;
        }

        public void setHitVelocity(int hitVelocity) {
            this.hitVelocity = hitVelocity;
        }

        public enum Command {
            PLAY,
            HIT,
            FINISH,
            STOP,
            NOOP
        }

        public enum Generator {
            HALF_TIME,
            BLAST_BEAT
        }
    }
}
