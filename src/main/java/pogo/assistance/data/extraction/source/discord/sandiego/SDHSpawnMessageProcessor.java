package pogo.assistance.data.extraction.source.discord.sandiego;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
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
    /**
     * CPs appear as "CP123".
     */
    private static final Pattern CP_PATTERN = Pattern.compile("CP(?<cp>[\\d\\.]+)");
    /**
     * Levels appear as "L13".
     */
    private static final Pattern LEVEL_PATTERN = Pattern.compile("L(?<level>[\\d]+)");

    @Override
    public boolean canProcess(@Nonnull final Message message) {
        return message.getAuthor().isBot()
                && message.getChannelType() == ChannelType.PRIVATE
                && message.getAuthor().getIdLong() == DiscordEntityConstants.USER_ID_SDHVIP_BOT;
    }

    @Override
    public Optional<PokemonSpawn> process(@Nonnull final Message message) {
        final MessageEmbed messageEmbed = message.getEmbeds().get(0);
        final String compiledText = messageEmbed.toJSONObject().toString();

        final PokedexEntry pokedexEntry = NovaBotProcessingUtils.inferPokedexEntryFromNovaBotAssetUrl(
                messageEmbed.getThumbnail().getUrl(),
                SpawnMessageParsingUtils.extractGender(messageEmbed.getTitle()).orElse(null));

        final ImmutablePokemonSpawn.Builder builder = ImmutablePokemonSpawn.builder();
        builder.from(SpawnMessageParsingUtils.parseGoogleMapQueryLink(messageEmbed.getUrl()));
        builder.pokedexEntry(pokedexEntry);

        extractLevel(compiledText).ifPresent(builder::level);
        extractCp(compiledText).ifPresent(builder::cp);
        extractIv(compiledText).ifPresent(builder::iv);

        return Optional.of(builder.build());
    }

    private static Optional<Double> extractIv(final String fullMessageText) {
        final Matcher matcher = ADS_STAT_PATTERN.matcher(fullMessageText);
        if (matcher.find()) {
            return Optional.ofNullable(matcher.group("iv"))
                    .map(String::trim)
                    .map(Double::parseDouble);
        }
        return Optional.empty();
    }

    private static Optional<Integer> extractLevel(final String fullMessageText) {
        final Matcher matcher = LEVEL_PATTERN.matcher(fullMessageText);
        if (matcher.find()) {
            return Optional.ofNullable(matcher.group("level"))
                    .map(String::trim)
                    .map(Integer::parseInt);
        }
        return Optional.empty();
    }

    private static Optional<Integer> extractCp(final String fullMessageText) {
        final Matcher matcher = CP_PATTERN.matcher(fullMessageText);
        if (matcher.find()) {
            return Optional.ofNullable(matcher.group("cp"))
                    .map(String::trim)
                    .map(Integer::parseInt);
        }
        return Optional.empty();
    }

    /**
     * @return
     *      Return true once every 20 calls since we don't want to fully launch this parser just yet.
     */
    private static boolean fudgeFactorCheck() {
        return ThreadLocalRandom.current().nextInt(1, 20) == 10;
    }
}
