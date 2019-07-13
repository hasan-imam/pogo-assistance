package pogo.assistance.bot.di;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DiscordEntityConstants {

    // Name for common named attributes. Used as args to @Named annotation.

    public static final String NAME_USER_TOKEN_OWNER = "owning_user_token";
    public static final String NAME_USER_TOKEN_M15M = "m15m_bot_token";
    public static final String NAME_USER_TOKEN_CORRUPTED = "corrupted_user_token";
    public static final String NAME_USER_TOKEN_BENIN = "benin_user_token";
    public static final String NAME_USER_TOKEN_NINERS = "niners_user_token";
    public static final String NAME_USER_TOKEN_JOHNNY = "johnny_user_token";
    public static final String NAME_USER_TOKEN_TIMBURTY = "timburty_user_token";
    public static final String NAME_USER_TOKEN_IRVIN88 = "irvin88_user_token";
    public static final String NAME_USER_TOKEN_HORUSEUS = "horuseus_user_token";
    public static final String NAME_USER_TOKEN_CONNOISSEUR = "connoisseur_user_token";
    public static final String NAME_USER_TOKEN_CHRONIC = "chronic_user_token";
    public static final String NAME_USER_TOKEN_CRANK = "crank_user_token";
    public static final String NAME_USER_TOKEN_POGO_HERO = "pogo_hero_user_token";
    public static final String NAME_USER_TOKEN_MICHELLEX = "michellex_user_token";
    public static final String NAME_USER_TOKEN_POKE_PETER = "poke_peter_user_token";

    public static final String NAME_JDA_BUILDER_OWNING_USER = "owning_user_jda_builder";
    public static final String NAME_JDA_OWNING_USER = "owning_user_jda";

    public static final String NAME_JDA_BUILDER_M15M_BOT = "m15m_bot_jda_builder";
    public static final String NAME_JDA_M15M_BOT = "m15m_bot_jda";

    public static final String NAME_JDA_BUILDER_CORRUPTED_USER = "corrupted_user_jda_builder";
    public static final String NAME_JDA_CORRUPTED_USER = "corrupted_user_jda";

    public static final String NAME_JDA_BENIN_USER = "benin_user_jda";
    public static final String NAME_JDA_NINERS_USER = "niners_user_jda";
    public static final String NAME_JDA_JOHNNY_USER = "johnny_user_jda";
    public static final String NAME_JDA_TIMBURTY_USER = "timburty_user_jda";
    public static final String NAME_JDA_IRVIN88_USER = "irvin88_user_jda";
    public static final String NAME_JDA_CONNOISSEUR_USER = "connoisseur_user_jda";
    public static final String NAME_JDA_CHRONIC_USER = "chronic_user_jda";
    public static final String NAME_JDA_CRANK_USER = "crank_user_jda";
    public static final String NAME_JDA_POGO_HERO_USER = "pogo_hero_user_jda";
    public static final String NAME_JDA_MICHELLEX_USER = "michellex_user_jda";
    public static final String NAME_JDA_POKE_PETER_USER = "poke_peter_user_jda";

    public static final String NAME_JDA_BUILDER_HORUSEUS_USER = "horuseus_user_jda_builder";
    public static final String NAME_JDA_HORUSEUS_USER = "horuseus_user_jda";

    public static final String NAME_PDEX100_BOT_COMMAND_CHANNEL = "pokedex100_bot_command_channel";

    // User tokens. Secret stuff. Real values should not be pushed to repo.

    public static final String M15M_BOT_TOKEN = "";
    public static final String OWNING_USER_TOKEN = "";
    public static final String M15MV1_USER_TOKEN = "";

    public static final String CORRUPTED_USER_TOKEN = "";
    public static final String BENIN_USER_TOKEN = "";
    public static final String NINERS_USER_TOKEN = "";
    public static final String JOHNNY_USER_TOKEN = "";
    public static final String TIMBURTY_USER_TOKEN = "";
    public static final String IRVIN88_USER_TOKEN = "";
    public static final String HORUSEUS_USER_TOKEN = "";
    public static final String CONNOISSEUR_USER_TOKEN = "";
    public static final String CHRONIC_USER_TOKEN = "";
    public static final String CRANK_USER_TOKEN = "";
    public static final String POGO_HERO_USER_TOKEN = "";
    public static final String MICHELLEX_USER_TOKEN = "";
    public static final String POKE_PETER_USER_TOKEN = "";

    // Users/bots

    public static final long USER_ID_H13M = 561201292693995520L;
    public static final long USER_ID_M15MV1 = 520113608978333706L;

    public static final long USER_ID_KYRION = 209827394721284097L;
    public static final long USER_ID_JOSH = 289560054708043777L;
    public static final long USER_ID_WOPZ = 237557740342476801L;
    public static final long USER_ID_GHOST = 331557091884138496L;
    public static final long USER_ID_HERO = 367080731350138880L;

    public static final long USER_ID_PDEX100_SUPER_BOT_P = 336443339165532162L;
    public static final long USER_ID_FLPM_ALERT_BOT = 419298077975904256L;
    public static final long USER_ID_AP_ALERT_BOT = 419631321577553921L;
    public static final Set<Long> USER_ID_SS_NOVA_BOTS = ImmutableSet.of(
            400906588581265410L,
            409803661502447626L,
            409919681106018315L,
            409786475564105728L,
            409780035470360576L);
    public static final long USER_ID_POGO_BADGERS_BOT = 508688611898687498L;
    public static final long USER_ID_POKEX_DM_BOT = 271388742558679040L;

    // IDs for discord servers/channels

    public static final Long SERVER_ID_SDHVIP = 433736422932086786L;
    public static final Set<Long> USER_IDS_SDHVIP_BOT = ImmutableSet.of(520698304200310785L, 520699525216731146L);
    public static final long CATEGORY_ID_SDHVIP_SIGHTING_REPORTS = 433736798166974467L;

    public static final long CHANNEL_ID_TEST_LIST_ROUTE_PREVIEW = 543431296211484672L;
    public static final long CHANNEL_ID_PDEX100_BOT_COMMAND = 252776251708801024L;

    public static final long CHANNEL_ID_PDEX100P_PLAYGROUND = 561519459374858245L;

    public static final long SERVER_ID_DD = 561529276944351243L;
    public static final long CHANNEL_ID_DD_BOT_TESTING = 561529516124536833L;

    public static final long SERVER_ID_POGOSJ1 = 346733317699141632L;
    public static final long CHANNEL_ID_POGOSJ1_TWEETS = 346733581814726657L;
    public static final long CHANNEL_ID_POGOSJ1_0IV = 373572173425803265L;
    public static final long CHANNEL_ID_POGOSJ1_100IV = 348769770671308800L;
    public static final long CHANNEL_ID_POGOSJ1_100IVMAX = 371106133382922250L;
    public static final Set<Long> CHANNEL_IDS_POGOSJ1_ULTRA_ALERTS = ImmutableSet.of(
            CHANNEL_ID_POGOSJ1_TWEETS,
            CHANNEL_ID_POGOSJ1_0IV,
            CHANNEL_ID_POGOSJ1_100IV,
            CHANNEL_ID_POGOSJ1_100IVMAX);
    public static final Set<Long> CATEGORY_IDS_POGOSJ1_SPAWN_CHANNELS = ImmutableSet.of(
            361185397415477258L,  // ULTRA ALERTS
            361185952078626818L,  // CANDY CHANNELS
            361185672607825932L); // GEO-CHANNELS

    public static final long SERVER_ID_NYCPOKEMAP = 301247864833572864L;
    public static final long CATEGORY_ID_IV_CP_LVL_ALERTS = 416791808954073114L;
    public static final long CHANNEL_ID_NYCPOKEMAP_IV0 = 416797849406406658L;
    public static final long CHANNEL_ID_NYCPOKEMAP_IV90 = 416890211604103169L;
    public static final long CHANNEL_ID_NYCPOKEMAP_IV95 = 324096818549882883L;
    public static final long CHANNEL_ID_NYCPOKEMAP_IV100 = 302491007382061057L;
    public static final long CHANNEL_ID_NYCPOKEMAP_IV100_LEVEL30 = 416791810610954241L;
    public static final long CHANNEL_ID_NYCPOKEMAP_CP2500 = 324096818918981632L;
    public static final long CHANNEL_ID_NYCPOKEMAP_CP3000 = 416797863427833859L;
    public static final long CHANNEL_ID_NYCPOKEMAP_LEVEL35 = 416797870986100738L;

    public static final long SERVER_ID_ALPHAPOKES = 273572648519204866L;
    public static final long CHANNEL_ID_ALPHAPOKES_ULTRARARE_TEST = 504760808358674452L;

    public static final long SERVER_ID_VASCANS = 528443479077158915L;
    public static final long CHANNEL_ID_VASCANS_HUNDOS = 538849532901851137L;

    public static final long SERVER_ID_WECATCH = 409426776419336202L;
    public static final long CHANNEL_ID_WECATCH_IV90UP = 493530728915664912L;

    public static final long SERVER_ID_TPF_BASIC = 395409007038300170L;
    public static final long SERVER_ID_TPF_PAID = 462255540651950090L;
    public static final Set<Long> CATEGORY_IDS_TPF_FEEDS = ImmutableSet.<Long>builder()
            // All categories are under the paid server
            .add(531596648623636505L) // SF MAP CHANNELS
            .add(551440473449955342L) // PENINSULA MAP CHANNELS
            .add(551480783697739789L) // EAST BAY MAP CHANNELS
            .add(551441426592956417L) // ALL GEO MAP CHANNELS
            .build();
    public static final long CHANNEL_ID_TPF_FAIRYMAPS_NEOSF90IV = 520135251717259274L;

    public static final long SERVER_ID_PINEAPPLE = 519605654529245224L;
    public static final long CHANNEL_ID_PINEAPPLE_NEWARK_100IV = 524609486011367434L;
    public static final long CHANNEL_ID_PINEAPPLE_FREMONT_LEVEL35 = 525029788440723456L;
    public static final long CHANNEL_ID_PINEAPPLE_HAYWARD_RARE = 525029359296184330L;

    public static final long SERVER_ID_CHICAGOLAND_POGO = 261908001332527115L;
    public static final long CHANNEL_ID_CHICAGOLAND_POGO_100IV = 283667771114520577L;
    public static final long CHANNEL_ID_CHICAGOLAND_POGO_RARESPAWNS = 510802889313353738L;
    public static final long CHANNEL_ID_CHICAGOLAND_POGO_90PLUS = 532807041375272993L;
    public static final Set<Long> SPAWN_CHANNEL_IDS_CHICAGOLAND_POGO = ImmutableSet.of(
            CHANNEL_ID_CHICAGOLAND_POGO_100IV,
            CHANNEL_ID_CHICAGOLAND_POGO_RARESPAWNS,
            CHANNEL_ID_CHICAGOLAND_POGO_90PLUS);

    public static final long SERVER_ID_SANDIEGOHILLS = 520776622887141388L;

    public static final long SERVER_ID_SOUTHWEST_POKEMON = 489168292301373452L;

    public static final long SERVER_ID_VCSCANS = 334404466948177922L;
    public static final long CHANNEL_ID_VCSCANS_100IV = 516049924195090442L;
    public static final long CHANNEL_ID_VCSCANS_0IV = 544756096837877784L;

    public static final long SERVER_ID_POGO_BADGERS = 504336735199821824L;

    public static final long SERVER_ID_NORTHHOUSTONTRAINERS = 480799758810742824L;
    public static final long CATEGORY_ID_NORTHHOUSTONTRAINERS_IV_FEED = 510562954291314688L;
    public static final long CHANNEL_ID_NORTHHOUSTONTRAINERS_GLOBAL100 = 532661073321525250L;

    public static final long SERVER_ID_POKESQUAD = 323035478460989440L;
    public static final Set<Long> SPAWN_CHANNEL_IDS_POKESQUAD = ImmutableSet.of(
            534430788737826847L, // rare
            532958068812677140L, // 0iv
            532950935463264301L, // 90iv
            532950971756707840L, // 95iv
            532950888499642368L, // 100iv
            532954783321948182L); // 100iv rare

    public static final long SERVER_ID_POGO_NINJA = 213950622473388032L;
    public static final Set<Long> SPAWN_CHANNEL_IDS_POGO_NINJA = ImmutableSet.of(522525269760802817L);

    public static final long SERVER_ID_SGV_SCANS = 532078398843912192L;
    public static final Set<Long> CATEGORY_IDS_SGV_SCANS_IV_FEED = ImmutableSet.<Long>builder()
            .add(533035470062485514L) // SGV
            .add(537027953221959680L) // ALHAMBRA-MPK
            .add(537027907311239181L) // ARCADIA-MONROVIA-DUARTE
            .add(537027594361634826L) // TG-SG-SM-ROSEMEAD
            .add(542907988659142657L) // EL MONTE
            .add(542908143957704704L) // PASADENA
            .add(546179226705985566L) // MONTEBELLO MALL
            .add(563249073407131669L) // COVINA
            .add(566435366488506388L) // AZUSA-GLENDORA
            .add(534917557438644241L) // ALHAMBRA-MPK II
            .build();

    public static final long SERVER_ID_BMPGO_WORLD = 498552619195695114L;
    public static final Set<Long> SPAWN_CHANNEL_IDS_BMPGO_WORLD = ImmutableSet.<Long>builder()
            .add(522730705902501889L) // 100-iv
            .add(530139543190503454L) // level-35-90-99-iv
            .add(525313353216688129L) // 90-99-iv
            .add(522789463601184768L) // rare-iv-89
            .add(522359037841375242L) // possibly-shiny-89
            .add(522771583140495366L) // regional-iv-89
            .build();

    public static final long SERVER_ID_POGO_ALERTS_847 = 303349928766210049L;

    public static final long SERVER_ID_POKEMON_MAPS_FLORIDA = 560001508628889629L;
    public static final Set<Long> CATEGORY_IDS_POKEMON_MAPS_FLORIDA_FEEDS = ImmutableSet.<Long>builder()
            .add(560747511480844310L) // CLEARWATER / DTCW
            .add(583297357274284032L) // NORTH TAMPA / USF
            .add(584464280867438592L) // SOUTH TAMPA / YBOR
            .add(584467711325896704L) // EAST TAMPA / BRANDON
            .add(584469974714679480L) // SAFETY HARBOR / OLDSMAR
            .add(584463140775919649L) // ST. PETESBURG / DTSP
            .add(584462223926231116L) // PALM HARBOR / DUNEDIN
            .add(579961624786501632L) // SARASOTA
            .add(584468866038497301L) // BRADENTON
            .build();

    public static final long SERVER_ID_VALLEY_POGO = 397624389601984523L;
    public static final long CHANNEL_ID_VALLEY_POGO_PERFECT_100 = 545256808705425428L;

    public static final long SERVER_ID_POGO_SOFIA = 320813469014294529L;
    public static final long CATEGORY_ID_POGO_SOFIA_SCANNER_COORDINATES = 522764598718496788L;

    public static final long SERVER_ID_POKE_XPLORER = 516999588708745217L;
    public static final Set<Long> CATEGORY_IDS_POKE_XPLORER_FEEDS = ImmutableSet.<Long>builder()
            .add(577705038840070144L) // SOLANO
            .add(535841739236442122L) // BOAZ
            .add(535842066572247051L) // HUNTSVILLE
            .add(552559108969070613L) // HENDERSONVILLE
            .add(552559847506444298L) // FELTCHER
            .add(552560149319909396L) // ASHEVILLE
            .add(557427702265544714L) // BREVARD
            .add(565211408225271829L) // AUBURN
            .add(565211507659505695L) // OPELIKA
            .build();

    public static final long SERVER_ID_PGAN = 237964415822069760L;
    public static final Set<Long> USER_ID_PGAN_BOTS = ImmutableSet.of(
            343127550752587788L,
            343128185744916520L,
            290601744042295296L);

    public static final long SERVER_ID_UTAH_POGO = 473845802943643679L;
    public static final long CATEGORY_ID_UTAH_POGO_POKEMON = 475705825508327434L;
    public static final Set<Long> CATEGORY_IDS_UTAH_POGO_FEEDS = ImmutableSet.<Long>builder()
            .add(CATEGORY_ID_UTAH_POGO_POKEMON) // POKEMON
            .add(529826586002980914L) // MAGNA-WESTVALLEY
            .add(529826953302114354L) // TAYLORSVILLE-WESTVALLEY-EAST
            .add(530275157508358144L) // SLC
            .add(529827346358992911L) // HERRIMAN-RIVERTON
            .add(550682557130473492L) // NORTH SALT LAKE
            .add(550682301256826890L) // BOUNTIFUL
            .add(550682366558076928L) // CENTERVILLE
            .add(561543822883029007L) // FARMINGTON
            .build();

    public static final long SERVER_ID_CVM = 305941907136053250L;
    public static final Set<Long> CATEGORY_IDS_CVM_FEEDS = ImmutableSet.<Long>builder()
            .add(422195279484420117L) // DAVIS
            .add(422243137839431691L) // DIXON
            .add(422244882300796938L) // FAIRFIELD
            .add(422248759657758741L) // VACAVILLE
            .add(422248839198670848L) // WOODLAND
            .add(422245947200765983L) // WINTERS
            .add(526672210463817739L) // NORTH NATOMAS
            .build();

    public static final long SERVER_ID_GPGM = 401321052262760450L;
    public static final Set<Long> CATEGORY_IDS_GPGM_FEEDS = ImmutableSet.<Long>builder()
            .add(567443788415500330L) // PORT HOPE & COBOURG
            .add(567443612208594965L) // TRENTON
            .add(567443682672771112L) // BELLEVILLE
            .add(425488827625242664L) // KINGSTON
            .build();

    public static final long SERVER_ID_LVRM = 443145770573758465L;
    public static final long CATEGORY_ID_LVRM_IV_HUNTING = 514969848845959181L;

    public static final long SERVER_ID_TOAST_MAPS = 493858109563863043L;
    public static final long CATEGORY_ID_TOAST_MAPS_TEST_STUFF = 558386903355752493L;
    public static final Set<Long> SPAWN_CHANNEL_IDS_TOAST_MAPS = ImmutableSet.<Long>builder()
            .add(558468422569295892L) // global-iv
            .add(583869538190360591L) // global-thiccness
            .build();

    public static final long SERVER_ID_OAK_PARK = 338751348650672128L;
    public static final long CATEGORY_ID_OAK_PARK_IV_SCANNERS = 414280150415573002L;

    public static final long SERVER_ID_OC_SCANS = 574451996656926720L;
    // Many of these categories have already disappeared but still keeping them in case they come back
    // TODO: review the set after a while and update as needed
    public static final Set<Long> CATEGORY_IDS_OC_SCANS_FEEDS = ImmutableSet.<Long>builder()
            .add(582939716395991050L) // DONOR ONLY
            .add(574453270156804106L) // LAKE FOREST
            .add(582582418477940756L) // MISSION VIEJO
            .add(583754315139907601L) // RANCHO SANTA MARGARITA
            .add(585631136756138035L) // IRVINE SPECTRUM
            .add(587561761717354517L) // LADERA RANCH
            .add(588550029879279619L) // COSTA MESA
            .add(579848978561237030L) // ANAHEIM
            .add(582934011991556096L) // SANTA ANA
            .add(586446917618106398L) // GARDEN GROVE
            .add(587563323478900766L) // ORANGE
            .add(588549652412891136L) // NEWPORT BEACH
            .add(588550660056678420L) // WESTMINSTER
            .build();

    public static final long SERVER_ID_POGO_ULM_KARTE = 505742401827241994L;
    public static final Set<Long> CATEGORY_IDS_POGO_ULM_KARTE_FEEDS = ImmutableSet.<Long>builder()
            .add(583706381295812628L) // POKE IV ULM
            .add(577245430950133771L) // POKE IV EBERSBACH
            .add(586629486884290580L) // POKE IV GEISLINGEN
            .add(577245645971259393L) // POKE IV MEMMINGEN
            .add(586635549104930828L) // POKE IV LEUTKIRCH
            .add(586629665448525836L) // POKE IV KEMPTEN
            .build();

    public static final long SERVER_ID_INDIGO_PLATEAU = 398695671001251842L;
    public static final Set<Long> CHANNEL_IDS_INDIGO_PLATEAU_FEEDS = ImmutableSet.<Long>builder()
            .add(546376030311546910L) // 100-durham
            .add(546375859397853205L) // 91-99-durham
            .add(597271434548740096L) // 100-sauga
            .add(597273467620032533L) // 91-99-sauga
            .build();

    public static final long SERVER_ID_POGO_CHCH = 413522045058547713L;
    public static final Set<Long> CHANNEL_IDS_POGO_CHCH_FEEDS = ImmutableSet.<Long>builder()
            .add(544485199023570964L) // hundred-wild-spawn-no-chat
            .add(546063393048625152L) // 90-and-over-no-chat
            .build();
}
