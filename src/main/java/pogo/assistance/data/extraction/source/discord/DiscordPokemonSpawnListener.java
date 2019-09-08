package pogo.assistance.data.extraction.source.discord;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.data.exchange.spawn.PokemonSpawnExchange;
import pogo.assistance.data.model.pokemon.PokemonSpawn;
import pogo.assistance.utils.debug.ServerLogger;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles pokemon spawn notification events from various Discord channels.
 *
 * Processes notification message using registered channel specific processor and submits the spawn information to
 * {@link #spawnExchange exchange}, which can pass the notification to interested parties.
 *
 * This needs to be {@link net.dv8tion.jda.api.JDA#addEventListener(Object...) registered to JDA} for it to start
 * receiving messages from Discord channels.
 */
@Slf4j
public class DiscordPokemonSpawnListener extends ListenerAdapter {

    private final Set<MessageProcessor<PokemonSpawn>> guildMessageProcessors;
    private final Set<MessageProcessor<PokemonSpawn>> privateMessageProcessors;
    private final PokemonSpawnExchange spawnExchange;
    private final ServerLogger logger;
    private final Gson gson;

    @Inject
    public DiscordPokemonSpawnListener(
            final Set<MessageProcessor<PokemonSpawn>> guildMessageProcessors,
            final Set<MessageProcessor<PokemonSpawn>> privateMessageProcessors,
            final PokemonSpawnExchange spawnExchange,
            final ServerLogger serverLogger,
            final Gson gson) {
        this.guildMessageProcessors = guildMessageProcessors;
        this.privateMessageProcessors = privateMessageProcessors;
        this.spawnExchange = spawnExchange;
        this.logger = serverLogger;
        this.gson = gson;
    }

    @Override
    public void onReady(final ReadyEvent event) {
        // TODO: Add some validation?
        final JDA jda = event.getJDA();
        final String accessibleSpawnSources = jda.getTextChannels().stream()
                .filter(textChannel -> DiscordEntityConstants.SPAWN_SOURCE_SERVER_IDS.contains(textChannel.getIdLong()))
                .map(TextChannel::getName)
                .collect(Collectors.joining(", "));
        log.info("'{}' listening to pokemon spawns posted in discord channels: {}",
                jda.getSelfUser().getName(), accessibleSpawnSources);
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
                sendErrorMessages(message, e);
            }
        });
    }

    private void sendErrorMessages(final Message messageThatFailedProcessing, final Exception exception) {
        final String messageSource;
        if (messageThatFailedProcessing.isFromGuild()) {
            messageSource = String.format("%s → %s → %s%n",
                    Optional.of(messageThatFailedProcessing.getGuild()).map(Guild::toString).orElse("Unknown guild"),
                    Optional.ofNullable(messageThatFailedProcessing.getCategory()).map(Category::toString).orElse("Unknown category"),
                    Optional.of(messageThatFailedProcessing.getChannel()).map(MessageChannel::toString).orElse("Unknown channel"));
        } else {
            messageSource = messageThatFailedProcessing.getChannelType().name();
        }
        final MessageBuilder errorSummaryMsgBuilder = new MessageBuilder();
        errorSummaryMsgBuilder.append("Source: ", MessageBuilder.Formatting.BOLD)
                .append(messageSource, MessageBuilder.Formatting.BLOCK)
                .append(System.lineSeparator());
        errorSummaryMsgBuilder.append("Sender: ", MessageBuilder.Formatting.BOLD)
                .append(messageThatFailedProcessing.getAuthor().toString())
                .append(System.lineSeparator());
        Optional.of(messageThatFailedProcessing.getJDA().getSelfUser())
                .map(selfUser -> String.format("%s (%s)", selfUser.getName(), selfUser.getEmail()))
                .ifPresent(receiver -> errorSummaryMsgBuilder.append("Receiver: ", MessageBuilder.Formatting.BOLD)
                        .append(receiver)
                        .append(System.lineSeparator()));
        errorSummaryMsgBuilder.append("Jump URL: ", MessageBuilder.Formatting.BOLD)
                // TODO: Update after this is resolved: https://github.com/DV8FromTheWorld/JDA/issues/1091
                .append(messageThatFailedProcessing.isFromGuild() ? messageThatFailedProcessing.getJumpUrl() : null)
                .append(System.lineSeparator());

        final MessageBuilder exceptionInfoMsgBuilder = new MessageBuilder()
                .appendCodeBlock(joinExceptionCauses(exception, new StringBuilder()), "bash");

        final MessageBuilder failedMessageAsJson = new MessageBuilder(messageThatFailedProcessing.getContentRaw());
        messageThatFailedProcessing.getEmbeds().forEach(messageEmbed ->
                failedMessageAsJson.appendCodeBlock(gson.toJson(messageEmbed.toData().toMap()), "json"));

        final Queue<Message> messagesToSend = new LinkedList<>();
        messagesToSend.addAll(errorSummaryMsgBuilder.buildAll(MessageBuilder.SplitPolicy.NEWLINE));
        messagesToSend.addAll(exceptionInfoMsgBuilder.buildAll(MessageBuilder.SplitPolicy.NEWLINE));
        messagesToSend.addAll(failedMessageAsJson.buildAll(MessageBuilder.SplitPolicy.NEWLINE));
        logger.sendDebugMessages(messagesToSend);
    }

    private static StringBuilder joinExceptionCauses(final Throwable throwable, final StringBuilder stringBuilder) {
        stringBuilder.append(String.format("%s: %s%n", throwable.getClass().getName(), throwable.getLocalizedMessage()));
        Optional.ofNullable(throwable.getCause()).ifPresent(t -> joinExceptionCauses(t, stringBuilder));
        return stringBuilder;
    }

}
