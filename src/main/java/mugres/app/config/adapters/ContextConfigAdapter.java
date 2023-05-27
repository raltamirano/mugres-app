package mugres.app.config.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import mugres.common.Key;
import mugres.common.TimeSignature;
import mugres.app.config.ContextConfig;

import java.io.IOException;

public class ContextConfigAdapter extends TypeAdapter<ContextConfig> {
    @Override
    public void write(final JsonWriter jsonWriter, final ContextConfig contextConfig) throws IOException {
        jsonWriter
                .beginObject()
                .name("tempo").value(contextConfig.getTempo())
                .name("key").value(contextConfig.getKey().label())
                .name("timeSignature").value(contextConfig.getTimeSignature().toString())
                .endObject()
                ;
    }

    @Override
    public ContextConfig read(final JsonReader jsonReader) throws IOException {
        final ContextConfig contextConfig = new ContextConfig();

        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch(jsonReader.nextName()) {
                case "tempo":
                    contextConfig.setTempo(jsonReader.nextInt());
                    break;
                case "key":
                    contextConfig.setKey(Key.fromLabel(jsonReader.nextString()));
                    break;
                case "timeSignature":
                    contextConfig.setTimeSignature(TimeSignature.of(jsonReader.nextString()));
                    break;
            }
        }
        jsonReader.endObject();

        return contextConfig;
    }
}
