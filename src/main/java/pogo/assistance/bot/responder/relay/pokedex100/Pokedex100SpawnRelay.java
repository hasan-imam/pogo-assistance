package pogo.assistance.bot.responder.relay.pokedex100;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
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
        final String command;
        if (pokemonSpawn.getIv().orElse(-1.0) == 100.0) {
            command = VerifierBotUtils.toPerfectIvSpawnCommand(pokemonSpawn);
            log.info("Sending 100 IV command: {}", command);
        } else if (pokemonSpawn.getIv().orElse(-1.0) >= 90.0) {
            command = VerifierBotUtils.toImperfectIvSpawnCommand(pokemonSpawn);
            log.info("Sending 90+ IV command: {}", command);
        } else if (CandySelector.isCandy(pokemonSpawn.getPokedexEntry())) {
            command = VerifierBotUtils.toImperfectIvSpawnCommand(pokemonSpawn);
            log.info("Sending candy command: {}", command);
        } else if (pokemonSpawn.getCp().orElse(0) >= 2000) {
            command = VerifierBotUtils.toImperfectIvSpawnCommand(pokemonSpawn);
            log.info("Sending high CP command: {}", command);
        } else {
            log.debug("Ignoring spawn that didn't match posting criteria: " + pokemonSpawn);
            return;
        }

        sendCommandToSuperBotP(command);
    }

    private void sendCommandToSuperBotP(final String command) {
        getSuperBotP().openPrivateChannel()
                .complete()
                .sendMessage(new MessageBuilder(command).build())
                .queue();
    }
}
