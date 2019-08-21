package pogo.assistance.bot.responder.relay.pokedex100;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.stream.Stream;
import javax.security.auth.login.LoginException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.data.extraction.source.discord.MessageStream;

class PokexToPokedex100TunnelTest {

    private static JDA userJdaReceivingPokexDm;

    @BeforeAll
    static void setUp() throws LoginException, InterruptedException {
        userJdaReceivingPokexDm = new JDABuilder(AccountType.CLIENT)
                .setToken(DiscordEntityConstants.CHRONIC_USER_TOKEN)
                .build()
                .awaitReady();
    }

    @AfterAll
    static void tearDown() {
        Optional.ofNullable(userJdaReceivingPokexDm).ifPresent(JDA::shutdown);
    }

    @ParameterizedTest
    @MethodSource(value = { "pokexBotDms", "pokedex100BotDms" })
    void onPrivateMessageReceived_SpawnDmFromBot_Relays(final Message message) {
        final PokexToPokedex100Tunnel pokexToPokedex100Tunnel = spy(new PokexToPokedex100Tunnel(mock(JDA.class)));
        doNothing().when(pokexToPokedex100Tunnel).sendMessage(anyString());
        // Send once -> should be relayed
        pokexToPokedex100Tunnel.onPrivateMessageReceived(new PrivateMessageReceivedEvent(userJdaReceivingPokexDm, message.getIdLong(), message));
        // Send again -> should not be relayed since it's a duplicate
        pokexToPokedex100Tunnel.onPrivateMessageReceived(new PrivateMessageReceivedEvent(userJdaReceivingPokexDm, message.getIdLong(), message));
        verify(pokexToPokedex100Tunnel, times(1)).sendMessage(anyString());
    }

    private static Stream<Message> pokexBotDms() {
        return Optional.of(userJdaReceivingPokexDm.getUserById(DiscordEntityConstants.USER_ID_POKEX_DM_BOT))
                .map(User::openPrivateChannel)
                .map(RestAction::complete)
                .map(MessageStream::lookbackMessageStream)
                .get()
                .filter(PokexToPokedex100Tunnel::isPokexSpawnNotificationDm)
                .limit(500);
    }

    private static Stream<Message> pokedex100BotDms() {
        return Optional.of(userJdaReceivingPokexDm.getUserById(DiscordEntityConstants.USER_ID_PDEX100_SUPER_SHIPPER_9))
                .map(User::openPrivateChannel)
                .map(RestAction::complete)
                .map(MessageStream::lookbackMessageStream)
                .get()
                .filter(PokexToPokedex100Tunnel::isPokedex100SpawnNotificationDm)
                .limit(500);
    }
}