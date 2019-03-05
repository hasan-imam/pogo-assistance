package pogo.assistance.data.extraction.source.discord.safarisight;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

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
import pogo.assistance.data.model.pokemon.PokedexEntry.Gender;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

@Disabled("Runs real query against server - only to be used for hand/integration testing")
class SafariSightSpawnMessageProcessorIntegrationTest {

    private static JDA jda;

    private static final MessageProcessor<PokemonSpawn> PROCESSOR = new SafariSightSpawnMessageProcessor();

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

    @Disabled
    @ParameterizedTest
    @MethodSource(value = {"safariSightNovaBotDMs"})
    void process_SafariSightNovaPrivateChannel_ReturnsExpected(final Message message) {
        final String failureMsgWithJumpUrl = "Failed to parse message: " + message.getJumpUrl();
        final PokemonSpawn pokemonSpawn = PROCESSOR.processWithoutThrowing(message)
                .orElse(null);
        assumeTrue(pokemonSpawn != null);
        assertAll(failureMsgWithJumpUrl,
                () -> assertTrue(pokemonSpawn.getIv().isPresent()),
                () -> assertTrue(pokemonSpawn.getCp().isPresent()),
                () -> assertTrue(pokemonSpawn.getLevel().isPresent()),
                () -> assertNotSame(pokemonSpawn.getPokedexEntry().getGender(), Gender.NONE),
                () -> assertTrue(pokemonSpawn.getPokedexEntry().getId() > 0),
                () -> assertFalse(pokemonSpawn.getPokedexEntry().getName().isEmpty()));
    }

    private static Stream<Message> safariSightNovaBotDMs() {
        return DiscordEntityConstants.USER_ID_SS_NOVA_BOTS.stream()
                .map(botId -> jda.getUserById(botId).openPrivateChannel().complete())
                .flatMap(MessageStream::lookbackMessageStream)
                .limit(100);
    }
}