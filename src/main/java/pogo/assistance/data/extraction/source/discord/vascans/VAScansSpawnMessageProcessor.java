package pogo.assistance.data.extraction.source.discord.vascans;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

import com.google.common.base.Verify;
import io.jenetics.jpx.Point;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.data.extraction.source.discord.MessageProcessor;
import pogo.assistance.data.extraction.source.discord.SpawnMessageParsingUtils;
import pogo.assistance.data.model.pokemon.CombatStats;
import pogo.assistance.data.model.pokemon.ImmutablePokemonSpawn;
import pogo.assistance.data.model.pokemon.Pokedex;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

public class VAScansSpawnMessageProcessor implements MessageProcessor<PokemonSpawn> {

    /*
     * Example title: "Nidoran-f (15/15/15) L35 CP:2513 ♀"
     * Gender segment is missing some times. All other sections are present quite consistently.
     */
    private static final Pattern MESSAGE_TITLE_PATTERN = Pattern.compile(
            "(?<pokemon>[\\w\\s\\-]+)" +
                    "(?<stats>\\(.+\\))?" + "(\\s*)" +
                    "(L(?<level>[\\d]+))?" + "(\\s*)" +
                    "(CP:(?<cp>[\\d]+))?" + "(\\s*)" +
                    "(?<gender>[♀♂⚲]+)?" + "(.*)");

    // Example description line: "IV:100.00% Boost: rain"
    private static final Pattern IV_BOOST_DESCRIPTION_MATCHER = Pattern.compile("(IV:(?<iv>[-\\d\\.]+)%)?(.*)");

    /*
     * Example thumbnail URLs:
     *  - https://raw.githubusercontent.com/seehuge/prdmicons/master/pokemon_icon_007_00.png
     *  - https://raw.githubusercontent.com/seehuge/prdmicons/master/pokemon_icon_088_73.png
     */
    private static final Pattern EMBED_THUMBNAIL_URL_WITH_ID_PATTERN = Pattern.compile("(.+/pokemon_icon_)(?<id>\\d+)_(\\d+)(\\.png)");
    /*
     * Example thumbnail URLs:
     *  - http://www.pokestadium.com/sprites/xy/jigglypuff.gif
     *  - http://www.pokestadium.com/sprites/xy/bulbasaur.gif
     */
    private static final Pattern EMBED_THUMBNAIL_URL_WITH_NAME_PATTERN = Pattern.compile("(.+/)(?<pokemon>.+)(\\.gif)");

    @Override
    public boolean canProcess(@Nonnull final Message message) {
        return message.getChannel().getIdLong() == DiscordEntityConstants.CHANNEL_ID_VASCANS_HUNDOS
                && message.getAuthor().isBot();
    }

    @Override
    public Optional<PokemonSpawn> process(@Nonnull final Message message) {
        final MessageEmbed messageEmbed = message.getEmbeds().get(0);
        final Matcher titleMatcher = MESSAGE_TITLE_PATTERN.matcher(messageEmbed.getTitle());
        Verify.verify(titleMatcher.find());

        final PokedexEntry pokedexEntry = Pokedex.getPokedexEntryFor(
                parsePokemonIdFromThumbnailUrl(messageEmbed.getThumbnail().getUrl()),
                SpawnMessageParsingUtils.parseGenderFromSign(titleMatcher.group("gender")))
                .orElseThrow(() -> new IllegalArgumentException(String.format(
                        "Failed to infer pokedex entry from title: '%s' and thumbnail URL: '%s'",
                        messageEmbed.getTitle(),
                        messageEmbed.getThumbnail().getUrl())));

        // Some extra verification on the description so we detect (i.e. throw error) if message format changes
        final String[] descriptionLines = messageEmbed.getDescription().split("\n");
        Verify.verify(descriptionLines.length >= 5);
        final Point point = Stream.of(descriptionLines)
                .map(String::trim)
                .filter(line -> line.startsWith("[Google Maps]"))
                .findFirst()
                .map(SpawnMessageParsingUtils::parseGoogleMapQueryLink)
                .orElseThrow(() -> new IllegalArgumentException("Couldn't find map URL in description: " + messageEmbed.getDescription()));

        final ImmutablePokemonSpawn.Builder builder = ImmutablePokemonSpawn.builder();
        builder.from(point);
        builder.pokedexEntry(pokedexEntry);
        Optional.ofNullable(titleMatcher.group("level"))
                .map(Integer::parseInt)
                .ifPresent(builder::level);
        Optional.ofNullable(titleMatcher.group("cp"))
                .map(Integer::parseInt)
                .ifPresent(builder::cp);

        SpawnMessageParsingUtils.extractCombatStats(messageEmbed.getTitle(), descriptionLines[0])
                .flatMap(CombatStats::combinedIv)
                .ifPresent(builder::iv);

        // TODO: parse location from 5th line

        return Optional.of(builder.build());
    }

    private static int parsePokemonIdFromThumbnailUrl(final String url) {
        final Matcher thumbnailUrlWithIdMatcher = EMBED_THUMBNAIL_URL_WITH_ID_PATTERN.matcher(url);
        if (thumbnailUrlWithIdMatcher.find()) {
            return Integer.parseInt(thumbnailUrlWithIdMatcher.group("id"));
        } else {
            /*
             * This is a valueless pattern matching but executed so that we throw error if we find any unexpected url
             * pattern. Also, these URLs don't have th pokemon ID but the name. If we have a way to lookup ID from name
             * we would actually be able to parse IDs out of these URLs.
             */
            final Matcher thumbnailUrlWithNameMatcher = EMBED_THUMBNAIL_URL_WITH_NAME_PATTERN.matcher(url);
            Verify.verify(thumbnailUrlWithNameMatcher.find());
            return -1;
        }
    }

}
