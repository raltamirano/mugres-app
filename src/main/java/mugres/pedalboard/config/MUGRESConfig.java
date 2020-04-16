package mugres.pedalboard.config;

import com.google.gson.Gson;
import mugres.core.common.DrumKit;
import mugres.core.common.TimeSignature;
import mugres.core.live.processors.drummer.Drummer;
import mugres.core.live.processors.drummer.Drummer.SwitchMode;
import mugres.pedalboard.config.DrummerConfig.Button.Command;
import mugres.pedalboard.config.DrummerConfig.Button.Generator;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class MUGRESConfig {
    private String midiInputPort;
    private String midiOutputPort;
    private int midiInputChannel;
    private List<PedalboardConfig> pedalboardConfigs = new ArrayList<>();

    public String getMidiInputPort() {
        return midiInputPort;
    }

    public void setMidiInputPort(String midiInputPort) {
        this.midiInputPort = midiInputPort;
    }

    public String getMidiOutputPort() {
        return midiOutputPort;
    }

    public void setMidiOutputPort(String midiOutputPort) {
        this.midiOutputPort = midiOutputPort;
    }

    public int getMidiInputChannel() {
        return midiInputChannel;
    }

    public void setMidiInputChannel(int midiInputChannel) {
        this.midiInputChannel = midiInputChannel;
    }

    public List<PedalboardConfig> getPedalboardConfigs() {
        return pedalboardConfigs;
    }

    public void setPedalboardConfigs(List<PedalboardConfig> pedalboardConfigs) {
        this.pedalboardConfigs = pedalboardConfigs;
    }

    public static MUGRESConfig read() {
        try {
            final File configFile = getConfigFile();
            if (!configFile.exists())
                return MUGRESConfig.defaultConfig();

            try (final Reader reader = Files.newBufferedReader(configFile.toPath())) {
                return GSON.fromJson(reader, MUGRESConfig.class);
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static MUGRESConfig defaultConfig() {
        final MUGRESConfig config = new MUGRESConfig();

        config.setMidiInputPort(System.getenv("mugres.inputPort"));
        config.setMidiOutputPort(System.getenv("mugres.outputPort"));
        config.setMidiInputChannel(1);

        final PedalboardConfig pedalboard = new PedalboardConfig();
        pedalboard.setName("Default");
        pedalboard.setProcessor(PedalboardConfig.Processor.DRUMMER);
        final DrummerConfig drummerConfig = new DrummerConfig();

        final DrummerConfig.Button button1 = new DrummerConfig.Button();
        button1.setNumber(1);
        button1.setTitle("Blast Beat");
        button1.setCommand(Command.PLAY);
        button1.setGenerator(Generator.BLAST_BEAT);
        button1.setLengthInMeasures(4);
        button1.setTempo(190);
        button1.setTimeSignature(TimeSignature.TS44);
        button1.setSwitchMode(SwitchMode.IMMEDIATELY_FILL);
        drummerConfig.getButtons().add(button1);

        final DrummerConfig.Button button2 = new DrummerConfig.Button();
        button2.setNumber(2);
        button2.setTitle("Half Time");
        button2.setCommand(Command.PLAY);
        button2.setGenerator(Generator.HALF_TIME);
        button2.setLengthInMeasures(4);
        button2.setTempo(110);
        button2.setTimeSignature(TimeSignature.TS44);
        button2.setSwitchMode(SwitchMode.IMMEDIATELY_FILL);
        drummerConfig.getButtons().add(button2);

        final DrummerConfig.Button button3 = new DrummerConfig.Button();
        button3.setNumber(3);
        button3.setTitle("Crash Cymbal hit");
        button3.setCommand(Command.HIT);
        button3.getHitOptions().add(DrumKit.CR1);
        button3.getHitOptions().add(DrumKit.CR2);
        button3.setHitVelocity(110);
        drummerConfig.getButtons().add(button3);

        final DrummerConfig.Button button4 = new DrummerConfig.Button();
        button4.setNumber(4);
        button4.setTitle("Finish");
        button4.setCommand(Command.FINISH);
        drummerConfig.getButtons().add(button4);

        final DrummerConfig.Button button5 = new DrummerConfig.Button();
        button5.setNumber(5);
        button5.setTitle("Stop now!");
        button5.setCommand(Command.STOP);
        drummerConfig.getButtons().add(button5);

        pedalboard.setDrummerConfig(drummerConfig);
        config.getPedalboardConfigs().add(pedalboard);

        return config;
    }

    public void save() {
        try {
            final File configFile = getConfigFile();
            try(final Writer writer = Files.newBufferedWriter(configFile.toPath())) {
                GSON.toJson(this, writer);
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static File getConfigFile() {
        return new File(System.getProperty("user.home"), "mugres-config.json");
    }

    private static final Gson GSON = new Gson();
}
