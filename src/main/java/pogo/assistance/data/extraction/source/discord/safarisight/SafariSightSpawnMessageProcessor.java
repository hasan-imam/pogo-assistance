package pogo.assistance.data.extraction.source.discord.safarisight;

import static pogo.assistance.bot.di.DiscordEntityConstants.USER_ID_SS_NOVA_BOTS;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

import com.google.common.base.Verify;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import pogo.assistance.data.extraction.source.discord.MessageProcessor;
import pogo.assistance.data.extraction.source.discord.SpawnMessageParsingUtils;
import pogo.assistance.data.extraction.source.discord.novabot.NovaBotProcessingUtils;
import pogo.assistance.data.model.pokemon.ImmutablePokemonSpawn;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

/**
 * Processes alert messages from FLPM and AP bots (although the name implies only the first).
 *
 * Known issue with this processor:
 *  - Can't process unowns. Example post: https://discordapp.com/channels/@me/555477426826772509/562028467970113537
 *    Unown + gender + letter throws off the pattern matcher.
 */
@Slf4j
public class SafariSightSpawnMessageProcessor implements MessageProcessor<PokemonSpawn> {

    // Example: "Machop ♀ Lvl 16"
    private static final Pattern DESCRIPTION_LINE_NAME_GENDER_LVL_PATTERN = Pattern.compile(
            "((?<pokemon>[\\w\\s\\-\\.']*)?)?" + "(\\s*)" +
                    "(?<gender>[♀♂⚲]+)?" + "(\\s*)" +
                    "(Lvl[\\s]*(?<level>[\\d]+))?" + "(\\s*)");

    // Example: "IV (100%) CP (584)"
    private static final Pattern DESCRIPTION_LINE_IV_CP_PATTERN = Pattern.compile(
            "(.*)" + "(IV[\\s]*\\((?<iv>[\\d.]+)%\\))" + "(.*)" +
                    "([\\s]*CP[\\s]*\\((?<cp>[\\d.]+)\\))" + "(.*)");

    @Override
    public boolean canProcess(@Nonnull final Message message) {
        return message.getChannel().getType() == ChannelType.PRIVATE
                && USER_ID_SS_NOVA_BOTS.contains(message.getAuthor().getIdLong());
    }

    @Override
    public Optional<PokemonSpawn> process(@Nonnull final Message message) {
        final MessageEmbed messageEmbed = message.getEmbeds().get(0);
        final String[] descriptionLines = messageEmbed.getDescription().split("\n");
        Verify.verify(descriptionLines.length == 9,
                "Unexpected number of lines in description");

        final Optional<String> locationDescription = Optional.of(descriptionLines[7].replace("_", "").trim())
                .filter(s -> !s.isEmpty())
                .map(s -> s + ", " + messageEmbed.getTitle());

        final Matcher nameGenderLevelMatcher = DESCRIPTION_LINE_NAME_GENDER_LVL_PATTERN.matcher(descriptionLines[0]);
        Verify.verify(nameGenderLevelMatcher.find());
        final int level = Integer.parseInt(nameGenderLevelMatcher.group("level"));

        final Matcher ivCpLineMatcher = DESCRIPTION_LINE_IV_CP_PATTERN.matcher(descriptionLines[1]);
        Verify.verify(ivCpLineMatcher.find());
        final double iv = Double.parseDouble(ivCpLineMatcher.group("iv"));
        final int cp = Integer.parseInt(ivCpLineMatcher.group("cp"));

        final PokedexEntry pokedexEntry = NovaBotProcessingUtils.inferPokedexEntryFromNovaBotAssetUrl(
                messageEmbed.getThumbnail().getUrl(),
                SpawnMessageParsingUtils.parseGenderFromSign(nameGenderLevelMatcher.group("gender")));
        final PokemonSpawn pokemonSpawn = ImmutablePokemonSpawn.builder()
                .from(SpawnMessageParsingUtils.parseGoogleMapQueryLink(messageEmbed.getUrl()))
                .pokedexEntry(pokedexEntry)
                .cp(cp)
                .iv(iv)
                .level(level)
                .locationDescription(locationDescription)
                .sourceMetadata(SpawnMessageParsingUtils.buildSourceMetadataFromMessage(message))
                .build();
        return Optional.of(pokemonSpawn);
    }

}
