package pogo.assistance.bot.manager;

import java.util.EnumSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.AccountType;
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
        final BotStarterInput input = new BotStarterInput();
        final CommandLine commandLine = new CommandLine(input);
        commandLine.parse(args);
        if (input.isJob()) {
            startJobs(input.getWorkflowIds());
        } else if (input.isBatchQuestPublish()) {
            log.info("Doing batch publish...");
        } else if (input.isInteractiveQuestPublish()) {
            log.info("Handling interactive commands...");
        } else if (input.isFeed()) {
            runPokemonSpawnExchange();
        } else if (input.isResponder()) {
            startResponder(input.getListenerIds());
        }

        log.info("Done setting up bot!");
    }

    private static void runPokemonSpawnExchange() {
        final SpawnDataCollectorBot bot = DaggerSpawnDataCollectorBotComponent.builder()
                .collectingUserToken(DiscordEntityConstants.CORRUPTED_USER_TOKEN)
                .relayingUserToken(DiscordEntityConstants.OWNING_USER_TOKEN)
                .build()
                .getSpawnDataCollectorBot();
        bot.run(); // run on this thread
    }

    private static void startResponder(final Set<ListenerId> inputListenerIds) {
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
                .userToken(USER_TOKEN)
                .listenerIds(listenerIds)
                .build()
                .getResponderBot();
        responderBot.run();
    }

    private static void startJobs(final Set<WorkflowId> inputWorkflowIds) {
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
    }

}
