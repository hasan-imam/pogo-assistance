package pogo.assistance.bot.collector;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.data.serde.SerDeModule;

@Singleton
@Component(modules = { CollectorJDAModule.class, SpawnDataExchangeModule.class,
        SerDeModule.class, SpawnWebCrawlerModule.class})
public interface SpawnDataCollectorBotComponent {

    SpawnDataCollectorBot getSpawnDataCollectorBot();

    @Component.Builder
    interface Builder {
        @BindsInstance
        SpawnDataCollectorBotComponent.Builder corruptedUserToken(
                @Named(DiscordEntityConstants.NAME_USER_TOKEN_CORRUPTED) final String corruptedUserToken);

        @BindsInstance
        SpawnDataCollectorBotComponent.Builder beninUserToken(
                @Named(DiscordEntityConstants.NAME_USER_TOKEN_BENIN) final String beninUserToken);

        @BindsInstance
        SpawnDataCollectorBotComponent.Builder ninersUserToken(
                @Named(DiscordEntityConstants.NAME_USER_TOKEN_NINERS) final String ninersUserToken);

        @BindsInstance
        SpawnDataCollectorBotComponent.Builder johnnyUserToken(
                @Named(DiscordEntityConstants.NAME_USER_TOKEN_JOHNNY) final String johnnyUserToken);

        @BindsInstance
        SpawnDataCollectorBotComponent.Builder controlUserToken(
                @Named(DiscordEntityConstants.NAME_USER_TOKEN_M15MV1) final String controlUserToken);

        SpawnDataCollectorBotComponent build();
    }

}
