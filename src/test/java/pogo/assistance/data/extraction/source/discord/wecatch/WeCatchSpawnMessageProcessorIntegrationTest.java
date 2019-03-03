package pogo.assistance.data.extraction.source.discord.wecatch;

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
class WeCatchSpawnMessageProcessorIntegrationTest {
    private static JDA owningUserJda;

    private static final MessageProcessor<PokemonSpawn> PROCESSOR = new WeCatchSpawnMessageProcessor();

    @BeforeAll
    static void setUp() throws LoginException, InterruptedException {
        owningUserJda = new JDABuilder(AccountType.CLIENT)
                .setToken(DiscordEntityConstants.OWNING_USER_TOKEN)
                .build()
                .awaitReady();
    }

    @AfterAll
    static void tearDown() {
        Optional.ofNullable(owningUserJda).ifPresent(JDA::shutdown);
    }

    @Disabled
    @ParameterizedTest
    @MethodSource(value = {"weCatchIv90upChannel"})
    void process_WeCatchIv90up_ReturnsExpected(final Message message) {
        final String failureMsgWithJumpUrl = "Failed to parse message: " + message.getJumpUrl();
        final PokemonSpawn pokemonSpawn = PROCESSOR.processWithoutThrowing(message)
                .orElse(null);
        assumeTrue(pokemonSpawn != null);
        assertAll(failureMsgWithJumpUrl,
                () -> assertTrue(pokemonSpawn.getIv().isPresent() && pokemonSpawn.getIv().get() >= 90.0),
                () -> assertTrue(pokemonSpawn.getCp().isPresent()),
                () -> assertTrue(pokemonSpawn.getLevel().isPresent()),
                () -> assertTrue((message.getEmbeds().get(0).getDescription().contains("⚲")
                        && pokemonSpawn.getPokedexEntry().getGender() == Gender.NONE)
                        || pokemonSpawn.getPokedexEntry().getGender() != Gender.NONE),
                () -> assertTrue(pokemonSpawn.getPokedexEntry().getId() > 0),
                () -> assertFalse(pokemonSpawn.getPokedexEntry().getName().isEmpty()));
    }

    private static Stream<Message> weCatchIv90upChannel() {
        return MessageStream.lookbackMessageStream(owningUserJda.getTextChannelById(DiscordEntityConstants.CHANNEL_ID_WECATCH_IV90UP))
                .filter(PROCESSOR::canProcess)
                .limit(20000);
    }
}