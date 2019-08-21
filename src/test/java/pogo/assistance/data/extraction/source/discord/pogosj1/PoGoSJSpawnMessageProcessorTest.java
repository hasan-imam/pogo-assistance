package pogo.assistance.data.extraction.source.discord.pogosj1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.jenetics.jpx.WayPoint;
import java.util.Collections;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import pogo.assistance.data.extraction.source.discord.MessageProcessor;
import pogo.assistance.data.model.ImmutableSourceMetadata;
import pogo.assistance.data.model.pokemon.ImmutablePokemonSpawn;
import pogo.assistance.data.model.pokemon.Pokedex;
import pogo.assistance.data.model.pokemon.PokedexEntry.Gender;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

/**
 * TODO: Cover these cases:
 *  - Ditto: https://discordapp.com/channels/346733317699141632/348769770671308800/421408933328977921
 *  - Twitter messages
 *  - Unown: https://discordapp.com/channels/346733317699141632/346733581814726657/530251661801816064
 *  - Post with missing IV/CP/Level
 *  - https://discordapp.com/channels/346733317699141632/346733581814726657/530371155920420884
 */
class PoGoSJSpawnMessageProcessorTest {

    private final MessageProcessor<PokemonSpawn> processor = new PoGoSJSpawnMessageProcessorV2();

    @ParameterizedTest
    @MethodSource(value = {"pogosj100ivMessages"})
    void process_ValidCases_ReturnsExpected(final Message message, final PokemonSpawn expected) {
        assertEquals(expected, processor.process(message).get());
        assertEquals(new PoGoSJSpawnMessageProcessor().process(message).get(), processor.process(message).get());
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
                                .pokedexEntry(Pokedex.getPokedexEntryFor(10, Gender.MALE).get())
                                .iv(100)
                                .cp(393)
                                .level(33)
                                .locationDescription("4815 Corte De Avellano (Erikson, San Jose)")
                                .sourceMetadata(ImmutableSourceMetadata.builder().sourceName("someGuildName").build())
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
                                .pokedexEntry(Pokedex.getPokedexEntryFor(337, Gender.NONE).get())
                                .iv(100)
                                .cp(1064)
                                .level(16)
                                .locationDescription("5286 Alan Avenue (Cambrian, San Jose)")
                                .sourceMetadata(ImmutableSourceMetadata.builder().sourceName("someGuildName").build())
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
                                .pokedexEntry(Pokedex.getPokedexEntryFor(351, Gender.FEMALE).get())
                                .iv(100)
                                .cp(1445)
                                .level(32)
                                .locationDescription("475 Hyde Park Drive (Edenvale, San Jose)")
                                .sourceMetadata(ImmutableSourceMetadata.builder().sourceName("someGuildName").build())
                                .build()
                },
                new Object[] {
                        mockMessageWithContent(
                                "Nidoran♂ ♂ 0.0% CP:1 (L1) San Jose",
                                "Poison Sting/Sludge Bomb till 09:05:29pm (verified) (18m 54s).\n" +
                                        "Weather Boost: Cloudy ☁️ \n" +
                                        "Location:unkn Lake Cunningham Inner Lake Path (East San Jose, San Jose) \n" +
                                        "Coordinates: 37.33702,-121.80461\n" +
                                        "http://maps.google.com/maps?q=37.3370226766417,-121.804612027665",
                                "http://maps.google.com/maps?q=37.3370226766417,-121.804612027665",
                                "https://raw.githubusercontent.com/novabot-sprites/novabot-sprites/master/32.png"),
                        ImmutablePokemonSpawn.builder()
                                .from(WayPoint.of(37.3370226766417, -121.804612027665))
                                .pokedexEntry(Pokedex.getPokedexEntryFor(32, Gender.MALE).get())
                                .iv(0)
                                .cp(1)
                                .level(1)
                                .locationDescription("unkn Lake Cunningham Inner Lake Path (East San Jose, San Jose)")
                                .sourceMetadata(ImmutableSourceMetadata.builder().sourceName("someGuildName").build())
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
        when(message.getGuild().getName()).thenReturn("someGuildName");
        when(message.getChannelType()).thenReturn(ChannelType.TEXT);

        final MessageEmbed messageEmbed = mock(MessageEmbed.class, Answers.RETURNS_DEEP_STUBS);
        when(messageEmbed.getTitle()).thenReturn(embedTitle);
        when(messageEmbed.getDescription()).thenReturn(embedDescription);
        when(messageEmbed.getUrl()).thenReturn(embedUrl);
        when(messageEmbed.getThumbnail().getUrl()).thenReturn(thumbnailUrl);
        when(message.getEmbeds()).thenReturn(Collections.singletonList(messageEmbed));

        return message;
    }
}