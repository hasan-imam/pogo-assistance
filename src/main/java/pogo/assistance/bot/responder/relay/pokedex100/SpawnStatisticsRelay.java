package pogo.assistance.bot.responder.relay.pokedex100;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.google.common.base.Stopwatch;
import lombok.AccessLevel;
import lombok.Getter;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.data.exchange.spawn.PokemonSpawnObserver;
import pogo.assistance.data.extraction.source.SpawnSummaryStatistics;
import pogo.assistance.data.model.SourceMetadata;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

/**
 * Listens to spawns, collect statistics and when called {@link #relayLatestStats()} relays the statistics to
 * some destination channel and clears internal states. Internal state is only cleared if sending message succeeds.
 *
 * @implNote
 *      Listening and relaying methods here are synchronized to prevent concurrent modifications.
 */
@Singleton
public class SpawnStatisticsRelay implements PokemonSpawnObserver {

    private final Map<SourceMetadata, SpawnSummaryStatistics> statisticsMap;
    @Getter
    private final Stopwatch stopwatch;
    private final Provider<JDA> relayingUserJda;

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final TextChannel serverLogChannel = relayingUserJda.get().getTextChannelById(DiscordEntityConstants.CHANNEL_ID_DD_BOT_TESTING);

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final PrivateChannel h13mDmChannel = relayingUserJda.get().getUserById(DiscordEntityConstants.USER_ID_H13M).openPrivateChannel().complete();

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final PrivateChannel joshDmChannel = relayingUserJda.get().getUserById(DiscordEntityConstants.USER_ID_JOSH).openPrivateChannel().complete();

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
        // Prepare message
        final EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(String.format("Spawn statistics for last %s minutes", getStopwatch().elapsed().toMinutes()), null);
        embedBuilder.setColor(Color.red);
        embedBuilder.setColor(new Color(0xF40C0C));
        embedBuilder.setColor(new Color(255, 0, 54));
        statisticsMap.forEach((sourceMetadata, spawnSummaryStatistics) -> {
            embedBuilder.addField(sourceMetadata.sourceName(), spawnSummaryStatistics.toString(), false);
        });

        // Relay
        final Message messageToServerLog = getServerLogChannel().sendMessage(embedBuilder.build()).complete();
        final Message messageToH13M = getH13mDmChannel().sendMessage(embedBuilder.build()).complete();
        final Message messageToJosh = getJoshDmChannel().sendMessage(embedBuilder.build()).complete();

        // Clear out data
        if (messageToServerLog != null && messageToH13M != null && messageToJosh != null) {
            getStopwatch().reset().start();
            statisticsMap.clear();
        }
    }

}
