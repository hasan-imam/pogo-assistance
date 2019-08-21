package pogo.assistance.data.extraction.source.discord;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pogo.assistance.bot.di.DiscordEntityConstants.CATEGORY_IDS_POKEMON_MAPS_FLORIDA_FEEDS;
import static pogo.assistance.bot.di.DiscordEntityConstants.CORRUPTED_USER_TOKEN;
import static pogo.assistance.bot.di.DiscordEntityConstants.SERVER_ID_POKEMON_MAPS_FLORIDA;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import javax.security.auth.login.LoginException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.TestAbortedException;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Message;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

class GenericSpawnMessageProcessorPokemonMapsFloridaIntegrationTest {

    private static JDA jda;

    private static final MessageProcessor<PokemonSpawn> PROCESSOR = new GenericSpawnMessageProcessor();

    @BeforeAll
    static void setUp() throws LoginException, InterruptedException {
        jda = new JDABuilder(AccountType.CLIENT)
                .setToken(CORRUPTED_USER_TOKEN)
                .build()
                .awaitReady();
    }

    @AfterAll
    static void tearDown() {
        Optional.ofNullable(jda).ifPresent(JDA::shutdown);
    }

    @ParameterizedTest
    @MethodSource(value = { "PokemonMapsFloridaFeedMessages" })
    void process_PokemonMapsFloridaFeedMessages_ReturnsExpected(final Message message) {
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

    private static Stream<Message> PokemonMapsFloridaFeedMessages() {
        return jda.getGuildById(SERVER_ID_POKEMON_MAPS_FLORIDA).getCategories().stream()
                .filter(category -> CATEGORY_IDS_POKEMON_MAPS_FLORIDA_FEEDS.contains(category.getIdLong()))
                .map(Category::getTextChannels)
                .flatMap(Collection::stream)
                .map(MessageStream::lookbackMessageStream)
                // Take some message from all channels
                .flatMap(regionalMessageStream -> regionalMessageStream.limit(250))
                // Filter out messages that cannot be processed (e.g. from non-feed channels)
                .filter(PROCESSOR::canProcess);
    }

}