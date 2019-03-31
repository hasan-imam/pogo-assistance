package pogo.assistance.data.extraction.source.discord;

import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import pogo.assistance.data.exchange.spawn.PokemonSpawnExchange;
import pogo.assistance.data.model.pokemon.PokemonSpawn;
import pogo.assistance.utils.debug.ServerLogger;

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
    private final ServerLogger logger;

    @Inject
    public DiscordPokemonSpawnListener(
            final Set<MessageProcessor<PokemonSpawn>> guildMessageProcessors,
            final Set<MessageProcessor<PokemonSpawn>> privateMessageProcessors,
            final PokemonSpawnExchange spawnExchange,
            final ServerLogger serverLogger) {
        this.guildMessageProcessors = guildMessageProcessors;
        this.privateMessageProcessors = privateMessageProcessors;
        this.spawnExchange = spawnExchange;
        this.logger = serverLogger;
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
        messageProcessors.stream().filter(processor -> processor.canProcess(message)).findAny().ifPresent(processor -> {
            try {
                processor.process(message).ifPresent(spawnExchange::offer);
            } catch (final Exception e) {
                // Log failed message and the error stack trace
                final String stackTrace = Throwables.getStackTraceAsString(e);
                logger.sendDebugMessage(message);
                new MessageBuilder()
                        .appendCodeBlock(stackTrace, "bash")
                        .buildAll(MessageBuilder.SplitPolicy.NEWLINE)
                        .forEach(logger::sendDebugMessage);
            }
        });
    }

}
