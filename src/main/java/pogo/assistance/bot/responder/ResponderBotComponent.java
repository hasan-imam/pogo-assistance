package pogo.assistance.bot.responder;

import java.util.Set;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import net.dv8tion.jda.core.AccountType;
import pogo.assistance.bot.di.DiscordEntityConstants;

@Singleton
@Component(modules = { ResponderBotModule.class })
public interface ResponderBotComponent {

    ResponderBot getResponderBot();

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder owningUserToken(@Named(DiscordEntityConstants.NAME_USER_TOKEN_OWNER) final String owningUserToken);

        @BindsInstance
        Builder horuseusUserToken(@Named(DiscordEntityConstants.NAME_USER_TOKEN_HORUSEUS) final String owningUserToken);

        @BindsInstance
        Builder controlUserToken(@Named(DiscordEntityConstants.NAME_USER_TOKEN_M15M) final String controlUserToken);

        @BindsInstance
        Builder accountType(final AccountType accountType);

        @BindsInstance
        Builder listenerIds(final Set<ListenerId> listenerIds);

        ResponderBotComponent build();
    }
}
