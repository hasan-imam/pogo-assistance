package pogo.assistance.bot.responder.relay.pogonice;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.data.extraction.source.discord.MessageStream;

import javax.security.auth.login.LoginException;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PokexToPoGoNiceTunnelTest {

    private static JDA userJdaReceivingPokexDm;

    @BeforeAll
    static void setUp() throws LoginException, InterruptedException {
        userJdaReceivingPokexDm = new JDABuilder(AccountType.CLIENT)
                .setToken(DiscordEntityConstants.COPERNICUS_USER_TOKEN)
                .build()
                .awaitReady();
    }

    @AfterAll
    static void tearDown() {
        Optional.ofNullable(userJdaReceivingPokexDm).ifPresent(JDA::shutdown);
    }

    @ParameterizedTest
    @MethodSource(value = { "pokexBotDms" })
    void onPrivateMessageReceived_SpawnDmFromBot_Relays(final Message message) {
        final PokexToPoGoNiceTunnel pokexToPoGoNiceTunnel = spy(new PokexToPoGoNiceTunnel());
        doNothing().when(pokexToPoGoNiceTunnel).sendMessage(anyString(), eq(userJdaReceivingPokexDm));
        // Send once -> should be relayed
        pokexToPoGoNiceTunnel.onPrivateMessageReceived(new PrivateMessageReceivedEvent(userJdaReceivingPokexDm, message.getIdLong(), message));
        // Send again -> should not be relayed since it's a duplicate
        pokexToPoGoNiceTunnel.onPrivateMessageReceived(new PrivateMessageReceivedEvent(userJdaReceivingPokexDm, message.getIdLong(), message));
        verify(pokexToPoGoNiceTunnel, times(1)).sendMessage(anyString(), eq(userJdaReceivingPokexDm));
    }

    private static Stream<Message> pokexBotDms() {
        return Optional.ofNullable(userJdaReceivingPokexDm.getUserById(DiscordEntityConstants.USER_ID_POKEX_DM_BOT))
                .map(User::openPrivateChannel)
                .map(RestAction::complete)
                .map(MessageStream::lookbackMessageStream)
                .get()
                .filter(PokexToPoGoNiceTunnel::isPokexSpawnNotificationDm)
                .limit(5);
    }
}