package pogo.assistance.bot.collector;

import java.util.Collections;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import dagger.Module;
import dagger.Provides;
import pogo.assistance.bot.responder.relay.pokedex100.Pokedex100SpawnRelay;
import pogo.assistance.bot.responder.relay.pokedex100.SpawnStatisticsRelay;
import pogo.assistance.data.exchange.spawn.PokemonSpawnExchange;
import pogo.assistance.data.extraction.source.discord.DiscordPokemonSpawnListener;
import pogo.assistance.data.extraction.source.discord.GenericSpawnMessageProcessor;
import pogo.assistance.data.extraction.source.discord.articuno.ArticunoSpawnMessageProcessor;
import pogo.assistance.data.extraction.source.discord.flpokemap.FLPokeMapSpawnMessageProcessor;
import pogo.assistance.data.extraction.source.discord.pgan.PGANSpawnMessageProcessor;
import pogo.assistance.data.extraction.source.discord.pineapplemap.PineappleMapSpawnMessageProcessor;
import pogo.assistance.data.extraction.source.discord.safarisight.SafariSightSpawnMessageProcessor;
import pogo.assistance.data.extraction.source.discord.sandiego.SDHSpawnMessageProcessor;
import pogo.assistance.data.extraction.source.discord.sgv.SGVSpawnMessageProcessor;
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
     *  - Pokemon Maps Florida
     */
    @Named(CollectorJDAModule.NAME_CORRUPTED_USER_SPAWN_LISTERNER)
    @Provides
    public static DiscordPokemonSpawnListener provideSpawnListenerToUserForCorruptedUserJDA(
            final PokemonSpawnExchange spawnExchange,
            final ServerLogger serverLogger,
            final Gson gson) {
        final FLPokeMapSpawnMessageProcessor flpmSpawnMessageProcessor = new FLPokeMapSpawnMessageProcessor();
        return new DiscordPokemonSpawnListener(
                ImmutableSet.of(flpmSpawnMessageProcessor, new GenericSpawnMessageProcessor()),
                ImmutableSet.of(flpmSpawnMessageProcessor, new SafariSightSpawnMessageProcessor()),
                spawnExchange,
                serverLogger,
                gson);
    }

    /**
     * 'Benin' user has access to: (none at the moment)
     */
    @Named(CollectorJDAModule.NAME_BENIN_USER_SPAWN_LISTENER)
    @Provides
    public static DiscordPokemonSpawnListener provideSpawnListenerToUserForBeninUserJDA(
            final PokemonSpawnExchange spawnExchange,
            final ServerLogger serverLogger,
            final Gson gson) {
        return new DiscordPokemonSpawnListener(
                Collections.emptySet(),
                Collections.emptySet(),
                spawnExchange,
                serverLogger,
                gson);
    }

    /**
     * 'Niners' user has access to:
     *  - POGO SJ
     *  - VAScans
     *  - We Catch
     *  - TPF Fairymaps (basic + paid)
     *  - Pineapple
     *  - PokeSquad
     *  - SGV Scans
     *  - BMPGO World
     *  - Valley PoGo
     */
    @Named(CollectorJDAModule.NAME_NINERS_USER_SPAWN_LISTENER)
    @Provides
    public static DiscordPokemonSpawnListener provideSpawnListenerToUserForNinersUserJDA(
            final PokemonSpawnExchange spawnExchange,
            final ServerLogger serverLogger,
            final Gson gson) {
        return new DiscordPokemonSpawnListener(
                ImmutableSet.of(
                        new VAScansSpawnMessageProcessor(),
                        new WeCatchSpawnMessageProcessor(),
                        new PineappleMapSpawnMessageProcessor(),
                        new SGVSpawnMessageProcessor(),
                        new GenericSpawnMessageProcessor()),
                Collections.emptySet(),
                spawnExchange,
                serverLogger,
                gson);
    }

    /**
     * 'Johnny' user has access to:
     *  - Chicagoland
     *  - PoGo Alerts 847
     */
    @Named(CollectorJDAModule.NAME_JOHNNY_USER_SPAWN_LISTENER)
    @Provides
    public static DiscordPokemonSpawnListener provideSpawnListenerToUserForJohnnyUserJDA(
            final PokemonSpawnExchange spawnExchange,
            final ServerLogger serverLogger,
            final Gson gson) {
        return new DiscordPokemonSpawnListener(
                ImmutableSet.of(
                        new PineappleMapSpawnMessageProcessor(),
                        new GenericSpawnMessageProcessor()),
                Collections.emptySet(),
                spawnExchange,
                serverLogger,
                gson);
    }

    /**
     * 'Timburty' user has access to:
     *  - Chicagoland
     *  - Southwest Pokemon
     */
    @Named(CollectorJDAModule.NAME_TIMBURTY_USER_SPAWN_LISTENER)
    @Provides
    public static DiscordPokemonSpawnListener provideSpawnListenerToUserForTimburtyUserJDA(
            final PokemonSpawnExchange spawnExchange,
            final ServerLogger serverLogger,
            final Gson gson) {
        return new DiscordPokemonSpawnListener(
                ImmutableSet.of(new GenericSpawnMessageProcessor()),
                Collections.emptySet(),
                spawnExchange,
                serverLogger,
                gson);
    }

    /**
     * 'Irvin88' user has access to:
     *  - VCPokeScan
     *  - PoGoBadger's Den
     *  - North Houston Trainers
     *  - PGAN
     */
    @Named(CollectorJDAModule.NAME_IRVIN88_USER_SPAWN_LISTENER)
    @Provides
    public static DiscordPokemonSpawnListener provideSpawnListenerToUserForIrvin88UserJDA(
            final PokemonSpawnExchange spawnExchange,
            final ServerLogger serverLogger,
            final Gson gson) {
        return new DiscordPokemonSpawnListener(
                ImmutableSet.of(new GenericSpawnMessageProcessor()),
                ImmutableSet.of(new PGANSpawnMessageProcessor()),
                spawnExchange,
                serverLogger,
                gson);
    }

    /**
     * 'connoisseur' user has access to:
     *  - PokeXplorer
     *  - Pokemon Go Sofia
     */
    @Named(CollectorJDAModule.NAME_CONNOISSEUR_USER_SPAWN_LISTENER)
    @Provides
    public static DiscordPokemonSpawnListener provideSpawnListenerToUserForConnoisseurUserJDA(
            final PokemonSpawnExchange spawnExchange,
            final ServerLogger serverLogger,
            final Gson gson) {
        return new DiscordPokemonSpawnListener(
                ImmutableSet.of(new GenericSpawnMessageProcessor()),
                Collections.emptySet(),
                spawnExchange,
                serverLogger,
                gson);
    }

    /**
     * 'chronic' user has access to:
     *  - PGAN
     *  - iTools (Articuno bot)
     */
    @Named(CollectorJDAModule.NAME_CHRONIC_USER_SPAWN_LISTENER)
    @Provides
    public static DiscordPokemonSpawnListener provideSpawnListenerToUserForChronicUserJDA(
            final PokemonSpawnExchange spawnExchange,
            final ServerLogger serverLogger,
            final Gson gson) {
        return new DiscordPokemonSpawnListener(
                ImmutableSet.of(new ArticunoSpawnMessageProcessor()),
                ImmutableSet.of(new PGANSpawnMessageProcessor()),
                spawnExchange,
                serverLogger,
                gson);
    }

    /**
     * 'crank' user has access to:
     *  - PGAN
     */
    @Named(CollectorJDAModule.NAME_CRANK_USER_SPAWN_LISTENER)
    @Provides
    public static DiscordPokemonSpawnListener provideSpawnListenerToUserForCrankUserJDA(
            final PokemonSpawnExchange spawnExchange,
            final ServerLogger serverLogger,
            final Gson gson) {
        return new DiscordPokemonSpawnListener(
                Collections.emptySet(),
                ImmutableSet.of(new PGANSpawnMessageProcessor()),
                spawnExchange,
                serverLogger,
                gson);
    }

    /**
     * 'pogo hero' user has access to:
     *  - Utah Pokemon Go
     *  - CVM
     *  - GPGM
     *  - OC Scans
     *  - LV Raid Map
     *  - Pogo Ulm Karte
     *  - Indigo Plateau
     *  - pogochch2.0
     */
    @Named(CollectorJDAModule.NAME_POGO_HERO_USER_SPAWN_LISTENER)
    @Provides
    public static DiscordPokemonSpawnListener provideSpawnListenerToUserForPoGoHeroUserJDA(
            final PokemonSpawnExchange spawnExchange,
            final ServerLogger serverLogger,
            final Gson gson) {
        return new DiscordPokemonSpawnListener(
                ImmutableSet.of(new GenericSpawnMessageProcessor()),
                Collections.emptySet(),
                spawnExchange,
                serverLogger,
                gson);
    }

    /**
     * 'Michellex' user has access to:
     *  - San Diego Hills
     */
    @Named(CollectorJDAModule.NAME_MICHELLEX_USER_SPAWN_LISTENER)
    @Provides
    public static DiscordPokemonSpawnListener provideSpawnListenerToUserForMichellexUserJDA(
            final PokemonSpawnExchange spawnExchange,
            final ServerLogger serverLogger,
            final Gson gson) {
        return new DiscordPokemonSpawnListener(
                Collections.emptySet(),
                ImmutableSet.of(new SDHSpawnMessageProcessor()),
                spawnExchange,
                serverLogger,
                gson);
    }

    /**
     * 'PokePeter' user has access to:
     *  - Toast Maps
     *  - Oak Park
     */
    @Named(CollectorJDAModule.NAME_POKE_PETER_USER_SPAWN_LISTENER)
    @Provides
    public static DiscordPokemonSpawnListener provideSpawnListenerToUserForPokePeterUserJDA(
            final PokemonSpawnExchange spawnExchange,
            final ServerLogger serverLogger,
            final Gson gson) {
        return new DiscordPokemonSpawnListener(
                Collections.emptySet(),
                ImmutableSet.of(new GenericSpawnMessageProcessor()),
                spawnExchange,
                serverLogger,
                gson);
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
