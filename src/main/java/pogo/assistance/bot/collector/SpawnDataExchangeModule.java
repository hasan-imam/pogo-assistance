package pogo.assistance.bot.collector;

import java.util.Collections;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableSet;
import dagger.Module;
import dagger.Provides;
import pogo.assistance.bot.responder.relay.pokedex100.Pokedex100SpawnRelay;
import pogo.assistance.data.exchange.spawn.PokemonSpawnExchange;
import pogo.assistance.data.extraction.source.discord.DiscordPokemonSpawnListener;
import pogo.assistance.data.extraction.source.discord.flpokemap.FLPokeMapSpawnMessageProcessor;
import pogo.assistance.data.extraction.source.discord.pineapplemap.PineappleMapSpawnMessageProcessor;
import pogo.assistance.data.extraction.source.discord.pogosj1.PoGoSJSpawnMessageProcessorV2;
import pogo.assistance.data.extraction.source.discord.pokefairy.PokeFairySpawnMessageProcessor;
import pogo.assistance.data.extraction.source.discord.safarisight.SafariSightSpawnMessageProcessor;
import pogo.assistance.data.extraction.source.discord.sandiego.SDHSpawnMessageProcessor;
import pogo.assistance.data.extraction.source.discord.vascans.VAScansSpawnMessageProcessor;
import pogo.assistance.data.extraction.source.discord.wecatch.WeCatchSpawnMessageProcessor;

@Module
class SpawnDataExchangeModule {

    /**
     * 'Corrupted' user has access to:
     *  - FLPM alerts
     *  - Alpha Pokes alerts
     *  - Safari Sight Nova
     */
    @Named(CollectorJDAModule.NAME_CORRUPTED_USER_SPAWN_LISTERNER)
    @Provides
    public static DiscordPokemonSpawnListener provideSpawnListenerToUserForCorruptedUserJDA(
            final PokemonSpawnExchange spawnExchange) {
        final FLPokeMapSpawnMessageProcessor flpmSpawnMessageProcessor = new FLPokeMapSpawnMessageProcessor();
        return new DiscordPokemonSpawnListener(
                ImmutableSet.of(flpmSpawnMessageProcessor),
                ImmutableSet.of(flpmSpawnMessageProcessor, new SafariSightSpawnMessageProcessor()),
                spawnExchange);
    }

    /**
     * 'Corrupted' user has access to:
     *  - San Diego Hills
     */
    @Named(CollectorJDAModule.NAME_BENIN_USER_SPAWN_LISTENER)
    @Provides
    public static DiscordPokemonSpawnListener provideSpawnListenerToUserForBeninUserJDA(
            final PokemonSpawnExchange spawnExchange) {
        return new DiscordPokemonSpawnListener(
                Collections.emptySet(),
                ImmutableSet.of(new SDHSpawnMessageProcessor()),
                spawnExchange);
    }

    /**
     * Owning user has access to:
     *  - POGO SJ
     *  - NYC PokeMap
     *  - VAScans
     *  - We Catch
     *  - Poke Fairy
     */
    @Named(CollectorJDAModule.NAME_OWNING_USER_SPAWN_LISTENER)
    @Provides
    public static DiscordPokemonSpawnListener provideSpawnListenerToUserForOwningUserJDA(
            final PokemonSpawnExchange spawnExchange) {
        return new DiscordPokemonSpawnListener(
                ImmutableSet.of(
//                        // Disabling in favor of the web crawler
//                        new NycPokeMapSpawnMessageProcessor(),

                        new PoGoSJSpawnMessageProcessorV2(),
                        new VAScansSpawnMessageProcessor(),
                        new WeCatchSpawnMessageProcessor(),
                        new PokeFairySpawnMessageProcessor(),
                        new PineappleMapSpawnMessageProcessor()),
                Collections.emptySet(),
                spawnExchange);
    }

    @Singleton
    @Provides
    public static PokemonSpawnExchange providePokemonSpawnExchange(
            final Provider<Pokedex100SpawnRelay> pokedex100SpawnRelayProvider) {
        return new PokemonSpawnExchange(ImmutableSet.of(pokedex100SpawnRelayProvider.get()));
    }

}
