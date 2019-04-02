package pogo.assistance.bot.di;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DiscordEntityConstants {

    // Name for common named attributes. Used as args to @Named annotation.

    public static final String NAME_USER_TOKEN_OWNER = "owning_user_token";
    public static final String NAME_USER_TOKEN_M15MV1 = "m15mv1_user_token";
    public static final String NAME_USER_TOKEN_M15M = "m15m_bot_token";
    public static final String NAME_USER_TOKEN_CORRUPTED = "corrupted_user_token";
    public static final String NAME_USER_TOKEN_BENIN = "benin_user_token";
    public static final String NAME_USER_TOKEN_NINERS = "niners_user_token";
    public static final String NAME_USER_TOKEN_JOHNNY = "johnny_user_token";
    public static final String NAME_USER_TOKEN_TIMBURTY = "timburty_user_token";
    public static final String NAME_USER_TOKEN_IRVIN88 = "irvin88_user_token";
    public static final String NAME_USER_TOKEN_HORUSEUS = "horuseus_user_token";

    public static final String NAME_JDA_BUILDER_OWNING_USER = "owning_user_jda_builder";
    public static final String NAME_JDA_OWNING_USER = "owning_user_jda";

    public static final String NAME_JDA_BUILDER_M15MV1_USER = "m15mv1_user_jda_builder";
    public static final String NAME_JDA_M15MV1_USER = "m15mv1_user_jda";

    public static final String NAME_JDA_BUILDER_M15M_BOT = "m15m_bot_jda_builder";
    public static final String NAME_JDA_M15M_BOT = "m15m_bot_jda";

    public static final String NAME_JDA_BUILDER_CORRUPTED_USER = "corrupted_user_jda_builder";
    public static final String NAME_JDA_CORRUPTED_USER = "corrupted_user_jda";

    public static final String NAME_JDA_BENIN_USER = "benin_user_jda";

    public static final String NAME_JDA_NINERS_USER = "niners_user_jda";

    public static final String NAME_JDA_JOHNNY_USER = "johnny_user_jda";

    public static final String NAME_JDA_TIMBURTY_USER = "timburty_user_jda";

    public static final String NAME_JDA_IRVIN88_USER = "irvin88_user_jda";

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

    // Users/bots

    public static final long USER_ID_H13M = 561201292693995520L;
    public static final long USER_ID_M15MV1 = 520113608978333706L;

    public static final long USER_ID_KYRION = 209827394721284097L;
    public static final long USER_ID_JOSH = 289560054708043777L;
    public static final long USER_ID_WOPZ = 237557740342476801L;
    public static final long USER_ID_GHOST = 331557091884138496L;
    public static final long USER_ID_HERO = 367080731350138880L;

    public static final long USER_ID_PDEX100_SUPER_BOT_P = 336443339165532162L;
    public static final long USER_ID_FLPM_ALERT_BOT_7 = 347260485210603531L;
    public static final long USER_ID_AP_ALERT_BOT = 419631321577553921L;
    public static final Set<Long> USER_ID_SS_NOVA_BOTS = ImmutableSet.of(
            400906588581265410L,
            409803661502447626L,
            409919681106018315L,
            409786475564105728L,
            409780035470360576L);
    public static final long USER_ID_SDHVIP_BOT = 520776622887141388L;
    public static final long USER_ID_POGO_BADGERS_BOT = 508688611898687498L;
    public static final long USER_ID_POKEX_DM_BOT = 271388742558679040L;

    // IDs for discord servers/channels

    public static final long CHANNEL_ID_TEST_LIST_ROUTE_PREVIEW = 543431296211484672L;
    public static final long CHANNEL_ID_PDEX100_BOT_COMMAND = 252776251708801024L;

    public static final long CHANNEL_ID_PDEX100P_PLAYGROUND = 561519459374858245L;

    public static final long SERVER_ID_DD = 561529276944351243L;
    public static final long CHANNEL_ID_DD_BOT_TESTING = 561529516124536833L;

    public static final long SERVER_ID_POGOSJ1 = 346733317699141632L;
    public static final long CHANNEL_ID_POGOSJ1_TWEETS = 346733581814726657L;
    public static final long CHANNEL_ID_POGOSJ1_100IV = 348769770671308800L;
    public static final long CHANNEL_ID_POGOSJ1_100IVMAX = 371106133382922250L;

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

    public static final long SERVER_ID_POKEFAIRY = 395409007038300170L;
    public static final long CHANNEL_ID_POKEFAIRY_NEOSF90IV = 520135251717259274L;

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

}
