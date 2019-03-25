package pogo.assistance.data.extraction.source.discord.novabot;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Verify;
import com.google.common.primitives.Ints;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import pogo.assistance.data.model.pokemon.Pokedex;
import pogo.assistance.data.model.pokemon.PokedexEntry;

@UtilityClass
public class NovaBotProcessingUtils {

    /*
     * Example thumbnail URLs:
     *  - https://raw.githubusercontent.com/novabot-sprites/novabot-sprites/master/401.png
     *  - https://raw.githubusercontent.com/novabot-sprites/novabot-sprites/master/88-73.png?5
     *  - https://image.cdstud.io/o/351-29.png (we catch channel - castform)
     *  - https://www.serebii.net/sunmoon/pokemon/050-a.png
     *
     * The form part of the URL can be missing for things that have multiple forms. In such cases some default (or unknown) form is inferred
     * picked, such as normal castform, non alolan, unown A etc.
     */
    private static final Pattern EMBED_THUMBNAIL_URL_PATTERN = Pattern.compile("(.+)/(?<id>\\d+)[\\-]?(?<id2>[\\w]+)?\\.png(.*)");

    /**
     * @param gender
     *      Optional gender input if the gender is already known
     */
    public static PokedexEntry inferPokedexEntryFromNovaBotAssetUrl(@NonNull final String url, @Nullable final PokedexEntry.Gender gender) {
        return Pokedex.getPokedexEntryFor(parsePokemonIdFromNovaBotAssetUrl(url), gender, parseFormsFromNovaBotAssetUrl(url))
                .orElseThrow(() -> new IllegalArgumentException("Failed to infer pokedex entry from novabot URL: " + url));
    }

    public static int parsePokemonIdFromNovaBotAssetUrl(@NonNull final String url) {
        final Matcher thumbnailUrlMatcher = EMBED_THUMBNAIL_URL_PATTERN.matcher(url);
        Verify.verify(thumbnailUrlMatcher.find());
        return Integer.parseInt(thumbnailUrlMatcher.group("id"));
    }

    public static Set<PokedexEntry.Form> parseFormsFromNovaBotAssetUrl(final String url) {
        final Matcher matcher = EMBED_THUMBNAIL_URL_PATTERN.matcher(url);
        if (matcher.find()) {
            final int pokemonId = Integer.parseInt(matcher.group("id"));
            final int id2 = Optional.ofNullable(matcher.group("id2"))
                    .map(Ints::tryParse)
                    .orElse(-1);
            if (Pokedex.canHaveAlolanForm(pokemonId)) {
                // By some miracle/coincidence, all alolan form id2 happens to be even numbers
                return (id2 % 2 == 0) ? Collections.singleton(PokedexEntry.Form.ALOLAN) : Collections.emptySet();
            } else if (pokemonId == 201) { // Unown
                if (id2 >= 1 && id2 <= 26) {
                    final char character = (char) ('A' + id2 - 1);
                    return Collections.singleton(PokedexEntry.Form.valueOf("UNOWN_" + character));
                } else if (id2 == 27) {
                    return Collections.singleton(PokedexEntry.Form.UNOWN_EXCLAMATION);
                } else if (id2 == 28) {
                    return Collections.singleton(PokedexEntry.Form.UNOWN_QUESTION);
                } else {
                    return Collections.emptySet();
                }
            } else if (pokemonId == 351) { // Castform
                switch (id2) {
                    case 29:
                        return Collections.singleton(PokedexEntry.Form.CASTFORM_NORMAL);
                    case 30:
                        return Collections.singleton(PokedexEntry.Form.CASTFORM_SUNNY);
                    case 31:
                        return Collections.singleton(PokedexEntry.Form.CASTFORM_RAINY);
                    case 32:
                        return Collections.singleton(PokedexEntry.Form.CASTFORM_SNOWY);
                    default:
                        return Collections.emptySet();
                }
            }
            // TODO: implement other form handling
            // see: https://github.com/novabot-sprites/novabot-sprites/blob/master/rename-charles-forms.sh
        }

        return Collections.emptySet();
    }

}
