package pogo.assistance.data.extraction.source.discord;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.base.Verify;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import io.jenetics.jpx.Point;
import io.jenetics.jpx.WayPoint;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.core.entities.Message;
import pogo.assistance.data.model.ImmutableSourceMetadata;
import pogo.assistance.data.model.SourceMetadata;
import pogo.assistance.data.model.pokemon.CombatStats;
import pogo.assistance.data.model.pokemon.ImmutableCombatStats;
import pogo.assistance.data.model.pokemon.PokedexEntry.Gender;

@UtilityClass
public class SpawnMessageParsingUtils {

    /**
     * Example map URLs:
     *  - http://maps.google.com/maps?q=37.4332914692569,-122.115651980398
     *  - https://www.google.com/maps/search/?api=1&query=37.5542702090763,-77.4791150614027
     */
    private static final Pattern GOOGLE_MAP_QUERY_URL =
            Pattern.compile("(.+(q=|query=))(?<latitude>[-\\d\\.]+),(?<longitude>[-\\d\\.]+)(.*)");

    /**
     * Example matched strings:
     *  - 100.00%
     *  - 1.1%
     *  - 1%
     *  - (100.00%)
     */
    private static final Pattern IV_PATTERN = Pattern.compile("\\(?" + "(?<iv>[\\d\\.]{1,6}|\\?)%" + "\\)?");

    /**
     * Example matched strings:
     *  - (15/15/15)
     *  - (15|15|15)
     *  - 15/15/15
     *  - 15 / 15 / 15
     *  - Atk: 14 / Def: 6 / Sta: 13
     *  - (Atk: 14 / Def: 6 / Sta: 13)
     *  - (14A / 6D / 13S)
     *  - A:15 D:15 S:15
     *
     * Verify online: https://regex101.com/r/vsxUsw/2
     */
    private static final Pattern ADS_STAT_PATTERN = Pattern.compile(
            "(^|\\(|\\|/|\\s+)" + // expects some delimiting things at the beginning, such as: white space, '(', '/', '|' etc. separators at the beginning
                    "(Atk:|A[:]?)?\\s?" + "(?<attack>[\\d?]{1,2})" + "A?" +
                    "[/\\|\\s]+" +
                    "(Def:|D[:]?)?\\s?" + "(?<defense>[\\d?]{1,2})" + "D?" +
                    "[/\\|\\s]+" +
                    "(Sta:|S[:]?)?\\s?" + "(?<stamina>[\\d?]{1,2})" + "S?" +
                    "($|\\)|\\|/|\\s+)"); // expects some delimiting things at the end

    /**
     * Example matched strings:
     *  - CP 123
     *  - CP:123
     *  - CP: 123
     *  - (CP: 123)
     *  - CP123
     *  - 123cp
     *
     * Verify online: https://regex101.com/r/bHvnAf/8
     */
    private static final Pattern CP_PATTERN = Pattern.compile(
            "(^|\\(|\\|/|\\s+)" + // expects some delimiting things at the beginning, such as: white space, '(', '/', '|' etc. separators at the beginning
                    "(((CP)[:\\s]*(?<cp>[\\d\\?]{1,4}))" + // 'CP', then digits
                    "|" +
                    "((?<cp2>[\\d\\?]{1,4})[:\\s]*(?i)cp))" + // digits, then 'CP'
                    "($|\\)|\\|/|\\s+)"); // expects some delimiting things at the end

    /**
     * Example matched strings:
     *  - Level 13
     *  - Level13
     *  - (L13)
     *  - L13
     *  - L:13
     *  - lvl 13
     *  - lvl: 13
     *
     * Verify online: https://regex101.com/r/lxvJaS/6
     */
    private static final Pattern LEVEL_PATTERN = Pattern.compile(
            "(^|\\(|\\|/|\\s+)" + // expects beginning of line, white space, '(', '/', '|' etc. separators at the beginning
                    "(Level[:\\s]?|L[:]?|(Lvl|lvl)[:\\s]+)" +
                    "(?<level>[\\d\\?]{1,2})" +
                    "($|\\)|\\|/|\\s+)");

    private static final Pattern FEMALE_GENDER_PATTERN = Pattern.compile("♀|:female:|Female");
    private static final Pattern MALE_GENDER_PATTERN = Pattern.compile("♂|:male:|Male");

    public static Point parseGoogleMapQueryLink(final String url) {
        final Matcher mapUrlMatcher = GOOGLE_MAP_QUERY_URL.matcher(url);
        Verify.verify(mapUrlMatcher.find());
        return WayPoint.of(Double.parseDouble(mapUrlMatcher.group("latitude")), Double.parseDouble(mapUrlMatcher.group("longitude")));
    }

