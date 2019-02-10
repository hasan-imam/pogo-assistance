package pogo.assistance.bot.manager;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.AccountType;
import picocli.CommandLine;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.bot.responder.DaggerResponderBotComponent;
import pogo.assistance.bot.responder.ResponderBot;

@Slf4j
public class BotStarter {

    public static void main(final String[] args) {
        final BotStarterInput input = new BotStarterInput();
        final CommandLine commandLine = new CommandLine(input);
        commandLine.parse(args);
        if (input.isBatchQuestPublish()) {
            log.info("Doing batch publish...");
        }
        if (input.isInteractiveQuestPublish()) {
            log.info("Handling interactive commands...");
        }
        if (input.isFeed()) {
            log.info("Starting off the feed bot...");
        }
        if (input.isResponder()) {
            log.info("Starting off the responder bot...");
            final ResponderBot responderBot = DaggerResponderBotComponent.builder()
                    .accountType(AccountType.CLIENT)
                    .userToken(DiscordEntityConstants.OWNING_USER_TOKEN)
                    .build()
                    .getResponderBot();
            responderBot.run();
        }

        log.info("Done setting up bot(s)!");
    }

}
