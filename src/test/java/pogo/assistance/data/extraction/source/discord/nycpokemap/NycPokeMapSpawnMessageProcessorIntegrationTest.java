package pogo.assistance.data.extraction.source.discord.nycpokemap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
class NycPokeMapSpawnMessageProcessorIntegrationTest {

    private static JDA jda;

    private static final MessageProcessor<PokemonSpawn> PROCESSOR = new NycPokeMapSpawnMessageProcessor();

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
    @MethodSource(value = {"nyc0ivMessages"})
    void process_MessageFromIv0Channel_ReturnsExpected(final Message message) {
        final String failureMsgWithJumpUrl = "Failed to parse message: " + message.getJumpUrl();
        final PokemonSpawn pokemonSpawn = PROCESSOR.processWithoutThrowing(message)
                .orElseThrow(() -> new AssertionError(failureMsgWithJumpUrl));
        assertThat(failureMsgWithJumpUrl, pokemonSpawn.getIv().orElse(-1.0), equalTo(0.0));
    }

    @Disabled
    @ParameterizedTest
    @MethodSource(value = {"nyc90ivMessages"})
    void process_MessageFromIv90Channel_ReturnsExpected(final Message message) {
        final String failureMsgWithJumpUrl = "Failed to parse message: " + message.getJumpUrl();
        final PokemonSpawn pokemonSpawn = PROCESSOR.processWithoutThrowing(message)
                .orElseThrow(() -> new AssertionError(failureMsgWithJumpUrl));
        assertThat(failureMsgWithJumpUrl, pokemonSpawn.getIv().orElse(-1.0), greaterThanOrEqualTo(90.0));
    }

    @Disabled
    @ParameterizedTest
    @MethodSource(value = {"nyc95ivMessages"})
    void process_MessageFromIv95Channel_ReturnsExpected(final Message message) {
        final String failureMsgWithJumpUrl = "Failed to parse message: " + message.getJumpUrl();
        final PokemonSpawn pokemonSpawn = PROCESSOR.processWithoutThrowing(message)
                .orElseThrow(() -> new AssertionError(failureMsgWithJumpUrl));
        assertThat(failureMsgWithJumpUrl, pokemonSpawn.getIv().orElse(-1.0), greaterThanOrEqualTo(95.0));
    }

    @Disabled
    @ParameterizedTest
    @MethodSource(value = {"nyc100ivMessages", "nyc100ivLevel30Messages"})
    void process_MessageFromIv100Channels_ReturnsExpected(final Message message) {
        final String failureMsgWithJumpUrl = "Failed to parse message: " + message.getJumpUrl();
        final PokemonSpawn pokemonSpawn = PROCESSOR.processWithoutThrowing(message)
                .orElseThrow(() -> new AssertionError(failureMsgWithJumpUrl));
        assertEquals(Double.valueOf(100), pokemonSpawn.getIv().orElse(-1.0), failureMsgWithJumpUrl);
    }

    private static Stream<Message> nyc0ivMessages() {
        return MessageStream.lookbackMessageStream(jda.getTextChannelById(DiscordEntityConstants.CHANNEL_ID_NYCPOKEMAP_IV0))
                .filter(PROCESSOR::canProcess)
                .limit(675);
    }

    private static Stream<Message> nyc90ivMessages() {
        return MessageStream.lookbackMessageStream(jda.getTextChannelById(DiscordEntityConstants.CHANNEL_ID_NYCPOKEMAP_IV90))
                .filter(PROCESSOR::canProcess)
                .limit(13000);
    }

    private static Stream<Message> nyc95ivMessages() {
        return MessageStream.lookbackMessageStream(jda.getTextChannelById(DiscordEntityConstants.CHANNEL_ID_NYCPOKEMAP_IV95))
                .filter(PROCESSOR::canProcess)
                .limit(29000);
    }

    private static Stream<Message> nyc100ivMessages() {
        return MessageStream.lookbackMessageStream(jda.getTextChannelById(DiscordEntityConstants.CHANNEL_ID_NYCPOKEMAP_IV100))
                .filter(PROCESSOR::canProcess)
                .limit(11800);
    }

    private static Stream<Message> nyc100ivLevel30Messages() {
        return MessageStream.lookbackMessageStream(jda.getTextChannelById(DiscordEntityConstants.CHANNEL_ID_NYCPOKEMAP_IV100_LEVEL30))
                .filter(PROCESSOR::canProcess)
                .limit(400);
    }

}