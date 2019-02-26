package pogo.assistance.data.serde.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.Duration;
import javax.annotation.Nullable;

public class DurationTypeAdapter extends TypeAdapter<Duration> {
    @Override
    public void write(final JsonWriter out, @Nullable final Duration value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        out.value(value.toNanos());
    }

    @Override
    public Duration read(final JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        return Duration.ofNanos(in.nextLong());
    }
}
