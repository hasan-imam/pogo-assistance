package pogo.assistance.data.extraction.source.discord.wecatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.jenetics.jpx.WayPoint;
import java.util.Collections;
import java.util.Optional;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.data.extraction.source.discord.MessageProcessor;
import pogo.assistance.data.model.ImmutableSourceMetadata;
import pogo.assistance.data.model.pokemon.ImmutablePokemonSpawn;
import pogo.assistance.data.model.pokemon.Pokedex;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokedexEntry.Gender;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

class WeCatchSpawnMessageProcessorTest {

    private static JDA owningUserJda;

    private final MessageProcessor<PokemonSpawn> processor = new WeCatchSpawnMessageProcessor();

    @BeforeAll
    static void setUp() throws LoginException, InterruptedException {
        owningUserJda = new JDABuilder(AccountType.CLIENT)
                .setToken(DiscordEntityConstants.NINERS_USER_TOKEN)
                .build()
                .awaitReady();
    }

    @AfterAll
    static void tearDown() {
        Optional.ofNullable(owningUserJda).ifPresent(JDA::shutdown);
    }

    @ParameterizedTest
    @MethodSource(value = {"wecatchChannelMessages"})
    void process_ValidCases_ReturnsExpected(final Message message, final PokemonSpawn expected) {
        assertEquals(expected, processor.process(message).orElse(null));
    }

