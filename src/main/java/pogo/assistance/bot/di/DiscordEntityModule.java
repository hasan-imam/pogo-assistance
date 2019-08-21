package pogo.assistance.bot.di;

import javax.inject.Named;
import javax.security.auth.login.LoginException;

import dagger.Module;
import dagger.Provides;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;

@Module
public class DiscordEntityModule {

    @Provides
    @Named(DiscordEntityConstants.NAME_JDA_OWNING_USER)
    public static JDA provideUserJda(@Named(DiscordEntityConstants.NAME_JDA_BUILDER_OWNING_USER) final JDABuilder jdaBuilder) {
        try {
            return jdaBuilder.build().awaitReady();
        } catch (final InterruptedException | LoginException e) {
            throw new RuntimeException("Failed to setup JDA", e);
        }
    }

    @Provides
    @Named(DiscordEntityConstants.NAME_JDA_BUILDER_OWNING_USER)
    public static JDABuilder provideUserJdaBuilder(
            @Named(DiscordEntityConstants.NAME_USER_TOKEN_OWNER) final String id,
            final AccountType accountType) {
        final JDABuilder jdaBuilder = new JDABuilder(accountType);
        jdaBuilder.setToken(id);
        return jdaBuilder;
    }

    @Provides
    @Named(DiscordEntityConstants.NAME_PDEX100_BOT_COMMAND_CHANNEL)
    public static TextChannel providesPDex100BotCommandChannel(
            @Named(DiscordEntityConstants.NAME_JDA_OWNING_USER) final JDA jda) {
        return jda.getTextChannelById(DiscordEntityConstants.CHANNEL_ID_PDEX100_BOT_COMMAND);
    }

}
