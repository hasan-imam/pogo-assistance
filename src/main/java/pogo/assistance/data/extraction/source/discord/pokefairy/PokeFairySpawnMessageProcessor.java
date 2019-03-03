package pogo.assistance.data.extraction.source.discord.pokefairy;

import static pogo.assistance.bot.di.DiscordEntityConstants.CHANNEL_ID_POKEFAIRY_NEOSF90IV;

import com.google.common.base.Verify;
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

@Slf4j
public class PokeFairySpawnMessageProcessor implements MessageProcessor<PokemonSpawn> {

    // Example: "Skitty  93.34% L21 - San Francisco"
    private static final Pattern MESSAGE_TITLE_PATTERN =
            Pattern.compile("(\\s*)" + "(?<pokemon>(.(?!\\d))*)?"
                    + "(\\s*)" + "((?<iv>[\\d.]+)%)?"
                    + "(\\s*)" + "(L(?<level>[\\d]+))?"
                    + "([\\s\\-]*)" + "(?<location>.*)?");

    // Example: 785CP | Gender:♂ (15/15/15)
    private static final Pattern DESCRIPTION_LINE_CP_GENDER_PATTERN = Pattern.compile(
            "([\\s]*(?<cp>[\\d]+)CP)" + "(.*)" +
            "(Gender:(?<gender>[♀♂⚲]))" + "(.*)");

    @Override
    public boolean canProcess(@Nonnull final Message message) {
        return message.getChannel().getIdLong() == CHANNEL_ID_POKEFAIRY_NEOSF90IV && message.getAuthor().isBot();
    }

    @Override
    public Optional<PokemonSpawn> process(@Nonnull final Message message) {
        final MessageEmbed messageEmbed = message.getEmbeds().get(0);
        final String[] descriptionLines = messageEmbed.getDescription().split("\n");
        Verify.verify(descriptionLines.length == 11,
                "Unexpected number of lines in description");

        final Matcher titleMatcher = MESSAGE_TITLE_PATTERN.matcher(messageEmbed.getTitle());
        Verify.verify(titleMatcher.find());
        final String pokemonName = titleMatcher.group("pokemon");
        final double iv = Double.parseDouble(titleMatcher.group("iv"));
        final int level = Integer.parseInt(titleMatcher.group("level"));
        final String locationDescription = descriptionLines[6];

        final Matcher cpAndGenderLineMatcher = DESCRIPTION_LINE_CP_GENDER_PATTERN.matcher(descriptionLines[0]);
        Verify.verify(cpAndGenderLineMatcher.find());
        final int cp = Integer.parseInt(cpAndGenderLineMatcher.group("cp"));
        final Gender gender = SpawnMessageParsingUtils.parseGenderFromSign(cpAndGenderLineMatcher.group("gender"));

        /*
         * We parse the pokemon name but actually use the ID to look it up in dex, things like male Nidoran gets posted
         * as 'Nidoranm' and messes up simple string match based look up.
         */
        final int pokemonId = SpawnMessageParsingUtils.parsePokemonIdFromNovaBotSprite(messageEmbed.getThumbnail().getUrl());
        final PokedexEntry pokedexEntry = Pokedex.getPokedexEntryFor(pokemonId, gender)
                .map(basePokedexEntry -> ImmutablePokedexEntry.builder().from(basePokedexEntry))
                .orElseThrow(() -> new IllegalArgumentException("Failed to lookup dex entry from id: " + pokemonId))
                .id(pokemonId)
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
