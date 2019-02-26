package pogo.assistance.bot.di;

import dagger.Module;
import dagger.Provides;
import javax.inject.Named;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.TextChannel;

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
            @Named(DiscordEntityConstants.NAME_OWNING_USER_TOKEN) final String id,
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
