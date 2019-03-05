package pogo.assistance.data.extraction.source.discord.pogosj1;

import static pogo.assistance.bot.di.DiscordEntityConstants.CHANNEL_ID_POGOSJ1_100IV;
import static pogo.assistance.bot.di.DiscordEntityConstants.CHANNEL_ID_POGOSJ1_100IVMAX;
import static pogo.assistance.bot.di.DiscordEntityConstants.CHANNEL_ID_POGOSJ1_TWEETS;

import com.google.common.base.Verify;
import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import pogo.assistance.data.extraction.source.discord.MessageProcessor;
import pogo.assistance.data.extraction.source.discord.SpawnMessageParsingUtils;
import pogo.assistance.data.model.pokemon.ImmutablePokemonSpawn;
import pogo.assistance.data.model.pokemon.ImmutablePokemonSpawn.Builder;
import pogo.assistance.data.model.pokemon.Pokedex;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokedexEntry.Gender;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

@Slf4j
public class PoGoSJSpawnMessageProcessorV2 implements MessageProcessor<PokemonSpawn> {

    private static final Set<Long> TARGET_CHANNELS = ImmutableSet.of(
            CHANNEL_ID_POGOSJ1_100IV,
            CHANNEL_ID_POGOSJ1_100IVMAX,
            CHANNEL_ID_POGOSJ1_TWEETS);

    private static final Pattern GENDER_PATTERN = Pattern.compile("[♀♂⚲]");
    private static final Pattern IV_PATTERN = Pattern.compile("(?<iv>[\\d\\.]+|\\?)%");
    private static final Pattern CP_PATTERN = Pattern.compile("CP[:\\s]?(?<cp>[\\d\\.]+|\\?)");
    private static final Pattern LEVEL_PATTERN = Pattern.compile("\\(L(?<level>[\\d]+|\\?)\\)");
    private static final Pattern ADS_STAT_PATTERN =
            Pattern.compile("\\(" +
                    "(?<attack>[\\d?]+)" + "/" +
                    "(?<defense>[\\d?]+)" + "/" +
                    "(?<stamina>[\\d?]+)" +
                    "\\)");
    private static final Pattern LOCATION_PATTERN = Pattern.compile("Location:(?<location>.*(?!\n))");
    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\(" +
            "(?<minutes>\\d+m)" + "[\\s]*" + "(?<seconds>\\d+s)" +
            "\\))");

    private static final Pattern UNOWN_PATTERN = Pattern.compile("(([U|u]nown)[\\s]*(?<letter>A-Za-z\\?!))");
    // Assumption: Disguise pokemon name is a single word and only has letters.
    // Assumption: Nothing else in the text contains the text "Ditto " other than the name
    private static final Pattern DITTO_PATTERN = Pattern.compile("((Ditto )[\\s]*(?<disguise>[A-Za-z]*))");

    @Override
    public boolean canProcess(@Nonnull final Message message) {
        return TARGET_CHANNELS.contains(message.getChannel().getIdLong()) && message.getAuthor().isBot();
    }

