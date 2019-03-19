package pogo.assistance.data.extraction.source.discord.flpokemap;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

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
import pogo.assistance.data.model.pokemon.PokedexEntry.Gender;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

@Disabled("Runs real query against server - only to be used for hand/integration testing")
class FLPokeMapSpawnMessageProcessorIntegrationTest {

    private static JDA jda;

    private static final MessageProcessor<PokemonSpawn> PROCESSOR = new FLPokeMapSpawnMessageProcessor();

    @BeforeAll
    static void setUp() throws LoginException, InterruptedException {
        jda = new JDABuilder(AccountType.CLIENT)
                .setToken(DiscordEntityConstants.CORRUPTED_USER_TOKEN)
                .build()
                .awaitReady();
    }

    @AfterAll
    static void tearDown() {
        Optional.ofNullable(jda).ifPresent(JDA::shutdown);
    }

    @ParameterizedTest
    @MethodSource(value = {"flpmAlertBot7Dms"})
    void process_FlpmPrivateChannel_ReturnsExpected(final Message message) {
        final String failureMsgWithJumpUrl = "Failed to parse message: " + message.getJumpUrl();
        final PokemonSpawn pokemonSpawn = PROCESSOR.processWithoutThrowing(message)
                .orElse(null);
        assumeTrue(pokemonSpawn != null);
        assertAll(failureMsgWithJumpUrl,
                () -> assertTrue(pokemonSpawn.getIv().isPresent()),
                () -> assertTrue(pokemonSpawn.getCp().isPresent()),
                () -> assertTrue(pokemonSpawn.getLevel().isPresent()),
                () -> assertNotSame(pokemonSpawn.getPokedexEntry().getGender(), Gender.UNKNOWN, "unknown gender"),
                () -> assertTrue(pokemonSpawn.getPokedexEntry().getId() > 0),
                () -> assertFalse(pokemonSpawn.getPokedexEntry().getName().isEmpty()));
    }

    @ParameterizedTest
    @MethodSource(value = {"apAlertBot7Dms"})
    void process_ApPrivateChannel_ReturnsExpected(final Message message) {
        final String failureMsgWithJumpUrl = "Failed to parse message: " + message.getJumpUrl();
        final PokemonSpawn pokemonSpawn = PROCESSOR.processWithoutThrowing(message)
                .orElse(null);
        assumeTrue(pokemonSpawn != null);
        assertAll(failureMsgWithJumpUrl,
                () -> assertTrue(pokemonSpawn.getIv().isPresent()),
                () -> assertTrue(pokemonSpawn.getCp().isPresent()),
                () -> assertTrue(pokemonSpawn.getLevel().isPresent()),
                () -> assertNotSame(pokemonSpawn.getPokedexEntry().getGender(), Gender.UNKNOWN, "unknown gender"),
                () -> assertTrue(pokemonSpawn.getPokedexEntry().getId() > 0),
                () -> assertFalse(pokemonSpawn.getPokedexEntry().getName().isEmpty()));
    }

    private static Stream<Message> flpmAlertBot7Dms() {
        // 201,137 message in this DM channel when checked on Feb 27, 2019
        // Earliest message dates back to 2018-12-27T10:23:32.155Z
        return MessageStream.lookbackMessageStream(jda.getUserById(DiscordEntityConstants.USER_ID_FLPM_ALERT_BOT_7).openPrivateChannel().complete())
                .filter(PROCESSOR::canProcess)
                .limit(20000);
    }

    private static Stream<Message> apAlertBot7Dms() {
        return MessageStream.lookbackMessageStream(jda.getUserById(DiscordEntityConstants.USER_ID_AP_ALERT_BOT).openPrivateChannel().complete())
                .filter(PROCESSOR::canProcess)
                .limit(15000);
    }
}