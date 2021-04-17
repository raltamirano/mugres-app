package mugres.pedalboard.config;

import static mugres.pedalboard.config.MUGRESConfig.GSON;

public class PedalboardConfig implements Cloneable {
    private String name;
    private Processor processor;
    private DrummerConfig drummerConfig;
    private TransformerConfig transformerConfig;
    private transient MUGRESConfig mugresConfig;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Processor getProcessor() {
        return processor;
    }

    public void setProcessor(Processor processor) {
        this.processor = processor;
    }

    public DrummerConfig getDrummerConfig() {
        return drummerConfig;
    }

    public void setDrummerConfig(DrummerConfig drummerConfig) {
        this.drummerConfig = drummerConfig;
    }

    public TransformerConfig getTransformerConfig() {
        return transformerConfig;
    }

    public void setTransformerConfig(TransformerConfig transformerConfig) {
        this.transformerConfig = transformerConfig;
    }

    public MUGRESConfig getMUGRESConfig() {
        return mugresConfig;
    }

    public void setMUGRESConfig(MUGRESConfig mugresConfig) {
        this.mugresConfig = mugresConfig;
    }

    @Override
    public PedalboardConfig clone() {
        return GSON.fromJson(GSON.toJson(this), this.getClass());
    }

    public enum Processor {
        DRUMMER,
        TRANSFORMER
    }
}
