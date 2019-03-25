package pogo.assistance.data.extraction.source.discord.sandiego;

import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import pogo.assistance.bot.di.DiscordEntityConstants;
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
     * A/D/S stats appear as "**97.78%** (15|14|15)" - all in a single line. Line doesn't contain any other info.
     */
    private static final Pattern ADS_STAT_PATTERN = Pattern.compile(
            "(?<iv>[\\d\\.]+)%" + "[*\\s]*" +
                    "\\(" + "(?<attack>\\d+)" + "[\\|\\s]+" + "(?<defense>\\d+)" + "[\\|\\s]+" + "(?<stamina>\\d+)" + "\\)");

    @Override
    public boolean canProcess(@Nonnull final Message message) {
        return message.getAuthor().isBot()
                && message.getChannelType() == ChannelType.PRIVATE
                && message.getAuthor().getIdLong() == DiscordEntityConstants.USER_ID_SDHVIP_BOT;
    }

    @Override
    public Optional<PokemonSpawn> process(@Nonnull final Message message) {
        final MessageEmbed messageEmbed = message.getEmbeds().get(0);
        final PokedexEntry pokedexEntry = NovaBotProcessingUtils.inferPokedexEntryFromNovaBotAssetUrl(
                messageEmbed.getThumbnail().getUrl(),
                SpawnMessageParsingUtils.extractGender(messageEmbed.getTitle()).orElse(null));

        final ImmutablePokemonSpawn.Builder builder = ImmutablePokemonSpawn.builder();
        builder.from(SpawnMessageParsingUtils.parseGoogleMapQueryLink(messageEmbed.getUrl()));
        builder.pokedexEntry(pokedexEntry);

        SpawnMessageParsingUtils.extractLevel(messageEmbed.getTitle()).ifPresent(builder::level);
        SpawnMessageParsingUtils.extractCp(messageEmbed.getTitle()).ifPresent(builder::cp);
        SpawnMessageParsingUtils.extractCombatStats(messageEmbed.getDescription(), messageEmbed.getTitle())
                .flatMap(CombatStats::combinedIv)
                .ifPresent(builder::iv);

        return Optional.of(builder.build());
    }
}
