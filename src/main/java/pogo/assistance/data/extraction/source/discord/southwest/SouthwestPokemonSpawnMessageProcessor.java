package pogo.assistance.data.extraction.source.discord.southwest;

import java.util.Optional;
import javax.annotation.Nonnull;

import com.google.common.base.Verify;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.entities.Category;
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
public class SouthwestPokemonSpawnMessageProcessor implements MessageProcessor<PokemonSpawn> {

    @Override
    public boolean canProcess(@Nonnull final Message message) {
        return message.getAuthor().isBot()
                && isFromSouthwestPokemonTargetChannel(message);
    }

    @Override
    public Optional<PokemonSpawn> process(@Nonnull final Message message) {
        Verify.verify(message.getEmbeds().size() == 1);
        final MessageEmbed messageEmbed = message.getEmbeds().get(0);

        final StringBuilder compiler = new StringBuilder();
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
        final String compiledText = compiler.toString();

        final Optional<PokedexEntry.Gender> gender = SpawnMessageParsingUtils.extractGender(compiledText);
        final PokemonSpawn pokemonSpawn = ImmutablePokemonSpawn.builder()
                .from(SpawnMessageParsingUtils.parseGoogleMapQueryLink(compiledText))
                .pokedexEntry(NovaBotProcessingUtils.inferPokedexEntryFromNovaBotAssetUrl(messageEmbed.getThumbnail().getUrl(), gender.orElse(null)))
                .iv(SpawnMessageParsingUtils.extractCombatStats(compiledText, compiledText).flatMap(CombatStats::combinedIv))
                .level(SpawnMessageParsingUtils.extractLevel(compiledText))
                .cp(SpawnMessageParsingUtils.extractCp(compiledText))
                .build();
        return Optional.of(pokemonSpawn);
    }

    private static boolean isFromSouthwestPokemonTargetChannel(final Message message) {
        if (message.getChannel().getType() != ChannelType.TEXT || message.getGuild().getIdLong() != DiscordEntityConstants.SERVER_ID_SOUTHWEST_POKEMON) {
            return false;
        }

        final String categoryId = Optional.ofNullable(message.getCategory()).map(Category::getId).orElse("");
        switch (categoryId) {
            case "501877862878674944": // ILLINOIS POKEMON
            case "514347470801862667": // WISCONSIN POKEMON
                return !message.getChannel().getName().contains("quest");
            case "556256006858866689": // LONDON CANARY WHARF
            case "556256886287106058": // LONDON CENTRAL EAST
            case "556256853353431040": // LONDON CENTRAL WEST
            case "556256280184881154": // LONDON NORTHEAST
            case "556256342923411456": // LONDON NORTHWEST
            case "556257052549316619": // LONDON SOUTHEAST
            case "556255782547488775": // LONDON SOUTHWEST
            case "556257301452030003": // LONDON BARNET
            case "556257350458277890": // LONDON BROMLEY
            case "556256621634781185": // LONDON CHINGFORD
            case "556271357013524481": // LONDON DULWICH
            case "556256427149361212": // LONDON EALING
            case "556256462914060291": // LONDON ENFIELD
            case "556271396242718743": // LONDON GREENWICH
            case "556271445441773588": // LONDON LEWISHAM
            case "556256125952196608": // LONDON HARROW
            case "556256551392903169": // LONDON WOODGREEN & HARINGEY
            case "556256673333903360": // LONDON WOOLWICH
                return !message.getChannel().getName().contains("quest")
                        && !message.getChannel().getName().contains("raid")
                        && !message.getChannel().getName().contains("egg");
            case "546137025430945803": // LONDON CD
                return !message.getChannel().getName().contains("zee");
            default:
                return false;
        }
    }

}
