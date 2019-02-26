package pogo.assistance.data.extraction.source.discord;

import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

@UtilityClass
public class MessageStream {

    /**
     * @return
     *      An unbounded stream of messages from the {@code messageChannel}'s history. This iterates chronologically
     *      backwards (from present to past). There's no guarantee that this traverses the history lazily.
     * @see net.dv8tion.jda.core.entities.MessageChannel#getIterableHistory()
     */
    public static Stream<Message> lookbackMessageStream(final MessageChannel messageChannel) {
        return messageChannel.getIterableHistory().stream();
    }

}
