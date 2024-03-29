package pogo.assistance.bot.collector;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.security.auth.login.LoginException;

import dagger.Module;
import dagger.Provides;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import pogo.assistance.data.extraction.source.discord.DiscordPokemonSpawnListener;

import static pogo.assistance.bot.di.DiscordEntityConstants.*;

@Module
class CollectorJDAModule {

    static final String NAME_CORRUPTED_USER_SPAWN_LISTERNER = "corrupted_user_spawn_listener";
    static final String NAME_BENIN_USER_SPAWN_LISTENER = "benin_user_spawn_listener";
    static final String NAME_NINERS_USER_SPAWN_LISTENER = "niners_user_spawn_listener";
    static final String NAME_JOHNNY_USER_SPAWN_LISTENER = "johnny_user_spawn_listener";
    static final String NAME_TIMBURTY_USER_SPAWN_LISTENER = "timburty_user_spawn_listener";
    static final String NAME_IRVIN88_USER_SPAWN_LISTENER = "irvin88_user_spawn_listener";
    static final String NAME_CONNOISSEUR_USER_SPAWN_LISTENER = "connoisseur_user_spawn_listener";
    static final String NAME_CHRONIC_USER_SPAWN_LISTENER = "chronic_user_spawn_listener";
    static final String NAME_CRANK_USER_SPAWN_LISTENER = "crank_user_spawn_listener";
    static final String NAME_POGO_HERO_USER_SPAWN_LISTENER = "pogo_hero_user_spawn_listener";
    static final String NAME_MICHELLEX_USER_SPAWN_LISTENER = "michellex_user_spawn_listener";
    static final String NAME_POKE_PETER_USER_SPAWN_LISTENER = "poke_peter_user_spawn_listener";
    static final String NAME_AMY_USER_SPAWN_LISTENER = "amy_user_spawn_listener";
    static final String NAME_ALEXA_USER_SPAWN_LISTENER = "alexa_user_spawn_listener";
    static final String NAME_SHADOW_USER_SPAWN_LISTENER = "shadow_user_spawn_listener";

    @Singleton
    @Provides
    @Named(NAME_JDA_CORRUPTED_USER)
    public static JDA provideCorruptedUserJda(
            @Named(NAME_USER_TOKEN_CORRUPTED) final String token,
            @Named(NAME_CORRUPTED_USER_SPAWN_LISTERNER) final DiscordPokemonSpawnListener discordPokemonSpawnListener) {

        final JDABuilder jdaBuilder = new JDABuilder(AccountType.CLIENT);
        jdaBuilder.setToken(token);
        jdaBuilder.addEventListeners(discordPokemonSpawnListener);
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
        jdaBuilder.addEventListeners(discordPokemonSpawnListener);

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
        jdaBuilder.addEventListeners(discordPokemonSpawnListener);

        try {
            return jdaBuilder.build().awaitReady();
        } catch (final InterruptedException | LoginException e) {
            throw new RuntimeException("Failed to setup JDA", e);
        }
    }

    @Singleton
    @Provides
    @Named(NAME_JDA_JOHNNY_USER)
    public static JDA provideJohnnyUserJda(
            @Named(NAME_USER_TOKEN_JOHNNY) final String token,
            @Named(NAME_JOHNNY_USER_SPAWN_LISTENER) final DiscordPokemonSpawnListener discordPokemonSpawnListener) {

        final JDABuilder jdaBuilder = new JDABuilder(AccountType.CLIENT);
        jdaBuilder.setToken(token);
        jdaBuilder.addEventListeners(discordPokemonSpawnListener);

        try {
            return jdaBuilder.build().awaitReady();
        } catch (final InterruptedException | LoginException e) {
            throw new RuntimeException("Failed to setup JDA", e);
        }
    }

    @Singleton
    @Provides
    @Named(NAME_JDA_TIMBURTY_USER)
    public static JDA provideTimburtyUserJda(
            @Named(NAME_USER_TOKEN_TIMBURTY) final String token,
            @Named(NAME_TIMBURTY_USER_SPAWN_LISTENER) final DiscordPokemonSpawnListener discordPokemonSpawnListener) {

        final JDABuilder jdaBuilder = new JDABuilder(AccountType.CLIENT);
        jdaBuilder.setToken(token);
        jdaBuilder.addEventListeners(discordPokemonSpawnListener);

        try {
            return jdaBuilder.build().awaitReady();
        } catch (final InterruptedException | LoginException e) {
            throw new RuntimeException("Failed to setup JDA", e);
        }
    }

