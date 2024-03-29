package pogo.assistance.data.extraction.source.discord;

import com.google.common.base.Verify;
import net.dv8tion.jda.api.entities.*;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.data.extraction.source.discord.novabot.NovaBotProcessingUtils;
import pogo.assistance.data.model.pokemon.CombatStats;
import pogo.assistance.data.model.pokemon.ImmutablePokemonSpawn;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Optional;

import static pogo.assistance.bot.di.DiscordEntityConstants.*;

/**
 * @implNote
 *      Assumptions:
 *          - All message has one embed and the embed's thumbnail URL is a novabot asset URL (i.e. parsable by
 *          {@link NovaBotProcessingUtils#inferPokedexEntryFromNovaBotAssetUrl(String, PokedexEntry.Gender)}).
 */
public class GenericSpawnMessageProcessor implements MessageProcessor<PokemonSpawn> {

    @Override
    public boolean canProcess(@Nonnull final Message message) {
        return isFromAlphaPokesTargetChannel(message)
                || isFromFLPokeMapTargetChannel(message)
                || isFromSouthwestPokemonTargetChannel(message)
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
                || isFromPogoChChTargetChannels(message)
                || isFromAzPoGoMapTargetChannels(message)
                || isFromPokeHunterEliteTargetChannels(message)
                || isFromPogoSaTargetChannels(message)
                || isFromPokemonOnTrentTargetChannels(message)
                || isFromTallyPokemonHuntersTargetChannels(message)
                || isFromPoGoSouthernMassTargetChannels(message);
    }

