package pogo.assistance.bot.responder.relay.pokedex100;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.google.common.util.concurrent.RateLimiter;
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
    private final User m15mv1 = relayingUserJda.get().getUserById(DiscordEntityConstants.USER_ID_M15MV1);

    private final RateLimiter rateLimiter = RateLimiter.create(1);

    /**
     * @param relayingUserJda
     *      JDA with user {@link JDA#getSelfUser() user} who will relay the spawn info. User needs to have necessary
     *      permissions, e.g. having a 'verified', 'candy tracker' etc. roles when relaying to Pokedex100.
     */
    @Inject
    public Pokedex100SpawnRelay(@Named(DiscordEntityConstants.NAME_JDA_M15MV1_USER) final Provider<JDA> relayingUserJda) {
        this.relayingUserJda = relayingUserJda;
    }

    @Override
    public void observe(final PokemonSpawn pokemonSpawn) {
        try {
            final String command;
            if (pokemonSpawn.getIv().orElse(-1.0) == 100.0) {
                command = VerifierBotUtils.toPerfectIvSpawnCommand(pokemonSpawn, false);
                log.info("Sending 100 IV command: {}", command);
            } else if (pokemonSpawn.getIv().orElse(-1.0) >= 90.0) {
                command = VerifierBotUtils.toImperfectIvSpawnCommand(pokemonSpawn, false);
                log.info("Sending 90+ IV command: {}", command);
            } else if (CandySelector.isCandy(pokemonSpawn.getPokedexEntry()) && pokemonSpawn.getIv().isPresent()) {
                // Check presence of IV on the candies. This is to limit the number of posts since most of the spawns
                // don't have IV info on them (especially the spawns coming from pokemaps).
                // TODO: Can we do this in a better way?
                command = VerifierBotUtils.toImperfectIvSpawnCommand(pokemonSpawn, false);
                log.info("Sending candy command: {}", command);
            } else if (pokemonSpawn.getCp().orElse(0) >= 2690) {
                // TODO: have made the cp threshold here high for now since all high cp posts mentions poster
                // should lower it back to 2k once that's fixed
                command = VerifierBotUtils.toImperfectIvSpawnCommand(pokemonSpawn, false);
                log.info("Sending high CP command: {}", command);
            } else {
                log.trace("Ignoring spawn that didn't match posting criteria: " + pokemonSpawn);
                return;
            }

            rateLimiter.acquire();
            sendCommandToSuperBotP(command);
        } catch (final RuntimeException e) {
            log.error("Error observing spawn: " + pokemonSpawn, e);
        }
    }

    private void sendCommandToSuperBotP(final String command) {
        getSuperBotP().openPrivateChannel()
                .complete()
                .sendMessage(new MessageBuilder(command).build())
                .queueAfter(ThreadLocalRandom.current().nextInt(30), TimeUnit.SECONDS);
    }
}
