package pogo.assistance.data.extraction.source.discord;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled("Uses live links in test data which may not be accessible in future")
class LocationLinkParsingUtilsTest {

    @Test
    void replaceMapRedirectingUrls_TextWithMultipleRedirectingLinks_ReplacesExpected() {
        assertEquals(
                // Expected
                "MapBot\n" +
                "**Cacnea** [Normal] 12/15/10 (82%)\n" +
                "Farmington | ✅ 3:39 PM (*6m 6s*)\n" +
                "Level 32 | CP 1063 FEMALE\n" +
                "GRASS Grass | CLEAR ***Boosted***\n" +
                "Ht: 0.4m | Wt: 49.84kg | all\n" +
                "Poison Sting POISON / Grass Knot GRASS\n" +
                "Directions:\n" +
                "[Google](https://www.google.com/maps?q=40.988951405837,-111.890144) | [Apple](https://www.google.com/maps?daddr=40.988951405837,-111.890144) | [Scan Map](http://link.spindamap.com/1844)",
                // Actual
                LocationLinkParsingUtils.replaceMapRedirectingUrls("MapBot\n" +
                        "**Cacnea** [Normal] 12/15/10 (82%)\n" +
                        "Farmington | ✅ 3:39 PM (*6m 6s*)\n" +
                        "Level 32 | CP 1063 FEMALE\n" +
                        "GRASS Grass | CLEAR ***Boosted***\n" +
                        "Ht: 0.4m | Wt: 49.84kg | all\n" +
                        "Poison Sting POISON / Grass Knot GRASS\n" +
                        "Directions:\n" +
                        "[Google](http://link.spindamap.com/1841) | [Apple](http://link.spindamap.com/1842) | [Scan Map](http://link.spindamap.com/1844)"));
        assertEquals(
                // Expected
                "MapBot\n" +
                        "**Seedot** [Normal] 13/15/13 (91%)\n" +
                        "Centerville | ✅ 3:50 PM (*15m 27s*)\n" +
                        "Level 14 | CP 232 FEMALE\n" +
                        "GRASS Grass | CLEAR ***Boosted***\n" +
                        "Ht: 0.55m | Wt: 5.11kg | all\n" +
                        "Bullet Seed GRASS / Foul Play DARK\n" +
                        "Directions:\n" +
                        "[Google](https://www.google.com/maps?q=40.9247425949881,-111.882222) | [Apple](https://www.google.com/maps?daddr=40.9247425949881,-111.882222) | [Scan Map](http://link.spindamap.com/fkt)",
                // Actual
                LocationLinkParsingUtils.replaceMapRedirectingUrls("MapBot\n" +
                        "**Seedot** [Normal] 13/15/13 (91%)\n" +
                        "Centerville | ✅ 3:50 PM (*15m 27s*)\n" +
                        "Level 14 | CP 232 FEMALE\n" +
                        "GRASS Grass | CLEAR ***Boosted***\n" +
                        "Ht: 0.55m | Wt: 5.11kg | all\n" +
                        "Bullet Seed GRASS / Foul Play DARK\n" +
                        "Directions:\n" +
                        "[Google](https://link.spindamap.com/fkj) | [Apple](http://link.spindamap.com/fkq) | [Scan Map](http://link.spindamap.com/fkt)"));
    }

    @Test
    void getRedirectedUrlUsingDirectConnection_SingleHopRedirection_ReturnsExpected() {
        assertEquals(
                "https://www.google.com/maps?q=40.9247425949881,-111.882222",
                LocationLinkParsingUtils.getRedirectedUrlUsingDirectConnection("https://link.spindamap.com/fkj")
                        .orElse("NOT FOUND"));
    }

    @Test
    void getRedirectedUrlUsingDirectConnection_WithProtocolAndHostRedirection_ReturnsExpected() {
        assertEquals(
                "https://www.google.com/maps?q=40.9247425949881,-111.882222",
                LocationLinkParsingUtils.getRedirectedUrlUsingDirectConnection("http://link.spindamap.com/fkj")
                        .orElse("NOT FOUND"));
    }

    @Test
    void getRedirectedUrlUsingDirectConnection_NoRedirection_ReturnsSame() {
        assertEquals(
                "https://www.google.com/maps?q=40.9247425949881,-111.882222",
                LocationLinkParsingUtils.getRedirectedUrlUsingDirectConnection("https://www.google.com/maps?q=40.9247425949881,-111.882222")
                        .orElse("NOT FOUND"));
    }

}