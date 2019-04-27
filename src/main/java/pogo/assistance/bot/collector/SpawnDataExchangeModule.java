package pogo.assistance.bot.collector;

import java.util.Collections;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableSet;
import dagger.Module;
import dagger.Provides;
import pogo.assistance.bot.responder.relay.pokedex100.Pokedex100SpawnRelay;
import pogo.assistance.bot.responder.relay.pokedex100.SpawnStatisticsRelay;
import pogo.assistance.data.exchange.spawn.PokemonSpawnExchange;
import pogo.assistance.data.extraction.source.discord.DiscordPokemonSpawnListener;
import pogo.assistance.data.extraction.source.discord.GenericSpawnMessageProcessor;
import pogo.assistance.data.extraction.source.discord.flpokemap.FLPokeMapSpawnMessageProcessor;
import pogo.assistance.data.extraction.source.discord.pineapplemap.PineappleMapSpawnMessageProcessor;
import pogo.assistance.data.extraction.source.discord.pogosj1.PoGoSJSpawnMessageProcessorV2;
import pogo.assistance.data.extraction.source.discord.pokefairy.PokeFairySpawnMessageProcessor;
import pogo.assistance.data.extraction.source.discord.safarisight.SafariSightSpawnMessageProcessor;
import pogo.assistance.data.extraction.source.discord.sandiego.SDHSpawnMessageProcessor;
import pogo.assistance.data.extraction.source.discord.southwest.SouthwestPokemonSpawnMessageProcessor;
import pogo.assistance.data.extraction.source.discord.vascans.VAScansSpawnMessageProcessor;
import pogo.assistance.data.extraction.source.discord.wecatch.WeCatchSpawnMessageProcessor;
import pogo.assistance.utils.debug.ServerLogger;

/**
 * This module registers the listeners we need to put on the data collecting user JDAs.
 */
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
            final PokemonSpawnExchange spawnExchange,
            final ServerLogger serverLogger) {
        final FLPokeMapSpawnMessageProcessor flpmSpawnMessageProcessor = new FLPokeMapSpawnMessageProcessor();
        return new DiscordPokemonSpawnListener(
                ImmutableSet.of(flpmSpawnMessageProcessor),
                ImmutableSet.of(flpmSpawnMessageProcessor, new SafariSightSpawnMessageProcessor()),
                spawnExchange,
                serverLogger);
    }

    /**
     * 'Benin' user has access to:
     *  - San Diego Hills
     */
    @Named(CollectorJDAModule.NAME_BENIN_USER_SPAWN_LISTENER)
    @Provides
    public static DiscordPokemonSpawnListener provideSpawnListenerToUserForBeninUserJDA(
            final PokemonSpawnExchange spawnExchange,
            final ServerLogger serverLogger) {
        return new DiscordPokemonSpawnListener(
                Collections.emptySet(),
                ImmutableSet.of(new SDHSpawnMessageProcessor()),
                spawnExchange,
                serverLogger);
    }

    /**
     * 'Niners' user has access to:
     *  - POGO SJ
     *  - NYC PokeMap (unused here since we have crawler for it)
     *  - VAScans
     *  - We Catch
     *  - Poke Fairy
     *  - Pineapple
     *  - PokeSquad
     *  - SGV Scans
     *  - BMPGO World
     */
    @Named(CollectorJDAModule.NAME_NINERS_USER_SPAWN_LISTENER)
    @Provides
    public static DiscordPokemonSpawnListener provideSpawnListenerToUserForNinersUserJDA(
            final PokemonSpawnExchange spawnExchange,
            final ServerLogger serverLogger) {
        return new DiscordPokemonSpawnListener(
                ImmutableSet.of(
                        new PoGoSJSpawnMessageProcessorV2(),
                        new VAScansSpawnMessageProcessor(),
                        new WeCatchSpawnMessageProcessor(),
                        new PokeFairySpawnMessageProcessor(),
                        new PineappleMapSpawnMessageProcessor(),
                        new GenericSpawnMessageProcessor()),
                Collections.emptySet(),
                spawnExchange,
                serverLogger);
    }

    /**
     * 'Johnny' user has access to:
     *  - Chicagoland
     */
    @Named(CollectorJDAModule.NAME_JOHNNY_USER_SPAWN_LISTENER)
    @Provides
    public static DiscordPokemonSpawnListener provideSpawnListenerToUserForJohnnyUserJDA(
            final PokemonSpawnExchange spawnExchange,
            final ServerLogger serverLogger) {
        return new DiscordPokemonSpawnListener(
                ImmutableSet.of(new PineappleMapSpawnMessageProcessor()),
                Collections.emptySet(),
                spawnExchange,
                serverLogger);
    }

    /**
     * 'Timburty' user has access to:
     *  - Chicagoland
     */
    @Named(CollectorJDAModule.NAME_TIMBURTY_USER_SPAWN_LISTENER)
    @Provides
    public static DiscordPokemonSpawnListener provideSpawnListenerToUserForTimburtyUserJDA(
            final PokemonSpawnExchange spawnExchange,
            final ServerLogger serverLogger) {
        return new DiscordPokemonSpawnListener(
                ImmutableSet.of(new SouthwestPokemonSpawnMessageProcessor()),
                Collections.emptySet(),
                spawnExchange,
                serverLogger);
    }

    /**
     * 'Irvin88' user has access to:
     *  - VCPokeScan
     *  - PoGoBadger's Den
     *  - North Houston Trainers
     */
    @Named(CollectorJDAModule.NAME_IRVIN88_USER_SPAWN_LISTENER)
    @Provides
    public static DiscordPokemonSpawnListener provideSpawnListenerToUserForIrvin88UserJDA(
            final PokemonSpawnExchange spawnExchange,
            final ServerLogger serverLogger) {
        return new DiscordPokemonSpawnListener(
                ImmutableSet.of(new GenericSpawnMessageProcessor()),
                Collections.emptySet(),
                spawnExchange,
                serverLogger);
    }

    @Singleton
    @Provides
    public static PokemonSpawnExchange providePokemonSpawnExchange(
            final Provider<Pokedex100SpawnRelay> pokedex100SpawnRelayProvider,
            final Provider<SpawnStatisticsRelay> statisticsRelayProvider) {
        return new PokemonSpawnExchange(ImmutableSet.of(
                pokedex100SpawnRelayProvider.get(),
                statisticsRelayProvider.get()));
    }

}
