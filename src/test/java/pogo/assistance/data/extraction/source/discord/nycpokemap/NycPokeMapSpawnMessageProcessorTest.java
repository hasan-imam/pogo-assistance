package pogo.assistance.data.extraction.source.discord.nycpokemap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.jenetics.jpx.WayPoint;
import net.dv8tion.jda.core.entities.Message;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import pogo.assistance.data.extraction.source.discord.MessageProcessor;
import pogo.assistance.data.model.pokemon.ImmutablePokedexEntry;
import pogo.assistance.data.model.pokemon.ImmutablePokemonSpawn;
import pogo.assistance.data.model.pokemon.PokedexEntry.Gender;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

class NycPokeMapSpawnMessageProcessorTest {

    private final MessageProcessor<PokemonSpawn> processor = new NycPokeMapSpawnMessageProcessor();

    @ParameterizedTest
    @MethodSource(value = {"nycValid100ivMessages"})
    void process_ValidCases_ReturnsExpected(final Message message, final PokemonSpawn expected) {
        assertEquals(expected, processor.process(message).get());
    }

    private static Object[][] nycValid100ivMessages() {
        return new Object[][] {
                new Object[] {
                        mockMessageWithContent("[Brownsville] Unown - K -  (97%) - (CP: 436) - (Level: 15)\n" +
                                "\n" +
                                "Until: 08:46:22AM (26:18 left) \n" +
                                "Weather boosted: Windy \n" +
                                "L30+ IV: 15 - 14 - 15  (97%) \n" +
                                "L30+ Moveset: Hidden Power - Struggle \n" +
                                "L30+ CP: 436 (Level 15)\n" +
                                "Address: 167 Livonia Ave\n" +
                                "Gender: \n" +
                                "Map: https://nycpokemap.com/#40.66286302,-73.91315637\n" +
                                "Google Map: https://maps.google.com/maps?q=40.66286302,-73.91315637"),
                        ImmutablePokemonSpawn.builder()
                                .from(WayPoint.of(40.66286302, -73.91315637))
                                .pokedexEntry(ImmutablePokedexEntry.builder().name("Unown K").id(-1).gender(Gender.NONE).build())
                                .iv(97)
                                .cp(436)
                                .level(15)
                                .locationDescription("Brownsville")
                                .build()
                },
                new Object[] {
                        mockMessageWithContent("\\[Mariners Harbor] Abra (97%) - (CP: 488) - (Level: 15)\n" +
                                "\n" +
                                "Until: 08:49:05AM (27:03 left) \n" +
                                "Weather boosted: Windy \n" +
                                "L30+ IV: 15 - 15 - 14  (97%) \n" +
                                "L30+ Moveset: Zen Headbutt - Shadow Ball \n" +
                                "L30+ CP: 488 (Level 15)\n" +
                                "Address: 164 Van Pelt Ave\n" +
                                "Gender: Male\n" +
                                "Map: https://nycpokemap.com/#40.63222953,-74.15542049\n" +
                                "Google Map: https://maps.google.com/maps?q=40.63222953,-74.15542049"),
                        ImmutablePokemonSpawn.builder()
                                .from(WayPoint.of(40.63222953, -74.15542049))
                                .pokedexEntry(ImmutablePokedexEntry.builder().name("Abra").id(-1).gender(Gender.NONE).build())
                                .iv(97)
                                .cp(488)
                                .level(15)
                                .locationDescription("Mariners Harbor")
                                .build()
                },
                new Object[] {
                        mockMessageWithContent("Yanma - Female - (100%) - (CP: 420) - (Level: 10)\n" +
                                "\n" +
                                "Until: 10:18:18AM (16:37 left)\n" +
                                "Weather boost: Rainy\n" +
                                "L30+ IV: 15 - 15 - 15 (100%) \n" +
                                "L30+ Moveset: Wing Attack - Ancient Power \n" +
                                "L30+ CP: 420 (Level: 10)\n" +
                                "Address: 501 Jerome Ave\n" +
                                "Gender: Female\n" +
                                "Map: https://nycpokemap.com/#40.88774859,-73.87769538\n" +
                                "Google Maps: https://maps.google.com/maps?q=40.88774859,-73.87769538"),
                        ImmutablePokemonSpawn.builder()
                                .from(WayPoint.of(40.88774859, -73.87769538))
                                .pokedexEntry(ImmutablePokedexEntry.builder().name("Yanma").id(-1).gender(Gender.FEMALE).build())
                                .iv(100)
                                .cp(420)
                                .level(10)
                                .build()
                },
                new Object[] {
                        mockMessageWithContent("[Midtown] Nidoran♂ - Male - (73%) - (CP: 739) - (Level: 35)\n" +
                                "\n" +
                                "Until: 05:37:33AM (19:51 left)\n" +
                                "Weather boost: Cloudy\n" +
                                "L30+ IV: 12 - 6 - 15 (73%) \n" +
                                "L30+ Moveset: Peck - Horn Attack \n" +
                                "L30+ CP: 739 (Level: 35)\n" +
                                "Address: 56 W 45th St\n" +
                                "Gender: Male\n" +
                                "Map: https://nycpokemap.com/#40.75605381,-73.9818551\n" +
                                "Google Maps: https://maps.google.com/maps?q=40.75605381,-73.9818551"),
                        ImmutablePokemonSpawn.builder()
                                .from(WayPoint.of(40.75605381, -73.9818551))
                                .pokedexEntry(ImmutablePokedexEntry.builder().name("Nidoran♂").id(-1).gender(Gender.MALE).build())
                                .iv(73)
                                .cp(739)
                                .level(35)
                                .locationDescription("Midtown")
                                .build()
                },
                new Object[] {
                        mockMessageWithContent("[LIC] Beldum - (77%) - (CP: 558) - (Level: 21)\n" +
                                "\n" +
                                "Until: 13:10:08PM (19:38 left)\n" +
                                "Weather boost: Windy\n" +
                                "L30+ IV: 13 - 12 - 10 (77%) \n" +
                                "L30+ Moveset: Take Down - Struggle \n" +
                                "L30+ CP: 558 (Level: 21)\n" +
                                "Address: 31-15 36th St\n" +
                                "Gender: \n" +
                                "Map: https://nycpokemap.com/#40.76193535,-73.91958643\n" +
                                "Google Maps: https://maps.google.com/maps?q=40.76193535,-73.91958643"),
                        ImmutablePokemonSpawn.builder()
                                .from(WayPoint.of(40.76193535, -73.91958643))
                                .pokedexEntry(ImmutablePokedexEntry.builder().name("Beldum").id(-1).gender(Gender.NONE).build())
                                .iv(77)
                                .cp(558)
                                .level(21)
                                .locationDescription("LIC")
                                .build()
                },
                new Object[] {
                        mockMessageWithContent("Wigglytuff (0%) - (CP: 853)\n" +
                                "\n" +
                                "Until: 05:14:32PM (29:29 left) \n" +
                                "Weather boosted: None \n" +
                                "L30+ IV: 0 - 0 - 0  (0%) \n" +
                                "L30+ Moveset: Pound - Hyper Beam \n" +
                                "L30+ CP: 853 (Level 19)\n" +
                                "Address: 1057 Mora Pl\n" +
                                "Gender: Female\n" +
                                "Map: https://nycpokemap.com/#40.63872944,-73.70995033\n" +
                                "Google Map: https://maps.google.com/maps?q=40.63872944,-73.70995033"),
                        ImmutablePokemonSpawn.builder()
                                .from(WayPoint.of(40.63872944, -73.70995033))
                                .pokedexEntry(ImmutablePokedexEntry.builder().name("Wigglytuff").id(-1).gender(Gender.NONE).build())
                                .iv(0)
                                .cp(853)
                                .build()
                }
        };
    }

    private static Message mockMessageWithContent(final String contentStripped) {
        final Message message = mock(Message.class, Answers.RETURNS_DEEP_STUBS);
        when(message.getContentStripped()).thenReturn(contentStripped);
        when(message.getAuthor().isBot()).thenReturn(true);
        return message;
    }
}