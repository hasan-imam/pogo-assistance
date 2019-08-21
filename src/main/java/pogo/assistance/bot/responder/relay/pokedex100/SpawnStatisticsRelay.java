package pogo.assistance.bot.responder.relay.pokedex100;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.google.common.base.Stopwatch;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.data.exchange.spawn.PokemonSpawnObserver;
import pogo.assistance.data.extraction.source.SpawnSummaryStatistics;
import pogo.assistance.data.model.SourceMetadata;
import pogo.assistance.data.model.pokemon.PokemonSpawn;
import pogo.assistance.ui.RenderingUtils;

/**
 * Listens to spawns, collect statistics and when called {@link #relayLatestStats()} relays the statistics to
 * some destination channel and clears internal states. Internal state is only cleared if sending message succeeds.
 *
 * @implNote
 *      Listening and relaying methods here are synchronized to prevent concurrent modifications.
 */
@Slf4j
@Singleton
public class SpawnStatisticsRelay implements PokemonSpawnObserver {

    private static final Color SUMMARY_REPORT_COLOR = Color.RED;
    private static final Color SOURCE_SPECIFIC_REPORT_COLOR = new Color(74, 144, 226);
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static {
        SIMPLE_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
    }

    private final Map<SourceMetadata, SpawnSummaryStatistics> statisticsMap;
    @Getter
    private final Stopwatch stopwatch;
    private final Provider<JDA> relayingUserJda;

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final TextChannel relayReportChannel = relayingUserJda.get().getTextChannelById(DiscordEntityConstants.CHANNEL_ID_DD_RELAY_REPORTS);

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final PrivateChannel h13mDmChannel = relayingUserJda.get().getUserById(DiscordEntityConstants.USER_ID_H13M).openPrivateChannel().complete();

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final PrivateChannel joshDmChannel = relayingUserJda.get().getUserById(DiscordEntityConstants.USER_ID_JOSH).openPrivateChannel().complete();

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final PrivateChannel mukDmChannel = relayingUserJda.get().getUserById(DiscordEntityConstants.USER_ID_MUK).openPrivateChannel().complete();

    @Inject
    public SpawnStatisticsRelay(@Named(DiscordEntityConstants.NAME_JDA_M15M_BOT) final Provider<JDA> relayingUserJda) {
        this.relayingUserJda = relayingUserJda;
        this.stopwatch = Stopwatch.createStarted();
        this.statisticsMap = new ConcurrentHashMap<>();
    }

    @Override
    public synchronized void observe(final PokemonSpawn pokemonSpawn) {
        statisticsMap.computeIfAbsent(pokemonSpawn.getSourceMetadata(), __ -> new SpawnSummaryStatistics())
                .accept(pokemonSpawn);
    }

    /**
     * Relays latest spawn statistics and clears the internal states of this instance.
     */
    public synchronized void relayLatestStats() {
        try {
            // Prepare messages
            final List<MessageEmbed> embedList = new ArrayList<>();
            embedList.add(getReportSummary());
            statisticsMap.entrySet().stream()
                    .sorted(Comparator.comparing(statisticsEntry -> statisticsEntry.getKey().sourceName()))
                    .forEachOrdered(statisticsEntry -> {
                        embedList.add(toMessageEmbed(statisticsEntry.getKey().sourceName(), SOURCE_SPECIFIC_REPORT_COLOR, statisticsEntry.getValue()));
                    });

            // Relay
            final long messageSentCount = embedList.stream()
                    .peek(messageEmbed -> getH13mDmChannel().sendMessage(messageEmbed).queue())
                    .peek(messageEmbed -> getJoshDmChannel().sendMessage(messageEmbed).queue())
                    .peek(messageEmbed -> getMukDmChannel().sendMessage(messageEmbed).queue())
                    .map(getRelayReportChannel()::sendMessage)
                    .map(MessageAction::complete)
                    .filter(Objects::nonNull)
                    .count();

            // Clear out data if some or all messages were sent
            if (messageSentCount > 0) {
                if (messageSentCount < embedList.size()) {
                    log.error("Some messages weren't sent. Logging all on console.");
                    embedList.stream().map(MessageEmbed::toData).map(DataObject::toString).forEach(log::info);
                }
                getStopwatch().reset().start();
                statisticsMap.clear();
            } else if (!embedList.isEmpty()) {
                log.error("Failed to send report messages");
            } else {
                log.info("No report message to send");
            }
        } catch (final Exception e) {
            log.error("Failed to send spawn stats report through Discord", e);
        }
    }

