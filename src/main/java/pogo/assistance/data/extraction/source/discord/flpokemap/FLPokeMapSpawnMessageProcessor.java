package pogo.assistance.data.extraction.source.discord.flpokemap;

import static pogo.assistance.bot.di.DiscordEntityConstants.*;

import com.google.common.base.Verify;
import java.util.Optional;
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
import pogo.assistance.data.model.pokemon.ImmutablePokedexEntry;
import pogo.assistance.data.model.pokemon.ImmutablePokemonSpawn;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokedexEntry.Gender;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

/**
 * Processes alert messages from FLPM and AP bots (although the name implies only the first).
 */
@Slf4j
public class FLPokeMapSpawnMessageProcessor implements MessageProcessor<PokemonSpawn> {

    // Example: "[Tarpon Springs] Dratini"
    private static final Pattern MESSAGE_TITLE_PATTERN =
            Pattern.compile("(\\s*)" + "(\\[(?<location>.*)?\\])?" + "(\\s*)" + "(?<pokemon>.*)?");

    // Example: Lvl30+ IVs: 15A/15D/15S (100%)
    private static final Pattern DESCRIPTION_LINE_IV_PATTERN = Pattern.compile("(.*)" + "(\\((?<iv>[\\d.]+)%\\))" + "(.*)");

    // Example: Lvl30+ CP: 171 (lvl 10)
    private static final Pattern DESCRIPTION_LINE_CP_LVL_PATTERN = Pattern.compile("(.*)" +
            "(CP:[\\s]*(?<cp>[\\d]+))" + "(.*)" +
            "(lvl[\\s]*(?<level>[\\d]+))" + "(.*)");

    private static final Pattern DESCRIPTION_LINE_PHYSIOLOGY_PATTERN = Pattern.compile(
            "(Gender:[\\s]*(?<gender>[♀♂⚲]))?" + "(.*)");

    @Override
    public boolean canProcess(@Nonnull final Message message) {
        switch (message.getChannel().getType()) {
            case PRIVATE:
                final long authorId = message.getAuthor().getIdLong();
                return authorId == USER_ID_FLPM_ALERT_BOT_7 || authorId == USER_ID_AP_ALERT_BOT;
            case TEXT:
                final String channelName = message.getChannel().getName();
                switch (message.getCategory().getId()) {
                    case "367523728491544577":
                        // ALPHARETTA category: target channels end with "spawns"
                        return channelName.contains("spawn");
                    case "367520522659168256":
                        // DOWNTOWN ATLANTA: target channels don't end with "chat", "raid" etc. suffix
                        // Some channels have different message format but they are super old, so not filtering them out
                        return !channelName.contains("chat")
                                && !channelName.contains("raid");
                    case "367346018272018437":
                        // AUSTIN: target channels don't end with "chat", "raid" etc. suffix
                        // Some channels have different message format but they are super old, so not filtering them out
                        return !channelName.contains("chat")
                                && !channelName.contains("raid");
                    case "360981255728267264":
                        // JACKSONVILLE: target channels don't end with the filtered out suffixes below
                        // Many channels have different message format but they are super old, so not filtering them out
                        return !channelName.contains("chat")
                                && !channelName.contains("custom_filters")
                                && !channelName.contains("raid");
//                    case "382579319119020042":
//                        // NEW ORLEANS: looks dead - commeting out
//                        return false;

                    default:
                        return false;
                }
            default:
                return false;
        }
    }

    @Override
    public Optional<PokemonSpawn> process(@Nonnull final Message message) {
        final MessageEmbed messageEmbed = message.getEmbeds().get(0);
        final String[] descriptionLines = messageEmbed.getDescription().split("\n");
        if (descriptionLines.length == 1 || descriptionLines.length == 4) {
            // Some message contains a single description line with just the despawn time, but nothing else
            // These messages contain 1 line in FLPM alerts and 4 lines in AP alerts
            // We ignore those for now
            log.debug("Ignoring message with missing spawn description: {}", message.getJumpUrl());
            return Optional.empty();
        }
        Verify.verify(descriptionLines.length == 7,
                "Unexpected number of lines in description");

        final Matcher titleMatcher = MESSAGE_TITLE_PATTERN.matcher(messageEmbed.getTitle());
        Verify.verify(titleMatcher.find());
        final String pokemonName = titleMatcher.group("pokemon");
        final String locationDescription = titleMatcher.group("location");

        final Matcher ivLineMatcher = DESCRIPTION_LINE_IV_PATTERN.matcher(descriptionLines[3]);
        Verify.verify(ivLineMatcher.find());
        final double iv = Double.parseDouble(ivLineMatcher.group("iv"));

        final Matcher cpAndLevelLineMatcher = DESCRIPTION_LINE_CP_LVL_PATTERN.matcher(descriptionLines[4]);
        Verify.verify(cpAndLevelLineMatcher.find());
        final int cp = Integer.parseInt(cpAndLevelLineMatcher.group("cp"));
        final int level = Integer.parseInt(cpAndLevelLineMatcher.group("level"));

        final Matcher physiologyDataLineMatcher = DESCRIPTION_LINE_PHYSIOLOGY_PATTERN.matcher(descriptionLines[6]);
        Verify.verify(physiologyDataLineMatcher.find());
        final Gender gender = SpawnMessageParsingUtils.parseGenderFromSign(physiologyDataLineMatcher.group("gender"));
        // TODO: parse height, weight

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
