package pogo.assistance.data.extraction.source.discord.articuno;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

import com.google.common.base.Verify;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import io.jenetics.jpx.WayPoint;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import pogo.assistance.bot.di.DiscordEntityConstants;
import pogo.assistance.data.extraction.source.discord.MessageProcessor;
import pogo.assistance.data.extraction.source.discord.SpawnMessageParsingUtils;
import pogo.assistance.data.model.pokemon.ImmutablePokemonSpawn;
import pogo.assistance.data.model.pokemon.Pokedex;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

public class ArticunoSpawnMessageProcessor implements MessageProcessor<PokemonSpawn> {

    private static final Pattern ID_PATTERN = Pattern.compile("^:(?<pokemonId>[\\d]+):");
    private static final Pattern IV_PATTERN = Pattern.compile(":(Iv|indval):[\\s]+(?<iv>[\\d\\.]+)");
    private static final Pattern CP_PATTERN = Pattern.compile(":(Cp|compow):[\\s]+(?<cp>[\\d]+)");
    private static final Pattern LEVEL_PATTERN = Pattern.compile(":(lv\\\\_t|lev):[\\s]+(?<level>[\\d]+)");
    private static final Pattern IPA_URL_PATTERN =
            Pattern.compile("www\\.pogoipa\\.com/dplnk\\.html\\?(?<latitude>[-\\d\\.]+),(?<longitude>[-\\d\\.]+)(.*)");

    @Override
    public boolean canProcess(@Nonnull final Message message) {
        return message.getAuthor().isBot()
                && message.getChannelType() == ChannelType.TEXT
                && DiscordEntityConstants.SPAWN_CHANNEL_IDS_ITOOLS.contains(message.getChannel().getIdLong());
    }

    @Override
    public Optional<PokemonSpawn> process(@Nonnull final Message message) {
        final String messageText = message.getContentStripped().trim();

        final ImmutablePokemonSpawn.Builder builder = ImmutablePokemonSpawn.builder();

        final Matcher mapUrlMatcher = IPA_URL_PATTERN.matcher(messageText);
        Verify.verify(mapUrlMatcher.find(), "Input didn't match IPA URL format. Input: %s", messageText);
        builder.from(WayPoint.of(Double.parseDouble(mapUrlMatcher.group("latitude")), Double.parseDouble(mapUrlMatcher.group("longitude"))));

        builder.sourceMetadata(SpawnMessageParsingUtils.buildSourceMetadataFromMessage(message));

        final Matcher idMatcher = ID_PATTERN.matcher(messageText);
        Verify.verify(idMatcher.find(), "Could not find ID in message: %s", messageText);

        Pokedex.getPokedexEntryFor(Ints.tryParse(idMatcher.group("pokemonId")), SpawnMessageParsingUtils.extractGender(messageText).orElse(null))
                .ifPresent(builder::pokedexEntry);

        final Matcher ivMatcher = IV_PATTERN.matcher(messageText);
        Verify.verify(ivMatcher.find(), "Could not find IV in message: %s", messageText);
        builder.iv(Doubles.tryParse(ivMatcher.group("iv")));

        final Matcher cpMatcher = CP_PATTERN.matcher(messageText);
        Verify.verify(cpMatcher.find(), "Could not find CP in message: %s", messageText);
        builder.cp(Ints.tryParse(cpMatcher.group("cp")));

        final Matcher levelMatcher = LEVEL_PATTERN.matcher(messageText);
        Verify.verify(levelMatcher.find(), "Could not find level in message: %s", messageText);
        builder.level(Ints.tryParse(levelMatcher.group("level")));

        final PokemonSpawn pokemonSpawn = builder.build();
        return pokemonSpawn.getPokedexEntry().getGender().equals(PokedexEntry.Gender.UNKNOWN) ? Optional.empty() : Optional.of(pokemonSpawn);
    }
}