    @Deprecated
    public static Gender parseGenderFromSign(@Nullable final String sign) {
        if (sign == null || sign.isEmpty()) {
            return Gender.UNKNOWN;
        }

        switch (sign) {
            case "♀":
                return Gender.FEMALE;
            case "♂":
                return Gender.MALE;
            case "⚲":
                return Gender.NONE;
            default:
                throw new IllegalArgumentException("Unrecognized/missing gender: " + sign);
        }
    }

    /**
     * Supported formats:
     *  - "♂" or "♀" or "⚲"
     *  - "Female" or "Male"
     *  - some combination of above, e.g. "♀Female", "♂Male"
     *
     * Can give unintended result when the passed text have gender signs or words in some irrelevant part of the text body,
     * e.g. word "Male" somewhere in text but it wasn't suppose to mean gender of the pokemon.
     */
    public static Optional<Gender> extractGender(final String text) {
        if (FEMALE_GENDER_PATTERN.matcher(text).find()) {
            return Optional.of(Gender.FEMALE);
        } else if (MALE_GENDER_PATTERN.matcher(text).find()) {
            return Optional.of(Gender.MALE);
        } else if (text.contains("⚲")) {
            return Optional.of(Gender.NONE);
        }

        return Optional.empty();
    }

    public static Optional<Integer> extractCp(final String text) {
        final Matcher cpMatcher = CP_PATTERN.matcher(text);
        Verify.verify(cpMatcher.find());
        final String cp = MoreObjects.firstNonNull(cpMatcher.group("cp"), cpMatcher.group("cp2"));
        if (!cp.equals("?")) {
            return Optional.ofNullable(Ints.tryParse(cp));
        }
        return Optional.empty();
    }

    public static Optional<Integer> extractLevel(final String text) {
        final Matcher levelMatcher = LEVEL_PATTERN.matcher(text);
        Verify.verify(levelMatcher.find());
        if (!levelMatcher.group("level").equals("?")) {
            return Optional.ofNullable(Ints.tryParse(levelMatcher.group("level")));
        }
        return Optional.empty();
    }

    public static Optional<CombatStats> extractCombatStats(final String textContainingAds, final String textContainingIv) {
        final Optional<Double> extractedIv = extractIv(textContainingIv);
        final Optional<CombatStats> combatStats = extractCombatStats(textContainingAds);
        if (!extractedIv.isPresent() && !combatStats.isPresent()) {
            return Optional.empty();
        }
        Verify.verify(extractedIv.isPresent() && combatStats.isPresent(),
                "Both ADS stats and IVs were expected to be present in input");
        final Double ivFromCombatStats = combatStats.get().combinedIv().get();
        final Double ivFromExtraction = extractedIv.get();
        // Different data sources round doubles off differently so for validation,
        // we just check the difference between the parsed and calculated values.
        Verify.verify(Math.abs(ivFromCombatStats - ivFromExtraction) < 1.0,
                "IV from ADS stats (%s) and extraction (%s) mismatched", ivFromCombatStats, ivFromExtraction);
        return combatStats;
    }

    public static SourceMetadata buildSourceMetadataFromMessage(final Message message) {
        if (message.getChannelType().isGuild()) {
            return ImmutableSourceMetadata.builder()
                    .sourceName(message.getGuild().getName())
                    .build();
        } else {
            // If non guild message, just name the source using the message author
            return ImmutableSourceMetadata.builder()
                    .sourceName(message.getAuthor().getName())
                    .build();
        }
    }

    private static Optional<CombatStats> extractCombatStats(final String textContainingAds) {
        final Matcher adsMatcher = ADS_STAT_PATTERN.matcher(textContainingAds);
        if (!adsMatcher.find()) {
            return Optional.empty();
        }
        return Optional.of(ImmutableCombatStats.builder()
                .attackIv(Ints.tryParse(adsMatcher.group("attack")))
                .defenseIv(Ints.tryParse(adsMatcher.group("defense")))
                .staminaIv(Ints.tryParse(adsMatcher.group("stamina")))
                .build());
    }

    private static Optional<Double> extractIv(final String text) {
        final Matcher ivMatcher = IV_PATTERN.matcher(text);
        if (ivMatcher.find()) {
            return Optional.ofNullable(Doubles.tryParse(ivMatcher.group("iv")));
        }
        return Optional.empty();
    }
}
