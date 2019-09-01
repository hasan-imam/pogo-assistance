package pogo.assistance.data.extraction.source.discord;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.TestAbortedException;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

import javax.security.auth.login.LoginException;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pogo.assistance.bot.di.DiscordEntityConstants.*;

class GenericSpawnMessageProcessorAzPoGoMapIntegrationTest {

    private static JDA jda;

    private static final MessageProcessor<PokemonSpawn> PROCESSOR = new GenericSpawnMessageProcessor();

    @BeforeAll
    static void setUp() throws LoginException, InterruptedException {
        jda = new JDABuilder(AccountType.CLIENT)
                .setToken(DiscordEntityConstants.AMY_USER_TOKEN)
                .build()
                .awaitReady();
    }

    @AfterAll
    static void tearDown() {
        Optional.ofNullable(jda).ifPresent(JDA::shutdown);
    }

    @ParameterizedTest
    @MethodSource(value = { "AzPoGoMapSpawnMessages" })
    void process_AzPoGoMapSpawnMessages_ReturnsExpected(final Message message) {
        final String failureMsgWithJumpUrl = "Failed to parse message: " + message.getJumpUrl();
        final PokemonSpawn pokemonSpawn = PROCESSOR.processWithoutThrowing(message)
                .orElseThrow(() -> new AssertionError(failureMsgWithJumpUrl));
        assertAll(failureMsgWithJumpUrl,
                () -> assertTrue(pokemonSpawn.getLevel().isPresent(), "missing level"),
                () -> assertTrue(pokemonSpawn.getCp().isPresent(), "missing cp"),
                () -> assertTrue(pokemonSpawn.getIv().isPresent()),
                () -> assertThat(pokemonSpawn.getPokedexEntry().getGender(), not(PokedexEntry.Gender.UNKNOWN)));

        final String channelName = message.getChannel().getName();
        if (channelName.contains("100iv")) {
            assertThat(failureMsgWithJumpUrl, pokemonSpawn.getIv().get(), equalTo(100.0));
        } else if (channelName.contains("98iv")) {
            assertThat(failureMsgWithJumpUrl, Math.round(pokemonSpawn.getIv().get()), greaterThanOrEqualTo(98L));
        } else if (channelName.contains("0iv")) {
            assertThat(failureMsgWithJumpUrl, pokemonSpawn.getIv().get(), greaterThanOrEqualTo(0.0));
        } else if (channelName.contains("unown")) {
            assertThat(failureMsgWithJumpUrl, pokemonSpawn.getPokedexEntry().getId(), equalTo(201));
        }
    }

    private static Stream<Message> AzPoGoMapSpawnMessages() {
        return jda.getGuildById(SERVER_ID_AZ_POGO_MAP).getCategories().stream()
                .filter(category -> CATEGORY_IDS_AZ_POGO_MAP.contains(category.getIdLong()))
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

    @Test
    void process_UnownSpawnMessage_ReturnsExpected() {
        jda.getGuildById(SERVER_ID_AZ_POGO_MAP)
                .getTextChannelById(599146522491486209L)
                // Fetch a specific message that has unwon spawn info with stats
                .getHistoryAround(599314949445124110L, 1)
                .queue(
                        messageHistory -> {
                            final Message message = messageHistory.getMessageById(599314949445124110L);
                            final String failureMsgWithJumpUrl = "Failed to parse message: " + message.getJumpUrl();
                            final PokemonSpawn pokemonSpawn = PROCESSOR.processWithoutThrowing(message)
                                    .orElseThrow(() -> new AssertionError(failureMsgWithJumpUrl));
                            assertAll(failureMsgWithJumpUrl,
                                    () -> assertTrue(pokemonSpawn.getLevel().isPresent(), "missing level"),
                                    () -> assertTrue(pokemonSpawn.getCp().isPresent(), "missing cp"),
                                    () -> assertTrue(pokemonSpawn.getIv().isPresent()),
                                    () -> assertThat(pokemonSpawn.getPokedexEntry().getGender(), not(PokedexEntry.Gender.UNKNOWN)));
                        },
                        throwable -> {
                            throw new TestAbortedException("Test message could not be retrieved", throwable);
                        });
    }

}