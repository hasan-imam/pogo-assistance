package pogo.assistance.data.extraction.source.discord;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

class GenericSpawnMessageProcessorSouthwestPokemonIntegrationTest {

    private static JDA jda;

    private static final MessageProcessor<PokemonSpawn> PROCESSOR = new GenericSpawnMessageProcessor();

    @BeforeAll
    static void setUp() throws LoginException, InterruptedException {
        jda = new JDABuilder(AccountType.CLIENT)
                .setToken(DiscordEntityConstants.TIMBURTY_USER_TOKEN)
                .build()
                .awaitReady();
    }

    @AfterAll
    static void tearDown() {
        Optional.ofNullable(jda).ifPresent(JDA::shutdown);
    }

    @ParameterizedTest
    @MethodSource(value = {"Illinois100ivMessages"})
    void process_Illinois100ivMessages_ReturnsExpected(final Message message) {
        final String failureMsgWithJumpUrl = "Failed to parse message: " + message.getJumpUrl();
        final PokemonSpawn pokemonSpawn = PROCESSOR.processWithoutThrowing(message)
                .orElseThrow(() -> new AssertionError(failureMsgWithJumpUrl));
        assertAll(failureMsgWithJumpUrl,
                () -> assertTrue(pokemonSpawn.getLevel().isPresent(), "missing level"),
                () -> assertTrue(pokemonSpawn.getCp().isPresent(), "missing cp"),
                () -> assertThat(pokemonSpawn.getIv().orElse(-1.0), equalTo(100.0)),
                () -> assertThat(pokemonSpawn.getPokedexEntry().getGender(), not(PokedexEntry.Gender.UNKNOWN)));
    }

    @ParameterizedTest
    @MethodSource(value = {"Wisconsin95ivMessages", "LondonCanaryWharf95ivMessages", "LondonCentralWest95ivMessages"})
    void process_Sample95ivMessages_ReturnsExpected(final Message message) {
        final String failureMsgWithJumpUrl = "Failed to parse message: " + message.getJumpUrl();
        final PokemonSpawn pokemonSpawn = PROCESSOR.processWithoutThrowing(message)
                .orElseThrow(() -> new AssertionError(failureMsgWithJumpUrl));
        assertAll(failureMsgWithJumpUrl,
                () -> assertTrue(pokemonSpawn.getLevel().isPresent(), "missing level"),
                () -> assertTrue(pokemonSpawn.getCp().isPresent(), "missing cp"),
                // Although the source channels for this test are 95iv channels now, they used to be 90iv in the past. So check for 90+ iv.
                () -> assertThat(pokemonSpawn.getIv().orElse(-1.0), greaterThanOrEqualTo(90.0)),
                () -> assertThat(pokemonSpawn.getPokedexEntry().getGender(), not(PokedexEntry.Gender.UNKNOWN)));
    }

    @ParameterizedTest
    @MethodSource(value = {"londonCentralEastRareSpawnMessages", "londonCentralWestRareSpawnMessages", "londonNorthEastRareSpawnMessages"})
    void process_SampleRareSpawnMessages_ReturnsExpected(final Message message) {
        final String failureMsgWithJumpUrl = "Failed to parse message: " + message.getJumpUrl();
        final PokemonSpawn pokemonSpawn = PROCESSOR.processWithoutThrowing(message)
                .orElseThrow(() -> new AssertionError(failureMsgWithJumpUrl));
        assertAll(failureMsgWithJumpUrl,
                () -> assertTrue(pokemonSpawn.getLevel().isPresent(), "missing level"),
                () -> assertTrue(pokemonSpawn.getCp().isPresent(), "missing cp"),
                () -> assertTrue(pokemonSpawn.getIv().isPresent(), "missing iv"),
                () -> assertThat(pokemonSpawn.getPokedexEntry().getGender(), not(PokedexEntry.Gender.UNKNOWN)));
    }

    private static Stream<Message> Illinois100ivMessages() {
        return MessageStream.lookbackMessageStream(jda.getTextChannelById(517197579855986691L))
                .filter(PROCESSOR::canProcess)
                .limit(2000);
    }

    private static Stream<Message> Wisconsin95ivMessages() {
        return MessageStream.lookbackMessageStream(jda.getTextChannelById(514347821759987732L))
                .filter(PROCESSOR::canProcess)
                .limit(2000);
    }

    private static Stream<Message> LondonCanaryWharf95ivMessages() {
        return MessageStream.lookbackMessageStream(jda.getTextChannelById(553761036776243201L))
                .filter(PROCESSOR::canProcess)
                .limit(2000);
    }

    private static Stream<Message> LondonCentralWest95ivMessages() {
        return MessageStream.lookbackMessageStream(jda.getTextChannelById(553760465466163210L))
                .filter(PROCESSOR::canProcess)
                .limit(2000);
    }

    private static Stream<Message> londonCentralEastRareSpawnMessages() {
        return MessageStream.lookbackMessageStream(jda.getTextChannelById(553760810166648842L))
                .filter(PROCESSOR::canProcess)
                .limit(2000);
    }

    private static Stream<Message> londonCentralWestRareSpawnMessages() {
        return MessageStream.lookbackMessageStream(jda.getTextChannelById(553760611046129665L))
                .filter(PROCESSOR::canProcess)
                .limit(2000);
    }

    private static Stream<Message> londonNorthEastRareSpawnMessages() {
        return MessageStream.lookbackMessageStream(jda.getTextChannelById(553761776664313857L))
                .filter(PROCESSOR::canProcess)
                .limit(2000);
    }

}