    @Singleton
    @Provides
    @Named(NAME_JDA_IRVIN88_USER)
    public static JDA provideIrvin88UserJda(
            @Named(NAME_USER_TOKEN_IRVIN88) final String token,
            @Named(NAME_IRVIN88_USER_SPAWN_LISTENER) final DiscordPokemonSpawnListener discordPokemonSpawnListener) {

        final JDABuilder jdaBuilder = new JDABuilder(AccountType.CLIENT);
        jdaBuilder.setToken(token);
        jdaBuilder.addEventListeners(discordPokemonSpawnListener);

        try {
            return jdaBuilder.build().awaitReady();
        } catch (final InterruptedException | LoginException e) {
            throw new RuntimeException("Failed to setup JDA", e);
        }
    }

    @Singleton
    @Provides
    @Named(NAME_JDA_CONNOISSEUR_USER)
    public static JDA provideConnoisseurUserJda(
            @Named(NAME_USER_TOKEN_CONNOISSEUR) final String token,
            @Named(NAME_CONNOISSEUR_USER_SPAWN_LISTENER) final DiscordPokemonSpawnListener discordPokemonSpawnListener) {

        final JDABuilder jdaBuilder = new JDABuilder(AccountType.CLIENT);
        jdaBuilder.setToken(token);
        jdaBuilder.addEventListeners(discordPokemonSpawnListener);

        try {
            return jdaBuilder.build().awaitReady();
        } catch (final InterruptedException | LoginException e) {
            throw new RuntimeException("Failed to setup JDA", e);
        }
    }

    @Singleton
    @Provides
    @Named(NAME_JDA_CHRONIC_USER)
    public static JDA provideChronicUserJda(
            @Named(NAME_USER_TOKEN_CHRONIC) final String token,
            @Named(NAME_CHRONIC_USER_SPAWN_LISTENER) final DiscordPokemonSpawnListener discordPokemonSpawnListener) {

        final JDABuilder jdaBuilder = new JDABuilder(AccountType.CLIENT);
        jdaBuilder.setToken(token);
        jdaBuilder.addEventListeners(discordPokemonSpawnListener);

        try {
            return jdaBuilder.build().awaitReady();
        } catch (final InterruptedException | LoginException e) {
            throw new RuntimeException("Failed to setup JDA", e);
        }
    }

    @Singleton
    @Provides
    @Named(NAME_JDA_CRANK_USER)
    public static JDA provideCrankUserJda(
            @Named(NAME_USER_TOKEN_CRANK) final String token,
            @Named(NAME_CRANK_USER_SPAWN_LISTENER) final DiscordPokemonSpawnListener discordPokemonSpawnListener) {

        final JDABuilder jdaBuilder = new JDABuilder(AccountType.CLIENT);
        jdaBuilder.setToken(token);
        jdaBuilder.addEventListeners(discordPokemonSpawnListener);

        try {
            return jdaBuilder.build().awaitReady();
        } catch (final InterruptedException | LoginException e) {
            throw new RuntimeException("Failed to setup JDA", e);
        }
    }

    @Singleton
    @Provides
    @Named(NAME_JDA_POGO_HERO_USER)
    public static JDA providePoGoHeroUserJda(
            @Named(NAME_USER_TOKEN_POGO_HERO) final String token,
            @Named(NAME_POGO_HERO_USER_SPAWN_LISTENER) final DiscordPokemonSpawnListener discordPokemonSpawnListener) {

        final JDABuilder jdaBuilder = new JDABuilder(AccountType.CLIENT);
        jdaBuilder.setToken(token);
        jdaBuilder.addEventListeners(discordPokemonSpawnListener);

        try {
            return jdaBuilder.build().awaitReady();
        } catch (final InterruptedException | LoginException e) {
            throw new RuntimeException("Failed to setup JDA", e);
        }
    }

