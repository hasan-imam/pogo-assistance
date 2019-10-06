package pogo.assistance.data.extraction.source.discord;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DespawnTimeParserUtilsTest {

    @ParameterizedTest
    @MethodSource(value = { "validDespawnTimeInputs" })
    void extractDespawnTime_HappyCases_ExtractsSuccessfully(final String compiledText, final Duration despawnTime) {
        assertEquals(despawnTime, DespawnTimeParserUtils.extractSpawnDuration(compiledText, true).get());
    }

    @ParameterizedTest
    @MethodSource(value = { "invalidDespawnTimeInputs" })
    void extractDespawnTime_InvalidCases_ReturnsEmpty(final String compiledText, final String invalidReason) {
        assertFalse(
                DespawnTimeParserUtils.extractSpawnDuration(compiledText, true).isPresent(),
                "Parsing should have failed. Reason: " + invalidReason);
    }

    private static Object[][] validDespawnTimeInputs() {
        return new Object[][] {
                new Object[] { ":timer::white_check_mark: 22m43s (12:26:12 PM)", Duration.ofSeconds(22 * 60 + 43) }
        };
    }

    private static Object[][] invalidDespawnTimeInputs() {
        return new Object[][] {
                new Object[] { ":timer::white_check_mark: ______", "Doesn't contain duration data" },
                new Object[] { ":timer: 19m58s (12:23:02 PM)", "Doesn't indicate whether this time has been verified" }
        };
    }
}