package mugres.pedalboard.config;

public class PedalboardConfig {
    private String name;
    private Processor processor;
    private DrummerConfig drummerConfig;
    private TransformerConfig transformerConfig;

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

    public enum Processor {
        DRUMMER,
        TRANSFORMER
    }
}
