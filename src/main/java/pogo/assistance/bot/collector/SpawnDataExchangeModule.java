package pogo.assistance.bot.collector;

import com.google.common.collect.ImmutableSet;
import dagger.Module;
import dagger.Provides;
import java.util.Collections;
import javax.inject.Named;
import javax.inject.Provider;
import pogo.assistance.bot.responder.relay.pokedex100.Pokedex100SpawnRelay;
import pogo.assistance.data.exchange.spawn.PokemonSpawnExchange;
import pogo.assistance.data.extraction.source.discord.DiscordPokemonSpawnListener;
import pogo.assistance.data.extraction.source.discord.flpokemap.FLPokeMapSpawnMessageProcessor;
import pogo.assistance.data.extraction.source.discord.nycpokemap.NycPokeMapSpawnMessageProcessor;
import pogo.assistance.data.extraction.source.discord.pogosj1.PoGoSJSpawnMessageProcessor;
import pogo.assistance.data.extraction.source.discord.vascans.VAScansSpawnMessageProcessor;

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
                ImmutableSet.of(flpmSpawnMessageProcessor),
                spawnExchange);
    }

    /**
     * Owning user has access to:
     *  - POGO SJ
     *  - NYC PokeMap
     *  - VAScans
     */
    @Named(CollectorJDAModule.NAME_OWNING_USER_SPAWN_LISTENER)
    @Provides
    public static DiscordPokemonSpawnListener provideSpawnListenerToUserForOwningUserJDA(
            final PokemonSpawnExchange spawnExchange) {
        return new DiscordPokemonSpawnListener(
                ImmutableSet.of(
                        new NycPokeMapSpawnMessageProcessor(),
                        new PoGoSJSpawnMessageProcessor(),
                        new VAScansSpawnMessageProcessor()),
                Collections.emptySet(),
                spawnExchange);
    }

    @Provides
    public static PokemonSpawnExchange providePokemonSpawnExchange(
            final Provider<Pokedex100SpawnRelay> pokedex100SpawnRelayProvider) {
        return new PokemonSpawnExchange(ImmutableSet.of(pokedex100SpawnRelayProvider.get()));
    }

}
