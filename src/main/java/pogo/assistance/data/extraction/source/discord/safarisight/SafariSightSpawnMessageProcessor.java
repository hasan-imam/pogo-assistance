package pogo.assistance.data.extraction.source.discord.safarisight;

import static pogo.assistance.bot.di.DiscordEntityConstants.USER_ID_SS_NOVA_BOT;

import com.google.common.base.Verify;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import pogo.assistance.data.extraction.source.discord.MessageProcessor;
import pogo.assistance.data.extraction.source.discord.SpawnMessageParsingUtils;
import pogo.assistance.data.model.pokemon.ImmutablePokedexEntry;
import pogo.assistance.data.model.pokemon.ImmutablePokemonSpawn;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokedexEntry.Gender;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

/**
 * Processes alert messages from FLPM and AP bots (although the name implies only the first).
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
                && message.getAuthor().getIdLong() == USER_ID_SS_NOVA_BOT;
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
        final String pokemonName = nameGenderLevelMatcher.group("pokemon").trim();
        final Gender gender = SpawnMessageParsingUtils.parseGenderFromSign(nameGenderLevelMatcher.group("gender"));
        final int level = Integer.parseInt(nameGenderLevelMatcher.group("level"));

        final Matcher ivCpLineMatcher = DESCRIPTION_LINE_IV_CP_PATTERN.matcher(descriptionLines[1]);
        Verify.verify(ivCpLineMatcher.find());
        final double iv = Double.parseDouble(ivCpLineMatcher.group("iv"));
        final int cp = Integer.parseInt(ivCpLineMatcher.group("cp"));

        final PokedexEntry pokedexEntry = ImmutablePokedexEntry.builder()
                .name(pokemonName)
                .id(SpawnMessageParsingUtils.parsePokemonIdFromNovaBotSprite(messageEmbed.getThumbnail().getUrl()))
                .gender(gender)
                .build();
        final PokemonSpawn pokemonSpawn = ImmutablePokemonSpawn.builder()
                .from(SpawnMessageParsingUtils.parseGoogleMapQueryLink(messageEmbed.getUrl()))
                .pokedexEntry(pokedexEntry)
                .cp(cp)
                .iv(iv)
                .level(level)
                .locationDescription(locationDescription)
                .build();
        return Optional.of(pokemonSpawn);
    }

}
