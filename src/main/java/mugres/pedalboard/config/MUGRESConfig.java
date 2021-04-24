package mugres.pedalboard.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mugres.core.common.DrumKit;
import mugres.core.common.TimeSignature;
import mugres.core.live.processor.drummer.Drummer.SwitchMode;
import mugres.pedalboard.config.DrummerConfig.Control.Command;
import mugres.pedalboard.config.DrummerConfig.Control.Generator;
import mugres.pedalboard.config.adapters.ContextConfigAdapter;

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
    private List<PedalboardConfig> pedalboards = new ArrayList<>();
    private ContextConfig context;

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

    public List<PedalboardConfig> getPedalboards() {
        return pedalboards;
    }

    public void setPedalboards(List<PedalboardConfig> pedalboards) {
        this.pedalboards = pedalboards;
    }

    public ContextConfig getContext() {
        return context;
    }

    public void setContext(ContextConfig context) {
        this.context = context;
    }

    public static MUGRESConfig read() {
        try {
            final File configFile = getConfigFile();
            if (!configFile.exists())
                return MUGRESConfig.defaultConfig();

            try (final Reader reader = Files.newBufferedReader(configFile.toPath())) {
                final MUGRESConfig config = GSON.fromJson(reader, MUGRESConfig.class);
                for(PedalboardConfig c : config.getPedalboards())
                    c.setMUGRESConfig(config);
                return config;
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

        final DrummerConfig.Control control1 = new DrummerConfig.Control();
        control1.setNumber(1);
        control1.setTitle("Blast Beat");
        control1.setCommand(Command.PLAY);
        control1.setGenerator(Generator.BLAST_BEAT);
        control1.setLengthInMeasures(4);
        control1.setTempo(190);
        control1.setTimeSignature(TimeSignature.TS44);
        control1.setSwitchMode(SwitchMode.IMMEDIATELY_FILL);
        drummerConfig.getControls().add(control1);

        final DrummerConfig.Control control2 = new DrummerConfig.Control();
        control2.setNumber(2);
        control2.setTitle("Half Time");
        control2.setCommand(Command.PLAY);
        control2.setGenerator(Generator.HALF_TIME);
        control2.setLengthInMeasures(4);
        control2.setTempo(110);
        control2.setTimeSignature(TimeSignature.TS44);
        control2.setSwitchMode(SwitchMode.IMMEDIATELY_FILL);
        drummerConfig.getControls().add(control2);

        final DrummerConfig.Control control3 = new DrummerConfig.Control();
        control3.setNumber(3);
        control3.setTitle("Crash Cymbal hit");
        control3.setCommand(Command.HIT);
        control3.getHitOptions().add(DrumKit.CR1);
        control3.getHitOptions().add(DrumKit.CR2);
        control3.setHitVelocity(110);
        drummerConfig.getControls().add(control3);

        final DrummerConfig.Control control4 = new DrummerConfig.Control();
        control4.setNumber(4);
        control4.setTitle("Finish");
        control4.setCommand(Command.FINISH);
        drummerConfig.getControls().add(control4);

        final DrummerConfig.Control control5 = new DrummerConfig.Control();
        control5.setNumber(5);
        control5.setTitle("Stop now!");
        control5.setCommand(Command.STOP);
        drummerConfig.getControls().add(control5);

        pedalboard.setDrummer(drummerConfig);
        config.getPedalboards().add(pedalboard);

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

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(ContextConfig.class, new ContextConfigAdapter())
            .create();
}
