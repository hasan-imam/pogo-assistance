package pogo.assistance.data.extraction.source.discord.pogosj1;

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

class PoGoSJSpawnMessageProcessorTest {

    private final MessageProcessor<PokemonSpawn> processor = new PoGoSJSpawnMessageProcessor();

    @ParameterizedTest
    @MethodSource(value = {"pogosj100ivMessages"})
    void process_ValidCases_ReturnsExpected(final Message message, final PokemonSpawn expected) {
        assertEquals(expected, processor.process(message).get());
    }

    private static Object[][] pogosj100ivMessages() {
        return new Object[][] {
                new Object[] {
                        mockMessageWithContent(
                                "Caterpie ♂ 100.0% CP:393 (L33) San Jose",
                                "Bug Bite/Struggle till 09:51:32pm (verified) (19m 53s).\n" +
                                        "Weather Boost: Rain ☔️ \n" +
                                        "Location:4815 Corte De Avellano (Erikson, San Jose) \n" +
                                        "Coordinates: 37.26267,-121.86627\n" +
                                        "http://maps.google.com/maps?q=37.2626688074602,-121.866267766066",
                                "http://maps.google.com/maps?q=37.2626688074602,-121.866267766066",
                                "https://raw.githubusercontent.com/novabot-sprites/novabot-sprites/master/10.png"),
                        ImmutablePokemonSpawn.builder()
                                .from(WayPoint.of(37.2626688074602, -121.866267766066))
                                .pokedexEntry(ImmutablePokedexEntry.builder().name("Caterpie").id(10).gender(Gender.MALE).build())
                                .iv(100)
                                .cp(393)
                                .level(33)
                                .locationDescription("4815 Corte De Avellano (Erikson, San Jose)")
                                .build()
                },
                new Object[] {
                        mockMessageWithContent(
                                "Lunatone ⚲ 100.0% CP:1064 (L16) San Jose",
                                "Rock Throw/Rock Slide till 03:43:22pm (21m 49s).\n" +
                                        "Weather Boost: None \n" +
                                        "Location:5286 Alan Avenue (Cambrian, San Jose) \n" +
                                        "Coordinates: 37.24202,-121.90780\n" +
                                        "http://maps.google.com/maps?q=37.2420169514537,-121.907797100189",
                                "http://maps.google.com/maps?q=37.2420169514537,-121.907797100189",
                                "https://raw.githubusercontent.com/novabot-sprites/novabot-sprites/master/337.png"),
                        ImmutablePokemonSpawn.builder()
                                .from(WayPoint.of(37.2420169514537, -121.907797100189))
                                .pokedexEntry(ImmutablePokedexEntry.builder().name("Lunatone").id(337).gender(Gender.NONE).build())
                                .iv(100)
                                .cp(1064)
                                .level(16)
                                .locationDescription("5286 Alan Avenue (Cambrian, San Jose)")
                                .build()
                },
                new Object[] {
                        mockMessageWithContent(
                                "Castform ♀ 100.0% CP:1445 (L32) San Jose",
                                "Tackle/Hydro Pump till 07:00:07pm (verified) (26m 42s).\n" +
                                        "Weather Boost: Rain ☔️ \n" +
                                        "Location:475 Hyde Park Drive (Edenvale, San Jose) \n" +
                                        "Coordinates: 37.26254,-121.84937\n" +
                                        "http://maps.google.com/maps?q=37.2625366115163,-121.849367001618",
                                "http://maps.google.com/maps?q=37.2625366115163,-121.849367001618",
                                "https://raw.githubusercontent.com/novabot-sprites/novabot-sprites/master/351-31.png?5"),
                        ImmutablePokemonSpawn.builder()
                                .from(WayPoint.of(37.2625366115163, -121.849367001618))
                                .pokedexEntry(ImmutablePokedexEntry.builder().name("Castform").id(351).gender(Gender.FEMALE).build())
                                .iv(100)
                                .cp(1445)
                                .level(32)
                                .locationDescription("475 Hyde Park Drive (Edenvale, San Jose)")
                                .build()
                }
        };
    }

    private static Message mockMessageWithContent(
            final String embedTitle,
            final String embedDescription,
            final String embedUrl,
            final String thumbnailUrl) {
        final Message message = mock(Message.class, Answers.RETURNS_DEEP_STUBS);
        when(message.getAuthor().isBot()).thenReturn(true);

        final MessageEmbed messageEmbed = mock(MessageEmbed.class, Answers.RETURNS_DEEP_STUBS);
        when(messageEmbed.getTitle()).thenReturn(embedTitle);
        when(messageEmbed.getDescription()).thenReturn(embedDescription);
        when(messageEmbed.getUrl()).thenReturn(embedUrl);
        when(messageEmbed.getThumbnail().getUrl()).thenReturn(thumbnailUrl);
        when(message.getEmbeds()).thenReturn(Collections.singletonList(messageEmbed));

        return message;
    }
}