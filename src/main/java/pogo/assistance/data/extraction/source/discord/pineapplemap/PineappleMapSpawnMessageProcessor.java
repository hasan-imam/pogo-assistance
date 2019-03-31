package pogo.assistance.data.extraction.source.discord.pineapplemap;

import static pogo.assistance.bot.di.DiscordEntityConstants.SERVER_ID_CHICAGOLAND_POGO;
import static pogo.assistance.bot.di.DiscordEntityConstants.SERVER_ID_PINEAPPLE;
import static pogo.assistance.bot.di.DiscordEntityConstants.SPAWN_CHANNEL_IDS_CHICAGOLAND_POGO;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

import io.jenetics.jpx.Point;
import io.jenetics.jpx.WayPoint;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import pogo.assistance.data.extraction.source.discord.MessageProcessor;
import pogo.assistance.data.extraction.source.discord.SpawnMessageParsingUtils;
import pogo.assistance.data.extraction.source.discord.novabot.NovaBotProcessingUtils;
import pogo.assistance.data.model.pokemon.ImmutablePokemonSpawn;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

/**
 * Processes spawn messages on Pineapple and Chicagoland channels. Currently parses only the common format. The server also seems to use message format akin to
 * some of the other channels, but those posts are rare and old. They most likely used NovaBot at some point but not sure if they will do that again.
 * <p>
 * TODO: may have to expand this to another server using similar messaging format
 * TODO: not ditto or unowns posted so current impl doesnt worry about them, but probably should keep an eye out for those posts
 * TODO: they don't seem to distinguish between castforms in the thumbnails, but the moveset and boost details make it clear what type it is
 */
@Slf4j
public class PineappleMapSpawnMessageProcessor implements MessageProcessor<PokemonSpawn> {

    /**
     * IV appears to be only in the title, always inside '()' braces and ending with '%'.
     */
    private static final Pattern IV_PATTERN = Pattern.compile("\\((?<iv>[\\d\\.]+)%\\)");
    /**
     * CPs appear as "CP 123" or "CP:123".
     */
    private static final Pattern CP_PATTERN = Pattern.compile("CP[:\\s]?(?<cp>[\\d\\.]+)");
    /**
     * Levels appear as "Level 13" or "(L13)".
     */
    private static final Pattern LEVEL_PATTERN = Pattern.compile("(Level[\\s]?|\\(L)(?<level>[\\d]+)");
    /**
     * A/D/S stats appear as "Atk: 14 / Def: 6 / Sta: 13" - all in a single line. Line doesn't contain any other info.
     */
    private static final Pattern ADS_STAT_PATTERN = Pattern.compile(
            "Atk:[\\s]?(?<attack>[\\d?]+)" + "[/\\s]?" +
            "Def:[\\s]?(?<defense>[\\d?]+)" + "[/\\s]?" +
            "Sta:[\\s]?(?<stamina>[\\d?]+)");

    // Example line with URL: "Map: <https://nycpokemap.com/#40.86878143,-73.79269157>"
    private static final Pattern GOOGLEMAP_URL_PATTERN =
            Pattern.compile("\\((.*google\\.com/maps\\?q=)(?<latitude>[-\\d\\.]+),(?<longitude>[-\\d\\.]+)\\)");

    @Override
    public boolean canProcess(@Nonnull final Message message) {
        if (!message.getAuthor().isBot()) {
            return false;
        }
        return isFromPineappleTargetChannel(message) || isFromChicagolandTargetChannel(message);
    }

