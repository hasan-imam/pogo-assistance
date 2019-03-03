package pogo.assistance.data.extraction.source.discord.pogosj1;

import com.google.common.base.Verify;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.data.extraction.source.discord.MessageProcessor;
import pogo.assistance.data.extraction.source.discord.SpawnMessageParsingUtils;
import pogo.assistance.data.model.pokemon.ImmutablePokedexEntry;
import pogo.assistance.data.model.pokemon.ImmutablePokemonSpawn;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokedexEntry.Gender;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

public class PoGoSJSpawnMessageProcessor implements MessageProcessor<PokemonSpawn> {

    /*
     * Example titles:
     *  - "Kricketot ♀ 100.0% CP:252 (L22) Palo Alto"
     *  - "Nidoran♂ ♂ 100.0% CP:418 (L17) San Jose"
     *
     * For simplicity, we replace gender signs from the title and then match it against this pattern.
     */
    private static final Pattern MESSAGE_TITLE_PATTERN =
            Pattern.compile("(?<pokemon>([\\w\\s'\\-]*(?![\\d\\.]+%)))" + "(\\s*)" +
                    "((?<iv>[\\d\\.]+)%)?" + "(\\s*)" +
                    "(CP:(?<cp>[\\d]+))?" + "(\\s*)" +
                    "(\\(L(?<level>[\\d]+)\\))?" + "([\\-\\s]*)" +
                    "([\\w ]+)");

    @Override
    public boolean canProcess(@Nonnull final Message message) {
        final long channelId = message.getChannel().getIdLong();
        return (channelId == DiscordEntityConstants.CHANNEL_ID_POGOSJ1_100IV
                || channelId == DiscordEntityConstants.CHANNEL_ID_POGOSJ1_100IVMAX)
                && message.getAuthor().isBot();
    }

    @Override
    public Optional<PokemonSpawn> process(@Nonnull final Message message) {
        if (!message.getAuthor().isBot()) {
            return Optional.empty();
        }

        final MessageEmbed messageEmbed = message.getEmbeds().get(0);
        final String embedTitleWithoutGenderSigns = messageEmbed.getTitle().replaceAll("[♀♂⚲]", "").trim();
        final Matcher titleMatcher = MESSAGE_TITLE_PATTERN.matcher(embedTitleWithoutGenderSigns);
        Verify.verify(titleMatcher.find());
        final PokedexEntry pokedexEntry = ImmutablePokedexEntry.builder()
                .id(SpawnMessageParsingUtils.parsePokemonIdFromNovaBotSprite(messageEmbed.getThumbnail().getUrl()))
                .name(titleMatcher.group("pokemon").trim())
                .gender(parseGenderFromEmbedTitle(messageEmbed.getTitle()))
                .build();

        // Some extra verification on the description so we detect (i.e. throw error) if message format changes
        final String[] descriptionLines = messageEmbed.getDescription().split("\n");
        Verify.verify(descriptionLines.length == 5);
        Verify.verify(descriptionLines[2].startsWith("Location:"));
        Verify.verify(descriptionLines[4].equals(messageEmbed.getUrl()));

        return Optional.of(ImmutablePokemonSpawn.builder()
                .from(SpawnMessageParsingUtils.parseGoogleMapQueryLink(messageEmbed.getUrl()))
                .pokedexEntry(pokedexEntry)
                .level(Integer.parseInt(titleMatcher.group("level")))
                .cp(Integer.parseInt(titleMatcher.group("cp")))
                .iv(Double.parseDouble(titleMatcher.group("iv")))
                .locationDescription(descriptionLines[2].replaceFirst("Location:", "").trim())
                .build());
    }

    private static Gender parseGenderFromEmbedTitle(final String title) {
        if (title.contains("♂")) {
            return Gender.MALE;
        } else if (title.contains("♀")) {
            return Gender.FEMALE;
        } else if (title.contains("⚲")) {
            return Gender.NONE;
        }
        return Gender.NONE;
    }

}