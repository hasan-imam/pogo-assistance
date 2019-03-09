package pogo.assistance.bot.collector;

import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_CORRUPTED_USER;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_OWNING_USER;

import com.google.common.base.Verify;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDA.Status;
import pogo.assistance.data.exchange.spawn.PokemonSpawnWebCrawler;

@Slf4j
public class SpawnDataCollectorBot extends AbstractExecutionThreadService {

    private final JDA relayingUserJda;
    private final JDA collectingUserJda;
    private final PokemonSpawnWebCrawler pokemonSpawnWebCrawler;

    private final AtomicBoolean shutdownTriggered = new AtomicBoolean(false);

    /**
     * @param relayingUserJda
     *      JDA with the relaying user. At this point, the relaying user's JDA is also used for data collection.
     */
    @Inject
    public SpawnDataCollectorBot(
            @Named(NAME_JDA_OWNING_USER) final JDA relayingUserJda,
            @Named(NAME_JDA_CORRUPTED_USER) final JDA collectingUserJda,
            final PokemonSpawnWebCrawler pokemonSpawnWebCrawler) {
        this.relayingUserJda = relayingUserJda;
        this.collectingUserJda = collectingUserJda;
        this.pokemonSpawnWebCrawler = pokemonSpawnWebCrawler;
    }

    @Override
    protected void startUp() {
        // JDAs should already be running. Just need to start up the crawler.
        pokemonSpawnWebCrawler.startAsync().awaitRunning();
    }

    @Override
    public void run() {
        while (!shutdownTriggered.get() && relayingUserJda.getStatus() != Status.SHUTDOWN) {
            // This verification throwing just puts the bot in a half dead state
            // TODO: do something better - important verification failure should kill the bot
            Verify.verify(!collectingUserJda.getRegisteredListeners().isEmpty(),
                    "Data collecting user's JDA is expected to have registered listener(s)");
            log.info("{}'s JDA status: {}, ping: {}, response count: {}",
                    collectingUserJda.getSelfUser().getName(), collectingUserJda.getStatus(), collectingUserJda.getPing(), collectingUserJda.getResponseTotal());
            log.info("{}'s JDA status: {}, ping: {}, response count: {}",
                    relayingUserJda.getSelfUser().getName(), relayingUserJda.getStatus(), relayingUserJda.getPing(), relayingUserJda.getResponseTotal());
            try {
                synchronized (shutdownTriggered) {
                    TimeUnit.MINUTES.timedWait(shutdownTriggered, 5);
//                    TimeUnit.MINUTES.sleep(5);
                }
            } catch (final InterruptedException e) {
                log.error("Spawn data collector/relay interrupted. Attempting to close underlying services.", e);
                triggerShutdown();
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    protected void shutDown() {
        pokemonSpawnWebCrawler.stopAsync();
        collectingUserJda.shutdown();
        relayingUserJda.shutdown();
        pokemonSpawnWebCrawler.awaitTerminated();
    }

    @Override
    protected void triggerShutdown() {
        shutdownTriggered.set(true);
        synchronized (shutdownTriggered) {
            shutdownTriggered.notifyAll();
        }
    }
}
