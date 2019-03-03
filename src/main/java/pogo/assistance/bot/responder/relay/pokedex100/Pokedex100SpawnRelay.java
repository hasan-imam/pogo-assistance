package pogo.assistance.bot.responder.relay.pokedex100;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.JDA;
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

    private final Provider<JDA> relayingUserJda;

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final User superBotP = relayingUserJda.get().getUserById(DiscordEntityConstants.USER_ID_PDEX100_SUPER_BOT_P);
    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final User h13m = relayingUserJda.get().getUserById(DiscordEntityConstants.USER_ID_H13M);
    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final User qn234 = relayingUserJda.get().getUserById(DiscordEntityConstants.USER_ID_QN234);

    /**
     * @param relayingUserJda
     *      JDA with user {@link JDA#getSelfUser() user} who will relay the spawn info. User needs to have necessary
     *      permissions, e.g. having a 'verified', 'candy tracker' etc. roles when relaying to Pokedex100.
     */
    @Inject
    public Pokedex100SpawnRelay(@Named(DiscordEntityConstants.NAME_JDA_OWNING_USER) final Provider<JDA> relayingUserJda) {
        this.relayingUserJda = relayingUserJda;
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
        getSuperBotP().openPrivateChannel()
                .complete()
                .sendMessage(new MessageBuilder(command + " ").append(getH13m()).build())
                .queue();
    }
}
