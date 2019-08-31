package pogo.assistance.data.extraction.source.discord;

import static pogo.assistance.bot.di.DiscordEntityConstants.CATEGORY_IDS_CVM_FEEDS;
import static pogo.assistance.bot.di.DiscordEntityConstants.CATEGORY_IDS_GPGM_FEEDS;
import static pogo.assistance.bot.di.DiscordEntityConstants.CATEGORY_IDS_POGOSJ1_SPAWN_CHANNELS;
import static pogo.assistance.bot.di.DiscordEntityConstants.CATEGORY_IDS_POGO_ULM_KARTE_FEEDS;
import static pogo.assistance.bot.di.DiscordEntityConstants.CATEGORY_IDS_POKEMON_MAPS_FLORIDA_FEEDS;
import static pogo.assistance.bot.di.DiscordEntityConstants.CATEGORY_IDS_POKE_XPLORER_FEEDS;
import static pogo.assistance.bot.di.DiscordEntityConstants.CATEGORY_IDS_TPF_FEEDS;
import static pogo.assistance.bot.di.DiscordEntityConstants.CATEGORY_IDS_UTAH_POGO_FEEDS;
import static pogo.assistance.bot.di.DiscordEntityConstants.CATEGORY_ID_LVRM_IV_HUNTING;
import static pogo.assistance.bot.di.DiscordEntityConstants.CATEGORY_ID_NORTHHOUSTONTRAINERS_IV_FEED;
import static pogo.assistance.bot.di.DiscordEntityConstants.CATEGORY_ID_OAK_PARK_IV_SCANNERS;
import static pogo.assistance.bot.di.DiscordEntityConstants.CATEGORY_ID_POGO_SOFIA_SCANNER_COORDINATES;
import static pogo.assistance.bot.di.DiscordEntityConstants.CATEGORY_ID_UTAH_POGO_POKEMON;
import static pogo.assistance.bot.di.DiscordEntityConstants.CHANNEL_IDS_INDIGO_PLATEAU_FEEDS;
import static pogo.assistance.bot.di.DiscordEntityConstants.CHANNEL_IDS_POGO_CHCH_FEEDS;
import static pogo.assistance.bot.di.DiscordEntityConstants.CHANNEL_ID_TPF_FAIRYMAPS_NEOSF90IV;
import static pogo.assistance.bot.di.DiscordEntityConstants.CHANNEL_ID_VALLEY_POGO_PERFECT_100;
import static pogo.assistance.bot.di.DiscordEntityConstants.CHANNEL_ID_VCSCANS_0IV;
import static pogo.assistance.bot.di.DiscordEntityConstants.CHANNEL_ID_VCSCANS_100IV;
import static pogo.assistance.bot.di.DiscordEntityConstants.SERVER_ID_BMPGO_WORLD;
import static pogo.assistance.bot.di.DiscordEntityConstants.SERVER_ID_CVM;
import static pogo.assistance.bot.di.DiscordEntityConstants.SERVER_ID_GPGM;
import static pogo.assistance.bot.di.DiscordEntityConstants.SERVER_ID_INDIGO_PLATEAU;
import static pogo.assistance.bot.di.DiscordEntityConstants.SERVER_ID_NORTHHOUSTONTRAINERS;
import static pogo.assistance.bot.di.DiscordEntityConstants.SERVER_ID_OAK_PARK;
import static pogo.assistance.bot.di.DiscordEntityConstants.SERVER_ID_OC_SCANS;
import static pogo.assistance.bot.di.DiscordEntityConstants.SERVER_ID_POGOSJ1;
import static pogo.assistance.bot.di.DiscordEntityConstants.SERVER_ID_POGO_ALERTS_847;
import static pogo.assistance.bot.di.DiscordEntityConstants.SERVER_ID_POGO_CHCH;
import static pogo.assistance.bot.di.DiscordEntityConstants.SERVER_ID_POGO_SOFIA;
import static pogo.assistance.bot.di.DiscordEntityConstants.SERVER_ID_POGO_ULM_KARTE;
import static pogo.assistance.bot.di.DiscordEntityConstants.SERVER_ID_POKEMON_MAPS_FLORIDA;
import static pogo.assistance.bot.di.DiscordEntityConstants.SERVER_ID_POKE_XPLORER;
import static pogo.assistance.bot.di.DiscordEntityConstants.SERVER_ID_TOAST_MAPS;
import static pogo.assistance.bot.di.DiscordEntityConstants.SERVER_ID_TPF_BASIC;
import static pogo.assistance.bot.di.DiscordEntityConstants.SERVER_ID_TPF_PAID;
import static pogo.assistance.bot.di.DiscordEntityConstants.SERVER_ID_UTAH_POGO;
import static pogo.assistance.bot.di.DiscordEntityConstants.SERVER_ID_VALLEY_POGO;
import static pogo.assistance.bot.di.DiscordEntityConstants.SERVER_ID_VCSCANS;
import static pogo.assistance.bot.di.DiscordEntityConstants.SPAWN_CHANNEL_IDS_POKESQUAD;
import static pogo.assistance.bot.di.DiscordEntityConstants.SPAWN_CHANNEL_IDS_TOAST_MAPS;
import static pogo.assistance.bot.di.DiscordEntityConstants.USER_ID_POGO_BADGERS_BOT;

