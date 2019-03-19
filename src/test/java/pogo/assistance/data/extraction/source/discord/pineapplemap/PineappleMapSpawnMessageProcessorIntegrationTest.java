package pogo.assistance.data.extraction.source.discord.pineapplemap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.stream.Stream;
import javax.security.auth.login.LoginException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Message;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.data.extraction.source.discord.MessageProcessor;
import pogo.assistance.data.extraction.source.discord.MessageStream;
import pogo.assistance.data.model.pokemon.Pokedex;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

@Disabled("Runs real query against server - only to be used for hand/integration testing")
class PineappleMapSpawnMessageProcessorIntegrationTest {

    private static JDA jda;

    private static final MessageProcessor<PokemonSpawn> PROCESSOR = new PineappleMapSpawnMessageProcessor();

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
    @MethodSource(value = {"chicagoland100ivMessages"})
    void process_MessageFromChicagoland100IvChannel_ReturnsExpected(final Message message) {
        final String failureMsgWithJumpUrl = "Failed to parse message: " + message.getJumpUrl();
        final PokemonSpawn pokemonSpawn = PROCESSOR.processWithoutThrowing(message)
                .orElseThrow(() -> new AssertionError(failureMsgWithJumpUrl));
        assertThat(failureMsgWithJumpUrl, pokemonSpawn.getIv().orElse(-1.0), equalTo(100.0));
    }

    @ParameterizedTest
    @MethodSource(value = {"pineappleNewark100ivMessages"})
    void process_MessageFromPineapple100IvChannel_ReturnsExpected(final Message message) {
        final String failureMsgWithJumpUrl = "Failed to parse message: " + message.getJumpUrl();
        final PokemonSpawn pokemonSpawn = PROCESSOR.processWithoutThrowing(message)
                .orElseThrow(() -> new AssertionError(failureMsgWithJumpUrl));
        assertThat(failureMsgWithJumpUrl, pokemonSpawn.getIv().orElse(-1.0), equalTo(100.0));
    }

    @ParameterizedTest
    @MethodSource(value = {"pineappleFremontLevel35Messages"})
    void process_MessageFromPineappleLevel35Channel_ReturnsExpected(final Message message) {
        final String failureMsgWithJumpUrl = "Failed to parse message: " + message.getJumpUrl();
        final PokemonSpawn pokemonSpawn = PROCESSOR.processWithoutThrowing(message)
                .orElseThrow(() -> new AssertionError(failureMsgWithJumpUrl));
        assertThat(failureMsgWithJumpUrl, pokemonSpawn.getLevel().orElse(-1), equalTo(35));
    }

    @ParameterizedTest
    @MethodSource(value = {"pineappleHaywardRareMessages"})
    void process_MessageFromPineappleRareChannel_ReturnsExpected(final Message message) {
        final String failureMsgWithJumpUrl = "Failed to parse message: " + message.getJumpUrl();
        final PokemonSpawn pokemonSpawn = PROCESSOR.processWithoutThrowing(message)
                .orElseThrow(() -> new AssertionError(failureMsgWithJumpUrl));
        assertTrue(pokemonSpawn.getLevel().isPresent(), failureMsgWithJumpUrl);
        assertTrue(pokemonSpawn.getCp().isPresent(), failureMsgWithJumpUrl);
        assertTrue(pokemonSpawn.getIv().isPresent(), failureMsgWithJumpUrl);
        assertTrue(Pokedex.isGenderLess(pokemonSpawn.getPokedexEntry().getId())
                || pokemonSpawn.getPokedexEntry().getGender() == PokedexEntry.Gender.MALE
                || pokemonSpawn.getPokedexEntry().getGender() == PokedexEntry.Gender.FEMALE);
    }

    private static Stream<Message> chicagoland100ivMessages() {
        // Before the latest 1800 messages, they used to have a different message format completely
        return MessageStream.lookbackMessageStream(jda.getTextChannelById(DiscordEntityConstants.CHANNEL_ID_CHICAGOLAND_POGO_100IV))
                .filter(PROCESSOR::canProcess)
                .limit(1800);
    }

    private static Stream<Message> pineappleNewark100ivMessages() {
        return MessageStream.lookbackMessageStream(jda.getTextChannelById(DiscordEntityConstants.CHANNEL_ID_PINEAPPLE_NEWARK_100IV))
                .filter(PROCESSOR::canProcess)
                .limit(50);
    }

    private static Stream<Message> pineappleFremontLevel35Messages() {
        // At the time of writing this, latest ~5k message had used their new message format, where the messages further in the past have used a different one
        return MessageStream.lookbackMessageStream(jda.getTextChannelById(DiscordEntityConstants.CHANNEL_ID_PINEAPPLE_FREMONT_LEVEL35))
                .filter(PROCESSOR::canProcess)
                .limit(10000);
    }

    private static Stream<Message> pineappleHaywardRareMessages() {
        return MessageStream.lookbackMessageStream(jda.getTextChannelById(DiscordEntityConstants.CHANNEL_ID_PINEAPPLE_HAYWARD_RARE))
                .filter(PROCESSOR::canProcess)
                .limit(10000);
    }
}