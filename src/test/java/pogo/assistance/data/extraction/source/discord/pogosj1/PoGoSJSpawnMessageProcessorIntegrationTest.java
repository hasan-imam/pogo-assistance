package pogo.assistance.data.extraction.source.discord.pogosj1;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
class PoGoSJSpawnMessageProcessorIntegrationTest {

    private static JDA jda;

    private static final MessageProcessor<PokemonSpawn> PROCESSOR = new PoGoSJSpawnMessageProcessorV2();

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

    @ParameterizedTest
    @MethodSource("pogosj100ivMessages")
    void process_MessageFromIv100Channel_ReturnsExpected(final Message message) {
        final String failureMsgWithJumpUrl = "Failed to parse message: " + message.getJumpUrl();
        final PokemonSpawn pokemonSpawn = PROCESSOR.processWithoutThrowing(message)
                .orElseThrow(() -> new AssertionError(failureMsgWithJumpUrl));
        assertAll(failureMsgWithJumpUrl,
                () -> assertThat(pokemonSpawn, equalTo(new PoGoSJSpawnMessageProcessor().process(message).get())),
                () -> assertThat(pokemonSpawn.getPokedexEntry().getId(), greaterThan(0)),
                () -> assertThat(pokemonSpawn.getPokedexEntry().getName(), not(emptyOrNullString())),
                () -> assertThat(pokemonSpawn.getIv().orElse(-1.0), equalTo(100.0)),
                () -> assertTrue(pokemonSpawn.getLevel().isPresent()),
                () -> assertTrue(pokemonSpawn.getCp().isPresent()),
                () -> assertTrue(pokemonSpawn.getLocationDescription().isPresent())
        );
    }

    @ParameterizedTest
    @MethodSource("pogosjMax100Messages")
    void process_MessageFromMax100Channel_ReturnsExpected(final Message message) {
        final String failureMsgWithJumpUrl = "Failed to parse message: " + message.getJumpUrl();
        final PokemonSpawn pokemonSpawn = PROCESSOR.processWithoutThrowing(message)
                .orElseThrow(() -> new AssertionError(failureMsgWithJumpUrl));
        assertAll(failureMsgWithJumpUrl,
                () -> assertThat(pokemonSpawn, equalTo(new PoGoSJSpawnMessageProcessor().process(message).get())),
                () -> assertThat(pokemonSpawn.getPokedexEntry().getId(), greaterThan(0)),
                () -> assertThat(pokemonSpawn.getPokedexEntry().getName(), not(emptyOrNullString())),
                () -> assertThat(pokemonSpawn.getIv().orElse(-1.0), equalTo(100.0)),
                () -> assertThat(pokemonSpawn.getLevel().orElse(-1), greaterThanOrEqualTo(30)),
                () -> assertTrue(pokemonSpawn.getCp().isPresent()),
                () -> assertTrue(pokemonSpawn.getLocationDescription().isPresent())
        );
    }

    @ParameterizedTest
    @MethodSource("pogosjTweetMessages")
    void process_MessageFromTweetsChannel_ReturnsExpected(final Message message) {
        final String failureMsgWithJumpUrl = "Failed to parse message: " + message.getJumpUrl();
        final PokemonSpawn pokemonSpawn = new PoGoSJSpawnMessageProcessorV2().processWithoutThrowing(message)
                .orElseThrow(() -> new AssertionError(failureMsgWithJumpUrl));
        assertAll(failureMsgWithJumpUrl,
                // Many will fail the cp/iv/level checks because they are candies
                () -> assertThat(pokemonSpawn.getPokedexEntry().getId(), greaterThan(0)),
                () -> assertThat(pokemonSpawn.getPokedexEntry().getName(), not(emptyOrNullString())),
                () -> assertTrue(pokemonSpawn.getIv().isPresent(), "missing iv"),
                () -> assertTrue(pokemonSpawn.getLevel().isPresent(), "missing level"),
                () -> assertTrue(pokemonSpawn.getCp().isPresent(), "missing cp"),
                () -> assertTrue(pokemonSpawn.getLocationDescription().isPresent(), "missing location")
        );
    }

    private static Stream<Message> pogosj100ivMessages() {
        return MessageStream.lookbackMessageStream(jda.getTextChannelById(DiscordEntityConstants.CHANNEL_ID_POGOSJ1_100IV))
                .filter(PROCESSOR::canProcess)
                .filter(message -> message.getIdLong() == 417840014093713409L)
                .limit(20000);
    }

    private static Stream<Message> pogosjMax100Messages() {
        return MessageStream.lookbackMessageStream(jda.getTextChannelById(DiscordEntityConstants.CHANNEL_ID_POGOSJ1_100IVMAX))
                .filter(PROCESSOR::canProcess)
                .limit(8200); // messages earlier than this limit have less description lines and fails parsing
    }

    private static Stream<Message> pogosjTweetMessages() {
        return MessageStream.lookbackMessageStream(jda.getTextChannelById(DiscordEntityConstants.CHANNEL_ID_POGOSJ1_TWEETS))
                .filter(new PoGoSJSpawnMessageProcessorV2()::canProcess)
                .limit(20000);
    }
}