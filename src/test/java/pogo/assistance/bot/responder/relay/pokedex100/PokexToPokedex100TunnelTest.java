package pogo.assistance.bot.responder.relay.pokedex100;

import java.util.Optional;
import javax.security.auth.login.LoginException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.data.extraction.source.discord.MessageStream;

@Disabled("Should only be enabled after changing code to point to test server/channel. Otherwise will send message to real output channel.")
class PokexToPokedex100TunnelTest {

    private static JDA userJdaReceivingPokexDm;
    private static JDA userJdaRelayingPokexDm;

    @BeforeAll
    static void setUp() throws LoginException, InterruptedException {
        userJdaReceivingPokexDm = new JDABuilder(AccountType.CLIENT)
                .setToken(DiscordEntityConstants.HORUSEUS_USER_TOKEN)
                .build()
                .awaitReady();
        userJdaRelayingPokexDm = new JDABuilder(AccountType.BOT)
                .setToken(DiscordEntityConstants.M15M_BOT_TOKEN)
                .build()
                .awaitReady();
    }

    @AfterAll
    static void tearDown() {
        Optional.ofNullable(userJdaReceivingPokexDm).ifPresent(JDA::shutdown);
        Optional.ofNullable(userJdaRelayingPokexDm).ifPresent(JDA::shutdown);
    }

    @Disabled
    @Test
    void onPrivateMessageReceived_SpawnDm_Relays() {
        final PokexToPokedex100Tunnel pokexToPokedex100Tunnel = new PokexToPokedex100Tunnel(userJdaRelayingPokexDm);
        MessageStream.lookbackMessageStream(userJdaReceivingPokexDm.getUserById(DiscordEntityConstants.USER_ID_POKEX_DM_BOT).openPrivateChannel().complete())
                .forEach(message -> {
                    pokexToPokedex100Tunnel.onPrivateMessageReceived(new PrivateMessageReceivedEvent(userJdaReceivingPokexDm, message.getIdLong(), message));
                });
    }
}