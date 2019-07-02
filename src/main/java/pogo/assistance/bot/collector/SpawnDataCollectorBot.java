package pogo.assistance.bot.collector;

import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_BENIN_USER;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_CHRONIC_USER;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_CONNOISSEUR_USER;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_CORRUPTED_USER;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_CRANK_USER;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_IRVIN88_USER;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_JOHNNY_USER;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_M15M_BOT;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_MICHELLEX_USER;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_NINERS_USER;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_POGO_HERO_USER;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_POKE_PETER_USER;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_TIMBURTY_USER;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.base.Verify;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDA.Status;
import pogo.assistance.bot.responder.relay.pokedex100.SpawnStatisticsRelay;
import pogo.assistance.data.exchange.spawn.PokemonSpawnWebCrawler;

@Slf4j
public class SpawnDataCollectorBot extends AbstractExecutionThreadService {

    private final JDA controlUserJda;
    private final Set<JDA> dataSourceJdas;
    private final SpawnStatisticsRelay spawnStatisticsRelay;
    private final PokemonSpawnWebCrawler pokemonSpawnWebCrawler;

    private final AtomicBoolean shutdownTriggered = new AtomicBoolean(false);

    /**
     * @param m15mBotJda
     *      Control user JDA - used for relaying and bot control. All other JDAs just collect data.
     */
    @Inject
    public SpawnDataCollectorBot(
            @Named(NAME_JDA_M15M_BOT) final JDA m15mBotJda,
            @Named(NAME_JDA_CORRUPTED_USER) final JDA corruptedUserJda,
            @Named(NAME_JDA_BENIN_USER) final JDA beninUserJda,
            @Named(NAME_JDA_NINERS_USER) final JDA ninersUserJda,
            @Named(NAME_JDA_JOHNNY_USER) final JDA johnnyUserJda,
            @Named(NAME_JDA_TIMBURTY_USER) final JDA timburtyUserJda,
            @Named(NAME_JDA_IRVIN88_USER) final JDA irvin88UserJda,
            @Named(NAME_JDA_CONNOISSEUR_USER) final JDA connoisseurUserJda,
            @Named(NAME_JDA_CHRONIC_USER) final JDA chronicUserJda,
            @Named(NAME_JDA_CRANK_USER) final JDA crankUserJda,
            @Named(NAME_JDA_POGO_HERO_USER) final JDA poGoHeroUserJda,
            @Named(NAME_JDA_MICHELLEX_USER) final JDA michellexUserJda,
            @Named(NAME_JDA_POKE_PETER_USER) final JDA pokePeterUserJda,
            final PokemonSpawnWebCrawler pokemonSpawnWebCrawler,
            final SpawnStatisticsRelay spawnStatisticsRelay) {

        Verify.verify(hasRegisteredListener(m15mBotJda), "Control user JDA is expected to have at least one listener (kill switch)");
        this.controlUserJda = m15mBotJda;

        this.dataSourceJdas = ImmutableSet.of(corruptedUserJda, beninUserJda, ninersUserJda,
                johnnyUserJda, timburtyUserJda, irvin88UserJda, connoisseurUserJda, poGoHeroUserJda,
                chronicUserJda, crankUserJda, michellexUserJda, pokePeterUserJda);
        this.dataSourceJdas.forEach(jda -> Verify.verify(
                hasRegisteredListener(jda),
                "%s user JDA is expected to have registered listener(s)",
                jda.getSelfUser().getName()));

        this.spawnStatisticsRelay = spawnStatisticsRelay;
        this.pokemonSpawnWebCrawler = pokemonSpawnWebCrawler;
    }

    @Override
    protected void startUp() {
        // JDAs should already be running. Just need to start up the crawler.
        pokemonSpawnWebCrawler.startAsync().awaitRunning();
    }

    @Override
    public void run() {
        while (!shutdownTriggered.get() && controlUserJda.getStatus() != Status.SHUTDOWN) {
            try {
                logJdaState(controlUserJda);
                dataSourceJdas.forEach(SpawnDataCollectorBot::logJdaState);

                if (spawnStatisticsRelay.getStopwatch().elapsed().compareTo(Duration.ofHours(6)) > 0) {
                    // Relay spawn stats (roughly) at some intervals
                    spawnStatisticsRelay.relayLatestStats();
                }

                try {
                    // TODO verify that this shutdown works as expected
                    synchronized (shutdownTriggered) {
                        TimeUnit.MINUTES.timedWait(shutdownTriggered, 5);
                    }
                } catch (final InterruptedException e) {
                    log.error("Spawn data collector/relay interrupted. Attempting to close underlying services.", e);
                    triggerShutdown();
                    Thread.currentThread().interrupt();
                }
            } catch (final Exception e) {
                log.error("Unexpected error in collector bot's status checking loop", e);
            }
        }
    }

    @Override
    protected void shutDown() {
        pokemonSpawnWebCrawler.stopAsync();

        dataSourceJdas.forEach(JDA::shutdown);
        controlUserJda.shutdown();

        pokemonSpawnWebCrawler.awaitTerminated();
    }

    @Override
    protected void triggerShutdown() {
        // Relay stats before shutting things down
        spawnStatisticsRelay.relayLatestStats();

        shutdownTriggered.set(true);
        synchronized (shutdownTriggered) {
            shutdownTriggered.notifyAll();
        }
    }

    private static void logJdaState(final JDA jda) {
        log.info("{}'s JDA status: {}, ping: {}, response count: {}",
                jda.getSelfUser().getName(), jda.getStatus(), jda.getPing(), jda.getResponseTotal());
    }

    private static boolean hasRegisteredListener(final JDA jda) {
        return !jda.getRegisteredListeners().isEmpty();
    }
}
