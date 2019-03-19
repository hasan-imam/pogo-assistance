package pogo.assistance.data.extraction.source.discord.sandiego;

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
class SDHSpawnMessageProcessorIntegrationTest {

    private static JDA jda;

    private static final MessageProcessor<PokemonSpawn> PROCESSOR = new SDHSpawnMessageProcessor();

    @BeforeAll
    static void setUp() throws LoginException, InterruptedException {
        jda = new JDABuilder(AccountType.CLIENT)
                .setToken(DiscordEntityConstants.BENIN_USER_TOKEN)
                .build()
                .awaitReady();
    }

    @AfterAll
    static void tearDown() {
        Optional.ofNullable(jda).ifPresent(JDA::shutdown);
    }

    @Disabled
    @ParameterizedTest
    @MethodSource(value = {"SDHVIPBotDirectMessages"})
    void process_MessageFromSDVVIPBot_ReturnsExpected(final Message message) {
        final String failureMsgWithJumpUrl = "Failed to parse message: " + message.getJumpUrl();
        final PokemonSpawn pokemonSpawn = PROCESSOR.processWithoutThrowing(message)
                .orElseThrow(() -> new AssertionError(failureMsgWithJumpUrl));
        assertTrue(pokemonSpawn.getLevel().isPresent(), failureMsgWithJumpUrl);
        assertTrue(pokemonSpawn.getCp().isPresent(), failureMsgWithJumpUrl);
        assertTrue(pokemonSpawn.getIv().isPresent(), failureMsgWithJumpUrl);
        assertTrue((Pokedex.isGenderLess(pokemonSpawn.getPokedexEntry().getId()) && pokemonSpawn.getPokedexEntry().getGender() == PokedexEntry.Gender.NONE)
                || pokemonSpawn.getPokedexEntry().getGender() == PokedexEntry.Gender.MALE
                || pokemonSpawn.getPokedexEntry().getGender() == PokedexEntry.Gender.FEMALE);
    }

    private static Stream<Message> SDHVIPBotDirectMessages() {
        return MessageStream.lookbackMessageStream(jda.getUserById(DiscordEntityConstants.USER_ID_SDHVIP_BOT).openPrivateChannel().complete())
                .filter(PROCESSOR::canProcess)
                .limit(500);
    }
}