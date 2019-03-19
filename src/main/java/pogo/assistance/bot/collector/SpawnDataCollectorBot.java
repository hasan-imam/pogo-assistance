package pogo.assistance.bot.collector;

import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_BENIN_USER;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_CORRUPTED_USER;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_OWNING_USER;

import com.google.common.base.Verify;
import com.google.common.util.concurrent.AbstractExecutionThreadService;

import java.util.Objects;
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
    private final JDA corruptedUserJda;
    private final JDA beninUserJda;
    private final PokemonSpawnWebCrawler pokemonSpawnWebCrawler;

    private final AtomicBoolean shutdownTriggered = new AtomicBoolean(false);

    /**
     * @param owningUserJda
     *      JDA with the relaying user. At this point, the relaying user's JDA is also used for data collection.
     */
    @Inject
    public SpawnDataCollectorBot(
            @Named(NAME_JDA_OWNING_USER) final JDA owningUserJda,
            @Named(NAME_JDA_CORRUPTED_USER) final JDA corruptedUserJda,
            @Named(NAME_JDA_BENIN_USER) final JDA beninUserJda,
            final PokemonSpawnWebCrawler pokemonSpawnWebCrawler) {

        Verify.verify(hasRegisteredListener(corruptedUserJda), "Corrupted user JDA is expected to have registered listener(s)");
        Verify.verify(hasRegisteredListener(beninUserJda), "Benin user JDA is expected to have registered listener(s)");
        Verify.verify(hasRegisteredListener(owningUserJda), "Owning user JDA is expected to have registered listener(s)");

        this.relayingUserJda = owningUserJda;
        this.corruptedUserJda = corruptedUserJda;
        this.beninUserJda = beninUserJda;
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
            log.info("{}'s JDA status: {}, ping: {}, response count: {}",
                    corruptedUserJda.getSelfUser().getName(), corruptedUserJda.getStatus(), corruptedUserJda.getPing(), corruptedUserJda.getResponseTotal());
            log.info("{}'s JDA status: {}, ping: {}, response count: {}",
                    relayingUserJda.getSelfUser().getName(), relayingUserJda.getStatus(), relayingUserJda.getPing(), relayingUserJda.getResponseTotal());
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
        }
    }

    @Override
    protected void shutDown() {
        pokemonSpawnWebCrawler.stopAsync();
        corruptedUserJda.shutdown();
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

    private static boolean hasRegisteredListener(final JDA jda) {
        return !jda.getRegisteredListeners().isEmpty();
    }
}
