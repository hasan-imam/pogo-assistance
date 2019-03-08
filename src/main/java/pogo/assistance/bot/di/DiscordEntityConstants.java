package pogo.assistance.bot.di;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DiscordEntityConstants {

    // Name for common named attributes. Used as args to @Named annotation.

    public static final String NAME_USER_TOKEN_OWNER = "owning_user_token";
    public static final String NAME_USER_TOKEN_M8M = "m8m_bot_token";
    public static final String NAME_USER_TOKEN_CORRUPTED = "corrupted_user_token";

    public static final String NAME_JDA_BUILDER_OWNING_USER = "owning_user_jda_builder";
    public static final String NAME_JDA_OWNING_USER = "owning_user_jda";

    public static final String NAME_JDA_BUILDER_M8M_BOT = "bot_user_jda_builder";
    public static final String NAME_JDA_M8M_BOT = "bot_user_jda";

    public static final String NAME_JDA_BUILDER_CORRUPTED_USER = "corrupted_user_jda_builder";
    public static final String NAME_JDA_CORRUPTED_USER = "corrupted_user_jda";

    public static final String NAME_PDEX100_BOT_COMMAND_CHANNEL = "pokedex100_bot_command_channel";

    // User tokens. Secret stuff. Real values should not be pushed to repo.

    public static final String M8M_BOT_TOKEN = "";
    public static final String OWNING_USER_TOKEN = "";
    public static final String CORRUPTED_USER_TOKEN = "";

    // Users/bots

    public static final long USER_ID_H13M = 471666614857629696L;
    public static final long USER_ID_QN234 = 520113608978333706L;

    public static final long USER_ID_KYRION = 209827394721284097L;
    public static final long USER_ID_JOSH = 289560054708043777L;
    public static final long USER_ID_WOPZ = 237557740342476801L;
    public static final long USER_ID_GHOST = 331557091884138496L;
    public static final long USER_ID_HERO = 367080731350138880L;

    public static final long USER_ID_PDEX100_SUPER_BOT_P = 336443339165532162L;
    public static final long USER_ID_FLPM_ALERT_BOT_7 = 347260485210603531L;
    public static final long USER_ID_AP_ALERT_BOT = 419631321577553921L;
    public static final Set<Long> USER_ID_SS_NOVA_BOTS = ImmutableSet.of(
            409919681106018315L,
            409786475564105728L,
            409780035470360576L);

    // IDs for discord servers/channels

    public static final long CHANNEL_ID_TEST_LIST_ROUTE_PREVIEW = 543431296211484672L;
    public static final long CHANNEL_ID_PDEX100_BOT_COMMAND = 252776251708801024L;

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
}
