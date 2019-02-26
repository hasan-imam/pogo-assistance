package pogo.assistance.data.serde.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.jenetics.jpx.Point;
import io.jenetics.jpx.WayPoint;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class GsonAdaptersPointTest {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Dhaka");

    @ParameterizedTest
    @MethodSource("validObjectCases")
    void rw_RoundTrip_NoErrorOrDataLoss(final Point expectedPoint, final String expectedSerialized) {
        final Gson gson = buildGson();
        final Point deserialized = gson.fromJson(expectedSerialized, Point.class);
        assertEquals(expectedPoint, deserialized);
        assertEquals(expectedSerialized, gson.toJson(deserialized));
    }

    @Test
    void rw_RoundTripWithUnkonwnFields_IgnoresUnknownFields() {
        final Point expectedPoint = WayPoint.builder().lat(1.2345).lon(-1.2345).build();
        final String expectedSerialized = "{\"latitude\":1.2345,\"longitude\":-1.2345}";
        final Gson gson = buildGson();

        final JsonObject withUnknownFields = gson.toJsonTree(expectedPoint).getAsJsonObject();
        withUnknownFields.addProperty("unknownString", "string");
        withUnknownFields.addProperty("unknownNumber", -1.2345);

        final Point deserialized = gson.fromJson(withUnknownFields.toString(), Point.class);
        assertEquals(expectedPoint, deserialized);
        assertEquals(expectedSerialized, gson.toJson(deserialized));
    }

    @Test
    void rw_RoundTripWithNullOptionalField_IgnoresNullFields() {
        final Point expectedPoint = WayPoint.builder().lat(1.2345).lon(-1.2345).build();
        final String serializedWithoutNulls = "{\"latitude\":1.2345,\"longitude\":-1.2345}";
        final String serializedWithNulls = "{\"latitude\":1.2345,\"longitude\":-1.2345,\"elevation\":null,\"time\":null}";
        final Gson gson = buildGson();
        final Point deserialized = gson.fromJson(serializedWithNulls, Point.class);
        assertEquals(expectedPoint, deserialized);
        assertEquals(serializedWithoutNulls, gson.toJson(deserialized));
    }

    private static Object[][] validObjectCases() {
        return new Object[][] {
                new Object[] { // with only mandatory fields
                        WayPoint.builder().lat(1.2345).lon(-1.2345).build(),
                        "{\"latitude\":1.2345,\"longitude\":-1.2345}"
                },
                new Object[] { // with time
                        WayPoint.builder().lat(1.2345).lon(-1.2345).time(Instant.EPOCH, ZONE_ID).build(),
                        "{\"latitude\":1.2345,\"longitude\":-1.2345,\"time\":\"" + Instant.EPOCH.atZone(ZONE_ID) + "\"}"
                },
                new Object[] { // with elevation
                        WayPoint.builder().lat(1.2345).lon(-1.2345).ele(1.2345).build(),
                        "{\"latitude\":1.2345,\"longitude\":-1.2345,\"elevation\":1.2345}"
                },
                new Object[] { // with both time and elevation
                        WayPoint.builder().lat(1.2345).lon(-1.2345).ele(1.2345).time(Instant.EPOCH, ZONE_ID).build(),
                        "{\"latitude\":1.2345,\"longitude\":-1.2345,\"elevation\":1.2345,\"time\":\"" + Instant.EPOCH.atZone(ZONE_ID) + "\"}"
                }
        };
    }

    private static Gson buildGson() {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapterFactory(new GsonAdaptersPoint());
        return gsonBuilder.create();
    }
}