package pogo.assistance.bot.collector;

import dagger.BindsInstance;
import dagger.Component;
import javax.inject.Named;
import javax.inject.Singleton;
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
                @Named(DiscordEntityConstants.NAME_USER_TOKEN_BENIN) final String corruptedUserToken);

        @BindsInstance
        SpawnDataCollectorBotComponent.Builder owningUserToken(
                @Named(DiscordEntityConstants.NAME_USER_TOKEN_OWNER) final String corruptedUserToken);

        SpawnDataCollectorBotComponent build();
    }

}
