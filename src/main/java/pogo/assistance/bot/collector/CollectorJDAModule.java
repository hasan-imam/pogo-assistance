package pogo.assistance.bot.collector;

import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_BENIN_USER;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_CORRUPTED_USER;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_M15MV1_USER;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_NINERS_USER;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_USER_TOKEN_BENIN;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_USER_TOKEN_CORRUPTED;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_USER_TOKEN_M15MV1;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_USER_TOKEN_NINERS;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.security.auth.login.LoginException;

import dagger.Module;
import dagger.Provides;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import pogo.assistance.data.extraction.source.discord.DiscordPokemonSpawnListener;

@Module
class CollectorJDAModule {

    static final String NAME_CORRUPTED_USER_SPAWN_LISTERNER = "corrupted_user_spawn_listener";
    static final String NAME_BENIN_USER_SPAWN_LISTENER = "benin_user_spawn_listener";
    static final String NAME_NINERS_USER_SPAWN_LISTENER = "niners_user_spawn_listener";

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
    @Named(NAME_JDA_BENIN_USER)
    public static JDA provideBeninUserJda(
            @Named(NAME_USER_TOKEN_BENIN) final String token,
            @Named(NAME_BENIN_USER_SPAWN_LISTENER) final DiscordPokemonSpawnListener discordPokemonSpawnListener) {

        final JDABuilder jdaBuilder = new JDABuilder(AccountType.CLIENT);
        jdaBuilder.setToken(token);
        jdaBuilder.addEventListener(discordPokemonSpawnListener);

        try {
            return jdaBuilder.build().awaitReady();
        } catch (final InterruptedException | LoginException e) {
            throw new RuntimeException("Failed to setup JDA", e);
        }
    }

    @Singleton
    @Provides
    @Named(NAME_JDA_NINERS_USER)
    public static JDA provideNinersUserJda(
            @Named(NAME_USER_TOKEN_NINERS) final String token,
            @Named(NAME_NINERS_USER_SPAWN_LISTENER) final DiscordPokemonSpawnListener discordPokemonSpawnListener) {

        final JDABuilder jdaBuilder = new JDABuilder(AccountType.CLIENT);
        jdaBuilder.setToken(token);
        jdaBuilder.addEventListener(discordPokemonSpawnListener);

        try {
            return jdaBuilder.build().awaitReady();
        } catch (final InterruptedException | LoginException e) {
            throw new RuntimeException("Failed to setup JDA", e);
        }
    }

    /**
     * No spawn listeners for user since it only relays to super bot, doesn't collect spawn data. It only registers control
     * elements such as the {@link KillSwitch}.
     */
    @Singleton
    @Provides
    @Named(NAME_JDA_M15MV1_USER)
    public static JDA provideControlUserJda(@Named(NAME_USER_TOKEN_M15MV1) final String token) {
        final JDABuilder jdaBuilder = new JDABuilder(AccountType.CLIENT);
        jdaBuilder.setToken(token);
        jdaBuilder.addEventListener(new KillSwitch());

        try {
            return jdaBuilder.build().awaitReady();
        } catch (final InterruptedException | LoginException e) {
            throw new RuntimeException("Failed to setup JDA", e);
        }
    }

}