    @Override
    public Optional<PokemonSpawn> process(@Nonnull final Message message) {
        final MessageEmbed messageEmbed = message.getEmbeds().get(0);
        final String compiledText = messageEmbed.toJSONObject().toString();

        final PokedexEntry pokedexEntry = NovaBotProcessingUtils.inferPokedexEntryFromNovaBotAssetUrl(
                messageEmbed.getThumbnail().getUrl(),
                // Genderless pokemons simply don't have any gender mentioned. Although that's indistinguishable from unknown gender, we will assume that all
                // posts that intentionally skip gender only does when genderless. Fair assumption since all processed messages should have verified pokemons.
                SpawnMessageParsingUtils.extractGender(compiledText).orElse(null));

        final ImmutablePokemonSpawn.Builder builder = ImmutablePokemonSpawn.builder();
        builder.from(extractLocation(compiledText)
                .orElseThrow(() -> new IllegalArgumentException("Failed to parse location from compiled text: " + compiledText)));
        builder.pokedexEntry(pokedexEntry);
        builder.sourceMetadata(SpawnMessageParsingUtils.buildSourceMetadataFromMessage(message));

        extractLevel(compiledText).ifPresent(builder::level);
        extractCp(compiledText).ifPresent(builder::cp);
        extractIv(compiledText).ifPresent(builder::iv);

        // TODO: handle unown character detection
        // TODO: parse duration
        // TODO: parse ADS stats

        return Optional.of(builder.build());
    }

    private static Optional<Point> extractLocation(final String fullMessageText) {
        final Matcher matcher = GOOGLEMAP_URL_PATTERN.matcher(fullMessageText);
        if (matcher.find()) {
            return Optional.of(WayPoint.of(
                    Double.parseDouble(matcher.group("latitude")),
                    Double.parseDouble(matcher.group("longitude"))));
        }
        return Optional.empty();
    }

    private static Optional<Integer> extractLevel(final String fullMessageText) {
        final Matcher matcher = LEVEL_PATTERN.matcher(fullMessageText);
        if (matcher.find()) {
            return Optional.ofNullable(matcher.group("level"))
                    .map(String::trim)
                    .filter(s -> s.matches("\\d+"))
                    .map(Integer::parseInt);
        }
        return Optional.empty();
    }

    private static Optional<Integer> extractCp(final String fullMessageText) {
        final Matcher matcher = CP_PATTERN.matcher(fullMessageText);
        if (matcher.find()) {
            return Optional.ofNullable(matcher.group("cp"))
                    .map(String::trim)
                    .filter(s -> !s.contains("?"))
                    //                    .filter(s -> s.matches("\\d+"))
                    .map(Integer::parseInt);
        }
        return Optional.empty();
    }

    private static Optional<Double> extractIv(final String fullMessageText) {
        final Matcher matcher = IV_PATTERN.matcher(fullMessageText);
        if (matcher.find()) {
            return Optional.ofNullable(matcher.group("iv"))
                    .map(String::trim)
                    .filter(s -> !s.contains("?"))
                    //                    .filter(s -> s.matches("[\\d\\.]+"))
                    .map(Double::parseDouble);
        }
        return Optional.empty();
    }

    private static boolean isFromChicagolandTargetChannel(final Message message) {
        return Optional.ofNullable(message.getGuild()).map(Guild::getIdLong).filter(id -> id == SERVER_ID_CHICAGOLAND_POGO).isPresent()
                && SPAWN_CHANNEL_IDS_CHICAGOLAND_POGO.contains(message.getChannel().getIdLong());
    }

    /**
     * @implNote
     *      Pineapple map has a lot of channels for which we don't want to manage channel IDs by hand. Instead, we match based on channel id and category it's
     *      under.
     */
    private static boolean isFromPineappleTargetChannel(final Message message) {
        if (!Optional.ofNullable(message.getGuild()).map(Guild::getIdLong).filter(id -> id == SERVER_ID_PINEAPPLE).isPresent()) {
            return false;
        }

        final String channelName = message.getChannel().getName();
        final String categoryId = Optional.ofNullable(message.getCategory())
                .map(Category::getId)
                .orElse(null);
        if (categoryId == null) { // Some channels do not fall under a category (ungrouped)
            return false;
        }

        switch (message.getCategory().getId()) {
            case "525091923334266901": // UNION CITY category
            case "524609321091203113": // NEWARK category
            case "524183331210788875": // FERMONT category
            case "525029303973576715": // HAYWARD category
            case "531004675038904341": // SAN LEANDRO category
            case "533949853265297439": // SAN RAMON category
                return !channelName.contains("raid") && !channelName.contains("chat");
            default:
                return false;
        }
    }

}
