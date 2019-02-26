package pogo.assistance.bot.responder.relay.pokedex100;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import net.dv8tion.jda.core.entities.User;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.data.exchange.spawn.PokemonSpawnObserver;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

/**
 * Observers pokemon spawning events and relays new spawns to PokeDex100 server.
 */
@Singleton
public class Pokedex100SpawnRelay implements PokemonSpawnObserver {

    private final User superBotP;

    @Inject
    public Pokedex100SpawnRelay(@Named(DiscordEntityConstants.NAME_PDEX100_SUPER_BOT_P) final User superBotP) {
        this.superBotP = superBotP;
    }

    @Override
    public void observe(final PokemonSpawn pokemonSpawn) {
        if (pokemonSpawn.getIv().orElse(-1.0) == 100) {
            superBotP.openPrivateChannel().complete()
                    .sendMessage(VerifierBotUtils.toVerifierBotCommand(pokemonSpawn)).queue();
        }
    }
}
