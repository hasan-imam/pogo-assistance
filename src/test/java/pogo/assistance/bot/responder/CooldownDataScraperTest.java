package pogo.assistance.bot.responder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pogo.assistance.bot.responder.CooldownDataScraper.ResponseData;

class CooldownDataScraperTest {

    @ParameterizedTest(name = "Match case # {index}")
    @MethodSource("validMsgCases")
    void fromResponseMessage_MatchCase_ReturnsExpected(
            final double lat1, final double lon1,
            final double lat2, final double lon2,
            final String distanceSegment, final double distance,
            final String cooldownSegment, final Duration cooldown) {
        final String messageContent = String.format(CooldownDataScraper.FORMAT_DIST_CMD_RESPONSE,
                lat1, lon1, lat2, lon2, distanceSegment, cooldownSegment);
        final ResponseData data = ResponseData.fromResponseMessage(messageContent);
        assertEquals(lat1, data.getFromLat());
        assertEquals(lon1, data.getFromLon());
        assertEquals(lat2, data.getToLat());
        assertEquals(lon2, data.getToLon());
        assertEquals(distance, data.getDistanceInMeters());
        assertEquals(cooldown, data.getCooldown());
    }

    // The distance between 40.73781921696575,-73.97955279078059 and 40.741762,-73.984418 is 600.19 meters.
    // The suggested cooldown time is 1 minute, @ᴀᴀʀᴏ́ɴ.
    private static Object[][] validMsgCases() {
        return new Object[][] {
                new Object[] { 40.73781921696575, -73.97955279078059, 40.741762, -73.984418,
                        "600.19 meters", 600.19, "1 minute", Duration.ofMinutes(1) },
                new Object[] { 41.65903219672377, -91.52945894747971, 35.662105, 139.735448,
                        "9958.46 kilometers", 9958460, "120 minutes (2 hours)", Duration.ofMinutes(120) },
                new Object[] { 37.88674, 139.06309, 35.698382, 139.7730742,
                        "251.41 kilometers", 251410, "46 minutes", Duration.ofMinutes(46) },
                new Object[] { 40.761795183124995, -73.98512046239155, 40.764128, -73.98125,
                        "416.59 meters", 416.59, "0 minute", Duration.ofMinutes(0) },
                new Object[] { 1.304978, 103.877949, 1.304978, 103.877949,
                        "0.00 meter", 0, "0 minute", Duration.ofMinutes(0) },
                new Object[] { 27.171818, 78.042904, 23.8350367, 91.2701091,
                        "1377.53 kilometers", 1377530, "120 minutes (2 hours)", Duration.ofMinutes(120) },
                new Object[] { 45.499217, -73.854115, 40.752921, -73.972047,
                        "527.85 kilometers", 527850, "65 minutes (1 hour and 5 minutes)", Duration.ofMinutes(65) }
        };
    }
}