package pogo.assistance.bot.responder;

import com.google.common.base.Verify;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDA.Status;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.bot.di.DiscordEntityModule;

/**
 * @implNote
 *      Constructor takes a JDA builder to be able to register event listeners before JDA is initialized. After JDA is
 *      initialized, all registered listeners receive a {@link ReadyEvent} which lets the handler do some validations
 *      to make sure it can do its work.
 *
 *      Uses {@link #jdaAtomicReference} to check if JDA has been started off with listeners. This ensures that multiple
 *      calls to {@link #run()} doesn't set up multiple listeners/JDA.
 *
 *      This extends {@link TimerTask}. Intent is that the initial {@link #run() execution} will start up the responder
 *      and subsequent executions will verify the state, make sure responder is running well and reinitialize
 *      JDA/listeners if needed.
 */
@Slf4j
public class ResponderBot extends TimerTask {

    private final JDABuilder jdaBuilder;
    private final Set<EventListener> listeners;
    private final AtomicReference<JDA> jdaAtomicReference = new AtomicReference<>();

    @Inject
    public ResponderBot(
            @NonNull @Named(DiscordEntityConstants.NAME_JDA_BUILDER_OWNING_USER) final JDABuilder jdaBuilder,
            @NonNull final Set<EventListener> listeners) {
        this.jdaBuilder = jdaBuilder;
        this.listeners = listeners;
    }

    @Override
    public synchronized void run() {
        initialize();
        ensureRunningState();
    }

    private void initialize() {
        if (jdaAtomicReference.get() != null
                && (jdaAtomicReference.get().getStatus() == Status.SHUTDOWN
                || jdaAtomicReference.get().getStatus() == Status.SHUTTING_DOWN)) {
            log.warn("Dropping reference to JDA since it's in {} state", jdaAtomicReference.get().getStatus());
            jdaAtomicReference.set(null);
        }

        if (jdaAtomicReference.get() == null) {
            jdaBuilder.addEventListener(listeners.toArray());
            jdaAtomicReference.set(DiscordEntityModule.provideUserJda(jdaBuilder));
            Verify.verifyNotNull(jdaAtomicReference.get());
        }
    }

    /**
     * Blocks until {@link JDA} reaches {@link Status#CONNECTED} state. If interrupted, tries to shutdown JDA and set
     * the interrupted state.
     */
    private void ensureRunningState() {
        final JDA jda = jdaAtomicReference.get();
        Verify.verifyNotNull(jda, "Should not call this method before setting up JDA");
        try {
            jda.awaitStatus(Status.CONNECTED);
            log.info("Responder's JDA status: {}, ping: {}", jda.getStatus(), jda.getPing());
        } catch (final InterruptedException e) {
            log.error("Got interrupted when waiting for JDA to get connected. Latest JDA state: {}. Forcing JDA shutdown.",
                    jda.getStatus());
            jda.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Check if registered listeners still have all the listeners this bot registered. This is based on the handler
        // behavior that if there's anything critically wrong with a handler, it will unregister itself. If any of our
        // listeners have unregistered themselves, we kill the application instead of running in the partially
        // functional state.
        final List<Object> registeredListeners = jda.getRegisteredListeners();
        if (!registeredListeners.containsAll(listeners)) {
            log.error("One or more event listeners have unregistered themselves. Responder bot terminating.");
            System.exit(1);
        }
    }

}
