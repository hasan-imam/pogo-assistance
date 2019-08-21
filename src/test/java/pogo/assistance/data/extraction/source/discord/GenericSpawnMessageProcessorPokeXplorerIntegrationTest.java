package pogo.assistance.data.extraction.source.discord;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static pogo.assistance.bot.di.DiscordEntityConstants.CATEGORY_IDS_POKE_XPLORER_FEEDS;
import static pogo.assistance.bot.di.DiscordEntityConstants.SERVER_ID_POKE_XPLORER;

import java.util.Collection;
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
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Message;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

class GenericSpawnMessageProcessorPokeXplorerIntegrationTest {

    private static JDA jda;

    private static final MessageProcessor<PokemonSpawn> PROCESSOR = new GenericSpawnMessageProcessor();

    @BeforeAll
    static void setUp() throws LoginException, InterruptedException {
        jda = new JDABuilder(AccountType.CLIENT)
                .setToken(DiscordEntityConstants.CONNOISSEUR_USER_TOKEN)
                .build()
                .awaitReady();
    }

    @AfterAll
    static void tearDown() {
        Optional.ofNullable(jda).ifPresent(JDA::shutdown);
    }

    @ParameterizedTest
    @MethodSource(value = { "PokeExplorerSpawnFeedMessages" })
    void process_PoGoSofiaSpawnFeedMessages_ReturnsExpected(final Message message) {
        final String failureMsgWithJumpUrl = "Failed to parse message: " + message.getJumpUrl();
        final Optional<PokemonSpawn> result = PROCESSOR.processWithoutThrowing(message);
        assumeTrue(result.isPresent(), "Skipped spawn with missing iv/cp/level: " + message.getJumpUrl());
        final PokemonSpawn pokemonSpawn = result.get();
        assertAll(failureMsgWithJumpUrl,
                () -> assertTrue(pokemonSpawn.getLevel().isPresent(), "missing level"),
                () -> assertTrue(pokemonSpawn.getCp().isPresent(), "missing cp"),
                () -> assertTrue(pokemonSpawn.getIv().isPresent(), "missing iv"),
                () -> assertThat(pokemonSpawn.getPokedexEntry().getGender(), not(PokedexEntry.Gender.UNKNOWN)));
    }

    private static Stream<Message> PokeExplorerSpawnFeedMessages() {
        return jda.getGuildById(SERVER_ID_POKE_XPLORER).getCategories().stream()
                .filter(category -> CATEGORY_IDS_POKE_XPLORER_FEEDS.contains(category.getIdLong()))
                .map(Category::getTextChannels)
                .flatMap(Collection::stream)
                .map(MessageStream::lookbackMessageStream)
                // Take some message from all channels, even the non-feed ones
                .flatMap(regionalMessageStream -> regionalMessageStream.limit(250))
                // Filter out messages that cannot be processed (e.g. from non-feed channels)
                .filter(PROCESSOR::canProcess);
    }

}