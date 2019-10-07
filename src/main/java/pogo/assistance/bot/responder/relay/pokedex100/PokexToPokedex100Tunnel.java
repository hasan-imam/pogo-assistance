package pogo.assistance.bot.responder.relay.pokedex100;

import java.util.Deque;
import java.util.LinkedList;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import pogo.assistance.bot.di.DiscordEntityConstants;

/**
 * @implNote
 *      Many inefficiencies in this class...
 *       - History lookup takes O(history size) time
 *       - Not all string replacements may always be necessary
 */
@Slf4j
public class PokexToPokedex100Tunnel extends ListenerAdapter {

    private static final long SERVER_ID_RELAYED_SERVER = 562454496022757377L;
    public static final long CHANNEL_ID_RELAYED_CHANNEL = 562454673454268427L;

    private static final int MESSAGE_HISTORY_QUEUE_SIZE_LIMIT = 30;

    private final JDA relayingUserJda;

    // To look up past messages and de-duplicate
    // Using list which would have poor lookup performance, but should be okay since we intend to keep it short
    private final Deque<String> pastMessages;

    @Inject
    public PokexToPokedex100Tunnel(@Named(DiscordEntityConstants.NAME_JDA_M15M_BOT) final JDA relayingUserJda) {
        this.relayingUserJda = relayingUserJda;
        this.pastMessages = new LinkedList<>();
    }

    @Override
    public void onReady(final ReadyEvent event) {
        log.info("Pokex-to-Pokedex100 tunnel is online!");
    }

    @Override
    public void onPrivateMessageReceived(final PrivateMessageReceivedEvent event) {
        if (isPokexSpawnNotificationDm(event.getMessage()) || isPokedex100SpawnNotificationDm(event.getMessage())) {
            handlePokexSpawnNotificationDm(event.getMessage());
        }
    }

    @VisibleForTesting
    static boolean isPokexSpawnNotificationDm(final Message message) {
        if (message.getChannelType() != ChannelType.PRIVATE
                || DiscordEntityConstants.USER_ID_POKEX_DM_BOTS.contains(message.getAuthor().getIdLong())
                || message.getEmbeds().isEmpty()) {
            return false;
        }

        final String messageTitle = message.getContentDisplay().toUpperCase();
        return messageTitle.contains("CP") && messageTitle.contains("LVL");
    }

    @VisibleForTesting
    static boolean isPokedex100SpawnNotificationDm(final Message message) {
        return message.getChannelType() == ChannelType.PRIVATE
                && message.getAuthor().getIdLong() == DiscordEntityConstants.USER_ID_PDEX100_SUPER_SHIPPER_9
                && message.getContentRaw().contains("Click to get coord");
    }

    /**
     * @implNote
     *      Made synchronized to prevent concurrent modification of {@link #pastMessages}. This is the only method that should be
     *      modifying and accessing the queue.
     */
    private synchronized void handlePokexSpawnNotificationDm(final Message message) {
        // Sample message: (at this point - they keep changing format)
        // ":408a: **Cranidos** :flag_tw: **New Taipei City**  :100IV:  :CP: **936** :LVL: **18** ***Take Down/Ancient Power*** :Female:"
        final String displayedMessage = message.getContentDisplay().trim();
        final String messageToRelay = displayedMessage.split("\n")[0]
                .replaceFirst("^:(\\w+):[\\s]*", "") // Remove pokemon emote from the beginning
                .replaceFirst("[\\s]?\\*\\*\\*[\\w\\s]+/[\\w\\s]+\\*\\*\\*", "") // Remove move sets
                .replaceFirst("[_\\s]*[\\d\\.]+kg[_]*", "") // Remove weight and formatting stuff around it
                .replaceFirst("[_\\s]*[\\d\\.]+m[_]*", "") // Remove height and formatting stuff around it
                .replaceAll("[\\s]*:BestATK:|[\\s]*:BestDEF:", "") // Remove move set related icons
                .replaceFirst(":CP:", "CP")
                .replaceFirst(":100IV:", "100 IV")
                .replaceFirst(":LVL:", "Level")
                .replaceFirst(":Male:", "♂")
                .replaceFirst(":Female:", "♀")
                .replaceFirst(":Neutral:", "⚲");

        // Check if this was already posted
        final boolean isDuplicate = isDuplicate(messageToRelay);
        if (!isDuplicate) {
            pastMessages.add(messageToRelay);
            sendMessage(messageToRelay);
            log.info("Pokex tunnel sent message: {}", messageToRelay);
        } else {
            log.info("Pokex tunnel filtering duplicate message: {}", messageToRelay);
        }

        // Clear remove element from the queue if it's past the size limit
        if (pastMessages.size() > MESSAGE_HISTORY_QUEUE_SIZE_LIMIT) {
            pastMessages.poll();
        }
    }

    @VisibleForTesting
    boolean isDuplicate(final String messageToRelay) {
        return pastMessages.stream().anyMatch(pastMessage -> {
            // [Pokex specific logic] Containment check used because earlier message is likely to be from donor feed,
            // which usually has more information such as height/weight etc. Latter repeated messages are copies from
            // the free channel where such premium info isn't always included, so likely to result in a message
            // contained in a past message.
            // Example msg: **Cranidos** :flag_de: __Bad Vilbel__ **HE**  100 IV  CP **1404** Level **27**  ♂
            return pastMessage.contains(messageToRelay);
        });
    }

    @VisibleForTesting
    void sendMessage(final String messageToRelay) {
        relayingUserJda.getGuildById(SERVER_ID_RELAYED_SERVER).getTextChannelById(CHANNEL_ID_RELAYED_CHANNEL)
                .sendMessage(messageToRelay)
                .complete();
    }
}
