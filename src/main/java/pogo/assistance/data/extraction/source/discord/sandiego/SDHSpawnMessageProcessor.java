package pogo.assistance.data.extraction.source.discord.sandiego;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.data.extraction.source.discord.*;
import pogo.assistance.data.extraction.source.discord.novabot.NovaBotProcessingUtils;
import pogo.assistance.data.model.pokemon.CombatStats;
import pogo.assistance.data.model.pokemon.ImmutablePokemonSpawn;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.regex.Pattern;

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
                .pokedexEntry(NovaBotProcessingUtils.inferPokedexEntryFromNovaBotAssetUrl(messageEmbed.getThumbnail().getUrl(), gender.orElse(null)))
                .iv(SpawnMessageParsingUtils.extractCombatStats(compiledText, compiledText).flatMap(CombatStats::combinedIv))
                .level(SpawnMessageParsingUtils.extractLevel(compiledText))
                .cp(SpawnMessageParsingUtils.extractCp(compiledText))
                .despawnTime(DespawnTimeParserUtils.extractDespawnTime(compiledText))
                .sourceMetadata(SpawnMessageParsingUtils.buildSourceMetadataFromMessage(message))
                .build();
        return Optional.of(pokemonSpawn);
    }

}
