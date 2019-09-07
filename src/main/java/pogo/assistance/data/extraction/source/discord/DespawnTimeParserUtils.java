package pogo.assistance.data.extraction.source.discord;

import com.google.common.primitives.Longs;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DespawnTimeParserUtils {

    private static final Pattern TIME_STRING =
            Pattern.compile("^(?<prefix>.*)"
                    + "(?<hours>\\d{1,2}):(?<minutes>\\d{1,2})(:(?<seconds>\\d{1,2}))?"
                    + "([\\s\\(](?<meridian>AM|PM)[\\s\\)])?"
                    + "(?<suffix>.*)$");

    /**
     * Assumption: despawn time period strings do not contain hour input since the time is shorter
     */
    private static final Pattern PERIOD_STRING =
            Pattern.compile("^(?<prefix>.*)" // matches (most) things before the period data
                    + "[\\(\\s]" // a boundary - either a white space or brace
                    + "(?<minutes>\\d|([0-6]\\d))m"
                    + "[\\s]?" // some texts have space between minutes and seconds, some don't
                    + "(?<seconds>\\d|([0-6]\\d))s"
                    + "\\)?"
                    + "(?<suffix>.*)$"); // matches things after the period data

    public static Optional<Instant> extractDespawnTime(final String compiledText) {
        final Matcher matcher = PERIOD_STRING.matcher(compiledText);
        if (!matcher.find() || !isDespawnTimeConfirmed(matcher.group("prefix"), matcher.group("suffix"))) {
            return Optional.empty();
        }
        final Duration despawnTimeLeft = Duration.ofMinutes(Longs.tryParse(matcher.group("minutes")))
                .plus(Duration.ofSeconds(Longs.tryParse(matcher.group("seconds"))));
        return Optional.of(Instant.now().plus(despawnTimeLeft));
    }

    /**
     * @param prefix
     *      Part of the line before the period string
     * @param suffix
     *      Part of the line after the period string
     * @return
     *      True if prefix of suffix indicates that the despawn time is confirmed. It checks for things like check
     *      marks or other indications of confirmation.
     */
    private static boolean isDespawnTimeConfirmed(final String prefix, final String suffix) {
        final String combined = prefix + suffix;
        if (combined.contains(":white_check_mark:") || combined.contains(":check_yes:")) {
            return true;
        }
        return false;
    }

}
