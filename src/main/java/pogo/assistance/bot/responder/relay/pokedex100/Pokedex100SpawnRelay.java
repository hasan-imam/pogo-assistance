package pogo.assistance.bot.responder.relay.pokedex100;

import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.User;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.data.exchange.spawn.PokemonSpawnObserver;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

/**
 * Observers pokemon spawning events and relays new spawns to PokeDex100 server.
 */
@Slf4j
@Singleton
public class Pokedex100SpawnRelay implements PokemonSpawnObserver {

    private final User superBotP;
    private final User h13m;

    private final User qn234;
    private final User josh;

    @Inject
    public Pokedex100SpawnRelay(@NonNull @Named(DiscordEntityConstants.NAME_PDEX100_SUPER_BOT_P) final User superBotP) {
        this.superBotP = superBotP;
        this.h13m = Objects.requireNonNull(superBotP.getJDA().getUserById(DiscordEntityConstants.USER_ID_H13M));

        // For debugging
        qn234 = Objects.requireNonNull(superBotP.getJDA().getUserById(DiscordEntityConstants.USER_ID_QN234));
        josh = Objects.requireNonNull(superBotP.getJDA().getUserById(DiscordEntityConstants.USER_ID_JOSH));
    }

    @Override
    public void observe(final PokemonSpawn pokemonSpawn) {
        if (pokemonSpawn.getIv().orElse(-1.0) == 100) {
            send100ivNotification(VerifierBotUtils.toVerifierBotCommand(pokemonSpawn));
        } else {
            log.debug("Ignoring spawn that didn't match posting criteria: " + pokemonSpawn);
        }
    }

    private void send100ivNotification(final String command) {
        // For debugging
        qn234.openPrivateChannel().complete().sendMessage(command).queue();
//        josh.openPrivateChannel().complete().sendMessage(command).queue();
        log.warn("Sending command: {}", command);

        superBotP.openPrivateChannel()
                .complete()
                .sendMessage(new MessageBuilder(command + " ").append(h13m).build())
                .queue();
    }
}
