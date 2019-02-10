package pogo.assistance.bot.manager;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class BotStarterInputTest {

    @Test
    void parse_WithAllPrograms_SetsExpectedFields() {
        final BotStarterInput input = new BotStarterInput();
        new CommandLine(input).parse("-feed", "-batchQuestPublish", "-interactiveQuestPublish", "-responder");
        assertTrue(input.isFeed());
        assertTrue(input.isBatchQuestPublish());
        assertTrue(input.isInteractiveQuestPublish());
        assertTrue(input.isResponder());
    }

}