    @Override
    public Optional<PokemonSpawn> process(@Nonnull final Message message) {
        if (!message.getAuthor().isBot()) {
            return Optional.empty();
        }

        final MessageEmbed messageEmbed = message.getEmbeds().get(0);
        final String compiledText = message.getAuthor().getName() + " " +
                messageEmbed.getTitle() + " " + messageEmbed.getDescription();

        final int id = SpawnMessageParsingUtils.parsePokemonIdFromNovaBotSprite(messageEmbed.getThumbnail().getUrl());
        final Gender gender = extractGender(messageEmbed.getTitle());
        final PokedexEntry pokedexEntry;
        // Special handling for Ditto: Since the thumbnail shows the disguised pokemon, parsing ID from the thumbnail
        // url essentailly gives us the wrong pokemon.
        final Matcher dittoMatcher = DITTO_PATTERN.matcher(compiledText);
        if (dittoMatcher.find()) {
            final String disguisePokemonName = dittoMatcher.group("disguise");
            Verify.verify(disguisePokemonName != null);
            final PokedexEntry disguisePokedexEntry = Pokedex.getPokedexEntryFor(disguisePokemonName, gender).orElseThrow(() ->
                    new UnsupportedOperationException("Failed to lookup Ditto's disguise pokemon: " + disguisePokemonName));
            Verify.verify(disguisePokedexEntry.getId() == id);
            pokedexEntry = Pokedex.getPokedexEntryFor("ditto", gender)
                    .orElseThrow(() -> new IllegalStateException("Failed to look up Ditto entry by name"));
        } else {
            pokedexEntry = Pokedex.getPokedexEntryFor(id, gender)
                    .orElseThrow(() -> new UnsupportedOperationException("Unable to lookup dex entry with ID: " + id));
        }

        final Builder builder = ImmutablePokemonSpawn.builder();
        builder.from(SpawnMessageParsingUtils.parseGoogleMapQueryLink(messageEmbed.getUrl()));
        builder.pokedexEntry(pokedexEntry);

        extractLevel(compiledText).ifPresent(builder::level);
        extractCp(compiledText).ifPresent(builder::cp);
        extractIv(compiledText).ifPresent(builder::iv);
        extractLocation(compiledText).ifPresent(builder::locationDescription);

        // TODO: handle unown character detection
        // TODO: parse duration

        return Optional.of(builder.build());
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

    private static Optional<String> extractLocation(final String fullMessageText) {
        final Matcher matcher = LOCATION_PATTERN.matcher(fullMessageText);
        if (matcher.find()) {
            return Optional.ofNullable(matcher.group("location")).map(String::trim);
        }
        return Optional.empty();
    }

    private static Gender extractGender(final String fullMessageText) {
        if (fullMessageText.contains("♂")) {
            return Gender.MALE;
        } else if (fullMessageText.contains("♀")) {
            return Gender.FEMALE;
        } else if (fullMessageText.contains("⚲")) {
            return Gender.NONE;
        }
        return Gender.UNKNOWN;
    }

//    private void verifyConsistencyWithAuthoName(final Message message, final PokemonSpawn pokemonSpawn) {
//        // Parse fields from summary
//        final String summaryWithoutGenderSigns = message.getAuthor().getName().replaceAll("[♀♂⚲]", "").trim();
//        final Matcher summaryMatcher = MESSAGE_TITLE_PATTERN.matcher(summaryWithoutGenderSigns);
//        Verify.verify(summaryMatcher.matches());
//        final String pokemonName = summaryMatcher.group("pokemon").trim().toLowerCase();
//        final Gender gender = extractGender(message.getAuthor().getName());
//        final int cp = Integer.parseInt(summaryMatcher.group("cp"));
//        final double iv = Double.parseDouble(summaryMatcher.group("iv"));
//        final int level = Integer.parseInt(summaryMatcher.group("level"));
//
//        // Match data from summary with the data parsed from embed
//        final PokedexEntry pokedexEntry = pokemonSpawn.getPokedexEntry();
//        if (!pokedexEntry.getName().toLowerCase().contains(pokemonName)) {
//            // Names can mismatch in some benign cases such as Unowns. So just logging a mismatch as warning.
//            log.warn("Pokemon name mismatched between dex entry ({}) and parsed name ({})",
//                    pokedexEntry.getName(), pokemonName);
//        }
//        Verify.verify(pokedexEntry.getGender().equals(gender));
//        Verify.verify(pokemonSpawn.getIv().isPresent() && pokemonSpawn.getIv().get().compareTo(iv) == 0);
//        Verify.verify(pokemonSpawn.getCp().isPresent() && pokemonSpawn.getCp().get().compareTo(cp) == 0);
//        Verify.verify(pokemonSpawn.getLevel().isPresent() && pokemonSpawn.getLevel().get().compareTo(level) == 0);
//    }

}
