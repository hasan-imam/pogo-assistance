package pogo.assistance.data.extraction.source.discord.pgan;

import com.google.common.base.Verify;
import com.google.common.primitives.Longs;
import io.jenetics.jpx.Point;
import io.jenetics.jpx.WayPoint;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import pogo.assistance.data.extraction.source.discord.GenericSpawnMessageProcessor;
import pogo.assistance.data.extraction.source.discord.MessageProcessor;
import pogo.assistance.data.extraction.source.discord.SpawnMessageParsingUtils;
import pogo.assistance.data.model.pokemon.*;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static pogo.assistance.bot.di.DiscordEntityConstants.USER_ID_PGAN_BOTS;

public class PGANSpawnMessageProcessor implements MessageProcessor<PokemonSpawn> {

    /**
     * Example URL:
     *  - https://beta.pogoalerts.net/?lat=32.9861643987888&lon=-96.8029907716483&encId=15064660849933713929&zoom=16
     */
    private static final Pattern PGAN_MAP_URL =
            Pattern.compile(".+lat=(?<latitude>[-\\d\\.]+)&lon=(?<longitude>[-\\d\\.]+).*");

    private static final Pattern DESPAWN_PERIOD_STRING =
            Pattern.compile("Despawns (exactly|approximately):.*\\((?<minutes>[0-5]?\\d)m\\)");

    @Override
    public boolean canProcess(@Nonnull final Message message) {
        return message.getChannelType() == ChannelType.PRIVATE
                && USER_ID_PGAN_BOTS.contains(message.getAuthor().getIdLong());
    }

    @Override
    public Optional<PokemonSpawn> process(@Nonnull final Message message) {
        final String compiledText = GenericSpawnMessageProcessor.compileMessageText(message);
        final String pokemonName = extractPokemonName(message);
        PokedexEntry pokedexEntry = Pokedex.getPokedexEntryFor(pokemonName, SpawnMessageParsingUtils.extractGender(compiledText).orElse(null))
                .orElseThrow(() -> new IllegalArgumentException("Failed to infer pokedex entry from pokemon name: " + pokemonName));
        if (isAlolan(pokemonName, compiledText)) {
            pokedexEntry = ImmutablePokedexEntry.builder().from(pokedexEntry).addForms(PokedexEntry.Form.ALOLAN).build();
        }
        final PokemonSpawn pokemonSpawn = ImmutablePokemonSpawn.builder()
                .from(extractLocationFromPganMapUrl(message.getEmbeds().get(0).getUrl()))
                .pokedexEntry(pokedexEntry)
                .iv(SpawnMessageParsingUtils.extractCombatStats(compiledText, compiledText).flatMap(CombatStats::combinedIv))
                .level(SpawnMessageParsingUtils.extractLevel(compiledText))
                .cp(SpawnMessageParsingUtils.extractCp(compiledText))
                .despawnTime(extractDespawnDuration(compiledText))
                .sourceMetadata(SpawnMessageParsingUtils.buildSourceMetadataFromMessage(message))
                .build();
        return Optional.of(pokemonSpawn);
    }

    private static String extractPokemonName(final Message message) {
        final MessageEmbed messageEmbed = message.getEmbeds().get(0);
        final String pokemonNameFromTitle = Optional.ofNullable(messageEmbed.getTitle())
                .map(String::trim)
                .map(title -> title.split("\\s"))
                .filter(strings -> strings.length > 0)
                .map(strings -> strings[0])
                .orElseThrow(() -> new IllegalArgumentException("Failed to parse pokemon name from message title"));
        final String thumbnailUrl = Optional.ofNullable(messageEmbed.getThumbnail())
                .map(MessageEmbed.Thumbnail::getUrl)
                .orElseThrow(() -> new IllegalArgumentException("PGAN spawn messages are expected to contain thumbnails"));
        Verify.verify(thumbnailUrl.toUpperCase().contains(pokemonNameFromTitle.toUpperCase()),
                "Name extracted from title (%s) didn't match with thumbnail URL (%s)", pokemonNameFromTitle, thumbnailUrl);
        return pokemonNameFromTitle;
    }

    private static Point extractLocationFromPganMapUrl(final String url) {
        final Matcher mapUrlMatcher = PGAN_MAP_URL.matcher(url);
        Verify.verify(mapUrlMatcher.find());
        return WayPoint.of(Double.parseDouble(mapUrlMatcher.group("latitude")), Double.parseDouble(mapUrlMatcher.group("longitude")));
    }

    private static Optional<Instant> extractDespawnDuration(final String compiledText) {
        final Matcher periodMatcher = DESPAWN_PERIOD_STRING.matcher(compiledText);
        if (periodMatcher.find()) {
            final Duration despawnTimeLeft = Duration.ofMinutes(Longs.tryParse(periodMatcher.group("minutes")));
            if (!despawnTimeLeft.isZero()) { // some dsp times are zeros or even negative; filtering those out
                return Optional.of(Instant.now().plus(despawnTimeLeft));
            }
        }
        return Optional.empty();
    }

    private static boolean isAlolan(final String pokemonName, final String compiledText) {
        final String movesetPattern;
        if (pokemonName.equalsIgnoreCase("Rattata")) {
            movesetPattern = ".*Moves.*((?i)dark|ghost).*";
        } else if (pokemonName.equalsIgnoreCase("Sandshrew")) {
            movesetPattern = ".*((?i)steel|ice).*((?i)steel|ice|dark).*";
        } else if (pokemonName.equalsIgnoreCase("Vulpix")) {
            movesetPattern = ".*((?i)ice|psychic).*((?i)ice|dark).*";
        } else if (pokemonName.equalsIgnoreCase("Geodude")) {
            movesetPattern = ".*Moves.*((?i)electric).*";
        } else {
            return false;
        }

        return Pattern.compile(movesetPattern, Pattern.DOTALL).asPredicate().test(compiledText);
    }

}