import java.util.Optional;
import javax.annotation.Nonnull;

import com.google.common.base.Verify;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.data.extraction.source.discord.novabot.NovaBotProcessingUtils;
import pogo.assistance.data.model.pokemon.CombatStats;
import pogo.assistance.data.model.pokemon.ImmutablePokemonSpawn;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

/**
 * @implNote
 *      Assumptions:
 *          - All message has one embed and the embed's thumbnail URL is a novabot asset URL (i.e. parsable by
 *          {@link NovaBotProcessingUtils#inferPokedexEntryFromNovaBotAssetUrl(String, PokedexEntry.Gender)}).
 */
public class GenericSpawnMessageProcessor implements MessageProcessor<PokemonSpawn> {

    @Override
    public boolean canProcess(@Nonnull final Message message) {
        return isFromSouthwestPokemonTargetChannel(message)
                || isFromVcPokeScanTargetChannels(message)
                || isFromPoGoSJ1TargetChannels(message)
                || isFromNHTTargetChannels(message)
                || isFromPoGoBadgersDmBot(message)
                || isFromPokeSquadTargetChannels(message)
                || isFromBMPGOWorldsTargetChannels(message)
                || isFromPoGoAlerts847TargetChannels(message)
                || isFromPokemonMapsFloridaTargetChannels(message)
                || isFromValleyPoGoTargetChannel(message)
                || isFromPoGoSofiaTargetChannels(message)
                || isFromPokeXplorerTargetChannels(message)
                || isFromUtahPoGoTargetChannels(message)
                || isFromCVMTargetChannels(message)
                || isFromGPGMTargetChannels(message)
                || isFromTPFFairyMaps(message)
                || isFromLVRMTargetChannels(message)
                || isFromToastMapsTargetChannels(message)
                || isFromOakParkTargetChannels(message)
                || isFromOCScansTargetChannels(message)
                || isFromPogoUlmKarteTargetChannels(message)
                || isFromIndigoPlateauTargetChannels(message)
                || isFromPogoChChTargetChannels(message);
    }

