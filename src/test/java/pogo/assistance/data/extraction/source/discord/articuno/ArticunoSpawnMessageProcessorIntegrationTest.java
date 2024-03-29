package pogo.assistance.data.extraction.source.discord.articuno;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.TestAbortedException;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.data.extraction.source.discord.MessageProcessor;
import pogo.assistance.data.extraction.source.discord.MessageStream;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

import javax.security.auth.login.LoginException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ArticunoSpawnMessageProcessorIntegrationTest {

    private static JDA jda;

    private static final MessageProcessor<PokemonSpawn> PROCESSOR = new ArticunoSpawnMessageProcessor();

    @BeforeAll
    static void setUp() throws LoginException, InterruptedException {
        jda = new JDABuilder(AccountType.CLIENT)
                .setToken(DiscordEntityConstants.CHRONIC_USER_TOKEN)
                .build()
                .awaitReady();
    }

    @ParameterizedTest
    @MethodSource(value = { "iToolsSpawnMessages" })
    void process_MessageFromIv100Channels_ReturnsExpected(final Message message) {
        final String failureMsgWithJumpUrl = "Failed to parse message: " + message.getJumpUrl();
        final PokemonSpawn pokemonSpawn = PROCESSOR.processWithoutThrowing(message)
                .orElseThrow(() -> new TestAbortedException("Produced empty - probably because the message contains no gender info - " + message.getJumpUrl()));
        assertAll(failureMsgWithJumpUrl,
                () -> assertTrue(pokemonSpawn.getLevel().isPresent(), "missing level"),
                () -> assertTrue(pokemonSpawn.getCp().isPresent(), "missing cp"),
                () -> assertEquals(Double.valueOf(100), pokemonSpawn.getIv().orElse(-1.0), "expected 100 iv"));
    }

    private static Stream<Message> iToolsSpawnMessages() {
        return DiscordEntityConstants.SPAWN_CHANNEL_IDS_ITOOLS.stream()
                .map(jda::getTextChannelById)
                .map(MessageStream::lookbackMessageStream)
                .flatMap(stream -> stream.limit(1000).filter(PROCESSOR::canProcess));
    }

}