package pogo.assistance.data.extraction.source.discord.pokefairy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import io.jenetics.jpx.WayPoint;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import pogo.assistance.data.extraction.source.discord.MessageProcessor;
import pogo.assistance.data.model.ImmutableSourceMetadata;
import pogo.assistance.data.model.pokemon.ImmutablePokemonSpawn;
import pogo.assistance.data.model.pokemon.Pokedex;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokedexEntry.Gender;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

class PokeFairySpawnMessageProcessorTest {

    private final MessageProcessor<PokemonSpawn> processor = new PokeFairySpawnMessageProcessor();

    @ParameterizedTest
    @MethodSource(value = {"neoSf90ivPosts"})
    void process_ValidCases_ReturnsExpected(final Message message, final PokemonSpawn expected) {
        assertEquals(expected, processor.process(message).orElse(null));
    }

    private static Object[][] neoSf90ivPosts() {
        return new Object[][] {
                new Object[] {
                        mockMessageWithContent(
                                "Porygon  95.56% L15 - San Francisco",
                                "730CP | Gender:⚲ (14/15/14)\n" +
                                        "<:electric:501878400395378688> Charge Beam/ <:electric:501878400395378688> Zap Cannon\n" +
                                        "**Weather Condition:** Partly Cloudy\n" +
                                        "**Available until:** 04:52:16 PM (23m 41s)\n" +
                                        "****\n" +
                                        "\n" +
                                        "1000 Great Highway, San Francisco\n" +
                                        "37.76933098418024,-122.51044439939496\n" +
                                        "[Google Maps](http://maps.google.com/maps?q=37.76933098418024,-122.51044439939496)\n" +
                                        "\n" +
                                        "Reply `!help` for list of FairyBot commands.",
                                "http://maps.google.com/maps?q=37.76933098418024,-122.51044439939496",
                                "https://raw.githubusercontent.com/novabot-sprites/novabot-sprites/master/137.png?5"),
                        ImmutablePokemonSpawn.builder()
                                .from(WayPoint.of(37.76933098418024, -122.51044439939496))
                                .pokedexEntry(Pokedex.getPokedexEntryFor(137, Gender.NONE).get())
                                .iv(95.56)
                                .cp(730)
                                .level(15)
                                .locationDescription("1000 Great Highway, San Francisco")
                                .sourceMetadata(ImmutableSourceMetadata.builder().sourceName("someGuildName").build())
                                .build()
                },
                new Object[] {
                        mockMessageWithContent( // alolan geodude
                                "Geodude  91.12% L20 - San Francisco",
                                "726CP | Gender:♂ (14/14/13)\n" +
                                        "<:electric:501878400395378688> Volt Switch/ <:electric:501878400395378688> Thunderbolt\n" +
                                        "**Weather Condition:** Partly Cloudy\n" +
                                        "**Available until:** 05:32:24 PM (22m 49s)\n" +
                                        "****\n" +
                                        "\n" +
                                        "117 Ashbury Street, San Francisco\n" +
                                        "37.77423857038744,-122.44791810650042\n" +
                                        "[Google Maps](http://maps.google.com/maps?q=37.77423857038744,-122.44791810650042)\n" +
                                        "\n" +
                                        "Reply `!help` for list of FairyBot commands.",
                                "http://maps.google.com/maps?q=37.77423857038744,-122.44791810650042",
                                "https://raw.githubusercontent.com/novabot-sprites/novabot-sprites/master/74-68.png?5"),
                        ImmutablePokemonSpawn.builder()
                                .from(WayPoint.of(37.77423857038744, -122.44791810650042))
                                .pokedexEntry(Pokedex.getPokedexEntryFor(74, Gender.MALE, Collections.singleton(PokedexEntry.Form.ALOLAN)).get())
                                .iv(91.12)
                                .cp(726)
                                .level(20)
                                .locationDescription("117 Ashbury Street, San Francisco")
                                .sourceMetadata(ImmutableSourceMetadata.builder().sourceName("someGuildName").build())
                                .build()
                },
                new Object[] {
                        mockMessageWithContent( // castform rainy
                                "Castform Rainy 93.34% L27 - San Francisco",
                                "1248CP | Gender:♀ (15/14/13)\n" +
                                        "<:normal:501878406699548682> Tackle/ <:electric:501878400395378688> Thunder\n" +
                                        "**Weather Condition:** Rain\n" +
                                        "**Available until:** 12:16:11 AM (23m 09s)\n" +
                                        "****\n" +
                                        "\n" +
                                        "233 Post Street, San Francisco\n" +
                                        "37.78826371099465,-122.40571164447206\n" +
                                        "[Google Maps](http://maps.google.com/maps?q=37.78826371099465,-122.40571164447206)\n" +
                                        "\n" +
                                        "Reply `!help` for list of FairyBot commands.",
                                "http://maps.google.com/maps?q=37.78826371099465,-122.40571164447206",
                                "https://raw.githubusercontent.com/novabot-sprites/novabot-sprites/master/351-31.png?5"),
                        ImmutablePokemonSpawn.builder()
                                .from(WayPoint.of(37.78826371099465, -122.40571164447206))
                                .pokedexEntry(Pokedex.getPokedexEntryFor(351, Gender.FEMALE, Collections.singleton(PokedexEntry.Form.CASTFORM_RAINY)).get())
                                .iv(93.34)
                                .cp(1248)
                                .level(27)
                                .locationDescription("233 Post Street, San Francisco")
                                .sourceMetadata(ImmutableSourceMetadata.builder().sourceName("someGuildName").build())
                                .build()
                },
                new Object[] {
                        mockMessageWithContent( // nidoran male
                                "Nidoranm  91.12% L34 - San Francisco",
                                "773CP | Gender:♂ (15/13/13)\n" +
                                        "<:flying:501878402710503425> Peck/ <:normal:501878406699548682> Body Slam\n" +
                                        "**Weather Condition:** Cloudy\n" +
                                        "**Available until:** 06:40:35 PM (08m 48s)\n" +
                                        "****\n" +
                                        "\n" +
                                        "1199 Great Meadow, San Francisco\n" +
                                        "37.76781713736468,-122.46852850226345\n" +
                                        "[Google Maps](http://maps.google.com/maps?q=37.76781713736468,-122.46852850226345)\n" +
                                        "\n" +
                                        "Reply `!help` for list of FairyBot commands.",
                                "http://maps.google.com/maps?q=37.76781713736468,-122.46852850226345",
                                "https://raw.githubusercontent.com/novabot-sprites/novabot-sprites/master/32.png?5"),
                        ImmutablePokemonSpawn.builder()
                                .from(WayPoint.of(37.76781713736468, -122.46852850226345))
                                .pokedexEntry(Pokedex.getPokedexEntryFor(32, Gender.MALE).get())
                                .iv(91.12)
                                .cp(773)
                                .level(34)
                                .locationDescription("1199 Great Meadow, San Francisco")
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
        when(message.getGuild().getName()).thenReturn("someGuildName");
        when(message.getChannelType()).thenReturn(ChannelType.TEXT);
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