package pogo.assistance.bot.responder;

import com.google.common.collect.ImmutableSet;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.inject.Named;
import javax.inject.Provider;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.hooks.EventListener;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.bot.responder.relay.pokedex100.Pokedex100SpawnRelay;
import pogo.assistance.data.exchange.spawn.PokemonSpawnObserver;
import pogo.assistance.data.extraction.source.discord.DiscordPokemonSpawnListener;

@Module
public class ResponderBotModule {

    @Provides
    public static Set<EventListener> providesEventListeners(
            final Set<ListenerId> listenerIds,
            final Provider<RepHandler> repHandlerProvider,
            final Provider<DiscordPokemonSpawnListener> discordPokemonSpawnListenerProvider) {
        final Set<EventListener> listeners = new LinkedHashSet<>();
        listenerIds.forEach(listenerId -> {
            switch (listenerId) {
                case REP_HANDLER:
                    listeners.add(repHandlerProvider.get());
                    break;
                case RELAY_PDEX100:
                    listeners.add(discordPokemonSpawnListenerProvider.get());
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled listener ID: " + listenerId);
            }
        });
        return listeners;
    }

    @Provides
    @ElementsIntoSet
    public static Set<PokemonSpawnObserver> providePokedex100SpawnRelay(
            final Provider<Pokedex100SpawnRelay> pokedex100SpawnRelayProvider) {
        return ImmutableSet.of(pokedex100SpawnRelayProvider.get());
    }

    /**
     * @param jda
     *      JDA with user {@link JDA#getSelfUser() user} who will send the DM to 'SuperBotP' in 'Pokedex100' server.
     *      This user needs to have 'verifier' role to be able to issue the DM commands to 'SuperBotP'.
     */
    @Provides
    @Named(DiscordEntityConstants.NAME_PDEX100_SUPER_BOT_P)
    public static User providesSuperBotPUser(@Named(DiscordEntityConstants.NAME_JDA_OWNING_USER) final JDA jda) {
        return jda.getUserById(DiscordEntityConstants.USER_ID_PDEX100_SUPER_BOT_P); // real super bot user
//        return jda.getUserById(DiscordEntityConstants.USER_ID_QN234);
    }

}
