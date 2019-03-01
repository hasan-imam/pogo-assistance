package pogo.assistance.data.extraction.source.discord.flpokemap;

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

class FLPokeMapSpawnMessageProcessorTest {

    private final MessageProcessor<PokemonSpawn> processor = new FLPokeMapSpawnMessageProcessor();

    @ParameterizedTest
    @MethodSource(value = {"flpmAlertBot7Dms"})
    void process_ValidCases_ReturnsExpected(final Message message, final PokemonSpawn expected) {
        assertEquals(expected, processor.process(message).orElse(null));
    }

    private static Object[][] flpmAlertBot7Dms() {
        return new Object[][] {
                new Object[] {
                        mockMessageWithContent(
                                "[Orlando] Glameow",
                                "**Available until: 22:40:07 (22m 25s)**\n" +
                                        "\n" +
                                        "Weather: Partly Cloudy\n" +
                                        "Lvl30+ IVs: 15A/15D/15S (100%)\n" +
                                        "Lvl30+ CP: 533 (lvl 20)\n" +
                                        "Lvl30+ Moveset: Quick Attack<:normal:511131143861436416> - Play Rough<:fairy:511131138266365953>\n" +
                                        "Gender: ♂, Height: 0.56, Weight: 5.29",
                                "http://maps.google.com/maps?q=28.55881632422255,-81.30920995052666",
                                "https://raw.githubusercontent.com/novabot-sprites/novabot-sprites/master/431.png?5"),
                        ImmutablePokemonSpawn.builder()
                                .from(WayPoint.of(28.55881632422255, -81.30920995052666))
                                .pokedexEntry(ImmutablePokedexEntry.builder().name("Glameow").id(431).gender(Gender.MALE).build())
                                .iv(100)
                                .cp(533)
                                .level(20)
                                .locationDescription("Orlando")
                                .build()
                },
                new Object[] {
                        mockMessageWithContent(
                                "[Lakeland] Flareon",
                                "**Available until: 10:08:25 (23m 58s)**\n" +
                                        "\n" +
                                        "Weather: None\n" +
                                        "Lvl30+ IVs: 15A/15D/15S (100%)\n" +
                                        "Lvl30+ CP: 1211 (lvl 14)\n" +
                                        "Lvl30+ Moveset: Fire Spin - Overheat\n" +
                                        "Gender: ♀, Height: 0.91, Weight: 21.17",
                                "http://maps.google.com/maps?q=28.04844609859285,-81.96017250685455",
                                "https://raw.githubusercontent.com/novabot-sprites/novabot-sprites/master/136.png?5"),
                        ImmutablePokemonSpawn.builder()
                                .from(WayPoint.of(28.04844609859285, -81.96017250685455))
                                .pokedexEntry(ImmutablePokedexEntry.builder().name("Flareon").id(136).gender(Gender.FEMALE).build())
                                .iv(100)
                                .cp(1211)
                                .level(14)
                                .locationDescription("Lakeland")
                                .build()
                },
                new Object[] {
                        mockMessageWithContent(
                                "[Winter Park] Beldum",
                                "Available until: 03:25:31 (22m 45s)\n" +
                                        "\n" +
                                        "Weather: None\n" +
                                        "Lvl30+ IVs: 15A/15D/15S (100%)\n" +
                                        "Lvl30+ CP: 72 (lvl 3)\n" +
                                        "Lvl30+ Moveset: Take Down - Struggle:normal:\n" +
                                        "Gender: ⚲, Height: 0.61, Weight: 89.05",
                                "http://maps.google.com/maps?q=53.48469169443342,-2.23511582540997",
                                "https://raw.githubusercontent.com/novabot-sprites/novabot-sprites/master/374.png"),
                        ImmutablePokemonSpawn.builder()
                                .from(WayPoint.of(53.48469169443342, -2.23511582540997))
                                .pokedexEntry(ImmutablePokedexEntry.builder().name("Beldum").id(374).gender(Gender.NONE).build())
                                .iv(100)
                                .cp(72)
                                .level(3)
                                .locationDescription("Winter Park")
                                .build()
                },
                new Object[] {
                        mockMessageWithContent(
                                "[Manchester] Dratini",
                                "**Available until: 09:10:09 (24m 57s)**",
                                "http://maps.google.com/maps?q=53.4766,-2.256032",
                                "https://raw.githubusercontent.com/novabot-sprites/novabot-sprites/master/147.png?5"),

                        null
//                        ImmutablePokemonSpawn.builder()
//                                .from(WayPoint.of(53.4766, -2.256032))
//                                .pokedexEntry(ImmutablePokedexEntry.builder().name("Dratini").id(147).gender(Gender.NONE).build())
//                                .locationDescription("Manchester")
//                                .build()
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