    @Override
    public Optional<PokemonSpawn> process(@Nonnull final Message message) {
        // Ignore messages from this source that doesn't contain iv/cp/level etc.
        // This is based on length check since messages that lack information has known number of lines in them.
        final String embedDescription = message.getEmbeds().get(0).getDescription();
        if (isFromPoGoBadgersDmBot(message)) {
            if (embedDescription.split("\n").length <= 2) {
                return Optional.empty();
            }
        } else if (isFromPokemonMapsFloridaTargetChannels(message)) {
            if (embedDescription.split("\n").length <= 5) {
                return Optional.empty();
            }
        } else if (isFromPoGoSJ1TargetChannels(message)) {
            if (message.getEmbeds().get(0).getTitle().contains("(?/?/?)")
                    || embedDescription.contains("(?/?/?)") || embedDescription.contains("CP:?") || embedDescription.contains("(L?)")) {
                return Optional.empty();
            }
        } else if (isFromCVMTargetChannels(message)) {
            if (message.getEmbeds().get(0).getTitle().contains("?% ?cp (L?)") || embedDescription.contains("?% ?/ ?/ ?")) {
                return Optional.empty();
            }
        } else if (isFromOCScansTargetChannels(message)) {
            if (embedDescription.split("\n").length <= 6) {
                return Optional.empty();
            }
        }

        final String compiledText = compileMessageText(message);
        final MessageEmbed messageEmbed = message.getEmbeds().get(0); // Assuming all message has embed
        final Optional<PokedexEntry.Gender> gender = SpawnMessageParsingUtils.extractGender(compiledText);
        final PokemonSpawn pokemonSpawn = ImmutablePokemonSpawn.builder()
                .from(SpawnMessageParsingUtils.parseGoogleMapQueryLink(compiledText))
                // Assuming embed's thumbnail is novabot asset URL and we can infer pokemon ID from it
                .pokedexEntry(NovaBotProcessingUtils.inferPokedexEntryFromNovaBotAssetUrl(messageEmbed.getThumbnail().getUrl(), gender.orElse(null)))
                .iv(SpawnMessageParsingUtils.extractCombatStats(compiledText, compiledText).flatMap(CombatStats::combinedIv))
                .level(SpawnMessageParsingUtils.extractLevel(compiledText))
                .cp(SpawnMessageParsingUtils.extractCp(compiledText))
                .sourceMetadata(SpawnMessageParsingUtils.buildSourceMetadataFromMessage(message))
                .build();
        return Optional.of(pokemonSpawn);
    }

    public static String compileMessageText(final Message message) {
        Verify.verify(message.getEmbeds().size() == 1);
        final MessageEmbed messageEmbed = message.getEmbeds().get(0);

        final StringBuilder compiler = new StringBuilder();
        Optional.ofNullable(message.getAuthor()).map(User::getName).ifPresent(name -> compiler.append(name).append(System.lineSeparator()));
        Optional.ofNullable(messageEmbed.getTitle()).ifPresent(title -> compiler.append(title).append(System.lineSeparator()));
        Optional.ofNullable(messageEmbed.getDescription()).ifPresent(description -> compiler.append(description.replaceAll("\\*", ""))
                .append(System.lineSeparator()));
        messageEmbed.getFields().forEach(field -> {
            compiler.append(field.getName());
            compiler.append(System.lineSeparator());
            compiler.append(field.getValue());
            compiler.append(System.lineSeparator());
        });
        Optional.ofNullable(messageEmbed.getUrl()).ifPresent(mapUrl -> compiler.append(mapUrl).append(System.lineSeparator()));
        return SpawnMessageParsingUtils.replaceEmojiWithPlainText(compiler.toString());
    }

    private static boolean isFromSouthwestPokemonTargetChannel(final Message message) {
        if (!message.getAuthor().isBot() || message.getChannel().getType() != ChannelType.TEXT
                || message.getGuild().getIdLong() != DiscordEntityConstants.SERVER_ID_SOUTHWEST_POKEMON) {
            return false;
        }

        final String channelName = message.getChannel().getName();
        final String categoryId = Optional.ofNullable(message.getCategory()).map(Category::getId).orElse("");
        switch (categoryId) {
            case "501877862878674944": // ILLINOIS POKEMON
            case "514347470801862667": // WISCONSIN POKEMON
            case "556256006858866689": // LONDON CANARY WHARF
            case "556256886287106058": // LONDON CENTRAL EAST
            case "556256853353431040": // LONDON CENTRAL WEST
            case "556256280184881154": // LONDON NORTHEAST
            case "556256342923411456": // LONDON NORTHWEST
            case "556257052549316619": // LONDON SOUTHEAST
            case "556255782547488775": // LONDON SOUTHWEST
            case "556257301452030003": // LONDON BARNET
            case "556257350458277890": // LONDON BROMLEY
            case "556256621634781185": // LONDON CHINGFORD
            case "565427412561559552": // LONDON CHARLTON
            case "560667251955466250": // LONDON CLAPTON
            case "556271357013524481": // LONDON DULWICH
            case "556256427149361212": // LONDON EALING
            case "556256462914060291": // LONDON ENFIELD
            case "556271396242718743": // LONDON GREENWICH
            case "560667326043783178": // LONDON HAMPSTEAD
            case "556256125952196608": // LONDON HARROW
            case "560667395526492170": // LONDON HOLLOWAY
            case "556271445441773588": // LONDON LEWISHAM
            case "560667528519745536": // LONDON STOKE NEWINGTON WOODBERRY
            case "580261123551395840": // LONDON WALTHAMSTOW
            case "556256551392903169": // LONDON WOODGREEN & HARINGEY
            case "556256673333903360": // LONDON WOOLWICH
                return !channelName.contains("quest")
                        && !channelName.contains("raid")
                        && !channelName.contains("egg")
                        && !channelName.contains("lure");
            case "546137025430945803": // LONDON CD
                return !channelName.contains("zee");
            default:
                return false;
        }
    }

