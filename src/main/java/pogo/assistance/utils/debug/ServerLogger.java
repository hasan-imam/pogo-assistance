package pogo.assistance.utils.debug;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import pogo.assistance.bot.di.DiscordEntityConstants;

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
}
