package pogo.assistance.bot.responder;

import dagger.Module;
import dagger.Provides;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.inject.Provider;
import net.dv8tion.jda.core.hooks.EventListener;

@Module
public class ResponderBotModule {

    @Provides
    public static Set<EventListener> providesEventListeners(
            final Set<ListenerId> listenerIds,
            final Provider<CooldownDataScraper> cooldownDataScraperProvider,
            final Provider<RepHandler> repHandlerProvider) {
        final Set<EventListener> listeners = new LinkedHashSet<>();
        listenerIds.forEach(listenerId -> {
            switch (listenerId) {
                case REP_HANDLER:
                    listeners.add(repHandlerProvider.get());
                    break;
                case COOLDOWN_SCRAPER:
                    listeners.add(cooldownDataScraperProvider.get());
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled listener ID: " + listenerId);
            }
        });
        return listeners;
    }

}
