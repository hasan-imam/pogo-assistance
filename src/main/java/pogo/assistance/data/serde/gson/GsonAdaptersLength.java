package pogo.assistance.data.serde.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import io.jenetics.jpx.Length;
import io.jenetics.jpx.Length.Unit;
import java.io.IOException;
import javax.annotation.Nullable;

public class GsonAdaptersLength implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> type) {
        if (adapts(type)) {
            return (TypeAdapter<T>) new LengthTypeAdapter();
        }
        return null;
    }

    private static boolean adapts(TypeToken<?> type) {
        return Length.class == type.getRawType();
    }

    private static class LengthTypeAdapter extends TypeAdapter<Length> {

        @Override
        public void write(final JsonWriter out, @Nullable final Length length) throws IOException {
            if (length == null) {
                out.nullValue();
                return;
            }

            out.value(length.to(Unit.METER));
        }

        @Override
        public Length read(final JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }

            return Length.of(in.nextDouble(), Unit.METER);
        }
    }

}
