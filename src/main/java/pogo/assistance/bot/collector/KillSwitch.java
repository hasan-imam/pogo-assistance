package pogo.assistance.bot.collector;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import pogo.assistance.bot.di.DiscordEntityConstants;

/**
 * A quick and dirty kill switch for the relay. If current execution is running multiple bots, it will kill all of them,
 * not just the relay.
 */
@Slf4j
class KillSwitch extends ListenerAdapter {

    private static final Set<Long> ADMIN_USER_IDS = ImmutableSet.of(
            // Owning user
            DiscordEntityConstants.USER_ID_H13M,
            DiscordEntityConstants.USER_ID_M15MV1,
            // Pokedex100 mods
            DiscordEntityConstants.USER_ID_KYRION,
            DiscordEntityConstants.USER_ID_JOSH,
            DiscordEntityConstants.USER_ID_GHOST,
            DiscordEntityConstants.USER_ID_WOPZ,
            DiscordEntityConstants.USER_ID_HERO);

    @Override
    public void onPrivateMessageReceived(final PrivateMessageReceivedEvent event) {
        if (ADMIN_USER_IDS.contains(event.getAuthor().getIdLong())) {
            if (event.getMessage().getContentStripped().equalsIgnoreCase("!kill relay")) {
                event.getChannel()
                        .sendMessage(new MessageBuilder("Executing kill switch as requested by ")
                                .append(event.getAuthor())
                                .build())
                        .complete();
                log.warn("User {} requested kill for the relay. Exiting application...", event.getAuthor().getName());
                System.exit(1);
            }
        }
    }

}
