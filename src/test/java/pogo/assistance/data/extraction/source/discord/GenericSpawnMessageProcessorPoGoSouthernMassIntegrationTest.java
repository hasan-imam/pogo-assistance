package pogo.assistance.data.extraction.source.discord;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

import javax.security.auth.login.LoginException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pogo.assistance.bot.di.DiscordEntityConstants.*;

class GenericSpawnMessageProcessorPoGoSouthernMassIntegrationTest {

    private static JDA jda;

    private static final MessageProcessor<PokemonSpawn> PROCESSOR = new GenericSpawnMessageProcessor();
    private static final Set<String> PROCESSED_CHANNEL_NAMES = new LinkedHashSet<>();

    @BeforeAll
    static void setUp() throws LoginException, InterruptedException {
        jda = new JDABuilder(AccountType.CLIENT)
                .setToken(DiscordEntityConstants.ALEXA_USER_TOKEN)
                .build()
                .awaitReady();
    }

    @AfterAll
    static void tearDown() {
        Optional.ofNullable(jda).ifPresent(JDA::shutdown);
        System.out.println("Processed channels: " + PROCESSED_CHANNEL_NAMES.stream()
                .map(name -> String.format("%n    ✓ %s", name))
                .collect(Collectors.joining()));
    }

    @ParameterizedTest
    @MethodSource(value = {"poGoSouthernMassSpawnMessages"})
    void process_TallyPokemonHuntersSpawnMessages_ReturnsExpected(final Message message) {
        final String failureMsgWithJumpUrl = "Failed to parse message: " + message.getJumpUrl();
        PROCESSED_CHANNEL_NAMES.add(message.getChannel().getName());
        final PokemonSpawn pokemonSpawn = PROCESSOR.processWithoutThrowing(message)
                .orElseThrow(() -> new AssertionError(failureMsgWithJumpUrl));
        assertAll(failureMsgWithJumpUrl,
                () -> assertTrue(pokemonSpawn.getLevel().isPresent(), "missing level"),
                () -> assertTrue(pokemonSpawn.getCp().isPresent(), "missing cp"),
                () -> assertTrue(pokemonSpawn.getIv().isPresent(), "missing iv"),
                () -> assertTrue(pokemonSpawn.getDespawnTime().isPresent(), "missing despawn time"),
                () -> assertThat(pokemonSpawn.getPokedexEntry().getGender(), not(PokedexEntry.Gender.UNKNOWN)));
        final String channelName = message.getChannel().getName();
        final Double ivRoundedUp = Math.ceil(pokemonSpawn.getIv().get());
        if (channelName.contains("100iv")) {
            assertThat(failureMsgWithJumpUrl, ivRoundedUp, equalTo(100.0));
        } else if (channelName.contains("96iv") && channelName.contains("15atk")) {
            assertThat(failureMsgWithJumpUrl, ivRoundedUp, greaterThanOrEqualTo(96.0));
        } else if (channelName.contains("0iv")) {
            assertThat(failureMsgWithJumpUrl, ivRoundedUp, equalTo(0.0));
        }
    }

    private static Stream<Message> poGoSouthernMassSpawnMessages() {
        return jda.getGuildById(SERVER_ID_POGO_SOUTHERN_MASS).getCategories().stream()
                .filter(category -> CATEGORY_IDS_POGO_SOUTHERN_MASS.contains(category.getIdLong()))
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