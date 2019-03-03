package pogo.assistance.data.extraction.source.discord;

import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
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

    private final Set<MessageProcessor<PokemonSpawn>> guildMessageProcessors;
    private final Set<MessageProcessor<PokemonSpawn>> privateMessageProcessors;
    private final PokemonSpawnExchange spawnExchange;

    @Inject
    public DiscordPokemonSpawnListener(
            final Set<MessageProcessor<PokemonSpawn>> guildMessageProcessors,
            final Set<MessageProcessor<PokemonSpawn>> privateMessageProcessors,
            final PokemonSpawnExchange spawnExchange) {
        this.guildMessageProcessors = guildMessageProcessors;
        this.privateMessageProcessors = privateMessageProcessors;
        this.spawnExchange = spawnExchange;
    }

    @Override
    public void onReady(final ReadyEvent event) {
        // TODO: Add some validation?
        log.info("Listening to pokemon spawns posted in discord channels...");
    }

    @Override
    public void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
        process(guildMessageProcessors, event.getMessage());
    }

    @Override
    public void onPrivateMessageReceived(final PrivateMessageReceivedEvent event) {
        process(privateMessageProcessors, event.getMessage());
    }

    private void process(final Set<MessageProcessor<PokemonSpawn>> messageProcessors, final Message message) {
        messageProcessors.stream()
                .filter(processor -> processor.canProcess(message))
                .map(processor -> processor.process(message))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny()
                .ifPresent(spawnExchange::offer);
    }

}