    @Singleton
    @Provides
    @Named(NAME_JDA_MICHELLEX_USER)
    public static JDA provideMichellexUserJda(
            @Named(NAME_USER_TOKEN_MICHELLEX) final String token,
            @Named(NAME_MICHELLEX_USER_SPAWN_LISTENER) final DiscordPokemonSpawnListener discordPokemonSpawnListener) {

        final JDABuilder jdaBuilder = new JDABuilder(AccountType.CLIENT);
        jdaBuilder.setToken(token);
        jdaBuilder.addEventListeners(discordPokemonSpawnListener);

        try {
            return jdaBuilder.build().awaitReady();
        } catch (final InterruptedException | LoginException e) {
            throw new RuntimeException("Failed to setup JDA", e);
        }
    }

    @Singleton
    @Provides
    @Named(NAME_JDA_POKE_PETER_USER)
    public static JDA providePokePeterUserJda(
            @Named(NAME_USER_TOKEN_POKE_PETER) final String token,
            @Named(NAME_POKE_PETER_USER_SPAWN_LISTENER) final DiscordPokemonSpawnListener discordPokemonSpawnListener) {

        final JDABuilder jdaBuilder = new JDABuilder(AccountType.CLIENT);
        jdaBuilder.setToken(token);
        jdaBuilder.addEventListeners(discordPokemonSpawnListener);

        try {
            return jdaBuilder.build().awaitReady();
        } catch (final InterruptedException | LoginException e) {
            throw new RuntimeException("Failed to setup JDA", e);
        }
    }

    @Singleton
    @Provides
    @Named(NAME_JDA_AMY_USER)
    public static JDA provideAmyUserJda(
            @Named(NAME_USER_TOKEN_AMY) final String token,
            @Named(NAME_AMY_USER_SPAWN_LISTENER) final DiscordPokemonSpawnListener discordPokemonSpawnListener) {

        final JDABuilder jdaBuilder = new JDABuilder(AccountType.CLIENT);
        jdaBuilder.setToken(token);
        jdaBuilder.addEventListeners(discordPokemonSpawnListener);

        try {
            return jdaBuilder.build().awaitReady();
        } catch (final InterruptedException | LoginException e) {
            throw new RuntimeException("Failed to setup JDA", e);
        }
    }

    @Singleton
    @Provides
    @Named(NAME_JDA_ALEXA_USER)
    public static JDA provideAlexaUserJda(
            @Named(NAME_USER_TOKEN_ALEXA) final String token,
            @Named(NAME_ALEXA_USER_SPAWN_LISTENER) final DiscordPokemonSpawnListener discordPokemonSpawnListener) {

        final JDABuilder jdaBuilder = new JDABuilder(AccountType.CLIENT);
        jdaBuilder.setToken(token);
        jdaBuilder.addEventListeners(discordPokemonSpawnListener);

        try {
            return jdaBuilder.build().awaitReady();
        } catch (final InterruptedException | LoginException e) {
            throw new RuntimeException("Failed to setup JDA", e);
        }
    }

    @Singleton
    @Provides
    @Named(NAME_JDA_SHADOW_USER)
    public static JDA provideShadowUserJda(
            @Named(NAME_USER_TOKEN_SHADOW) final String token,
            @Named(NAME_SHADOW_USER_SPAWN_LISTENER) final DiscordPokemonSpawnListener discordPokemonSpawnListener) {

        final JDABuilder jdaBuilder = new JDABuilder(AccountType.CLIENT);
        jdaBuilder.setToken(token);
        jdaBuilder.addEventListeners(discordPokemonSpawnListener);

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
    @Named(NAME_JDA_M15M_BOT)
    public static JDA provideControlUserJda(@Named(NAME_USER_TOKEN_M15M) final String token) {
        final JDABuilder jdaBuilder = new JDABuilder(AccountType.BOT);
        jdaBuilder.setToken(token);
        jdaBuilder.addEventListeners(new KillSwitch());

        try {
            return jdaBuilder.build().awaitReady();
        } catch (final InterruptedException | LoginException e) {
            throw new RuntimeException("Failed to setup JDA", e);
        }
    }

}
