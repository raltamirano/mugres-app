package mugres.app.config;

import static mugres.app.config.MUGRESConfig.GSON;

public class ProcessorConfig implements Cloneable {
    private String name;
    private Processor processor;
    private DrummerConfig drummer;
    private TransformerConfig transformer;
    private SpirographoneConfig spirographone;
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

    public DrummerConfig getDrummer() {
        return drummer;
    }

    public void setDrummer(DrummerConfig drummer) {
        this.drummer = drummer;
    }

    public TransformerConfig getTransformer() {
        return transformer;
    }

    public void setTransformer(TransformerConfig transformer) {
        this.transformer = transformer;
    }

    public SpirographoneConfig getSpirographone() {
        return spirographone;
    }

    public void setSpirographone(SpirographoneConfig spirographone) {
        this.spirographone = spirographone;
    }

    public MUGRESConfig getMUGRESConfig() {
        return mugresConfig;
    }

    public void setMUGRESConfig(MUGRESConfig mugresConfig) {
        this.mugresConfig = mugresConfig;
    }

    @Override
    public ProcessorConfig clone() {
        return GSON.fromJson(GSON.toJson(this), this.getClass());
    }

    public enum Processor {
        DRUMMER,
        TRANSFORMER,
        SPIROGRAPHONE
    }
}
