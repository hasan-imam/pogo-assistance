package pogo.assistance.bot.responder.relay.pokedex100;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import pogo.assistance.bot.di.DiscordEntityConstants;

@Slf4j
public class PokexToPokedex100Tunnel extends ListenerAdapter {

    private static final long SERVER_ID_RELAYED_SERVER = 562454496022757377L;
    public static final long CHANNEL_ID_RELAYED_CHANNEL = 562454673454268427L;

    private final JDA relayingUserJda;

    @Inject
    public PokexToPokedex100Tunnel(@Named(DiscordEntityConstants.NAME_JDA_M15M_BOT) final JDA relayingUserJda) {
        this.relayingUserJda = relayingUserJda;
    }

    @Override
    public void onReady(final ReadyEvent event) {
        log.info("Pokex-to-Pokedex100 tunnel is online!");
    }

    @Override
    public void onPrivateMessageReceived(final PrivateMessageReceivedEvent event) {
        if (isPokexSpawnNotificationDm(event.getMessage())) {
            handlePokexSpawnNotificationDm(event.getMessage());
        }
    }

    private static boolean isPokexSpawnNotificationDm(final Message message) {
        if (message.getChannelType() != ChannelType.PRIVATE
                || message.getAuthor().getIdLong() != DiscordEntityConstants.USER_ID_POKEX_DM_BOT
                || message.getEmbeds().isEmpty()) {
            return false;
        }

        final String messageTitle = message.getContentDisplay().toUpperCase();
        return messageTitle.contains("CP") && messageTitle.contains("LVL");
    }

    private void handlePokexSpawnNotificationDm(final Message message) {
        // Sample message: (at this point - they keep changing format)
        // ":408a: **Cranidos** :flag_tw: **New Taipei City**  :100IV:  :CP: **936** :LVL: **18** ***Take Down/Ancient Power*** :Female:"
        final String displayedMessage = message.getContentDisplay().trim();
        final String messageToRelay = displayedMessage.split("\n")[0]
                .replaceFirst("^:(\\w+):[\\s]*", "") // Remove pokemon emote from the beginning
                .replaceFirst(":CP:", "CP")
                .replaceFirst(":100IV:", "100 iv")
                .replaceFirst(":LVL:", "level")
                .replaceFirst(":Male:", "♂")
                .replaceFirst(":Female:", "♀");
        relayingUserJda.getGuildById(SERVER_ID_RELAYED_SERVER).getTextChannelById(CHANNEL_ID_RELAYED_CHANNEL)
                .sendMessage(messageToRelay)
                .complete();
    }

}