    private static boolean isFromVcPokeScanTargetChannels(final Message message) {
        if (!message.getAuthor().isBot() || message.getChannelType() != ChannelType.TEXT || message.getGuild().getIdLong() != SERVER_ID_VCSCANS) {
            return false;
        }
        if (message.getChannel().getIdLong() == CHANNEL_ID_VCSCANS_100IV || message.getChannel().getIdLong() == CHANNEL_ID_VCSCANS_0IV) {
            return true;
        }

        final String channelName = message.getChannel().getName();
        final String categoryId = Optional.ofNullable(message.getCategory())
                .map(Category::getId)
                .orElse(null);
        if (categoryId == null) { // Some channels do not fall under a category (ungrouped)
            return false;
        }

        switch (categoryId) {
            case "360806354824462337": // OXNARD
            case "360806055640301568": // VENTURA
            case "360801783053942785": // CAMARILLO
            case "360804192589447169": // SANTA PAULA
            case "468453386266738688": // FILLMORE
                return !channelName.contains("raid") && !channelName.contains("chat") && !channelName.contains("quest");
            default:
                return false;
        }
    }

    private static boolean isFromOCScansTargetChannels(final Message message) {
        if (!message.getAuthor().isBot() || message.getChannelType() != ChannelType.TEXT || message.getGuild().getIdLong() != SERVER_ID_OC_SCANS) {
            return false;
        }

        final String channelName = message.getChannel().getName();
        final String categoryId = Optional.ofNullable(message.getCategory())
                .map(Category::getId)
                .orElse(null);
        if (categoryId == null) { // Some channels do not fall under a category (ungrouped)
            return false;
        }

        switch (categoryId) {
            case "582939716395991050": // DONOR ONLY
            case "586446917618106398": // GARDEN GROVE FEED
                return channelName.contains("iv") || channelName.contains("cp") || channelName.contains("spawn");
            default:
                return false;
        }
    }

    private static boolean isFromPoGoSJ1TargetChannels(final Message message) {
        if (!message.getAuthor().isBot() || message.getChannelType() != ChannelType.TEXT || message.getGuild().getIdLong() != SERVER_ID_POGOSJ1) {
            return false;
        }

        return Optional.ofNullable(message.getCategory())
                .map(Category::getIdLong)
                .filter(CATEGORY_IDS_POGOSJ1_SPAWN_CHANNELS::contains)
                .isPresent();
    }

    private static boolean isFromPoGoBadgersDmBot(final Message message) {
        return message.getChannelType() == ChannelType.PRIVATE
                && message.getAuthor().getIdLong() == USER_ID_POGO_BADGERS_BOT;
    }

    private static boolean isFromNHTTargetChannels(final Message message) {
        return message.getAuthor().isBot()
                && Optional.ofNullable(message.getGuild()).map(Guild::getIdLong).filter(id -> id == SERVER_ID_NORTHHOUSTONTRAINERS).isPresent()
                && Optional.ofNullable(message.getCategory())
                .map(Category::getIdLong)
                .filter(id -> id == CATEGORY_ID_NORTHHOUSTONTRAINERS_IV_FEED)
                .isPresent();
    }

