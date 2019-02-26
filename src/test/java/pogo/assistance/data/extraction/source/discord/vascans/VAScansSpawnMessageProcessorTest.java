package pogo.assistance.data.extraction.source.discord.vascans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.jenetics.jpx.WayPoint;
import java.util.Collections;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import pogo.assistance.data.extraction.source.discord.MessageProcessor;
import pogo.assistance.data.model.pokemon.ImmutablePokedexEntry;
import pogo.assistance.data.model.pokemon.ImmutablePokemonSpawn;
import pogo.assistance.data.model.pokemon.PokedexEntry.Gender;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

class VAScansSpawnMessageProcessorTest {

    private final MessageProcessor<PokemonSpawn> processor = new VAScansSpawnMessageProcessor();

    @ParameterizedTest
    @MethodSource(value = {"vaScans100ivMessages"})
    void process_ValidCases_ReturnsExpected(final Message message, final PokemonSpawn expected) {
        assertEquals(expected, processor.process(message).get());
    }

    private static Object[][] vaScans100ivMessages() {
        return new Object[][] {
                new Object[] {
                        mockMessageWithContent(
                                "Squirtle (15/15/15) L31 CP:824 ♂",
                                "IV:100.00% Boost: none\n" +
                                        "Despawn: 2:11:14 PM\n" +
                                        "Remaining: 25m 5s \n" +
                                        "This timer should be exact!\n" +
                                        "37.53499, -77.4599   \n" +
                                        "  [Google Maps](https://www.google.com/maps/search/?api=1&query=37.5349949208584,-77.4599340276508)",
                                "https://raw.githubusercontent.com/seehuge/prdmicons/master/pokemon_icon_152_00.png"),
                        ImmutablePokemonSpawn.builder()
                                .from(WayPoint.of(37.5349949208584, -77.4599340276508))
                                .pokedexEntry(ImmutablePokedexEntry.builder().name("Squirtle").id(152).gender(Gender.MALE).build())
                                .iv(100)
                                .cp(824)
                                .level(31)
                                .build()
                },
                new Object[] {
                        mockMessageWithContent(
                                "Porygon (15/15/15) L28 CP:1376 ⭕",
                                "IV:100.00% Boost: none\n" +
                                        "Despawn: 1:50:17 PM\n" +
                                        "Remaining: 27m 23s \n" +
                                        "This timer should be exact!\n" +
                                        "37.50329, -77.4763 East Belt Boulevard 404 \n" +
                                        "  [Google Maps](https://www.google.com/maps/search/?api=1&query=37.5032968319611,-77.4763366993049)",
                                "https://raw.githubusercontent.com/seehuge/prdmicons/master/pokemon_icon_137_00.png"),
                        ImmutablePokemonSpawn.builder()
                                .from(WayPoint.of(37.5032968319611, -77.4763366993049))
                                .pokedexEntry(ImmutablePokedexEntry.builder().name("Porygon").id(137).gender(Gender.NONE).build())
                                .iv(100)
                                .cp(1376)
                                .level(28)
                                .build()
                },
                new Object[] {
                        mockMessageWithContent(
                                "Snubbull (15/15/15) L9 CP:316",
                                "IV:100.00% Boost: cloudy\n" +
                                        "Despawn: 10:56:13 PM\n" +
                                        "Remaining: 16m 48s \n" +
                                        "This timer is a guesstimate!\n" +
                                        "37.55900, -77.4713 Park Avenue 2753 \n" +
                                        "  [Google Maps](https://www.google.com/maps/search/?api=1&query=37.5590068664644,-77.4713175517577)",
                                "http://www.pokestadium.com/sprites/xy/snubbull.gif"),
                        ImmutablePokemonSpawn.builder()
                                .from(WayPoint.of(37.5590068664644, -77.4713175517577))
                                .pokedexEntry(ImmutablePokedexEntry.builder().name("Snubbull").id(-1).gender(Gender.NONE).build())
                                .iv(100)
                                .cp(316)
                                .level(9)
                                .build()
                },
                new Object[] {
                        mockMessageWithContent(
                                "Ponyta (15/15/15) L7 CP:331",
                                "IV:100.00% Weather: sunny\n" +
                                        "Approx despawn: 10:03:15 AM\n" +
                                        "Approx time left: 17m 58s \n" +
                                        "37.53238, -77.4220 East Grace Street  \n" +
                                        "  [Google Maps](https://www.google.com/maps/search/?api=1&query=37.532389113078,-77.422010687266)",
                                "http://www.pokestadium.com/sprites/xy/ponyta.gif"),
                        ImmutablePokemonSpawn.builder()
                                .from(WayPoint.of(37.532389113078, -77.422010687266))
                                .pokedexEntry(ImmutablePokedexEntry.builder().name("Ponyta").id(-1).gender(Gender.NONE).build())
                                .iv(100)
                                .cp(331)
                                .level(7)
                                .build()
                },
        };
    }

    private static Message mockMessageWithContent(
            final String embedTitle,
            final String embedDescription,
            final String thumbnailUrl) {
        final Message message = mock(Message.class, Answers.RETURNS_DEEP_STUBS);
        when(message.getAuthor().isBot()).thenReturn(true);

        final MessageEmbed messageEmbed = mock(MessageEmbed.class, Answers.RETURNS_DEEP_STUBS);
        when(messageEmbed.getTitle()).thenReturn(embedTitle);
        when(messageEmbed.getDescription()).thenReturn(embedDescription);
        when(messageEmbed.getThumbnail().getUrl()).thenReturn(thumbnailUrl);
        when(message.getEmbeds()).thenReturn(Collections.singletonList(messageEmbed));

        return message;
    }

}