    private static Object[][] wecatchChannelMessages() {
        return new Object[][] {
                new Object[] { // combee
                        getMessageById(551597236157546498L),
//                        mockMessageWithContent(
//                                "三蜜蜂  [324台灣桃園市平鎮區延平路三段104巷200號]",
//                                "**消失於: 11:08:23 (17m 53s)**\n" +
//                                        "天氣: None\n" +
//                                        "Lvl30+ IVs: 15攻/12防/14耐 (91.12%)\n" +
//                                        "Lvl30+ CP: 277 (lvl 20)\n" +
//                                        "Lvl30+ 技能: 蟲咬<:bugtype:527231883990269952> - 蟲鳴<:bugtype:527231883990269952>\n" +
//                                        "性別: ♂, 身高: 0.34, 體重: 6.56",
//                                "https://www.wecatch.net/?lat=24.94371711900664&lng=121.20096800677095",
//                                "https://image.cdstud.io/o/415.png"),
                        ImmutablePokemonSpawn.builder()
                                .from(WayPoint.of(24.94371711900664, 121.20096800677095))
                                .pokedexEntry(Pokedex.getPokedexEntryFor(415, Gender.MALE).get())
                                .iv(91.12)
                                .cp(277)
                                .level(20)
                                .locationDescription("324台灣桃園市平鎮區延平路三段104巷200號")
                                .sourceMetadata(ImmutableSourceMetadata.builder().sourceName("WeCatch").build())
                                .build()
                },
                new Object[] { // bronzong - gender neutral
                        getMessageById(551037464152899596L),
//                        mockMessageWithContent(
//                                "青銅鐘  [710台灣台南市永康區中正路277號]",
//                                "**消失於: 22:05:15 (19m 06s)**\n" +
//                                        "天氣: None\n" +
//                                        "Lvl30+ IVs: 13攻/14防/14耐 (91.12%)\n" +
//                                        "Lvl30+ CP: 563 (lvl 9)\n" +
//                                        "Lvl30+ 技能: 念力<:psychic:527231893234647041> - 精神強念<:psychic:527231893234647041>\n" +
//                                        "性別: ⚲, 身高: 1.18, 體重: 163.95",
//                                "https://www.wecatch.net/?lat=23.03260475324322&lng=120.24654005533672",
//                                "https://image.cdstud.io/o/437.png"),
                        ImmutablePokemonSpawn.builder()
                                .from(WayPoint.of(23.03260475324322, 120.24654005533672))
                                .pokedexEntry(Pokedex.getPokedexEntryFor(437, Gender.NONE).get())
                                .iv(91.12)
                                .cp(563)
                                .level(9)
                                .locationDescription("710台灣台南市永康區中正路277號")
                                .sourceMetadata(ImmutableSourceMetadata.builder().sourceName("WeCatch").build())
                                .build()
                },
                new Object[] { // castform - normal
                        getMessageById(551413147202879492L),
//                        mockMessageWithContent(
//                                "飄浮泡泡 普通 [737台灣台南市鹽水區南榮科技大學]",
//                                "**消失於: 22:55:39 (16m 40s)**\n" +
//                                        "天氣: Partly Cloudy<:partlycloudy:527231902566842388>\n" +
//                                        "Lvl30+ IVs: 12攻/15防/15耐 (93.34%)\n" +
//                                        "Lvl30+ CP: 1417 (lvl 32)\n" +
//                                        "Lvl30+ 技能: 撞擊<:normal:527231892064567307> - 能量球\n" +
//                                        "性別: ♀, 身高: 0.28, 體重: 0.66",
//                                "https://www.wecatch.net/?lat=23.32469150855182&lng=120.27484410435356",
//                                "https://image.cdstud.io/o/351-29.png"),
                        ImmutablePokemonSpawn.builder()
                                .from(WayPoint.of(23.32469150855182, 120.27484410435356))
                                .pokedexEntry(Pokedex.getPokedexEntryFor(351, Gender.FEMALE, Collections.singleton(PokedexEntry.Form.CASTFORM_NORMAL)).get())
                                .iv(93.34)
                                .cp(1417)
                                .level(32)
                                .locationDescription("737台灣台南市鹽水區南榮科技大學")
                                .sourceMetadata(ImmutableSourceMetadata.builder().sourceName("WeCatch").build())
                                .build()
                },
                new Object[] { // castform - sunny
                        getMessageById(551273233513709568L),
//                        mockMessageWithContent(
//                                "飄浮泡泡 太陽 [406台灣台中市太平區祥順路一段11號]",
//                                "**消失於: 13:39:46 (16m 45s)**\n" +
//                                        "天氣: Clear<:clear:527231901484974081>\n" +
//                                        "Lvl30+ IVs: 14攻/13防/14耐 (91.12%)\n" +
//                                        "Lvl30+ CP: 459 (lvl 10)\n" +
//                                        "Lvl30+ 技能: 火花<:firetype:527231887509553172> - 日光束<:grass:527231889761763329>\n" +
//                                        "性別: ♀, 身高: 0.3, 體重: 0.85",
//                                "https://www.wecatch.net/?lat=24.1359195272511&lng=120.71177779000884",
//                                "https://image.cdstud.io/o/351-30.png"),
                        // TODO: verify form once we support it
                        ImmutablePokemonSpawn.builder()
                                .from(WayPoint.of(24.1359195272511, 120.71177779000884))
                                .pokedexEntry(Pokedex.getPokedexEntryFor(351, Gender.FEMALE, Collections.singleton(PokedexEntry.Form.CASTFORM_SUNNY)).get())
                                .iv(91.12)
                                .cp(459)
                                .level(10)
                                .locationDescription("406台灣台中市太平區祥順路一段11號")
                                .sourceMetadata(ImmutableSourceMetadata.builder().sourceName("WeCatch").build())
                                .build()
                },
                new Object[] { // nidoran - female
                        getMessageById(551350106046201859L),
//                        mockMessageWithContent(
//                                "尼多蘭  [尚義8之2號邮政编码: 891]",
//                                "**消失於: 18:45:11 (16m 42s)**\n" +
//                                        "天氣: Cloudy<:cloudy:527231903271747615>\n" +
//                                        "Lvl30+ IVs: 15攻/13防/13耐 (91.12%)\n" +
//                                        "Lvl30+ CP: 527 (lvl 23)\n" +
//                                        "Lvl30+ 技能: 毒針<:poison:527231892605632532> - 污泥炸彈<:poison:527231892605632532>\n" +
//                                        "性別: ♀, 身高: 0.48, 體重: 10.96",
//                                "https://www.wecatch.net/?lat=24.43745373216914&lng=118.3785629458719",
//                                "https://image.cdstud.io/o/29.png"),
                        ImmutablePokemonSpawn.builder()
                                .from(WayPoint.of(24.43745373216914, 118.3785629458719))
                                .pokedexEntry(Pokedex.getPokedexEntryFor(29, Gender.FEMALE).get())
                                .iv(91.12)
                                .cp(527)
                                .level(23)
                                .locationDescription("尚義8之2號邮政编码: 891")
                                .sourceMetadata(ImmutableSourceMetadata.builder().sourceName("WeCatch").build())
                                .build()
                },
                new Object[] { // alolan geodude
                        getMessageById(551334596113989633L),
//                        mockMessageWithContent(
//                                "小拳石 阿羅拉 [614台灣嘉義縣東石鄉東石村觀海三路300號]",
//                                "**消失於: 17:45:02 (18m 11s)**\n" +
//                                        "天氣: Partly Cloudy<:partlycloudy:527231902566842388>\n" +
//                                        "Lvl30+ IVs: 15攻/15防/15耐 (100%)\n" +
//                                        "Lvl30+ CP: 923 (lvl 25)\n" +
//                                        "Lvl30+ 技能: 落石<:rock:527231893935226890> - 岩石封鎖<:rock:527231893935226890>\n" +
//                                        "性別: ♀, 身高: 0.42, 體重: 20.35",
//                                "https://www.wecatch.net/?lat=23.45309234576382&lng=120.13894736654322",
//                                "https://image.cdstud.io/o/74-68.png"),
                        // TODO: verify form once we support it
                        ImmutablePokemonSpawn.builder()
                                .from(WayPoint.of(23.45309234576382, 120.13894736654322))
                                .pokedexEntry(Pokedex.getPokedexEntryFor(74, Gender.FEMALE, Collections.singleton(PokedexEntry.Form.ALOLAN)).get())
                                .iv(100)
                                .cp(923)
                                .level(25)
                                .locationDescription("614台灣嘉義縣東石鄉東石村觀海三路300號")
                                .sourceMetadata(ImmutableSourceMetadata.builder().sourceName("WeCatch").build())
                                .build()
                },
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

    private static Message getMessageById(final long messageId) {
        return owningUserJda.getTextChannelById(DiscordEntityConstants.CHANNEL_ID_WECATCH_IV90UP)
                .getHistoryAround(messageId, 1).complete().getMessageById(messageId);
    }

}