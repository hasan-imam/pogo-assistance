package pogo.assistance.bot.responder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.stream.Stream;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RepHandlerTest {

    private static final String REP_SENDING_MEMBER = "sendingMember";
    private static final String REP_RECEIVING_MEMBER = "@receivingMember";

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private GuildMessageReceivedEvent commandMessageEvent;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private GuildMessageReceivedEvent attributionMessageEvent;

    @Mock
    private SelfUser selfUser;
    @Mock
    private Member commandMessageAuthoringMember;
    @Mock
    private User attributionMessageAuthoringUser;

    private final RepHandler repHandler = new RepHandler();

    @BeforeEach
    void setUp() {
        when(attributionMessageEvent.getJDA().getSelfUser()).thenReturn(selfUser);
        // Set right channel
        lenient().when(commandMessageEvent.getChannel().getIdLong()).thenReturn(repHandler.getTargetChannelId());
        lenient().when(attributionMessageEvent.getChannel().getIdLong()).thenReturn(repHandler.getTargetChannelId());
        // Mention self-user in the messages
        when(commandMessageEvent.getMessage().getMentionedUsers()).thenReturn(Collections.singletonList(selfUser));
        when(attributionMessageEvent.getMessage().getMentionedUsers()).thenReturn(Collections.singletonList(selfUser));

        when(attributionMessageEvent.getAuthor()).thenReturn(attributionMessageAuthoringUser);
        when(attributionMessageAuthoringUser.getIdLong()).thenReturn(repHandler.getRepAttributorId());

        when(attributionMessageEvent.getMessage().getContentStripped())
                .thenReturn(String.format(RepHandler.FORMAT_REP_ATTRIBUTION_MSG, REP_SENDING_MEMBER, REP_RECEIVING_MEMBER));

        lenient().when(commandMessageEvent.getMessage().getContentStripped())
                .thenReturn(String.format(RepHandler.FORMAT_REP_COMMAND_MSG, REP_RECEIVING_MEMBER));
        lenient().when(commandMessageEvent.getMember()).thenReturn(commandMessageAuthoringMember);
        lenient().when(commandMessageAuthoringMember.getEffectiveName()).thenReturn(REP_SENDING_MEMBER);
    }

    @Test
    void onGuildMessageReceived_SingleRepWorkflow_SendsCookieAsExpected() {
        final RepHandler repHandler = spy(this.repHandler);
        doNothing().when(repHandler).giveCookie(any(), any());
        repHandler.onGuildMessageReceived(commandMessageEvent);
        repHandler.onGuildMessageReceived(attributionMessageEvent);
        verify(repHandler).giveCookie(commandMessageAuthoringMember, attributionMessageEvent);
    }

    @Test
    void onGuildMessageReceived_AttributionWithoutCommand_Noop() {
        final RepHandler repHandler = spy(this.repHandler);
        repHandler.onGuildMessageReceived(attributionMessageEvent);
        verify(repHandler, never()).giveCookie(commandMessageAuthoringMember, attributionMessageEvent);
    }

    @ParameterizedTest(name = "Match case # {index}: {0} → {1}")
    @MethodSource("validRepAttributionMsgCases")
    void hasAttributedReputationPoint_MatchCase_ReturnsTrue(final String sender, final String receiver) {
        final String messageContent = String.format(RepHandler.FORMAT_REP_ATTRIBUTION_MSG, sender, receiver);
        when(attributionMessageEvent.getMessage().getContentStripped()).thenReturn(messageContent);
        assertTrue(repHandler.hasAttributedReputationPoint(attributionMessageEvent).isPresent());
    }

    @ParameterizedTest(name = "Mismatch case # {index}")
    @MethodSource("invalidRepAttributionMsgCases")
    void hasAttributedReputationPoint_MismatchCase_ReturnsFalse(final String messageContent) {
        when(attributionMessageEvent.getMessage().getContentStripped()).thenReturn(messageContent);
        assertFalse(repHandler.hasAttributedReputationPoint(attributionMessageEvent).isPresent());
    }

    private static Object[][] validRepAttributionMsgCases() {
        return new Object[][] {
                new Object[] {"\uD83C\uDF38 յσ\\~ςί ςհίηψᶜᴿᴬᵞეRႩჄ\uD83C\uDF38\uD83D\uDC95", "@Zαƒΐrε"},
                new Object[] {"Am i Visible° ͜ʖ ͡ -:question:", "@stuff \uD83D\uDC94 stuff \uD83D\uDE0F"},
                new Object[] {"ֆɨxՇђֆɛռֆɛ", "@\uD83C\uDDEA\uD83C\uDDEC✊\uD83C\uDFFD person \uD83E\uDD19\uD83C\uDFFD\uD83C\uDDFA\uD83C\uDDF8"}
        };
    }

    private static Stream<String> invalidRepAttributionMsgCases() {
        return Stream.of(
                ":up: | personGivingRep has given @PersonGettingRep a reputation point!",
                "\uD83C\uDD99 | personGivingRep has given @PersonGettingRep a reputation point!"); // 🆙 at the beginning
    }
    
}