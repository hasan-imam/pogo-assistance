package pogo.assistance.data.extraction.source.discord;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

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
import net.dv8tion.jda.core.entities.Message;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.data.model.pokemon.PokedexEntry.Gender;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

class GenericSpawnMessageProcessorTPFFairymapsIntegrationTest {

    private static JDA jda;

    private static final MessageProcessor<PokemonSpawn> PROCESSOR = new GenericSpawnMessageProcessor();

    @BeforeAll
    static void setUp() throws LoginException, InterruptedException {
        jda = new JDABuilder(AccountType.CLIENT)
                .setToken(DiscordEntityConstants.NINERS_USER_TOKEN)
                .build()
                .awaitReady();
    }

    @AfterAll
    static void tearDown() {
        Optional.ofNullable(jda).ifPresent(JDA::shutdown);
    }

    @ParameterizedTest
    @MethodSource(value = {"neoSf90ivPosts"})
    void process_NeoSf90IVChannelMessages_ReturnsExpected(final Message message) {
        final String failureMsgWithJumpUrl = "Failed to parse message: " + message.getJumpUrl();
        final PokemonSpawn pokemonSpawn = PROCESSOR.processWithoutThrowing(message)
                .orElse(null);
        assumeTrue(pokemonSpawn != null);
        assertAll(failureMsgWithJumpUrl,
                () -> assertTrue(pokemonSpawn.getIv().orElse(-1.0) >= 90),
                () -> assertTrue(pokemonSpawn.getCp().isPresent()),
                () -> assertTrue(pokemonSpawn.getLevel().isPresent()),
                () -> assertNotSame(pokemonSpawn.getPokedexEntry().getGender(), Gender.UNKNOWN),
                () -> assertTrue(pokemonSpawn.getPokedexEntry().getId() > 0),
                () -> assertFalse(pokemonSpawn.getPokedexEntry().getName().isEmpty()),
                () -> assertTrue(!pokemonSpawn.getPokedexEntry().getName().equals("Unown") || !pokemonSpawn.getPokedexEntry().getForms().isEmpty()));
    }

    private static Stream<Message> neoSf90ivPosts() {
        return MessageStream.lookbackMessageStream(jda.getTextChannelById(DiscordEntityConstants.CHANNEL_ID_TPF_FAIRYMAPS_NEOSF90IV))
                .filter(PROCESSOR::canProcess)
                .limit(16000);
    }

}