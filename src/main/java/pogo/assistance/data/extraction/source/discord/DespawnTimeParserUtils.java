package pogo.assistance.data.extraction.source.discord;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Verify;
import com.google.common.primitives.Longs;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
public class DespawnTimeParserUtils {

    /**
     * Verify online: https://regex101.com/r/nAwP1Z/2
     */
    private static final Pattern DESPAWN_TIME_STRING =
            Pattern.compile("(?<hours>[0-2]?\\d)\\:(?<minutes>[0-5]?\\d)(\\:(?<seconds>[0-5]?\\d))?" +
                    "([\\s](?<meridian>AM|PM)[\\s\\\\)])?");

    /**
     * Assumption: Despawn time period strings do not contain hour input since max despawn time is 60 minutes. So period
     * text contains minutes and seconds.
     * Verify online: https://regex101.com/r/pUNJBs/2
     */
    private static final Pattern DESPAWN_PERIOD_STRING =
            Pattern.compile("[\\(\\s]?" // boundary start - either a white space or brace
                    + "(?<minutes>[0-5]?\\d)m"
                    + "[\\s]?" // some texts have space between minutes and seconds, some don't
                    + "(?<seconds>[0-5]?\\d)s"
                    + "[\\)\\s]?"); // boundary end - either a white space or brace

    /**
     * @param compiledText
     *      Text from which data is parsed. Each line of this input is processed separately because spawn time info
     *      usually isn't spread across multiple lines.
     * @return
     *      Extracts duration the spawn has left, then adds the duration to current instant to get despawn time.
     */
    public static Optional<Instant> extractDespawnTime(final String compiledText) {
        return extractSpawnDuration(compiledText).map(Instant.now()::plus);
    }

    @VisibleForTesting
    static Optional<Duration> extractSpawnDuration(final String compiledText) {
        return Stream.of(compiledText.split("\\r?\\n|\\r"))
                .map(DespawnTimeParserUtils::extractSpawnDurationFromLine)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny();
    }

    private static Optional<Duration> extractSpawnDurationFromLine(final String line) {
        final Matcher periodMatcher = DESPAWN_PERIOD_STRING.matcher(line);
        final boolean foundPeriod = periodMatcher.find();
        if (!foundPeriod) {
            return Optional.empty();
        }
        final Duration despawnTimeLeft = Duration.ofMinutes(Longs.tryParse(periodMatcher.group("minutes")))
                .plus(Duration.ofSeconds(Longs.tryParse(periodMatcher.group("seconds"))));

        final StringBuffer sb = new StringBuffer(line.length());
        periodMatcher.appendReplacement(sb, Matcher.quoteReplacement(periodMatcher.group(1)));
        Verify.verify(!periodMatcher.find(), "Shouldn't have found a second period segment in line: %s", line);
        periodMatcher.appendTail(sb);

        final String lineWithoutPeriodSection = sb.toString();
        if (!doesMessageContainSignsOfConfirmation(lineWithoutPeriodSection)) {
            return Optional.empty();
        }

        return Optional.of(despawnTimeLeft);
    }

    /**
     * @param line
     *      Input of {@link DespawnTimeParserUtils#extractSpawnDurationFromLine(String)} or that input mutated to remove
     *      the period segment.
     * @return
     *      True if prefix or suffix indicates that the despawn time is confirmed. It checks for things like check
     *      marks or other indications of confirmation.
     */
    private static boolean doesMessageContainSignsOfConfirmation(final String line) {
        final boolean indicatesConfirmed = line.matches(".*((?i)check_yes|white_check_mark).*");
        final boolean indicatesUnconfirmed = line.matches(".*((?i)yellow_question).*");
        if (!indicatesConfirmed && !indicatesUnconfirmed) {
            log.warn("No indication about despawn time confirmation found on line: {}", line);
        }
        return indicatesConfirmed || indicatesUnconfirmed;
    }

}