    @Override
    public Optional<PokemonSpawn> process(@Nonnull final Message message) {
        // Ignore messages from this source that doesn't contain iv/cp/level etc.
        // This is based on length check since messages that lack information has known number of lines in them.
        if (message.getEmbeds().isEmpty()) {
            return Optional.empty();
        }
        Verify.verify(
                message.getEmbeds().size() == 1,
                "Spawn messages are expected to have only one embed, but this has %s",
                message.getEmbeds().size());
        final String embedDescription = Optional.ofNullable(message.getEmbeds().get(0))
                .map(MessageEmbed::getDescription)
                .orElse("");
        final int embedDescriptionLength = embedDescription.split("\n").length;
        final String embedTitle = Optional.ofNullable(message.getEmbeds().get(0).getTitle()).orElse("");
        if (isFromAlphaPokesTargetChannel(message)) {
            if (embedDescriptionLength == 4 || embedTitle.contains("(?/?/?)")) {
                return Optional.empty();
            }
        } else if (isFromFLPokeMapTargetChannel(message)) {
            if (embedDescriptionLength == 1) {
                return Optional.empty();
            }
        } else if (isFromPoGoBadgersDmBot(message)) {
            if (embedDescriptionLength <= 2) {
                return Optional.empty();
            }
        } else if (isFromPokemonMapsFloridaTargetChannels(message)) {
            if (embedDescriptionLength <= 8) {
                return Optional.empty();
            }
        } else if (isFromPoGoSJ1TargetChannels(message)) {
            if (embedTitle.contains("(?/?/?)")
                    || embedDescription.contains("(?/?/?)") || embedDescription.contains("CP:?") || embedDescription.contains("(L?)")) {
                return Optional.empty();
            }
        } else if (isFromCVMTargetChannels(message)) {
            if (embedTitle.contains("?% ?cp (L?)") || embedDescription.contains("?% ?/ ?/ ?")) {
                return Optional.empty();
            }
        } else if (isFromOCScansTargetChannels(message)) {
            if (embedDescriptionLength <= 6) {
                return Optional.empty();
            }
        } else if (isFromAzPoGoMapTargetChannels(message)) {
            if (embedDescriptionLength <= 3) {
                return Optional.empty();
            }
        } else if (isFromPokeHunterEliteTargetChannels(message)) {
            if (embedDescriptionLength <= 4) {
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
                .despawnTime(extractDespawnTime(message, compiledText))
                .build();
        return Optional.of(pokemonSpawn);
    }

    public static String compileMessageText(final Message message) {
        Verify.verify(message.getEmbeds().size() == 1);
        final MessageEmbed messageEmbed = message.getEmbeds().get(0);

        final StringBuilder compiler = new StringBuilder();
        Optional.of(message.getAuthor()).map(User::getName).ifPresent(name -> compiler.append(name).append(System.lineSeparator()));
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

        final String compiledText = SpawnMessageParsingUtils.replaceEmojiWithPlainText(compiler.toString());
        // Sources that has non-map links that needs to be resolved using redirection, needs to be passed to this
        // replacement process
        if (isFromUtahPoGoTargetChannels(message)) {
            return LocationLinkParsingUtils.replaceMapRedirectingUrls(compiledText);
        }
        return compiledText;
    }

    private static Optional<Instant> extractDespawnTime(final Message message, final String compiledText) {
        // TODO: enable despawn parsing for all servers and remove this gating method
        if (isFromPogoSaTargetChannels(message)
                || isFromPogoChChTargetChannels(message)
                || isFromPoGoAlerts847TargetChannels(message)
                || isFromPokeHunterEliteTargetChannels(message)
                || isFromAzPoGoMapTargetChannels(message)
                || isFromPokemonMapsFloridaTargetChannels(message)
                || isFromFLPokeMapTargetChannel(message)
                || isFromAlphaPokesTargetChannel(message)
                || isFromTallyPokemonHuntersTargetChannels(message)
                || isFromPoGoSouthernMassTargetChannels(message)) {
            return DespawnTimeParserUtils.extractDespawnTime(compiledText);
        } else if (isFromPokemonOnTrentTargetChannels(message)) {
            return DespawnTimeParserUtils.extractLowConfidenceDespawnTime(compiledText);
        }
        return Optional.empty();
    }

    private static boolean isFromFLPokeMapTargetChannel(@Nonnull final Message message) {
        return message.getChannelType() == ChannelType.PRIVATE
                && message.getAuthor().getIdLong() == USER_ID_FLPM_ALERT_BOT;
    }

    private static boolean isFromAlphaPokesTargetChannel(@Nonnull final Message message) {
        if (message.getChannel().getType() == ChannelType.PRIVATE) {
            return message.getAuthor().getIdLong() == USER_ID_AP_ALERT_BOT;
        } else if (!message.getAuthor().isBot() || message.getChannel().getType() != ChannelType.TEXT
                || message.getGuild().getIdLong() != SERVER_ID_ALPHAPOKES) {
            return false;
        }

        // Target some specific channels
        final long channelId = message.getChannel().getIdLong();
        if (channelId == DiscordEntityConstants.CHANNEL_ID_ALPHAPOKES_ULTRARARE_TEST) {
            return true;
        }

        // Broader set of channels for which we don't want to manage channel IDs by hand, but instead match
        // based on channel name and category it's under.
        final String channelName = message.getChannel().getName();
        final String categoryId = Optional.ofNullable(message.getCategory()).map(Category::getId).orElse("");
        switch (categoryId) {
            case "367523728491544577": // ALPHARETTA
                return channelName.contains("spawn"); // target channels end with "spawns"
            case "367520522659168256": // DOWNTOWN ATLANTA
            case "367346018272018437": // AUSTIN
            case "360981255728267264": // JACKSONVILLE
                return !channelName.matches(".*(chat|raid|custom_filters|vacation).*");
//            case "382579319119020042":
//                // NEW ORLEANS: looks dead - commenting out
//                return false;
            default:
                return false;
        }
    }

    private static boolean isFromSouthwestPokemonTargetChannel(final Message message) {
        if (!message.getAuthor().isBot() || message.getChannel().getType() != ChannelType.TEXT
                || message.getGuild().getIdLong() != DiscordEntityConstants.SERVER_ID_SOUTHWEST_POKEMON) {
            return false;
        }

        final String channelName = message.getChannel().getName();
        final String categoryId = Optional.ofNullable(message.getCategory()).map(Category::getId).orElse("");
        switch (categoryId) {
            case "628441307391590401": // Balham
            case "628441732429774878": // Bromley
            case "628465220599152650": // Canary Wharf
            case "628438147046572042": // Central East
            case "628438559992446977": // Central West
            case "628442194801328168": // Charlton
            case "628442157195198505": // Chingford
            case "628442601082716166": // Clapton
            case "628442653473636352": // Croydon
            case "628442991832465418": // Croydon South
            case "628443206626836511": // Dulwich
            case "628443541286420501": // Ealing
            case "628443616121061430": // Earley
            case "628443914596122624": // Enfield
            case "628443955192922113": // Epping CM5
            case "628444283178844171": // Greenwich
            case "628444801536098335": // Hampstead
            case "628445336293212180": // Harrow
            case "628445364759822346": // Holloway
            case "628445848606474241": // Lewisham
            case "632000177183653888": // Leyton
            case "632000210490621953": // Leytonstone
            case "628445905258938378": // Mitcham
            case "629198438038372352": // Northeast
            case "628438863710388244": // Northwest
            case "628446208762970112": // Ongar CM16
            case "628446248499806218": // Purley
            case "628440154767622164": // Southeast
            case "628447082310664212": // Stoke Newington Woodberry
            case "632000237719912458": // Stratford
            case "628448185299370003": // Stratham
            case "628448657414422548": // Tooting
            case "628448944049094666": // Wimbledon
            case "628449183447384064": // Woodgreen & Haringey
            case "628449277890265108": // Woodley
            case "628450305129840650": // Woolwich
            case "628441645095976960": // barnet
            case "628440788073971736": // southwest
            case "628448904631025674": // wathamstow
                return !channelName.contains("quest")
                        && !channelName.contains("raid")
                        && !channelName.contains("egg")
                        && !channelName.contains("lure");
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

    private static boolean isFromPokeHunterEliteTargetChannels(final Message message) {
        if (!message.getAuthor().isBot() || message.getChannelType() != ChannelType.TEXT || message.getGuild().getIdLong() != SERVER_ID_POKE_HUNTER_ELITE) {
            return false;
        }

        if (CHANNEL_IDS_POKE_HUNTER_ELITE.contains(message.getChannel().getIdLong())) {
            return true;
        }

        final Long categoryId = Optional.ofNullable(message.getCategory())
                .map(Category::getIdLong)
                .orElse(null);
        return CATEGORY_IDS_POKE_HUNTER_ELITE.contains(categoryId)
                && message.getChannel().getName().matches("(.*-iv-pokemon)");
    }

    private static boolean isFromAzPoGoMapTargetChannels(final Message message) {
        return message.getAuthor().isBot()
                && message.getChannelType() == ChannelType.TEXT
                && message.getGuild().getIdLong() == SERVER_ID_AZ_POGO_MAP
                && Optional.ofNullable(message.getCategory()).map(Category::getIdLong).filter(CATEGORY_IDS_AZ_POGO_MAP::contains).isPresent();
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
        return message.getChannelType() == ChannelType.TEXT
                && message.getAuthor().isBot()
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
                && message.getChannel().getIdLong() == 553598599670398983L; // channel: best-of-the-best
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

    private static boolean isFromPogoSaTargetChannels(final Message message) {
        return message.getAuthor().isBot()
                && message.getChannelType() == ChannelType.TEXT
                && message.getGuild().getIdLong() == SERVER_ID_POGO_SA
                && Optional.ofNullable(message.getCategory()).map(Category::getIdLong).filter(CATEGORY_IDS_POGO_SA::contains).isPresent();
    }

    private static boolean isFromPokemonOnTrentTargetChannels(final Message message) {
        if (!message.getAuthor().isBot() || message.getChannelType() != ChannelType.TEXT || message.getGuild().getIdLong() != SERVER_ID_POKEMON_ON_TRENT) {
            return false;
        }

        final String channelName = message.getChannel().getName();
        final Long categoryId = Optional.ofNullable(message.getCategory())
                .map(Category::getIdLong)
                .orElse(null);

        return CATEGORY_IDS_POKEMON_ON_TRENT.contains(categoryId)
                && channelName.matches("(.*spawns|heroes|pvp|rare-spawns|rares.*)");
    }

    private static boolean isFromTallyPokemonHuntersTargetChannels(final Message message) {
        if (!message.getAuthor().isBot() || message.getChannelType() != ChannelType.TEXT || message.getGuild().getIdLong() != SERVER_ID_TALLY_POKEMON_HUNTERS) {
            return false;
        }

        final String channelName = message.getChannel().getName();
        final Long categoryId = Optional.ofNullable(message.getCategory())
                .map(Category::getIdLong)
                .orElse(null);

        // TODO: Add ditto parsing for this source (channel: ditto_feed)
        // Not bothering to exclude the ditto channel though
        return CATEGORY_IDS_TALLY_POKEMON_HUNTERS.contains(categoryId)
                && channelName.matches("(.*feed.*)");
    }

    private static boolean isFromPoGoSouthernMassTargetChannels(final Message message) {
        if (!message.getAuthor().isBot() || message.getChannelType() != ChannelType.TEXT || message.getGuild().getIdLong() != SERVER_ID_POGO_SOUTHERN_MASS) {
            return false;
        }

        final String channelName = message.getChannel().getName();
        final Long categoryId = Optional.ofNullable(message.getCategory())
                .map(Category::getIdLong)
                .orElse(null);

        // TODO: This source has special channel for high attack spawns. If we add combat stats to be part of spawn
        // entry, we can verify that stats are parsing properly, not just the overall IV.
        return CATEGORY_IDS_POGO_SOUTHERN_MASS.contains(categoryId)
                && channelName.matches("(.*scans-rare-spawns.*)|(.*-scans-\\d+iv.*)");
    }

}
