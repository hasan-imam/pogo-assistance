package pogo.assistance.data.extraction.source.discord.sandiego;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

import com.google.common.base.Verify;
import io.jenetics.jpx.Point;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.data.extraction.source.discord.GenericSpawnMessageProcessor;
import pogo.assistance.data.extraction.source.discord.LocationLinkParsingUtils;
import pogo.assistance.data.extraction.source.discord.MessageProcessor;
import pogo.assistance.data.extraction.source.discord.SpawnMessageParsingUtils;
import pogo.assistance.data.extraction.source.discord.novabot.NovaBotProcessingUtils;
import pogo.assistance.data.model.pokemon.CombatStats;
import pogo.assistance.data.model.pokemon.ImmutablePokemonSpawn;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

@Slf4j
public class SDHSpawnMessageProcessor implements MessageProcessor<PokemonSpawn> {

    /**
     * Verify online: https://regex101.com/r/V10pLh/1
     */
    private static final Pattern URL_PATTERN = Pattern.compile("\\[(?<description>.*)]\\((?<url>.*http.*\\.com.*)\\)");

    @Override
    public boolean canProcess(@Nonnull final Message message) {
        if (message.getChannelType() == ChannelType.PRIVATE) {
            return DiscordEntityConstants.USER_IDS_SDHVIP_BOT.contains(message.getAuthor().getIdLong());
        }

        if (message.getAuthor().isBot() && message.getChannel().getType() == ChannelType.TEXT) {
            return Optional.ofNullable(message.getCategory())
                    .map(Category::getIdLong)
                    .filter(categoryId -> categoryId == DiscordEntityConstants.CATEGORY_ID_SDHVIP_SIGHTING_REPORTS)
                    .isPresent();
        }

        return false;
    }

    @Override
    public Optional<PokemonSpawn> process(@Nonnull final Message message) {
        if (message.getEmbeds().get(0).getDescription().split("\n").length <= 5) {
            return Optional.empty();
        }

        final String compiledText = GenericSpawnMessageProcessor.compileMessageText(message);
        final MessageEmbed messageEmbed = message.getEmbeds().get(0); // Assuming all message has embed
        final Optional<PokedexEntry.Gender> gender = SpawnMessageParsingUtils.extractGender(compiledText);
        final PokemonSpawn pokemonSpawn = ImmutablePokemonSpawn.builder()
                .from(LocationLinkParsingUtils.extractLocation(compiledText))
//                .from(extractLocationFromCompiledText(compiledText))
                .pokedexEntry(NovaBotProcessingUtils.inferPokedexEntryFromNovaBotAssetUrl(messageEmbed.getThumbnail().getUrl(), gender.orElse(null)))
                .iv(SpawnMessageParsingUtils.extractCombatStats(compiledText, compiledText).flatMap(CombatStats::combinedIv))
                .level(SpawnMessageParsingUtils.extractLevel(compiledText))
                .cp(SpawnMessageParsingUtils.extractCp(compiledText))
                .sourceMetadata(SpawnMessageParsingUtils.buildSourceMetadataFromMessage(message))
                .build();
        return Optional.of(pokemonSpawn);
    }

    /**
     * This method mostly exists to counter misdirection attempts from SDHVIP. They currently put two URLs in their messages,
     * one containing the real spawn location and another to throw off our processing. This method filters out URLs that
     * sounds fake and picks up location from a none-fake-sounding URL.
     */
    private static Point extractLocationFromCompiledText(final String compiledText) {
        // Prevent misdirection from wrong links
        //  - remove google map links that we think are wrong
        //  - check if after removal there's only one link in the compiled message, as a signal that removal worked
        final Matcher urlMatcher = URL_PATTERN.matcher(compiledText);
        final Map<String, String> descriptionToUrl = new HashMap<>();
        while (urlMatcher.find()) {
            descriptionToUrl.put(urlMatcher.group("description").toUpperCase(), urlMatcher.group("url"));
        }

        final AtomicInteger misdirectionUrlCount = new AtomicInteger();
        final AtomicReference<Point> validLocation = new AtomicReference<>();
        descriptionToUrl.forEach((description, url) -> {
            if (description.matches(".*(?i)(do.*not|don't|dont|ignore).*")) {
                misdirectionUrlCount.incrementAndGet();
            } else {
                validLocation.set(SpawnMessageParsingUtils.parseGoogleMapQueryLink(url));
            }
        });

        // There should be two links, one valid and one to misdirect
        Verify.verify(descriptionToUrl.size() == 2);
        Verify.verify(misdirectionUrlCount.get() > 0);
        // Make sure we found the real link
        Verify.verify(
                validLocation.get() != null,
                "Failed to find valid location URL in compiled message: %s%s",
                System.lineSeparator(),
                compiledText);
        return validLocation.get();
    }
}
