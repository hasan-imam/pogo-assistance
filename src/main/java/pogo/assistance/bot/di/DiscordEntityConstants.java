package pogo.assistance.bot.di;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DiscordEntityConstants {

    // Name for common named attributes. Used as args to @Named annotation.

    public static final String NAME_OWNING_USER_TOKEN = "owning_user_token";

    public static final String NAME_JDA_BUILDER_OWNING_USER = "owning_user_jda_builder";
    public static final String NAME_JDA_OWNING_USER = "owning_user_jda";

    public static final String NAME_JDA_BUILDER_BOT_USER = "bot_user_jda_builder";
    public static final String NAME_JDA_BOT_USER = "bot_user_jda";

    public static final String NAME_PDEX100_BOT_COMMAND_CHANNEL = "pokedex100_bot_command_channel";

    // User tokens. Secret stuff. Real values should not be pushed to repo.

    public static final String M8M_BOT_TOKEN = "";
    public static final String OWNING_USER_TOKEN = "";
    public static final String CORRUPTED_USER_TOKEN = "";

    // Users/bots

    public static final long USER_ID_H13M = 471666614857629696L;
    public static final long USER_ID_QN234 = 520113608978333706L;
    public static final long USER_ID_JOSH = 289560054708043777L;

    public static final String NAME_PDEX100_SUPER_BOT_P = "superbot-p";
    public static final long USER_ID_PDEX100_SUPER_BOT_P = 336443339165532162L;

    public static final long USER_ID_FLPM_ALERT_BOT_7 = 347260485210603531L;

    // IDs for discord servers/channels

    public static final long CHANNEL_ID_TEST_LIST_ROUTE_PREVIEW = 543431296211484672L;
    public static final long CHANNEL_ID_PDEX100_BOT_COMMAND = 252776251708801024L;

    public static final long SERVER_ID_POGOSJ1 = 346733317699141632L;
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

    public static final long SERVER_ID_VASCANS = 528443479077158915L;
    public static final long CHANNEL_ID_VASCANS_HUNDOS = 538849532901851137L;
}
