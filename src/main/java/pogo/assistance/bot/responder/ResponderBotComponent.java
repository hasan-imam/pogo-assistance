package pogo.assistance.bot.responder;

import dagger.BindsInstance;
import dagger.Component;
import javax.inject.Named;
import javax.inject.Singleton;
import net.dv8tion.jda.core.AccountType;
import pogo.assistance.bot.di.DiscordEntityModule;
import pogo.assistance.bot.di.DiscordEntityConstants;

@Singleton
@Component(modules = { DiscordEntityModule.class })
public interface ResponderBotComponent {

    ResponderBot getResponderBot();

    @Component.Builder
    interface Builder {
        /**
         * {@link net.dv8tion.jda.core.JDA} will use this token to login.
         */
        @BindsInstance
        Builder userToken(@Named(DiscordEntityConstants.NAME_TOKEN) final String userName);

        @BindsInstance
        Builder accountType(final AccountType accountType);

        ResponderBotComponent build();
    }
}
