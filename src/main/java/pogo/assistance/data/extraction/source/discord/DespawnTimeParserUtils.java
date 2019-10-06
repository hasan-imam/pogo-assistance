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
     *
     * Note the top level group that encapsulates the subgroups. Being able to address the whole period section is
     * necessary in our implementation (see {@link #extractSpawnDurationFromLine(String, boolean)}).
     *
     * Verify online: https://regex101.com/r/pUNJBs/3
     */
    private static final Pattern DESPAWN_PERIOD_STRING =
            Pattern.compile("[\\(\\s]?" // boundary start - either a white space or brace
                    + "((?<minutes>[0-5]?\\d)m"
                    + "[\\s]?" // some texts have space between minutes and seconds, some don't
                    + "(?<seconds>[0-5]?\\d)s)"
                    + "[\\)\\s]?"); // boundary end - either a white space or brace

    /**
     * @param compiledText
     *      Text from which data is parsed. Each line of this input is processed separately because spawn time info
     *      usually isn't spread across multiple lines.
     * @return
     *      Extracts duration the spawn has left, then adds the duration to current instant to get despawn time.
     */
    public static Optional<Instant> extractDespawnTime(final String compiledText) {
        return extractSpawnDuration(compiledText, true).map(Instant.now()::plus);
    }

    /**
     * Same as {@link #extractDespawnTime(String)}, but skips doing further checks to ensure that extracted time is a
     * despawn time. The general extraction uses {@link #doesLineLookLikeItHasDespawnTime(String)} to see other strings
     * in the line to see if the time in the line has good chance of being a valid despawn time (and not something
     * else). This method skips that check since such checks need to be more nuanced for some sources and we don't
     * bother being that surgical in current implementation of {@link #doesLineLookLikeItHasDespawnTime(String)}.
     *
     * TODO: make validation more robust, specifically checking previous and following line for signs. Check marks and
     * other emotes we look for aren't always put on the same line as the despawn time.
     */
    public static Optional<Instant> extractLowConfidenceDespawnTime(final String compiledText) {
        return extractSpawnDuration(compiledText, false).map(Instant.now()::plus);
    }

    @VisibleForTesting
    static Optional<Duration> extractSpawnDuration(final String compiledText, final boolean doRunValidations) {
        return Stream.of(compiledText.split("\\r?\\n|\\r"))
                .map(line -> extractSpawnDurationFromLine(line, doRunValidations))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny();
    }

    private static Optional<Duration> extractSpawnDurationFromLine(final String line, final boolean doRunValidations) {
        final Matcher periodMatcher = DESPAWN_PERIOD_STRING.matcher(line);
        final boolean foundPeriod = periodMatcher.find();
        if (!foundPeriod) {
            return Optional.empty();
        }
        final Duration despawnTimeLeft = Duration.ofMinutes(Longs.tryParse(periodMatcher.group("minutes")))
                .plus(Duration.ofSeconds(Longs.tryParse(periodMatcher.group("seconds"))));

        final StringBuffer sb = new StringBuffer(line.length());
        periodMatcher.appendReplacement(sb, ""); // replaced matched despawn time portion with empty string
        Verify.verify(!periodMatcher.find(), "Shouldn't have found a second period segment in line: %s", line);
        periodMatcher.appendTail(sb);

        final String lineWithoutPeriodSection = sb.toString();
        if (doRunValidations && !doesLineLookLikeItHasDespawnTime(lineWithoutPeriodSection)) {
            return Optional.empty();
        }

        return Optional.of(despawnTimeLeft);
    }

    /**
     * @param line
     *      Input of {@link DespawnTimeParserUtils#extractSpawnDurationFromLine(String)} or that input mutated to remove
     *      the period segment.
     * @return
     *      True if the input line looks like it has despawn time information. It checks for things like check marks,
     *      other icons or specific strings.
     */
    private static boolean doesLineLookLikeItHasDespawnTime(final String line) {
        final boolean indicatesDespawnTimeInfo = line.matches(".*((?i)Available until|left|DSP|Expires|Despawn).*");
        final boolean indicatesConfirmed = line.matches(".*((?i)check_yes|white_check_mark|✅).*");
        final boolean indicatesUnconfirmed = line.matches(".*((?i)yellow_question).*");
        return indicatesDespawnTimeInfo || indicatesConfirmed || indicatesUnconfirmed;
    }

}
