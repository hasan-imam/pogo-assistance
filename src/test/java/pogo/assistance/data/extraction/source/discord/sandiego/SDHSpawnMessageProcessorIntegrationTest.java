package pogo.assistance.data.extraction.source.discord.sandiego;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.TestAbortedException;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.data.extraction.source.discord.MessageProcessor;
import pogo.assistance.data.extraction.source.discord.MessageStream;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

import javax.security.auth.login.LoginException;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pogo.assistance.bot.di.DiscordEntityConstants.CATEGORY_ID_SDHVIP_SIGHTING_REPORTS;
import static pogo.assistance.bot.di.DiscordEntityConstants.SERVER_ID_SDHVIP;

class SDHSpawnMessageProcessorIntegrationTest {

    private static JDA jda;

    private static final MessageProcessor<PokemonSpawn> PROCESSOR = new SDHSpawnMessageProcessor();

    @BeforeAll
    static void setUp() throws LoginException, InterruptedException {
        jda = new JDABuilder(AccountType.CLIENT)
                .setToken(DiscordEntityConstants.MICHELLEX_USER_TOKEN)
                .build()
                .awaitReady();
    }

    @AfterAll
    static void tearDown() {
        Optional.ofNullable(jda).ifPresent(JDA::shutdown);
    }

    @ParameterizedTest
    @MethodSource(value = { "SDHVIPBotDirectMessages", "SDHVIPSightingReportsCategoryMessages" })
    void process_MessageFromSDVVIPBot_ReturnsExpected(final Message message) {
        final String failureMsgWithJumpUrl = "Failed to parse message: " + message.getJumpUrl();
        final PokemonSpawn pokemonSpawn = PROCESSOR.processWithoutThrowing(message)
                .orElseThrow(() -> new TestAbortedException("Spawn ignored, potentially due to missing iv/other stats"));
        assertAll(failureMsgWithJumpUrl,
                () -> assertTrue(pokemonSpawn.getLevel().isPresent(), "missing level"),
                () -> assertTrue(pokemonSpawn.getCp().isPresent(), "missing cp"),
                () -> assertTrue(pokemonSpawn.getIv().isPresent(), "missing iv"),
                () -> assertTrue(pokemonSpawn.getDespawnTime().isPresent(), "missing despawn time"),
                () -> assertThat(pokemonSpawn.getPokedexEntry().getGender(), not(PokedexEntry.Gender.UNKNOWN)));
    }

    private static Stream<Message> SDHVIPBotDirectMessages() {
        return DiscordEntityConstants.USER_IDS_SDHVIP_BOT.stream()
                .map(botUserId -> jda.getUserById(botUserId))
                .map(botUser -> botUser.openPrivateChannel().complete())
                .flatMap(MessageStream::lookbackMessageStream)
                .filter(PROCESSOR::canProcess)
                .limit(1000);
    }

    private static Stream<Message> SDHVIPSightingReportsCategoryMessages() {
        return jda.getGuildById(SERVER_ID_SDHVIP).getCategoryById(CATEGORY_ID_SDHVIP_SIGHTING_REPORTS).getTextChannels().stream()
                .map(MessageStream::lookbackMessageStream)
                .flatMap(regionalMessageStream -> regionalMessageStream.limit(5).filter(PROCESSOR::canProcess));
    }

}