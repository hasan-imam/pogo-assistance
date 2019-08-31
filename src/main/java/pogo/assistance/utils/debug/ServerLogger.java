package pogo.assistance.utils.debug;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import pogo.assistance.bot.di.DiscordEntityConstants;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Queue;

@Singleton
public class ServerLogger {

    private final JDA loggingUserJda;

    @Inject
    public ServerLogger(@Named(DiscordEntityConstants.NAME_JDA_M15M_BOT) final JDA loggingUserJda) {
        this.loggingUserJda = loggingUserJda;
    }

    public void sendDebugMessage(final String message) {
        sendDebugMessage(new MessageBuilder(message).build());
    }

    public void sendDebugMessage(final Message message) {
        loggingUserJda.getTextChannelById(DiscordEntityConstants.CHANNEL_ID_DD_BOT_TESTING).sendMessage(message).queue();
    }

    public void sendDebugMessages(final Queue<Message> messages) {
        final TextChannel channel = loggingUserJda.getTextChannelById(DiscordEntityConstants.CHANNEL_ID_DD_BOT_TESTING);
        for (final Message message : messages) {
            channel.sendMessage(message).complete();
        }
    }
}
