package pogo.assistance.data.extraction.source.discord.sgv;

import static pogo.assistance.bot.di.DiscordEntityConstants.CATEGORY_IDS_SGV_SCANS_IV_FEED;
import static pogo.assistance.bot.di.DiscordEntityConstants.SERVER_ID_SGV_SCANS;

import java.util.Optional;
import javax.annotation.Nonnull;

import io.jenetics.jpx.Point;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import pogo.assistance.bot.responder.relay.pokedex100.CandySelector;
import pogo.assistance.data.extraction.source.discord.GenericSpawnMessageProcessor;
import pogo.assistance.data.extraction.source.discord.MessageProcessor;
import pogo.assistance.data.extraction.source.discord.SpawnMessageParsingUtils;
import pogo.assistance.data.extraction.source.discord.novabot.NovaBotProcessingUtils;
import pogo.assistance.data.model.pokemon.CombatStats;
import pogo.assistance.data.model.pokemon.ImmutablePokemonSpawn;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class SGVSpawnMessageProcessor implements MessageProcessor<PokemonSpawn> {

    private final boolean disableFiltering;

    public SGVSpawnMessageProcessor() {
        disableFiltering = false;
    }

    @Override
    public boolean canProcess(@Nonnull final Message message) {
        if (!message.getAuthor().isBot() || message.getChannelType() != ChannelType.TEXT || message.getGuild().getIdLong() != SERVER_ID_SGV_SCANS) {
            return false;
        }

        if (Optional.ofNullable(message.getCategory()).map(Category::getIdLong).filter(CATEGORY_IDS_SGV_SCANS_IV_FEED::contains).isPresent()) {
            final String channelName = message.getChannel().getName().toLowerCase();
            return channelName.contains("iv") || channelName.contains("all-spawns") || channelName.contains("unown");
        }

        return false;
    }

    @Override
    public Optional<PokemonSpawn> process(@Nonnull final Message message) {
        if (message.getEmbeds().get(0).getDescription().split("\n").length <= 3) {
            return Optional.empty();
        }

        final String compiledText = GenericSpawnMessageProcessor.compileMessageText(message);
        final MessageEmbed messageEmbed = message.getEmbeds().get(0); // Assuming all message has embed
        if (messageEmbed.getDescription().split("\n").length <= 6) {
            // Spawns with missing IV/CP information -> ignored
            return Optional.empty();
        }

        final Optional<PokedexEntry.Gender> gender = SpawnMessageParsingUtils.extractGender(compiledText);
        final PokedexEntry pokedexEntry = NovaBotProcessingUtils.inferPokedexEntryFromNovaBotAssetUrl(messageEmbed.getThumbnail().getUrl(), gender.orElse(null));
        final Optional<Double> iv = SpawnMessageParsingUtils.extractCombatStats(compiledText, compiledText).flatMap(CombatStats::combinedIv);
        final Optional<Integer> cp = SpawnMessageParsingUtils.extractCp(compiledText);

        // Parsing more messages exposes us to the server because we will end up accessing a lot of server URLs
        // Limiting ourselves to only high value spawns to reduce this exposure
        if (iv.orElse(-1.0) == 100.0
                || (CandySelector.isCandy(pokedexEntry) && iv.orElse(-1.0) >= 90.0)
                || cp.orElse(-1) >= 2600
                || disableFiltering) { // disablement for testing
            final PokemonSpawn pokemonSpawn = ImmutablePokemonSpawn.builder()
                    .from(extractLocationFromServerUrl(compiledText)) // expensive operation -> see implementation
                    .pokedexEntry(pokedexEntry)
                    .iv(iv)
                    .level(SpawnMessageParsingUtils.extractLevel(compiledText))
                    .cp(cp)
                    .sourceMetadata(SpawnMessageParsingUtils.buildSourceMetadataFromMessage(message))
                    .build();
            return Optional.of(pokemonSpawn);
        } else {
            return Optional.empty();
        }
    }

    private static Point extractLocationFromServerUrl(final String compiledText) {
        return SpawnMessageParsingUtils.parseGoogleMapQueryLink(SGVMessageProcessorUtils.getGoogleMapUrl(compiledText));
    }

}
