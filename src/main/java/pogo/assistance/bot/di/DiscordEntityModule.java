package pogo.assistance.bot.di;

import dagger.Module;
import dagger.Provides;
import javax.inject.Named;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;

@Module
public class DiscordEntityModule {

    @Provides
    public static JDA provideJda(final JDABuilder jdaBuilder) {
        try {
            return jdaBuilder.build().awaitReady();
        } catch (final InterruptedException | LoginException e) {
            throw new RuntimeException("Failed to setup JDA", e);
        }
    }

    @Provides
    public static JDABuilder provideJdaBuilder(
            @Named(DiscordEntityConstants.NAME_TOKEN) final String id,
            final AccountType accountType) {
        final JDABuilder jdaBuilder = new JDABuilder(accountType);
        jdaBuilder.setToken(id);
        return jdaBuilder;
    }

}
