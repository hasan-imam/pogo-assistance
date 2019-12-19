package pogo.assistance.bot.manager;

import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.slf4j.bridge.SLF4JBridgeHandler;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.AccountType;
import picocli.CommandLine;
import pogo.assistance.bot.collector.DaggerSpawnDataCollectorBotComponent;
import pogo.assistance.bot.collector.SpawnDataCollectorBot;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.bot.job.DaggerJobExecutionBotComponent;
import pogo.assistance.bot.job.JobExecutionBot;
import pogo.assistance.bot.job.WorkflowId;
import pogo.assistance.bot.responder.DaggerResponderBotComponent;
import pogo.assistance.bot.responder.ListenerId;
import pogo.assistance.bot.responder.ResponderBot;

@Slf4j
public class BotStarter {

    private final static AccountType ACCOUNT_TYPE = AccountType.CLIENT;
    private final static String USER_TOKEN = DiscordEntityConstants.OWNING_USER_TOKEN;

    public static void main(final String[] args) {
        setupJULToSlf4jBridge();

        final BotStarterInput input = new BotStarterInput();
        final CommandLine commandLine = new CommandLine(input);
        commandLine.parse(args);
        if (input.isJob()) {
            startJobs(input.getWorkflowIds());
        } else if (input.isBatchQuestPublish()) {
            log.info("Doing batch publish...");
        } else if (input.isInteractiveQuestPublish()) {
            log.info("Handling interactive commands...");
        }

        // Following can run in parallel to other things

        if (input.isFeed()) {
            runPokemonSpawnExchange();
        }

        if (input.isResponder()) {
            startResponder(input.getListenerIds());
        }

        log.info("Done setting up bot!");
    }

    private static SpawnDataCollectorBot runPokemonSpawnExchange() {
        final SpawnDataCollectorBot bot = DaggerSpawnDataCollectorBotComponent.builder()
                .corruptedUserToken(DiscordEntityConstants.CORRUPTED_USER_TOKEN)
                .beninUserToken(DiscordEntityConstants.BENIN_USER_TOKEN)
                .ninersUserToken(DiscordEntityConstants.NINERS_USER_TOKEN)
                .johnnyUserToken(DiscordEntityConstants.JOHNNY_USER_TOKEN)
                .timburtyUserToken(DiscordEntityConstants.TIMBURTY_USER_TOKEN)
                .irvin88UserToken(DiscordEntityConstants.IRVIN88_USER_TOKEN)
                .connoisseurUserToken(DiscordEntityConstants.CONNOISSEUR_USER_TOKEN)
                .chronicUserToken(DiscordEntityConstants.CHRONIC_USER_TOKEN)
                .crankUserToken(DiscordEntityConstants.CRANK_USER_TOKEN)
                .poGoHeroUserToken(DiscordEntityConstants.POGO_HERO_USER_TOKEN)
                .michellexUserToken(DiscordEntityConstants.MICHELLEX_USER_TOKEN)
                .pokePeterUserToken(DiscordEntityConstants.POKE_PETER_USER_TOKEN)
                .amyUserToken(DiscordEntityConstants.AMY_USER_TOKEN)
                .alexaUserToken(DiscordEntityConstants.ALEXA_USER_TOKEN)
                .shadowUserToken(DiscordEntityConstants.SHADOW_USER_TOKEN)
                .controlUserToken(DiscordEntityConstants.M15M_BOT_TOKEN)
                .build()
                .getSpawnDataCollectorBot();
        bot.startAsync().awaitRunning();
        return bot;
    }

    private static ResponderBot startResponder(final Set<ListenerId> inputListenerIds) {
        final Set<ListenerId> listenerIds;
        if (inputListenerIds == null || inputListenerIds.isEmpty()) {
            log.info("No lister explicitly mentioned - registering all of them!");
            listenerIds = EnumSet.allOf(ListenerId.class);
        } else {
            listenerIds = inputListenerIds;
        }
        log.info("Starting off the responder bot with these listeners: {}", listenerIds);

        final ResponderBot responderBot = DaggerResponderBotComponent.builder()
                .accountType(ACCOUNT_TYPE)
                .owningUserToken(DiscordEntityConstants.OWNING_USER_TOKEN)
                .horuseusUserToken(DiscordEntityConstants.HORUSEUS_USER_TOKEN)
                .copernicusUserToken(DiscordEntityConstants.COPERNICUS_USER_TOKEN)
                .controlUserToken(DiscordEntityConstants.M15M_BOT_TOKEN)
                .listenerIds(listenerIds)
                .build()
                .getResponderBot();
        responderBot.startAsync();
        return responderBot;
    }

    private static JobExecutionBot startJobs(final Set<WorkflowId> inputWorkflowIds) {
        final Set<WorkflowId> workflowIds;
        if (inputWorkflowIds == null || inputWorkflowIds.isEmpty()) {
            log.info("No workflow explicitly mentioned - registering all of them!");
            workflowIds = EnumSet.allOf(WorkflowId.class);
        } else {
            workflowIds = inputWorkflowIds;
        }
        log.info("Starting off the execution bot with these jobs: {}", workflowIds);

        final JobExecutionBot jobExecutionBot = DaggerJobExecutionBotComponent.builder()
                .accountType(ACCOUNT_TYPE)
                .userToken(USER_TOKEN)
                .workflowIds(workflowIds)
                .build()
                .getJobExecutionBot();
        jobExecutionBot.run();
        return jobExecutionBot;
    }

    // For reference: https://stackoverflow.com/questions/9117030/jul-to-slf4j-bridge
    private static void setupJULToSlf4jBridge() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        Logger.getGlobal().setLevel(Level.ALL);

        // Unless JUL is configured to log CONFIG level messages, this line won't print
        Logger.getLogger(BotStarter.class.getName()).log(Level.CONFIG, "JUL-to-SLF4J bridge working");
    }

}
