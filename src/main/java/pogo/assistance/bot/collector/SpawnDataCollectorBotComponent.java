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
        SpawnDataCollectorBotComponent.Builder collectingUserToken(
                @Named(DiscordEntityConstants.NAME_USER_TOKEN_CORRUPTED) final String corruptedUserToken);

        @BindsInstance
        SpawnDataCollectorBotComponent.Builder relayingUserToken(
                @Named(DiscordEntityConstants.NAME_USER_TOKEN_OWNER) final String corruptedUserToken);

        SpawnDataCollectorBotComponent build();
    }

}
