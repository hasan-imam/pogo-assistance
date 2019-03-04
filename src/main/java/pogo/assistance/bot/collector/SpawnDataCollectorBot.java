package pogo.assistance.bot.collector;

import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_CORRUPTED_USER;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_OWNING_USER;

import com.google.common.base.Verify;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.JDA;

@Slf4j
public class SpawnDataCollectorBot implements Runnable {

    private final JDA relayingUserJda;
    private final JDA collectingUserJda;
    private final AtomicBoolean stopRunning = new AtomicBoolean(false);

    @Inject
    public SpawnDataCollectorBot(
            @Named(NAME_JDA_OWNING_USER) final JDA relayingUserJda,
            @Named(NAME_JDA_CORRUPTED_USER) final JDA collectingUserJda) {
        this.relayingUserJda = relayingUserJda;
        this.collectingUserJda = collectingUserJda;
    }

    @Override
    public void run() {
        while (!stopRunning.get()) {
            Verify.verify(!collectingUserJda.getRegisteredListeners().isEmpty(),
                    "Data collecting user's JDA is expected to have registered listener(s)");
            log.info("{}'s JDA status: {}, ping: {}, response count: {}",
                    collectingUserJda.getSelfUser().getName(), collectingUserJda.getStatus(), collectingUserJda.getPing(), collectingUserJda.getResponseTotal());
            log.info("{}'s JDA status: {}, ping: {}, response count: {}",
                    relayingUserJda.getSelfUser().getName(), relayingUserJda.getStatus(), relayingUserJda.getPing(), relayingUserJda.getResponseTotal());
            try {
                TimeUnit.MINUTES.sleep(5);
            } catch (final InterruptedException e) {
                collectingUserJda.shutdown();
                relayingUserJda.shutdown();
                log.error("Spawn data collector/relay interrupted", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    public void stopRunning() {
        stopRunning.set(true);
    }
}
