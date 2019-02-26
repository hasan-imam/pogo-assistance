package pogo.assistance.data.serde.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import io.jenetics.jpx.Latitude;
import io.jenetics.jpx.Longitude;
import io.jenetics.jpx.Point;
import io.jenetics.jpx.WayPoint;
import java.io.IOException;
import java.time.ZonedDateTime;
import javax.annotation.Nullable;

public class GsonAdaptersPoint implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> type) {
        if (adapts(type)) {
            return (TypeAdapter<T>) new PointTypeAdapter();
        }
        return null;
    }

    private static boolean adapts(TypeToken<?> type) {
        return Point.class == type.getRawType()
                || WayPoint.class == type.getRawType();
    }

    private static class PointTypeAdapter extends TypeAdapter<Point> {
        private static final String NAME_LATITUDE = "latitude";
        private static final String NAME_LONGITUDE = "longitude";
        private static final String NAME_ELEVATION = "elevation";
        private static final String NAME_TIME = "time";

        @Override
        public void write(final JsonWriter out, @Nullable final Point point) throws IOException {
            if (point == null) {
                out.nullValue();
                return;
            }

            out.beginObject();
            out.name(NAME_LATITUDE).value(point.getLatitude().toDegrees());
            out.name(NAME_LONGITUDE).value(point.getLongitude().toDegrees());
            if (point.getElevation().isPresent()) {
                out.name(NAME_ELEVATION).value(point.getElevation().get().doubleValue());
            }
            if (point.getTime().isPresent()) {
                out.name(NAME_TIME).value(point.getTime().get().toString());
            }
            out.endObject();
        }

        @Override
        public Point read(final JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }

            final WayPoint.Builder builder = WayPoint.builder();
            in.beginObject();
            while (in.hasNext()) {
                final String fieldName = in.nextName();

                if (in.peek() == JsonToken.NULL) { // Skip nulls
                    in.nextNull();
                    continue;
                }

                switch (fieldName) {
                    case NAME_LATITUDE:
                        builder.lat(Latitude.ofDegrees(in.nextDouble())); break;
                    case NAME_LONGITUDE:
                        builder.lon(Longitude.ofDegrees(in.nextDouble())); break;
                    case NAME_ELEVATION:
                        builder.ele(in.nextDouble()); break;
                    case NAME_TIME:
                        builder.time(ZonedDateTime.parse(in.nextString())); break;
                    default: // Skip unknown
                        in.nextString(); break;
                }
            }
            in.endObject();
            return builder.build();
        }
    }
}
