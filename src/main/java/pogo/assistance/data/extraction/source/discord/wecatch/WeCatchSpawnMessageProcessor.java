package pogo.assistance.data.extraction.source.discord.wecatch;

import static pogo.assistance.bot.di.DiscordEntityConstants.CHANNEL_ID_WECATCH_IV90UP;

import com.google.common.base.Verify;
import io.jenetics.jpx.Point;
import io.jenetics.jpx.WayPoint;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import pogo.assistance.data.extraction.source.discord.MessageProcessor;
import pogo.assistance.data.extraction.source.discord.SpawnMessageParsingUtils;
import pogo.assistance.data.model.pokemon.ImmutablePokedexEntry;
import pogo.assistance.data.model.pokemon.ImmutablePokemonSpawn;
import pogo.assistance.data.model.pokemon.Pokedex;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokedexEntry.Gender;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

/**
 * Processes alert messages from FLPM, AP and WeCatch bots (although the name implies only the first).
 */
@Slf4j
public class WeCatchSpawnMessageProcessor implements MessageProcessor<PokemonSpawn> {

    // Example: "三蜜蜂  [324台灣桃園市平鎮區延平路三段104巷200號]"
    private static final Pattern MESSAGE_TITLE_PATTERN =
            Pattern.compile("(?<pokemon>(.(?!\\[))+)?" + "(\\s*)" + "(\\[(?<location>.*)?\\])?" + "(\\s*)");

    // Example: Lvl30+ IVs: 15A/15D/15S (100%)
    private static final Pattern DESCRIPTION_LINE_IV_PATTERN = Pattern.compile("(.*)" + "(\\((?<iv>[\\d.]+)%\\))" + "(.*)");

    // Example: Lvl30+ CP: 171 (lvl 10)
    private static final Pattern DESCRIPTION_LINE_CP_LVL_PATTERN = Pattern.compile("(.*)" +
            "(CP:[\\s]*(?<cp>[\\d]+))" + "(.*)" +
            "(lvl[\\s]*(?<level>[\\d]+))" + "(.*)");

    private static final Pattern DESCRIPTION_LINE_PHYSIOLOGY_PATTERN = Pattern.compile(
            "(性別:[\\s]*(?<gender>[♀♂⚲]))?" + "(.*)");

    // Example: https://www.wecatch.net/?lat=23.32469150855182&lng=120.27484410435356
    private static final Pattern WECATCH_LOCATION_URL =
            Pattern.compile("(.*)(lat=(?<latitude>[-\\d\\.]+))&(lng=(?<longitude>[-\\d\\.]+))(.*)");

    @Override
    public boolean canProcess(@Nonnull final Message message) {
        return message.getAuthor().isBot() && message.getChannel().getIdLong() == CHANNEL_ID_WECATCH_IV90UP;
    }

    @Override
    public Optional<PokemonSpawn> process(@Nonnull final Message message) {
        final MessageEmbed messageEmbed = message.getEmbeds().get(0);
        // Some bots put an empty line after the first one. This replacement makes all message not have that extra line.
        final String[] descriptionLines = messageEmbed.getDescription().split("\n");
        if (descriptionLines.length == 4) {
            // Some message contains a single description line with just the despawn time, but nothing else
            // We ignore those for now
            // Example: https://discordapp.com/channels/409426776419336202/493530728915664912/527314497698922507
            log.warn("Ignoring message from '{}' which was unexpectedly missing spawn description: {}",
                    message.getChannel().getName(), message.getJumpUrl());
            return Optional.empty();
        }
        Verify.verify(descriptionLines.length == 6,
                "Unexpected number of lines in description");

        final Matcher titleMatcher = MESSAGE_TITLE_PATTERN.matcher(messageEmbed.getTitle());
        Verify.verify(titleMatcher.find());
        final String pokemonName = titleMatcher.group("pokemon").trim(); // unused - English name from dex is used
        final String locationDescription = titleMatcher.group("location").trim();

        final Matcher ivLineMatcher = DESCRIPTION_LINE_IV_PATTERN.matcher(descriptionLines[2]);
        Verify.verify(ivLineMatcher.find());
        final double iv = Double.parseDouble(ivLineMatcher.group("iv"));

        final Matcher cpAndLevelLineMatcher = DESCRIPTION_LINE_CP_LVL_PATTERN.matcher(descriptionLines[3]);
        Verify.verify(cpAndLevelLineMatcher.find());
        final int cp = Integer.parseInt(cpAndLevelLineMatcher.group("cp"));
        final int level = Integer.parseInt(cpAndLevelLineMatcher.group("level"));

        final Matcher physiologyDataLineMatcher = DESCRIPTION_LINE_PHYSIOLOGY_PATTERN.matcher(descriptionLines[5]);
        Verify.verify(physiologyDataLineMatcher.find());
        final Gender gender = SpawnMessageParsingUtils.parseGenderFromSign(physiologyDataLineMatcher.group("gender"));
        // TODO: parse height, weight

        final int pokemonId = SpawnMessageParsingUtils.parsePokemonIdFromNovaBotSprite(messageEmbed.getThumbnail().getUrl());
        final PokedexEntry pokedexEntry = Pokedex.getPokedexEntryFor(pokemonId, gender)
                .orElseThrow(() -> new IllegalArgumentException("Failed to lookup dex entry from id: " + pokemonId));
        final PokemonSpawn pokemonSpawn = ImmutablePokemonSpawn.builder()
                .from(parseWeCatchLocationLink(messageEmbed.getUrl()))
                .pokedexEntry(pokedexEntry)
                .cp(cp)
                .iv(iv)
                .level(level)
                .locationDescription(locationDescription)
                .build();
        return Optional.of(pokemonSpawn);
    }

    private static Point parseWeCatchLocationLink(final String url) {
        final Matcher mapUrlMatcher = WECATCH_LOCATION_URL.matcher(url);
        Verify.verify(mapUrlMatcher.find());
        return WayPoint.of(Double.parseDouble(mapUrlMatcher.group("latitude")), Double.parseDouble(mapUrlMatcher.group("longitude")));
    }

}
