package pogo.assistance.data.extraction.source.discord.vascans;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.stream.Stream;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Message;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.data.extraction.source.discord.MessageProcessor;
import pogo.assistance.data.extraction.source.discord.MessageStream;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

@Disabled("Runs real query against server - only to be used for hand/integration testing")
class VAScansSpawnMessageProcessorIntegrationTest {

    private static JDA jda;

    private static final MessageProcessor<PokemonSpawn> PROCESSOR = new VAScansSpawnMessageProcessor();

    @BeforeAll
    static void setUp() throws LoginException, InterruptedException {
        jda = new JDABuilder(AccountType.CLIENT)
                .setToken(DiscordEntityConstants.OWNING_USER_TOKEN)
                .build()
                .awaitReady();
    }

    @AfterAll
    static void tearDown() {
        Optional.ofNullable(jda).ifPresent(JDA::shutdown);
    }

    @Disabled
    @ParameterizedTest
    @MethodSource(value = {"vaScans100ivMessages"})
    void process_MessageFromIv100Channel_ReturnsExpected(final Message message) {
        final String failureMsgWithJumpUrl = "Failed to parse message: " + message.getJumpUrl();
        final PokemonSpawn pokemonSpawn = PROCESSOR.processWithoutThrowing(message)
                .orElseThrow(() -> new AssertionError(failureMsgWithJumpUrl));
        assertThat(failureMsgWithJumpUrl, pokemonSpawn.getIv().orElse(-1.0), equalTo(100.0));
        assertTrue(pokemonSpawn.getCp().isPresent(), failureMsgWithJumpUrl);
        assertTrue(pokemonSpawn.getLevel().isPresent(), failureMsgWithJumpUrl);
    }

    private static Stream<Message> vaScans100ivMessages() {
        return MessageStream.lookbackMessageStream(jda.getTextChannelById(DiscordEntityConstants.CHANNEL_ID_VASCANS_HUNDOS))
                .filter(PROCESSOR::canProcess)
                .limit(150);
    }
}