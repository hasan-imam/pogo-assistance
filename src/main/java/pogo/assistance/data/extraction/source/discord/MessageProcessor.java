package pogo.assistance.data.extraction.source.discord;

import java.util.Optional;
import javax.annotation.Nonnull;
import net.dv8tion.jda.core.entities.Message;
import org.slf4j.LoggerFactory;

/**
 * Generic processor of {@link Message discord message}. Transforms an input message to some object {@link T}, usually
 * by parsing to capture some data on the message on to a POJO {@link T}.
 *
 * The processor can expect to get irrelevant messages to be passed to it. It returns empty for such irrelevant
 * messages.
 */
public interface MessageProcessor<T> {

    /**
     * @return
     *      True if the {@code message} can be processed by this processor. Callers can optionally use this to check
     *      if the message is relevant to a processor before passing for actual processing.
     */
    boolean canProcess(@Nonnull final Message message);

    /**
     * @return
     *      {@link Optional} containing {@link T} produced from processing the input message. Or
     *      {@link Optional#empty()} if the message is not relevant to this processor.
     */
    Optional<T> process(@Nonnull final Message message);

    default Optional<T> processWithoutThrowing(@Nonnull final Message message) {
        try {
            return process(message);
        } catch (final Exception e) {
            LoggerFactory.getLogger(MessageProcessor.class).error(
                    String.format("Failed to process message (ID: %s, URL: %s): %s",
                            message.getId(), message.getJumpUrl(), message.getContentRaw()),
                    e);
            return Optional.empty();
        }
    }

}
