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
    @MethodSource(value = {"woolwich100ivMessages"})
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
    @MethodSource(value = {"lewisham95ivMessages", "hampstead95ivMessages", "northeast95ivMessages"})
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
    @MethodSource(value = {"northwestRareSpawnMessages", "centraleastRareSpawnMessages", "barnetRareSpawnMessages"})
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

    private static Stream<Message> woolwich100ivMessages() {
        return MessageStream.lookbackMessageStream(jda.getTextChannelById(628450346947051520L))
                .filter(PROCESSOR::canProcess)
                .limit(2000);
    }

    private static Stream<Message> lewisham95ivMessages() {
        return MessageStream.lookbackMessageStream(jda.getTextChannelById(628446244137861130L))
                .filter(PROCESSOR::canProcess)
                .limit(2000);
    }

    private static Stream<Message> hampstead95ivMessages() {
        return MessageStream.lookbackMessageStream(jda.getTextChannelById(628444879978102794L))
                .filter(PROCESSOR::canProcess)
                .limit(2000);
    }

    private static Stream<Message> northeast95ivMessages() {
        return MessageStream.lookbackMessageStream(jda.getTextChannelById(629198817526415360L))
                .filter(PROCESSOR::canProcess)
                .limit(2000);
    }

    private static Stream<Message> northwestRareSpawnMessages() {
        return MessageStream.lookbackMessageStream(jda.getTextChannelById(628439140446502963L))
                .filter(PROCESSOR::canProcess)
                .limit(2000);
    }

    private static Stream<Message> centraleastRareSpawnMessages() {
        return MessageStream.lookbackMessageStream(jda.getTextChannelById(628438282316808242L))
                .filter(PROCESSOR::canProcess)
                .limit(2000);
    }

    private static Stream<Message> barnetRareSpawnMessages() {
        return MessageStream.lookbackMessageStream(jda.getTextChannelById(628441916370845707L))
                .filter(PROCESSOR::canProcess)
                .limit(2000);
    }

}