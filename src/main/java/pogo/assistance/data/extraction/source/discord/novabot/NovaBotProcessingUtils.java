package pogo.assistance.data.extraction.source.discord.novabot;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import com.google.common.base.Verify;
import com.google.common.primitives.Ints;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import pogo.assistance.data.model.pokemon.Pokedex;
import pogo.assistance.data.model.pokemon.PokedexEntry;

@Slf4j
@UtilityClass
public class NovaBotProcessingUtils {

    /*
     * Example thumbnail URLs:
     *  - https://raw.githubusercontent.com/novabot-sprites/novabot-sprites/master/401.png
     *  - https://raw.githubusercontent.com/mizu-github/PogoAssets/sugimori/nova_256/412-118.png?5
     *  - https://image.cdstud.io/o/351-29.png (we catch channel - castform)
     *  - https://www.serebii.net/sunmoon/pokemon/050-a.png
     *  - https://raw.githubusercontent.com/whitewillem/PogoAssets/resized/no_border/pokemon_icon_459_00.png
     *  - https://www.serebii.net/sunmoon/pokemon/019-a.png
     *  - https://raw.githubusercontent.com/plinytheelder/PoGo-Icons/master/pogo/019_046.png (Valley PoGo - alolan rattata)
     *
     * The form part of the URL can be missing for things that have multiple forms. In such cases some default (or unknown) form is inferred
     * picked, such as normal castform, non alolan, unown A etc.
     */
    private static final Pattern EMBED_THUMBNAIL_URL_PATTERN = Pattern.compile("(.+)/(pokemon_icon_)?(?<id>\\d+)[\\-_]?(?<id2>[\\w]+)?\\.png(.*)");

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
            if (url.toLowerCase().contains("serebii") && id2 == -1) {
                // Special handling for images from www.serebii.net
                if (Optional.ofNullable(matcher.group("id2")).filter("a"::equals).isPresent()) {
                    return Collections.singleton(PokedexEntry.Form.ALOLAN);
                }
            } else if (Pokedex.canHaveAlolanForm(pokemonId)) {
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
                    log.error("Unexpected unown form ID: {}", id2);
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
                        log.error("Unexpected castform form ID: {}", id2);
                        return Collections.emptySet();
                }
            } /*else if (pokemonId == 412) { // Burmy
                // This mapping is based on mizu-github/PogoAssets repo. May not match to same forms in another repo.
                switch (id2) {
                    case 118:
                        return Collections.singleton(PokedexEntry.Form.PLANT_CLOAK);
                    case 119:
                        return Collections.singleton(PokedexEntry.Form.SANDY_CLOAK);
                    case 120:
                        return Collections.singleton(PokedexEntry.Form.TRASH_CLOAK);
                    default:
                        log.error("Unexpected burmy form ID: {}", id2);
                        return Collections.emptySet();
                }
            }*/
            // TODO: implement other form handling
            // see: https://github.com/novabot-sprites/novabot-sprites/blob/master/rename-charles-forms.sh
        }

        return Collections.emptySet();
    }

}
