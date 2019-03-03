package pogo.assistance.bot.responder;

import dagger.BindsInstance;
import dagger.Component;
import java.util.Set;
import javax.inject.Named;
import javax.inject.Singleton;
import net.dv8tion.jda.core.AccountType;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.bot.di.DiscordEntityModule;
import pogo.assistance.data.exchange.spawn.PokemonSpawnExchangeModule;

@Singleton
@Component(modules = { DiscordEntityModule.class, ResponderBotModule.class, PokemonSpawnExchangeModule.class})
public interface ResponderBotComponent {

    ResponderBot getResponderBot();

    @Component.Builder
    interface Builder {
        /**
         * {@link net.dv8tion.jda.core.JDA} will use this token to login. Limitation here is that everything under this
         * component will use the same token.
         */
        @BindsInstance
        Builder userToken(@Named(DiscordEntityConstants.NAME_USER_TOKEN_OWNER) final String owningUserToken);

        @BindsInstance
        Builder accountType(final AccountType accountType);

        @BindsInstance
        Builder listenerIds(final Set<ListenerId> listenerIds);

        ResponderBotComponent build();
    }
}
