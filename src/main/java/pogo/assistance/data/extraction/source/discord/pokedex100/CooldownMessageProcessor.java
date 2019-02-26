package pogo.assistance.data.extraction.source.discord.pokedex100;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Verify;
import io.jenetics.jpx.Latitude;
import io.jenetics.jpx.Length;
import io.jenetics.jpx.Length.Unit;
import io.jenetics.jpx.Point;
import io.jenetics.jpx.WayPoint;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.entities.Message;
import org.jetbrains.annotations.NotNull;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.data.extraction.source.discord.MessageProcessor;

@Slf4j
@RequiredArgsConstructor
public class CooldownMessageProcessor implements MessageProcessor<CooldownRecord> {

    /**
     * Format used by bot to respond to `?dist` command. This needs to match both v1 and v2 printing formats.
     */
    private static final Pattern PATTERN_DIST_CMD_RESPONSE =
            Pattern.compile("The distance between ([-\\d\\.]+),([-\\d\\.]+) and ([-\\d\\.]+),([-\\d\\.]+)"
                    + " is ([-\\d\\.]+ [\\w]+)\\. (The suggested )*[C|c]ooldown time is (?<cooldown>.*)[,|\\.] (.*)");

    /**
     * Format (v1) up to message ID 416905502786846735 (on 24/02/2018) was:
     * "... Cooldown time is 59 min. @MemberName"
     */
    @VisibleForTesting
    static final String FORMAT_DIST_CMD_RESPONSE_V1 = "The distance between %s,%s and %s,%s is %s."
            + " The suggested cooldown time is %s, @memberName.";

    /**
     * Format (v2) at message ID 416906664755593226 (on 24/02/2018) and beforehand was:
     * "... The suggested cooldown time is 106 minutes (1 hours and 46 minutes), @MemberName."
     * Trivia: @trungnguyen19, one of the developers, seem to have deployed the switch. And although this is called 'v2'
     * its actually the old formatting of these messages. It was just handled later and named 'v2'.
     */
    @VisibleForTesting
    static final String FORMAT_DIST_CMD_RESPONSE_V2 = "The distance between %s,%s and %s,%s is %s."
            + " Cooldown time is %s @memberName";

    private static final Pattern DECIMAL_EXTRACTION_PATTERN = Pattern.compile("[\\D]*([-\\d\\.]+)[\\D]*");

    @Override
    public boolean canProcess(@NotNull final Message message) {
        return message.getChannel().getIdLong() == DiscordEntityConstants.CHANNEL_ID_PDEX100_BOT_COMMAND
                && message.getAuthor().isBot()
                && PATTERN_DIST_CMD_RESPONSE.matcher(message.getContentStripped()).matches();
    }

    @Override
    public Optional<CooldownRecord> process(@NotNull final Message message) {
        return fromResponseMessage(message);
    }

    private static Optional<CooldownRecord> fromResponseMessage(final Message message) {
        final Matcher matcher = PATTERN_DIST_CMD_RESPONSE.matcher(message.getContentStripped());
        if (!matcher.find()) {
            return Optional.empty();
        }

        final Point fromPoint = toValidPoint(Double.parseDouble(matcher.group(1)), Double.parseDouble(matcher.group(2)));
        final Point toPoint = toValidPoint(Double.parseDouble(matcher.group(3)), Double.parseDouble(matcher.group(4)));
        if (fromPoint == null || toPoint == null) {
            log.trace("Latitude/longitude value failed validation. Ignoring message: {}", message.getContentStripped());
            return Optional.empty();
        }

        final CooldownRecord cooldownRecord = ImmutableCooldownRecord.builder()
                .fromPoint(WayPoint.of(Double.parseDouble(matcher.group(1)), Double.parseDouble(matcher.group(2))))
                .toPoint(WayPoint.of(Double.parseDouble(matcher.group(3)), Double.parseDouble(matcher.group(4))))
                .pokedex100Distance(parseDistanceSegment(matcher.group(5)))
                .pokedex100Cooldown(parseDurationSegment(matcher.group("cooldown")))
                .build();
        return Optional.of(cooldownRecord);
    }

    /**
     * @param segment
     *      Example formats: "5.70 kilometers", "100.05 meters", "13343.39 kilometers"
     * @return
     *      Distance in meters
     */
    private static Length parseDistanceSegment(final String segment) {
        final Matcher matcher = DECIMAL_EXTRACTION_PATTERN.matcher(segment);
        Verify.verify(matcher.find());
        final double d = Double.parseDouble(matcher.group(1));
        if (segment.contains("kilometer")) {
            return Length.of(d, Unit.KILOMETER);
        } else if (segment.contains("meter")) {
            return Length.of(d, Unit.METER);
        }
        throw new AssertionError(String.format("Unexpected unit in distance segment: %s", segment));
    }

    /**
     * @param segment
     *      Example formats: "120 minutes (2 hours)", "1 minute", "72 minutes (1 hour and 12 minutes)"
     * @return
     *      Cooldown duration
     * @implNote
     *      Assumption: Segment will always contain total cooldown minute as the first number
     */
    private static Duration parseDurationSegment(final String segment) {
        final Matcher matcher = DECIMAL_EXTRACTION_PATTERN.matcher(segment);
        Verify.verify(segment.contains("minute") || segment.contains("min"));
        Verify.verify(matcher.find());
        return Duration.ofMinutes(Integer.parseInt(matcher.group(1)));
    }

    @Nullable
    private static Point toValidPoint(final double lat, final double lon) {
        if (lat >= Latitude.MIN_VALUE.toDegrees() && lat <= Latitude.MAX_VALUE.toDegrees()
                && lon >= -180 && lon <= 180) {
            return WayPoint.of(lat, lon);
        }
        return null;
    }
}
