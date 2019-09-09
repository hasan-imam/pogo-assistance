package pogo.assistance.bot.responder.relay.pokedex100;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.RateLimiter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.data.exchange.spawn.PokemonSpawnObserver;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

/**
 * Observers pokemon spawning events and relays new spawns to PokeDex100 server.
 */
@Slf4j
@Singleton
public class Pokedex100SpawnRelay implements PokemonSpawnObserver {

    // Pokemons on this list are too common, so criteria for posting them is more restrictive
    private static final Set<Integer> CRAP_POKEMON_IDS = ImmutableSet.<Integer>builder()
            .add(13).add(21).add(48)
            .add(163).add(167)
            .add(218).add(273)/*.add(293)*/
            .add(316).add(322).add(331)/*.add(339)*/.add(399)
            .add(401)
            .build();

    private final Provider<JDA> relayingUserJda;

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final TextChannel commandRelayChannel = relayingUserJda.get().getTextChannelById(DiscordEntityConstants.CHANNEL_ID_PDEX100_PLAYGROUND);
    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final TextChannel serverLogChannel = relayingUserJda.get().getTextChannelById(DiscordEntityConstants.CHANNEL_ID_DD_BOT_TESTING);

    private final RateLimiter rateLimiter = RateLimiter.create(1);

    /**
     * @param relayingUserJda
     *      JDA with user {@link JDA#getSelfUser() user} who will relay the spawn info. User needs to have necessary
     *      permissions, for relaying to Pokedex100-Playground.
     */
    @Inject
    public Pokedex100SpawnRelay(@Named(DiscordEntityConstants.NAME_JDA_M15M_BOT) final Provider<JDA> relayingUserJda) {
        this.relayingUserJda = relayingUserJda;
    }

    @Override
    public void observe(final PokemonSpawn pokemonSpawn) {
        if (shouldBeExcluded(pokemonSpawn)) {
            return;
        }

        // Need to send certain spawns to donor channels to reduce visibility
        final String sourceName = pokemonSpawn.getSourceMetadata().sourceName().toUpperCase();
        final boolean isForDonors = sourceName.contains("SDHVIP")
                || sourceName.contains("SANDIEGOHILLS")
                || sourceName.contains("LVRaidMap");

        try {
            final String command;

            if (pokemonSpawn.getIv().orElse(-1.0) == 100.0) {
                command = VerifierBotUtils.toPerfectIvSpawnCommand(pokemonSpawn, false, isForDonors);
                log.info("Sending 100 IV command from {}: {}", pokemonSpawn.getSourceMetadata().sourceName(), command);
            } else if (pokemonSpawn.getIv().orElse(-1.0) >= 90.0) {
                command = VerifierBotUtils.toImperfectIvSpawnCommand(pokemonSpawn, false, isForDonors);
                log.info("Sending 90+ IV command from {}: {}", pokemonSpawn.getSourceMetadata().sourceName(), command);
            } else if (pokemonSpawn.getIv().orElse(-1.0) == 0.0) {
                command = VerifierBotUtils.toImperfectIvSpawnCommand(pokemonSpawn, false, isForDonors);
                log.info("Sending 0 IV command from {}: {}", pokemonSpawn.getSourceMetadata().sourceName(), command);
            } else if (CandySelector.isCandy(pokemonSpawn.getPokedexEntry()) && pokemonSpawn.getIv().isPresent()) {
                // Check presence of IV on the candies. This is to limit the number of posts since most of the spawns
                // don't have IV info on them (especially the spawns coming from pokemaps).
                // TODO: Can we do this in a better way?
                command = VerifierBotUtils.toImperfectIvSpawnCommand(pokemonSpawn, false, isForDonors);
                log.info("Sending candy command from {}: {}", pokemonSpawn.getSourceMetadata().sourceName(), command);
            } else if (pokemonSpawn.getCp().orElse(0) >= 2000) {
                command = VerifierBotUtils.toImperfectIvSpawnCommand(pokemonSpawn, false, isForDonors);
                log.info("Sending high CP command from {}: {}", pokemonSpawn.getSourceMetadata().sourceName(), command);
            } else {
                log.trace("Ignoring spawn that didn't match posting criteria: " + pokemonSpawn);
                return;
            }

            rateLimiter.acquire();

            sendCommandToSuperBotP(command);
        } catch (final RuntimeException e) {
            log.error("Error relaying spawn: " + pokemonSpawn, e);
        }
    }

    private void sendCommandToSuperBotP(final String command) {
        try {
            getCommandRelayChannel()
                    .sendMessage(new MessageBuilder(command).build())
                    .queueAfter(ThreadLocalRandom.current().nextInt(30), TimeUnit.SECONDS);
        } catch (final InsufficientPermissionException e) {
            // Sometimes the bot's access to posting spawns is restricting in lieu of killing the bot with all of its
            // functionalities. We don't want to log exceptions for such cases since it fills up disk with long logs.
            // Silencing those exceptions here but we should have validation checks at start time to ensure it has
            // access to posting things at.
        }
    }

    private static boolean shouldBeExcluded(final PokemonSpawn pokemonSpawn) {
        // If any of the exclusion rules apply, return true
        if (CRAP_POKEMON_IDS.contains(pokemonSpawn.getPokedexEntry().getId())) {
            return pokemonSpawn.getIv().orElse(-1.0) < 100.0 || pokemonSpawn.getLevel().orElse(-1) < 30;
        }

        if (pokemonSpawn.getPokedexEntry().getId() == 147 || pokemonSpawn.getPokedexEntry().getId() == 366) {
            // Reduce dratini and clamperl since they are spawning more due to an event
            return pokemonSpawn.getIv().orElse(-1.0) < 85.0;
        }

        return false;
    }

}
