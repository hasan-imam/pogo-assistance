package pogo.assistance.data.extraction.source.discord.nycpokemap;

import com.google.common.base.Verify;
import io.jenetics.jpx.Point;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.entities.Message;
import org.jetbrains.annotations.NotNull;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.data.extraction.source.discord.MessageProcessor;
import pogo.assistance.data.extraction.source.discord.SpawnMessageParsingUtils;
import pogo.assistance.data.model.pokemon.ImmutablePokemonSpawn;
import pogo.assistance.data.model.pokemon.Pokedex;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokedexEntry.Gender;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

@Slf4j
public class NycPokeMapSpawnMessageProcessor implements MessageProcessor<PokemonSpawn> {

    /*
     * Example titles:
     *  * Geodude (100%) - (CP: 233) - (Level: 7)
     *  * Yanma - Male - (100%) - (CP: 630) - (Level: 15)
     *  * [Far Rockaway] Gyarados (100%) - (CP: 2718) - (Level: 29)
     *  * [LIC] Beldum - (100%) - (CP: 307) - (Level: 11)
     */
    private static final Pattern MESSAGE_TITLE_PATTERN = Pattern.compile(
            // Some titles starts with a stray '\' so we consume that, if present
            "[\\\\]*" +
                    // Match location, if present
                    "(?<location>\\[.*\\])?" + "([\\s]*)"
                    /*
                     * Match pokemon name which can contain special characters like gender sign (e.g. "Nidoran♀"),
                     * hyphen (e.g. "Unown - K"), apostrophe (e.g. "Farfetch'd") etc. Matches until it encounters "Male"
                     * or "Female" strings to prevent consuming gender info into the name.
                     */
                    + "(?<pokemon>([\\w\\s'♀♂\\-](?!Male|Female))*)?" + "([\\s-]*)"
                    // Match gender, if present
                    + "(?<gender>Male|Female)?" + "([\\s-]*)"
                    // Match iv - which always ends with '%' and is wrapped with '()' braces
                    + "(?<iv>\\([\\d\\.]+%\\))?" + "([\\s-]*)"
                    // Match cp - which is wrapped with '()' braces and the integer value is prefixed by "CP: "
                    + "(?<cp>\\(CP: [\\d]+\\))?" + "([\\s-]*)"
                    // Match level - which is wrapped with '()' braces and the integer value is prefixed by "Level: "
                    + "(?<level>\\(Level: [\\d]+\\))?" + "([\\s]*)");

    // Example line with URL: "Map: <https://nycpokemap.com/#40.86878143,-73.79269157>"
    private static final Pattern NYCPOKEMAP_QUERY_URL_PATTERN =
            Pattern.compile("(.*)(https://nycpokemap\\.com/#)(?<latitude>[-\\d\\.]+),(?<longitude>[-\\d\\.]+)(.*)");

    @Override
    public boolean canProcess(@Nonnull final Message message) {
        return message.getGuild().getIdLong() == DiscordEntityConstants.SERVER_ID_NYCPOKEMAP
                && message.getCategory().getIdLong() == DiscordEntityConstants.CATEGORY_ID_IV_CP_LVL_ALERTS
                && message.getAuthor().isBot();
    }

    @Override
    public Optional<PokemonSpawn> process(@NotNull final Message message) {
        if (!message.getAuthor().isBot()) {
            return Optional.empty();
        }

        final String contentStripped = message.getContentStripped();
        final String[] messageLines = contentStripped.split("\n");

        final Matcher titleMatcher = MESSAGE_TITLE_PATTERN.matcher(messageLines[0].trim());
        Verify.verify(titleMatcher.find(), "Title didn't match expected pattern: %s", messageLines[0].trim());
        final String pokemonName = titleMatcher.group("pokemon")
                .replaceAll("[\\s\\-]+", " ") // can contain separator dashes and white spaces
                .trim();
        final Gender gender = Optional.ofNullable(titleMatcher.group("gender"))
                .map(genderString -> genderString.equalsIgnoreCase("Male") ? Gender.MALE
                        : (genderString.equalsIgnoreCase("Female") ? Gender.FEMALE : null))
                .orElse(Gender.UNKNOWN);
        final PokedexEntry pokedexEntry = Pokedex.getPokedexEntryFor(pokemonName, gender)
                .orElseThrow(() -> new UnsupportedOperationException("Unable to lookup dex entry with name: " + pokemonName));

        final Optional<Double> iv = Optional.ofNullable(titleMatcher.group("iv"))
                .map(ivString -> ivString.replaceAll("[\\D]*", ""))
                .map(Double::parseDouble);
        final Optional<Integer> cp = Optional.ofNullable(titleMatcher.group("cp"))
                .map(cpString -> cpString.replaceAll("[\\D]*", ""))
                .map(Integer::parseInt);
        final Optional<Integer> level = Optional.ofNullable(titleMatcher.group("level"))
                .map(levelString -> levelString.replaceAll("[\\D]*", ""))
                .map(Integer::parseInt);
        final Optional<String> area = Optional.ofNullable(titleMatcher.group("location"))
                .map(areaString -> areaString.replaceAll("[\\[\\]]", ""))
                .filter(areaString -> !areaString.isEmpty());

        final Point point = Stream.of(messageLines)
                .filter(line -> line.contains("google") && line.contains("map"))
                .map(SpawnMessageParsingUtils::parseGoogleMapQueryLink)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Could not find map URL in the message: " + contentStripped));

        final ImmutablePokemonSpawn.Builder builder = ImmutablePokemonSpawn.builder();
        builder.from(point);
        builder.pokedexEntry(pokedexEntry);
        area.ifPresent(builder::locationDescription);
        iv.ifPresent(builder::iv);
        cp.ifPresent(builder::cp);
        level.ifPresent(builder::level);

        return Optional.of(builder.build());
    }

}
