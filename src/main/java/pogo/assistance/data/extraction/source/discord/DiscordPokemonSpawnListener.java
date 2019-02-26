package pogo.assistance.data.extraction.source.discord;

import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import pogo.assistance.data.exchange.spawn.PokemonSpawnExchange;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

/**
 * Handles pokemon spawn notification events from various Discord channels.
 *
 * Processes notification message using registered channel specific processor and submits the spawn information to
 * {@link #spawnExchange exchange}, which can pass the notification to interested parties.
 *
 * This needs to be {@link net.dv8tion.jda.core.JDA#addEventListener(Object...) registered to JDA} for it to start
 * receiving messages from Discord channels.
 */
@Slf4j
public class DiscordPokemonSpawnListener extends ListenerAdapter {

    private final Set<MessageProcessor<PokemonSpawn>> messageProcessors;
    private final PokemonSpawnExchange spawnExchange;

    @Inject
    public DiscordPokemonSpawnListener(
            final Set<MessageProcessor<PokemonSpawn>> messageProcessors,
            final PokemonSpawnExchange spawnExchange) {
        this.messageProcessors = messageProcessors;
        this.spawnExchange = spawnExchange;
    }

    @Override
    public void onReady(final ReadyEvent event) {
        // TODO: Add some validation?
        log.info("Listening to pokemon spawns posted in discord channels...");
    }

    @Override
    public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
        messageProcessors.stream()
                .filter(processor -> processor.canProcess(event.getMessage()))
                .map(processor -> processor.process(event.getMessage()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny()
                .ifPresent(spawnExchange::offer);
    }

}
