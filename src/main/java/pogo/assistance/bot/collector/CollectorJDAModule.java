package pogo.assistance.bot.collector;

import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_CORRUPTED_USER;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_OWNING_USER;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_USER_TOKEN_CORRUPTED;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_USER_TOKEN_OWNER;

import dagger.Module;
import dagger.Provides;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import pogo.assistance.data.extraction.source.discord.DiscordPokemonSpawnListener;

@Module
class CollectorJDAModule {

    static final String NAME_CORRUPTED_USER_SPAWN_LISTERNER = "corrupted_user_spawn_listener";
    static final String NAME_OWNING_USER_SPAWN_LISTENER = "owning_user_spawn_listener";

    @Singleton
    @Provides
    @Named(NAME_JDA_CORRUPTED_USER)
    public static JDA provideCorruptedUserJda(
            @Named(NAME_USER_TOKEN_CORRUPTED) final String token,
            @Named(NAME_CORRUPTED_USER_SPAWN_LISTERNER) final DiscordPokemonSpawnListener discordPokemonSpawnListener) {

        final JDABuilder jdaBuilder = new JDABuilder(AccountType.CLIENT);
        jdaBuilder.setToken(token);
        jdaBuilder.addEventListener(discordPokemonSpawnListener);
        // TODO: add logging of what's being registered

        try {
            return jdaBuilder.build().awaitReady();
        } catch (final InterruptedException | LoginException e) {
            throw new RuntimeException("Failed to setup JDA", e);
        }
    }

    @Singleton
    @Provides
    @Named(NAME_JDA_OWNING_USER)
    public static JDA provideUserJda(
            @Named(NAME_USER_TOKEN_OWNER) final String token,
            @Named(NAME_OWNING_USER_SPAWN_LISTENER) final DiscordPokemonSpawnListener discordPokemonSpawnListener) {

        final JDABuilder jdaBuilder = new JDABuilder(AccountType.CLIENT);
        jdaBuilder.setToken(token);
        jdaBuilder.addEventListener(discordPokemonSpawnListener);
        jdaBuilder.addEventListener(new KillSwitch());

        try {
            return jdaBuilder.build().awaitReady();
        } catch (final InterruptedException | LoginException e) {
            throw new RuntimeException("Failed to setup JDA", e);
        }
    }

}
