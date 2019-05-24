package pogo.assistance.data.extraction.source.discord.pgan;

import static pogo.assistance.bot.di.DiscordEntityConstants.USER_ID_PGAN_BOTS;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

import com.google.common.base.Verify;
import io.jenetics.jpx.Point;
import io.jenetics.jpx.WayPoint;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import pogo.assistance.data.extraction.source.discord.GenericSpawnMessageProcessor;
import pogo.assistance.data.extraction.source.discord.MessageProcessor;
import pogo.assistance.data.extraction.source.discord.SpawnMessageParsingUtils;
import pogo.assistance.data.model.pokemon.CombatStats;
import pogo.assistance.data.model.pokemon.ImmutablePokemonSpawn;
import pogo.assistance.data.model.pokemon.Pokedex;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

public class PGANSpawnMessageProcessor implements MessageProcessor<PokemonSpawn> {

    /**
     * Example URL:
     *  - https://beta.pogoalerts.net/?lat=32.9861643987888&lon=-96.8029907716483&encId=15064660849933713929&zoom=16
     */
    private static final Pattern PGAN_MAP_URL =
            Pattern.compile(".+lat=(?<latitude>[-\\d\\.]+)&lon=(?<longitude>[-\\d\\.]+).*");

    @Override
    public boolean canProcess(@Nonnull final Message message) {
        return message.getChannelType() == ChannelType.PRIVATE
                && USER_ID_PGAN_BOTS.contains(message.getAuthor().getIdLong());
    }

    @Override
    public Optional<PokemonSpawn> process(@Nonnull final Message message) {
        final String compiledText = GenericSpawnMessageProcessor.compileMessageText(message);
        final String pokemonName = extractPokemonName(message);
        final PokedexEntry pokedexEntry = Pokedex.getPokedexEntryFor(pokemonName, SpawnMessageParsingUtils.extractGender(compiledText).orElse(null))
                .orElseThrow(() -> new IllegalArgumentException("Failed to infer pokedex entry from pokemon name: " + pokemonName));
        final PokemonSpawn pokemonSpawn = ImmutablePokemonSpawn.builder()
                .from(extractLocationFromPganMapUrl(message.getEmbeds().get(0).getUrl()))
                .pokedexEntry(pokedexEntry)
                .iv(SpawnMessageParsingUtils.extractCombatStats(compiledText, compiledText).flatMap(CombatStats::combinedIv))
                .level(SpawnMessageParsingUtils.extractLevel(compiledText))
                .cp(SpawnMessageParsingUtils.extractCp(compiledText))
                .sourceMetadata(SpawnMessageParsingUtils.buildSourceMetadataFromMessage(message))
                .build();
        return Optional.of(pokemonSpawn);
    }

    private static String extractPokemonName(final Message message) {
        final MessageEmbed messageEmbed = message.getEmbeds().get(0);
        final String pokemonNameFromTitle = messageEmbed.getTitle().trim().split("\\s")[0];
        final String thumbnailUrl = messageEmbed.getThumbnail().getUrl();
        Verify.verify(thumbnailUrl.toUpperCase().contains(pokemonNameFromTitle.toUpperCase()),
                "Name extracted from title (%s) didn't match with thumbnail URL (%s)", pokemonNameFromTitle, thumbnailUrl);
        return pokemonNameFromTitle;
    }

    private static Point extractLocationFromPganMapUrl(final String url) {
        final Matcher mapUrlMatcher = PGAN_MAP_URL.matcher(url);
        Verify.verify(mapUrlMatcher.find());
        return WayPoint.of(Double.parseDouble(mapUrlMatcher.group("latitude")), Double.parseDouble(mapUrlMatcher.group("longitude")));
    }

}
