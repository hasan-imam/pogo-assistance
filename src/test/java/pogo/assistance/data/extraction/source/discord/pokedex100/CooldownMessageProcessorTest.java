package pogo.assistance.data.extraction.source.discord.pokedex100;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.jenetics.jpx.Length.Unit;
import java.time.Duration;
import net.dv8tion.jda.core.entities.Message;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pogo.assistance.data.extraction.source.discord.MessageProcessor;

class CooldownMessageProcessorTest {

    private final MessageProcessor<CooldownRecord> processor = new CooldownMessageProcessor();

    @ParameterizedTest(name = "Match case # {index}")
    @MethodSource("validMsgCases")
    void process_ValidMessageCasesV1_ReturnsExpected(
            final double lat1, final double lon1,
            final double lat2, final double lon2,
            final String distanceSegment, final double distance,
            final String cooldownSegmentV1, final String __, final Duration cooldown) {
        final Message message = mock(Message.class);
        when(message.getContentStripped())
                .thenReturn(String.format(
                        CooldownMessageProcessor.FORMAT_DIST_CMD_RESPONSE_V1,
                        lat1, lon1, lat2, lon2, distanceSegment, cooldownSegmentV1));
        final CooldownRecord cooldownRecord = processor.process(message)
                .orElseThrow(() -> new AssertionError("Failed to process message: " + message.getContentStripped()));
        verifyRecordMatch(lat1, lon1, lat2, lon2, distance, cooldown, cooldownRecord);
    }

    @ParameterizedTest(name = "Match case # {index}")
    @MethodSource("validMsgCases")
    void process_ValidMessageCasesV2_ReturnsExpected(
            final double lat1, final double lon1,
            final double lat2, final double lon2,
            final String distanceSegment, final double distance,
            final String __, final String cooldownSegmentV2, final Duration cooldown) {
        final Message message = mock(Message.class);
        when(message.getContentStripped())
                .thenReturn(String.format(
                        CooldownMessageProcessor.FORMAT_DIST_CMD_RESPONSE_V2,
                        lat1, lon1, lat2, lon2, distanceSegment, cooldownSegmentV2));
        final CooldownRecord cooldownRecord = processor.process(message)
                .orElseThrow(() -> new AssertionError("Failed to process message: " + message.getContentStripped()));
        verifyRecordMatch(lat1, lon1, lat2, lon2, distance, cooldown, cooldownRecord);
    }

    private static void verifyRecordMatch(
            // Expected
            final double lat1, final double lon1,
            final double lat2, final double lon2,
            final double distance, final Duration cooldown,
            // Actual
            final CooldownRecord cooldownRecord) {
        assertEquals(lat1, cooldownRecord.fromPoint().getLatitude().toDegrees());
        assertEquals(lon1, cooldownRecord.fromPoint().getLongitude().toDegrees());
        assertEquals(lat2, cooldownRecord.toPoint().getLatitude().toDegrees());
        assertEquals(lon2, cooldownRecord.toPoint().getLongitude().toDegrees());
        assertEquals(distance, cooldownRecord.pokedex100Distance().get().to(Unit.METER));
        assertEquals(cooldown, cooldownRecord.pokedex100Cooldown().get());
    }

    private static Object[][] validMsgCases() {
        return new Object[][] {
                new Object[] { 40.73781921696575, -73.97955279078059, 40.741762, -73.984418,
                        "600.19 meters", 600.19, "1 min.", "1 minute,", Duration.ofMinutes(1) },
                new Object[] { 41.65903219672377, -91.52945894747971, 35.662105, 139.735448,
                        "9958.46 kilometers", 9958460, "120 min.", "120 minutes (2 hours),", Duration.ofMinutes(120) },
                new Object[] { 37.88674, 139.06309, 35.698382, 139.7730742,
                        "251.41 kilometers", 251410, "46 min.", "46 minutes,", Duration.ofMinutes(46) },
                new Object[] { 40.761795183124995, -73.98512046239155, 40.764128, -73.98125,
                        "416.59 meters", 416.59, "0 min.", "0 minute,", Duration.ofMinutes(0) },
                new Object[] { 1.304978, 103.877949, 1.304978, 103.877949,
                        "0.00 meter", 0, "0 min.", "0 minute,", Duration.ofMinutes(0) },
                new Object[] { 27.171818, 78.042904, 23.8350367, 91.2701091,
                        "1377.53 kilometers", 1377530, "120 min.", "120 minutes (2 hours),", Duration.ofMinutes(120) },
                new Object[] { 45.499217, -73.854115, 40.752921, -73.972047,
                        "527.85 kilometers", 527850, "65 min.", "65 minutes (1 hour and 5 minutes),", Duration.ofMinutes(65) }
        };
    }
}