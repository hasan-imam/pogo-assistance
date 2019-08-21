package pogo.assistance.bot.responder;

import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_JDA_M15M_BOT;
import static pogo.assistance.bot.di.DiscordEntityConstants.NAME_USER_TOKEN_M15M;

import java.util.Set;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.security.auth.login.LoginException;

import dagger.Module;
import dagger.Provides;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.bot.responder.relay.pokedex100.PokexToPokedex100Tunnel;

@Module
class ResponderBotModule {

    @Singleton
    @Provides
    @Named(NAME_JDA_M15M_BOT)
    public static JDA provideControlUserJda(@Named(NAME_USER_TOKEN_M15M) final String token) {
        final JDABuilder jdaBuilder = new JDABuilder(AccountType.BOT);
        jdaBuilder.setToken(token);
        try {
            return jdaBuilder.build().awaitReady();
        } catch (final InterruptedException | LoginException e) {
            throw new RuntimeException("Failed to setup JDA", e);
        }
    }

    @Provides
    @Named(DiscordEntityConstants.NAME_JDA_BUILDER_OWNING_USER)
    public static JDABuilder provideOwningUserJdaBuilder(
            @Named(DiscordEntityConstants.NAME_USER_TOKEN_OWNER) final String id,
            final AccountType accountType,
            final Set<ListenerId> listenerIds,
            final Provider<RepHandler> repHandlerProvider) {
        final JDABuilder jdaBuilder = new JDABuilder(accountType);
        jdaBuilder.setToken(id);

        if (listenerIds.contains(ListenerId.REP_HANDLER)) {
            jdaBuilder.addEventListeners(repHandlerProvider.get());
        }

        return jdaBuilder;
    }

    @Provides
    @Named(DiscordEntityConstants.NAME_JDA_BUILDER_HORUSEUS_USER)
    public static JDABuilder provideHoruseusUserJdaBuilder(
            @Named(DiscordEntityConstants.NAME_USER_TOKEN_HORUSEUS) final String id,
            final AccountType accountType,
            final Set<ListenerId> listenerIds,
            final Provider<PokexToPokedex100Tunnel> pokexToPokedex100TunnelProvider) {
        final JDABuilder jdaBuilder = new JDABuilder(accountType);
        jdaBuilder.setToken(id);

        if (listenerIds.contains(ListenerId.RELAY_POKEX_TO_PDEX100)) {
            jdaBuilder.addEventListeners(pokexToPokedex100TunnelProvider.get());
        }

        return jdaBuilder;
    }

}