    private static boolean isFromPokeSquadTargetChannels(final Message message) {
        return message.getChannelType() == ChannelType.TEXT
                && message.getAuthor().isBot()
                && SPAWN_CHANNEL_IDS_POKESQUAD.contains(message.getChannel().getIdLong());
    }

    private static boolean isFromBMPGOWorldsTargetChannels(final Message message) {
        return message.getAuthor().isBot()
                && message.getChannelType() == ChannelType.TEXT
                && message.getGuild().getIdLong() == SERVER_ID_BMPGO_WORLD
                && DiscordEntityConstants.SPAWN_CHANNEL_IDS_BMPGO_WORLD.contains(message.getChannel().getIdLong());
    }

    private static boolean isFromPoGoAlerts847TargetChannels(final Message message) {
        return message.getAuthor().isBot()
                && message.getChannelType() == ChannelType.TEXT
                && message.getGuild().getIdLong() == SERVER_ID_POGO_ALERTS_847
                && message.getChannel().getIdLong() == 553598599670398983L;
    }

    private static boolean isFromPokemonMapsFloridaTargetChannels(final Message message) {
        return message.getAuthor().isBot()
                && message.getChannelType() == ChannelType.TEXT
                && message.getGuild().getIdLong() == SERVER_ID_POKEMON_MAPS_FLORIDA
                && Optional.ofNullable(message.getCategory()).map(Category::getIdLong).filter(CATEGORY_IDS_POKEMON_MAPS_FLORIDA_FEEDS::contains).isPresent();
    }

    private static boolean isFromValleyPoGoTargetChannel(final Message message) {
        return message.getAuthor().isBot()
                && message.getChannelType() == ChannelType.TEXT
                && message.getGuild().getIdLong() == SERVER_ID_VALLEY_POGO
                && message.getChannel().getIdLong() == CHANNEL_ID_VALLEY_POGO_PERFECT_100;
    }

    private static boolean isFromPoGoSofiaTargetChannels(final Message message) {
        return message.getAuthor().isBot()
                && message.getChannelType() == ChannelType.TEXT
                && message.getGuild().getIdLong() == SERVER_ID_POGO_SOFIA
                && Optional.ofNullable(message.getCategory()).map(Category::getIdLong).filter(id -> id == CATEGORY_ID_POGO_SOFIA_SCANNER_COORDINATES).isPresent()
                && message.getChannel().getName().matches("(.*iv.*)|(.*lvl.*)|rare|unown");
    }

    private static boolean isFromPokeXplorerTargetChannels(final Message message) {
        return message.getAuthor().isBot()
                && message.getChannelType() == ChannelType.TEXT
                && message.getGuild().getIdLong() == SERVER_ID_POKE_XPLORER
                && Optional.ofNullable(message.getCategory()).map(Category::getIdLong).filter(CATEGORY_IDS_POKE_XPLORER_FEEDS::contains).isPresent()
                && message.getChannel().getName().matches("(.*-iv)|(.*lvl.*)");
    }

    private static boolean isFromUtahPoGoTargetChannels(final Message message) {
        if (!message.getAuthor().isBot() || message.getChannelType() != ChannelType.TEXT || message.getGuild().getIdLong() != SERVER_ID_UTAH_POGO) {
            return false;
        }

        final String channelName = message.getChannel().getName();
        final Long categoryId = Optional.ofNullable(message.getCategory())
                .map(Category::getIdLong)
                .orElse(null);
        return CATEGORY_IDS_UTAH_POGO_FEEDS.contains(categoryId)
                && (channelName.matches("(ivs-.*)") || categoryId == CATEGORY_ID_UTAH_POGO_POKEMON);
    }

    private static boolean isFromCVMTargetChannels(final Message message) {
        if (!message.getAuthor().isBot() || message.getChannelType() != ChannelType.TEXT || message.getGuild().getIdLong() != SERVER_ID_CVM) {
            return false;
        }

        final String channelName = message.getChannel().getName();
        final Long categoryId = Optional.ofNullable(message.getCategory())
                .map(Category::getIdLong)
                .orElse(null);
        return CATEGORY_IDS_CVM_FEEDS.contains(categoryId)
                && channelName.matches("(.*pokemon)|(.*iv)|(.*event)");
    }

