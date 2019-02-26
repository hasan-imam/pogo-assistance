package pogo.assistance.bot.manager;

import java.util.Set;
import lombok.Getter;
import picocli.CommandLine.Option;
import pogo.assistance.bot.responder.ListenerId;
import pogo.assistance.bot.job.WorkflowId;

@Getter
class BotStarterInput {

    @Option(names = "-batchQuestPublish", description = "executes batch quest publishing")
    private boolean batchQuestPublish;

    @Option(names = "-interactiveQuestPublish", description = "starts off interactive quest request handler")
    private boolean interactiveQuestPublish;

    @Option(names = "-feed", description = "starts off feed bot")
    private boolean feed;

    @Option(names = "-responder", description = "starts off responder bot")
    private boolean responder;

    @Option(names = "-listener", description = "adds one/more listener(s) to responder bot", arity = "1..*")
    private Set<ListenerId> listenerIds;

    @Option(names = "-job", description = "runs one or more jobs")
    private boolean job;

    @Option(names = "-workflow", description = "adds one/more workflow(s) to job execution bot", arity = "1..*")
    private Set<WorkflowId> workflowIds;

}
