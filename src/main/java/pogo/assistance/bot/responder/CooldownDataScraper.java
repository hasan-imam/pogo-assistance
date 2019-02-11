package pogo.assistance.bot.responder;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Verify;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

@Slf4j
public class CooldownDataScraper extends ListenerAdapter {

    private static final Long POKEDEX_100_GUILD_ID = 252776251708801024L;
    private static final Long POKEDEX_100_BOT_CHANNEL = 252776251708801024L;

    private static final Pattern PATTERN_DIST_CMD_RESPONSE =
            Pattern.compile("The distance between ([-\\d\\.]+),([-\\d\\.]+) and ([-\\d\\.]+),([-\\d\\.]+)"
                    + " is ([-\\d\\.]+ [\\w]+)\\. The suggested cooldown time is (.*), .*");

    @VisibleForTesting
    static final String FORMAT_DIST_CMD_RESPONSE = "The distance between %s,%s and %s,%s is %s."
            + " The suggested cooldown time is %s, @memberName.";

    private final long targetGuildId;
    private final long targetChannelId;

    @Inject
    public CooldownDataScraper() {
        // Production setup
        targetGuildId = POKEDEX_100_GUILD_ID;
        targetChannelId = POKEDEX_100_BOT_CHANNEL;

//        // Use for testing on some dummy channel
//        this(ROUTER_GUILD_ID, CHANNEL_TEST_LIST_ROUTE_PREVIEW, H13M_ID);
    }

    @Override
    public void onReady(final ReadyEvent event) {
        try {
            final Guild targetGuild = event.getJDA().getGuildById(targetGuildId);
            Verify.verifyNotNull(targetGuild, "Expected to have access to target guild");

            final TextChannel repActivityChannel = targetGuild.getTextChannelById(targetChannelId);
            Verify.verifyNotNull(repActivityChannel, "Expected to have access to bot command channel");
        } catch (final RuntimeException e) {
            log.error("Verification error - this listener cannot function correctly. Unregistering myself from JDA.", e);
            event.getJDA().removeEventListener(this);
        }

        log.info("Cooldown data scraper online!");
    }

    @Override
    public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
        final TextChannel eventChannel = event.getChannel();
        if (eventChannel.getIdLong() != targetChannelId && !event.getAuthor().isBot()) {
            return;
        }

        Optional.of(event.getMessage().getContentStripped())
                .flatMap(ResponseData::fromResponseMessage)
                .ifPresent(CooldownDataScraper::logData);
    }

    private static void logData(final ResponseData responseData) {
        log.info("[Cooldown data] {},{},{},{},{},{}",
                responseData.getFromLat(), responseData.getFromLon(),
                responseData.getToLat(), responseData.getToLon(),
                responseData.getDistanceInMeters(), responseData.getCooldown().getSeconds());
    }

    @Getter
    @VisibleForTesting
    @RequiredArgsConstructor
    static class ResponseData {
        private static final Pattern DECIMAL_EXTRACTION_PATTERN = Pattern.compile("[\\D]*([-\\d\\.]+)[\\D]*");

        private final double fromLat;
        private final double fromLon;
        private final double toLat;
        private final double toLon;
        private final double distanceInMeters;
        private final Duration cooldown;

        public static Optional<ResponseData> fromResponseMessage(final String message) {
            final Matcher matcher = PATTERN_DIST_CMD_RESPONSE.matcher(message);
            if (!matcher.find()) {
                return Optional.empty();
            }
            return Optional.of(new ResponseData(
                    Double.parseDouble(matcher.group(1)), Double.parseDouble(matcher.group(2)),
                    Double.parseDouble(matcher.group(3)), Double.parseDouble(matcher.group(4)),
                    parseDistanceSegment(matcher.group(5)),
                    parseDurationSegment(matcher.group(6))));
        }

        /**
         * @param segment
         *      Example formats: "5.70 kilometers", "100.05 meters", "13343.39 kilometers"
         * @return
         *      Distance in meters
         */
        private static double parseDistanceSegment(final String segment) {
            final Matcher matcher = DECIMAL_EXTRACTION_PATTERN.matcher(segment);
            Verify.verify(matcher.find());
            final double d = Double.parseDouble(matcher.group(1));
            if (segment.contains("kilometer")) {
                return d * 1000;
            } else if (segment.contains("meter")) {
                return d;
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
            Verify.verify(segment.contains("minute"));
            Verify.verify(matcher.find());
            return Duration.ofMinutes(Integer.parseInt(matcher.group(1)));
        }
    }
}
