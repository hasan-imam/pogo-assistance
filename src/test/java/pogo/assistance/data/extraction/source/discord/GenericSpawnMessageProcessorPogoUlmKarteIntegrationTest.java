package pogo.assistance.data.extraction.source.discord;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static pogo.assistance.bot.di.DiscordEntityConstants.CATEGORY_IDS_POGO_ULM_KARTE_FEEDS;
import static pogo.assistance.bot.di.DiscordEntityConstants.SERVER_ID_POGO_ULM_KARTE;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import javax.security.auth.login.LoginException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

class GenericSpawnMessageProcessorPogoUlmKarteIntegrationTest {

    private static JDA jda;

    private static final MessageProcessor<PokemonSpawn> PROCESSOR = new GenericSpawnMessageProcessor();

    @BeforeAll
    static void setUp() throws LoginException, InterruptedException {
        jda = new JDABuilder(AccountType.CLIENT)
                .setToken(DiscordEntityConstants.POGO_HERO_USER_TOKEN)
                .build()
                .awaitReady();
    }

    @AfterAll
    static void tearDown() {
        Optional.ofNullable(jda).ifPresent(JDA::shutdown);
    }

    @ParameterizedTest
    @MethodSource(value = { "PogoUlmKarteSpawnMessages" })
    void process_LVRM_ReturnsExpected(final Message message) {
        final String failureMsgWithJumpUrl = "Failed to parse message: " + message.getJumpUrl();
        final Optional<PokemonSpawn> result = PROCESSOR.processWithoutThrowing(message);
        assumeTrue(result.isPresent(), "Skipped spawn with missing iv/cp/level: " + message.getJumpUrl());
        final PokemonSpawn pokemonSpawn = result.get();
        assertAll(failureMsgWithJumpUrl,
                () -> assertTrue(pokemonSpawn.getLevel().isPresent(), "missing level"),
                () -> assertTrue(pokemonSpawn.getCp().isPresent(), "missing cp"),
                () -> assertTrue(pokemonSpawn.getIv().isPresent(), "missing iv"),
                () -> assertThat(pokemonSpawn.getPokedexEntry().getGender(), not(PokedexEntry.Gender.UNKNOWN)));

        final String channelName = message.getChannel().getName();
        final Double ivRoundedUp = Math.ceil(pokemonSpawn.getIv().get());
        if (channelName.contains("100")) {
            assertThat(failureMsgWithJumpUrl, ivRoundedUp, equalTo(100.0));
        } else if (channelName.contains("95-99")) {
            assertThat(failureMsgWithJumpUrl, ivRoundedUp, greaterThanOrEqualTo(95.0));
        } else if (channelName.contains("90-95")) {
            assertThat(failureMsgWithJumpUrl, ivRoundedUp, greaterThanOrEqualTo(90.0));
        }
    }

    private static Stream<Message> PogoUlmKarteSpawnMessages() {
        return jda.getGuildById(SERVER_ID_POGO_ULM_KARTE).getCategories().stream()
                .filter(category -> CATEGORY_IDS_POGO_ULM_KARTE_FEEDS.contains(category.getIdLong()))
                .map(Category::getTextChannels)
                .flatMap(Collection::stream)
                .map(messageChannel -> {
                    try {
                        return MessageStream.lookbackMessageStream(messageChannel);
                    } catch (final InsufficientPermissionException e) {
                        // There seems to be a channel that gets listed in this operation but we don't have permission to view
                        System.err.println("Don't have permission to lookup message history for channel: " + messageChannel.getName());
                        return Stream.<Message>empty();
                    }
                })
                // Take some message from all channels, even the non-feed ones
                .flatMap(regionalMessageStream -> regionalMessageStream.limit(500))
                // Filter out messages that cannot be processed (e.g. from non-feed channels)
                .filter(PROCESSOR::canProcess);
    }

}