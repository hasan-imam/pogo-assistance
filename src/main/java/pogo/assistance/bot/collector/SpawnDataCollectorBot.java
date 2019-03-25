package pogo.assistance.bot.collector;

import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_BENIN_USER;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_CORRUPTED_USER;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_JOHNNY_USER;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_M15MV1_USER;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_NINERS_USER;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_TIMBURTY_USER;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.base.Verify;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDA.Status;
import pogo.assistance.data.exchange.spawn.PokemonSpawnWebCrawler;

@Slf4j
public class SpawnDataCollectorBot extends AbstractExecutionThreadService {

    private final JDA controlUserJda;
    private final JDA corruptedUserJda;
    private final JDA beninUserJda;
    private final JDA ninersUserJda;
    private final JDA johnnyUserJda;
    private final JDA timburtyUserJda;
    private final PokemonSpawnWebCrawler pokemonSpawnWebCrawler;

    private final AtomicBoolean shutdownTriggered = new AtomicBoolean(false);

    /**
     * @param m15mv1UserJda
     *      Control user JDA - used for relaying and bot control. All other JDAs just collect data.
     */
    @Inject
    public SpawnDataCollectorBot(
            @Named(NAME_JDA_M15MV1_USER) final JDA m15mv1UserJda,
            @Named(NAME_JDA_CORRUPTED_USER) final JDA corruptedUserJda,
            @Named(NAME_JDA_BENIN_USER) final JDA beninUserJda,
            @Named(NAME_JDA_NINERS_USER) final JDA ninersUserJda,
            @Named(NAME_JDA_JOHNNY_USER) final JDA johnnyUserJda,
            @Named(NAME_JDA_TIMBURTY_USER) final JDA timburtyUserJda,
            final PokemonSpawnWebCrawler pokemonSpawnWebCrawler) {

        Verify.verify(hasRegisteredListener(m15mv1UserJda), "Control user JDA is expected to have at least one listener (kill switch)");
        Verify.verify(hasRegisteredListener(corruptedUserJda), "Corrupted user JDA is expected to have registered listener(s)");
        Verify.verify(hasRegisteredListener(beninUserJda), "Benin user JDA is expected to have registered listener(s)");
        Verify.verify(hasRegisteredListener(ninersUserJda), "Niners user JDA is expected to have registered listener(s)");
        Verify.verify(hasRegisteredListener(ninersUserJda), "Johnny user JDA is expected to have registered listener(s)");
        Verify.verify(hasRegisteredListener(ninersUserJda), "Timburty user JDA is expected to have registered listener(s)");

        this.controlUserJda = m15mv1UserJda;
        this.corruptedUserJda = corruptedUserJda;
        this.beninUserJda = beninUserJda;
        this.ninersUserJda = ninersUserJda;
        this.johnnyUserJda = johnnyUserJda;
        this.timburtyUserJda = timburtyUserJda;
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
            logJdaState(corruptedUserJda);
            logJdaState(beninUserJda);
            logJdaState(ninersUserJda);
            logJdaState(johnnyUserJda);
            logJdaState(timburtyUserJda);
            logJdaState(controlUserJda);

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
        beninUserJda.shutdown();
        ninersUserJda.shutdown();
        johnnyUserJda.shutdown();
        timburtyUserJda.shutdown();

        controlUserJda.shutdown();

        pokemonSpawnWebCrawler.awaitTerminated();
    }

    @Override
    protected void triggerShutdown() {
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
