package pogo.assistance.bot.responder;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.security.auth.login.LoginException;

import com.google.common.util.concurrent.AbstractIdleService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import pogo.assistance.bot.di.DiscordEntityConstants;

/**
 * @implNote
 *      Constructor takes a JDA builder to be able to register only after {@link #startUp()} has been called.
 *
 *      Uses {@link #jdas} to check if JDAs has been started off with listeners. This ensures that multiple
 *      calls to {@link #startUp()} ()} doesn't set up multiple listeners/JDA. The {@link #startUp()}
 *      method have also been made 'synchronized' for the same concern.
 */
@Slf4j
public class ResponderBot extends AbstractIdleService {

    private final List<JDABuilder> jdaBuilders;
    private final List<JDA> jdas;

    @Inject
    public ResponderBot(
            @Named(DiscordEntityConstants.NAME_JDA_BUILDER_OWNING_USER) final JDABuilder owningUserJdaBuilder,
            @Named(DiscordEntityConstants.NAME_JDA_BUILDER_HORUSEUS_USER) final JDABuilder horuseusUserJdaBuilder,
            @Named(DiscordEntityConstants.NAME_JDA_BUILDER_COPERNICUS_USER) final JDABuilder copernicusUserJdaBuilder) {
        jdas = new ArrayList<>();
        jdaBuilders = new ArrayList<>();
        jdaBuilders.add(owningUserJdaBuilder);
        jdaBuilders.add(horuseusUserJdaBuilder);
        jdaBuilders.add(copernicusUserJdaBuilder);
    }

    @Override
    protected synchronized void startUp() {
        if (jdas.isEmpty()) {
            jdaBuilders.forEach(jdaBuilder -> {
                try {
                    final JDA jda = jdaBuilder.build().awaitReady();
                    if (jda.getRegisteredListeners().isEmpty()) {
                        // Unfortunately the build doesn't expose any way to check if it has any registered listeners
                        // So we build the JDA, check if it has anything registered and shutdown if there's no listener on it
                        log.info("Shutting down {} JDA since it doesn't have any responder/listener registered", jda.getSelfUser().getName());
                        jda.shutdown();
                    } else {
                        jdas.add(jda);
                    }
                } catch (final LoginException | InterruptedException e) {
                    throw new RuntimeException("Failed to initialize JDA", e);
                }
            });
        }
    }

    protected void shutDown() {
        // Initiate shutdown
        jdas.forEach(JDA::shutdown);
        // Wait for shutdown to complete
        for (final JDA jda : jdas) {
            try {
                jda.awaitStatus(JDA.Status.SHUTDOWN);
            } catch (final InterruptedException e) {
                log.error("Interrupted while waiting for JDA shutdown to complete");
                break;
            }
        }
    }

}
