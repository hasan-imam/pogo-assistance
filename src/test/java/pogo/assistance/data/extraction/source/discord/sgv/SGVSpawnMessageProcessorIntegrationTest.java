package pogo.assistance.data.extraction.source.discord.sgv;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pogo.assistance.bot.di.DiscordEntityConstants.CATEGORY_IDS_SGV_SCANS_IV_FEED;
import static pogo.assistance.bot.di.DiscordEntityConstants.NINERS_USER_TOKEN;
import static pogo.assistance.bot.di.DiscordEntityConstants.SERVER_ID_SGV_SCANS;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import javax.security.auth.login.LoginException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.TestAbortedException;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Message;
import pogo.assistance.data.extraction.source.discord.MessageProcessor;
import pogo.assistance.data.extraction.source.discord.MessageStream;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

class SGVSpawnMessageProcessorIntegrationTest {

    private static JDA jda;

    private static final MessageProcessor<PokemonSpawn> PROCESSOR = new SGVSpawnMessageProcessor(true);

    @BeforeAll
    static void setUp() throws LoginException, InterruptedException {
        jda = new JDABuilder(AccountType.CLIENT)
                .setToken(NINERS_USER_TOKEN)
                .build()
                .awaitReady();
    }

    @AfterAll
    static void tearDown() {
        Optional.ofNullable(jda).ifPresent(JDA::shutdown);
    }

    @ParameterizedTest
    @MethodSource(value = { "SGVScansSampledMessagesFromAllFeedChannels" })
    void process_SGVRegionalFeedMessages_ReturnsExpected(final Message message) {
        final String failureMsgWithJumpUrl = "Failed to parse message: " + message.getJumpUrl();
        final PokemonSpawn pokemonSpawn = PROCESSOR.processWithoutThrowing(message)
                .orElseThrow(() -> new TestAbortedException("Spawn ignored, potentially due to missing iv/other stats"));
        assertAll(failureMsgWithJumpUrl,
                () -> assertTrue(pokemonSpawn.getLevel().isPresent(), "missing level"),
                () -> assertTrue(pokemonSpawn.getCp().isPresent(), "missing cp"),
                () -> assertTrue(pokemonSpawn.getIv().isPresent(), "missing iv"),
                () -> assertThat(pokemonSpawn.getPokedexEntry().getGender(), not(PokedexEntry.Gender.UNKNOWN)));
        if (message.getChannel().getName().contains("100iv")) {
            assertThat(pokemonSpawn.getIv().get(), equalTo(100.0));
        }
    }

    private static Stream<Message> SGVScansSampledMessagesFromAllFeedChannels() {
        return jda.getGuildById(SERVER_ID_SGV_SCANS).getCategories().stream()
                .filter(category -> CATEGORY_IDS_SGV_SCANS_IV_FEED.contains(category.getIdLong()))
                .map(Category::getTextChannels)
                .flatMap(Collection::stream)
                .map(MessageStream::lookbackMessageStream)
                // Take some message from all channels, even the non-feed ones
                // Don't go too far in the past since map URLs expire and result in parsing error
                .flatMap(regionalMessageStream -> regionalMessageStream.limit(10))
                // Filter out messages that cannot be processed (e.g. from non-feed channels)
                .filter(PROCESSOR::canProcess);
    }

}