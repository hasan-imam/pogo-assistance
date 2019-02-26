package pogo.assistance.data.exchange.spawn;

import com.google.common.collect.ImmutableSet;
import dagger.Module;
import dagger.Provides;
import java.util.Set;
import pogo.assistance.data.extraction.source.discord.DiscordPokemonSpawnListener;
import pogo.assistance.data.extraction.source.discord.MessageProcessor;
import pogo.assistance.data.extraction.source.discord.nycpokemap.NycPokeMapSpawnMessageProcessor;
import pogo.assistance.data.extraction.source.discord.pogosj1.PoGoSJSpawnMessageProcessor;
import pogo.assistance.data.extraction.source.discord.vascans.VAScansSpawnMessageProcessor;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

@Module
public class PokemonSpawnExchangeModule {

    @Provides
    public static DiscordPokemonSpawnListener provideDiscordPokemonSpawnListener(
            final Set<MessageProcessor<PokemonSpawn>> messageProcessors,
            final PokemonSpawnExchange pokemonSpawnExchange) {
        return new DiscordPokemonSpawnListener(messageProcessors, pokemonSpawnExchange);
    }

    @Provides
    public static Set<MessageProcessor<PokemonSpawn>> provideMessageProcessors() {
        return ImmutableSet.of(
                new NycPokeMapSpawnMessageProcessor(),
                new PoGoSJSpawnMessageProcessor(),
                new VAScansSpawnMessageProcessor());
    }

}
