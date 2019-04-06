package pogo.assistance.data.extraction.source.discord;

import static pogo.assistance.bot.di.DiscordEntityConstants.CATEGORY_ID_NORTHHOUSTONTRAINERS_IV_FEED;
import static pogo.assistance.bot.di.DiscordEntityConstants.CHANNEL_ID_VCSCANS_0IV;
import static pogo.assistance.bot.di.DiscordEntityConstants.CHANNEL_ID_VCSCANS_100IV;
import static pogo.assistance.bot.di.DiscordEntityConstants.SERVER_ID_NORTHHOUSTONTRAINERS;
import static pogo.assistance.bot.di.DiscordEntityConstants.SERVER_ID_VCSCANS;
import static pogo.assistance.bot.di.DiscordEntityConstants.SPAWN_CHANNEL_IDS_POKESQUAD;
import static pogo.assistance.bot.di.DiscordEntityConstants.USER_ID_POGO_BADGERS_BOT;

import java.util.Optional;
import javax.annotation.Nonnull;

import com.google.common.base.Verify;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;
import pogo.assistance.data.extraction.source.discord.novabot.NovaBotProcessingUtils;
import pogo.assistance.data.model.pokemon.CombatStats;
import pogo.assistance.data.model.pokemon.ImmutablePokemonSpawn;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

/**
 * @implNote
 *      Assumptions:
 *          - All message has one embed and the embed's thumbnail URL is a novabot asset URL (i.e. parsable by
 *          {@link NovaBotProcessingUtils#inferPokedexEntryFromNovaBotAssetUrl(String, PokedexEntry.Gender)}).
 */
public class GenericSpawnMessageProcessor implements MessageProcessor<PokemonSpawn> {

    @Override
    public boolean canProcess(@Nonnull final Message message) {
        return isFromVcPokeScanTargetChannels(message)
                || isFromNHTTargetChannels(message)
                || isFromPoGoBadgersDmBot(message)
                || isFromPokeSquadTargetChannels(message);
    }

    @Override
    public Optional<PokemonSpawn> process(@Nonnull final Message message) {
        if (isFromPoGoBadgersDmBot(message)) {
            // Ignore DMs from this source that doesn't contain iv/cp/level etc.
            if (message.getEmbeds().get(0).getDescription().split("\n").length <= 2) {
                return Optional.empty();
            }
        }

        final String compiledText = compileMessageText(message);
        final MessageEmbed messageEmbed = message.getEmbeds().get(0); // Assuming all message has embed
        final Optional<PokedexEntry.Gender> gender = SpawnMessageParsingUtils.extractGender(compiledText);
        final PokemonSpawn pokemonSpawn = ImmutablePokemonSpawn.builder()
                .from(SpawnMessageParsingUtils.parseGoogleMapQueryLink(compiledText))
                // Assuming embed's thumbnail is novabot asset URL and we can infer pokemon ID from it
                .pokedexEntry(NovaBotProcessingUtils.inferPokedexEntryFromNovaBotAssetUrl(messageEmbed.getThumbnail().getUrl(), gender.orElse(null)))
                .iv(SpawnMessageParsingUtils.extractCombatStats(compiledText, compiledText).flatMap(CombatStats::combinedIv))
                .level(SpawnMessageParsingUtils.extractLevel(compiledText))
                .cp(SpawnMessageParsingUtils.extractCp(compiledText))
                .sourceMetadata(SpawnMessageParsingUtils.buildSourceMetadataFromMessage(message))
                .build();
        return Optional.of(pokemonSpawn);
    }

    private static String compileMessageText(final Message message) {
        Verify.verify(message.getEmbeds().size() == 1);
        final MessageEmbed messageEmbed = message.getEmbeds().get(0);

        final StringBuilder compiler = new StringBuilder();
        Optional.ofNullable(message.getAuthor()).map(User::getName).ifPresent(name -> compiler.append(name).append(System.lineSeparator()));
        Optional.ofNullable(messageEmbed.getTitle()).ifPresent(title -> compiler.append(title).append(System.lineSeparator()));
        Optional.ofNullable(messageEmbed.getDescription()).ifPresent(description -> compiler.append(description.replaceAll("\\*", " "))
                .append(System.lineSeparator()));
        messageEmbed.getFields().forEach(field -> {
            compiler.append(field.getName());
            compiler.append(System.lineSeparator());
            compiler.append(field.getValue());
            compiler.append(System.lineSeparator());
        });
        Optional.ofNullable(messageEmbed.getUrl()).ifPresent(mapUrl -> compiler.append(mapUrl).append(System.lineSeparator()));
        return compiler.toString();
    }

    private static boolean isFromVcPokeScanTargetChannels(final Message message) {
        if (!message.getAuthor().isBot() || message.getChannelType() != ChannelType.TEXT || message.getGuild().getIdLong() != SERVER_ID_VCSCANS) {
            return false;
        }
        if (message.getChannel().getIdLong() == CHANNEL_ID_VCSCANS_100IV || message.getChannel().getIdLong() == CHANNEL_ID_VCSCANS_0IV) {
            return true;
        }

        final String channelName = message.getChannel().getName();
        final String categoryId = Optional.ofNullable(message.getCategory())
                .map(Category::getId)
                .orElse(null);
        if (categoryId == null) { // Some channels do not fall under a category (ungrouped)
            return false;
        }

        switch (categoryId) {
            case "360806354824462337": // OXNARD
            case "360806055640301568": // VENTURA
            case "360801783053942785": // CAMARILLO
            case "360804192589447169": // SANTA PAULA
            case "468453386266738688": // FILLMORE
                return !channelName.contains("raid") && !channelName.contains("chat") && !channelName.contains("quest");
            default:
                return false;
        }
    }

    private static boolean isFromPoGoBadgersDmBot(final Message message) {
        return message.getChannelType() == ChannelType.PRIVATE
                && message.getAuthor().getIdLong() == USER_ID_POGO_BADGERS_BOT;
    }

    private static boolean isFromNHTTargetChannels(final Message message) {
        return message.getAuthor().isBot()
                && Optional.ofNullable(message.getGuild()).map(Guild::getIdLong).filter(id -> id == SERVER_ID_NORTHHOUSTONTRAINERS).isPresent()
                && Optional.ofNullable(message.getCategory())
                .map(Category::getIdLong)
                .filter(id -> id == CATEGORY_ID_NORTHHOUSTONTRAINERS_IV_FEED)
                .isPresent();
    }

    private static boolean isFromPokeSquadTargetChannels(final Message message) {
        return message.getChannelType() == ChannelType.TEXT
                && message.getAuthor().isBot()
                && SPAWN_CHANNEL_IDS_POKESQUAD.contains(message.getChannel().getIdLong());
    }

}