    private static boolean isFromGPGMTargetChannels(final Message message) {
        if (!message.getAuthor().isBot() || message.getChannelType() != ChannelType.TEXT || message.getGuild().getIdLong() != SERVER_ID_GPGM) {
            return false;
        }

        final String channelName = message.getChannel().getName();
        final Long categoryId = Optional.ofNullable(message.getCategory())
                .map(Category::getIdLong)
                .orElse(null);

        return CATEGORY_IDS_GPGM_FEEDS.contains(categoryId)
                && !channelName.matches("(.*egg.*)|(.*raid.*)");
    }

    private static boolean isFromTPFFairyMaps(final Message message) {
        if (!message.getAuthor().isBot() || message.getChannelType() != ChannelType.TEXT
                || (message.getGuild().getIdLong() != SERVER_ID_TPF_BASIC && message.getGuild().getIdLong() != SERVER_ID_TPF_PAID)) {
            return false;
        }

        if (message.getChannel().getIdLong() == CHANNEL_ID_TPF_FAIRYMAPS_NEOSF90IV) {
            return true;
        }

        final String channelName = message.getChannel().getName();
        final Long categoryId = Optional.ofNullable(message.getCategory())
                .map(Category::getIdLong)
                .orElse(null);
        return CATEGORY_IDS_TPF_FEEDS.contains(categoryId)
                && channelName.matches("(.*iv.*)|(.*pvp_wreckers.*)");
    }

    private static boolean isFromLVRMTargetChannels(final Message message) {
        return message.getAuthor().isBot()
                && Optional.ofNullable(message.getCategory()).map(Category::getIdLong).filter(id -> id == CATEGORY_ID_LVRM_IV_HUNTING).isPresent();
    }

    private static boolean isFromToastMapsTargetChannels(final Message message) {
        return message.getAuthor().isBot()
                && message.getChannelType() == ChannelType.TEXT
                && message.getGuild().getIdLong() == SERVER_ID_TOAST_MAPS
                && Optional.ofNullable(message.getChannel()).map(MessageChannel::getIdLong).filter(SPAWN_CHANNEL_IDS_TOAST_MAPS::contains).isPresent();
    }

    private static boolean isFromOakParkTargetChannels(final Message message) {
        return message.getAuthor().isBot()
                && message.getChannelType() == ChannelType.TEXT
                && message.getGuild().getIdLong() == SERVER_ID_OAK_PARK
                && Optional.ofNullable(message.getCategory()).map(Category::getIdLong).filter(id -> CATEGORY_ID_OAK_PARK_IV_SCANNERS == id).isPresent();
    }

    private static boolean isFromPogoUlmKarteTargetChannels(final Message message) {
        return message.getAuthor().isBot()
                && message.getChannelType() == ChannelType.TEXT
                && message.getGuild().getIdLong() == SERVER_ID_POGO_ULM_KARTE
                && Optional.ofNullable(message.getCategory()).map(Category::getIdLong).filter(CATEGORY_IDS_POGO_ULM_KARTE_FEEDS::contains).isPresent();
    }

    private static boolean isFromIndigoPlateauTargetChannels(final Message message) {
        return message.getAuthor().isBot()
                && message.getChannelType() == ChannelType.TEXT
                && message.getGuild().getIdLong() == SERVER_ID_INDIGO_PLATEAU
                && Optional.ofNullable(message.getChannel()).map(MessageChannel::getIdLong).filter(CHANNEL_IDS_INDIGO_PLATEAU_FEEDS::contains).isPresent();
    }

    private static boolean isFromPogoChChTargetChannels(final Message message) {
        return message.getAuthor().isBot()
                && message.getChannelType() == ChannelType.TEXT
                && message.getGuild().getIdLong() == SERVER_ID_POGO_CHCH
                && Optional.ofNullable(message.getChannel()).map(MessageChannel::getIdLong).filter(CHANNEL_IDS_POGO_CHCH_FEEDS::contains).isPresent();
    }

}