    private MessageEmbed getReportSummary() {
        return statisticsMap.values().stream()
                .reduce(SpawnSummaryStatistics::accumulate)
                .map(spawnSummaryStatistics -> {
                    // Build the general message, then add special jazz for the summary
                    final EmbedBuilder embedBuilder = new EmbedBuilder(toMessageEmbed("Summary report", SUMMARY_REPORT_COLOR, spawnSummaryStatistics));
                    embedBuilder.addField(
                            String.format("Sources (%d)", statisticsMap.size()),
                            statisticsMap.entrySet().stream()
                                    .map(entry -> String.format("%s (%d)", entry.getKey().sourceName(), entry.getValue().getCountTotal()))
                                    .sorted()
                                    .collect(Collectors.joining(", ")),
                            false);
                    // Add candy counts
                    embedBuilder.addField(
                            "Candy count by pokemon",
                            spawnSummaryStatistics.getCandyCounts().entrySet().stream()
                                    .map(entry -> entry.getKey() + " → " + entry.getValue())
                                    .collect(Collectors.joining(", ")),
                            false);
                    return embedBuilder.build();
                }).get();
    }

    private MessageEmbed toMessageEmbed(
            final String embedTitle,
            final Color embedColor,
            final SpawnSummaryStatistics spawnSummaryStatistics) {
        final EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(embedTitle);
        embedBuilder.setColor(embedColor);

        embedBuilder.appendDescription(new StringBuilder()
                .append(String.format("Total spawns: %d%n", spawnSummaryStatistics.getCountTotal()))
                .append(String.format("Total candies: %d%n", spawnSummaryStatistics.getCountTotalCandies()))
                .append(String.format("Spawns with IV info: %d%n", spawnSummaryStatistics.getCountHasIv()))
                .append(String.format("Spawns with CP info: %d%n", spawnSummaryStatistics.getCountHasCp()))
                .append(String.format("Spawns with level info: %d%n", spawnSummaryStatistics.getCountHasLevel()))
                .toString());

        embedBuilder.addField(
                "IV stats",
                new StringBuilder()
                        .append(String.format("100 IV: %d%n", spawnSummaryStatistics.getCountWithIv100()))
                        .append(String.format("90-99 IV: %d%n", spawnSummaryStatistics.getCountWithIv90()))
                        .append(String.format("80-89 IV: %d%n", spawnSummaryStatistics.getCountWithIv80()))
                        .append(String.format("50-79 IV: %d%n", spawnSummaryStatistics.getCountWithIv50()))
                        .append(String.format("0 IV: %d%n", spawnSummaryStatistics.getCountWithIv0()))
                        .toString(),
                true);

        embedBuilder.addField(
                "CP stats",
                new StringBuilder()
                        .append(String.format("CP 3000+: %d%n", spawnSummaryStatistics.getCountWithCp3000()))
                        .append(String.format("CP 2000+: %d%n", spawnSummaryStatistics.getCountWithCp2000()))
                        .toString(),
                true);

        embedBuilder.addField(
                "Level stats",
                new StringBuilder()
                        .append(String.format("Lvl 35: %d%n", spawnSummaryStatistics.getCountWithLevel35()))
                        .append(String.format("Lvl 30-34: %d%n", spawnSummaryStatistics.getCountWithLevel30()))
                        .toString(),
                true);

        final Duration elapsed = getStopwatch().elapsed();
        final Date startTime = Date.from(Instant.now().minus(elapsed));
        final Date endTime = Date.from(Instant.now());
        final String elapsedString = RenderingUtils.toString(Duration.ofSeconds(elapsed.getSeconds()));
        embedBuilder.setFooter(
                String.format("From %s PST to %s PST (%s)", SIMPLE_DATE_FORMAT.format(startTime), SIMPLE_DATE_FORMAT.format(endTime), elapsedString),
                null);

        return embedBuilder.build();
    }

}
