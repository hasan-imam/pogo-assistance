package pogo.assistance.bot.responder.relay.pokedex100;

import static pogo.assistance.data.model.pokemon.PokedexEntry.Form.ALOLAN;
import static pogo.assistance.data.model.pokemon.PokedexEntry.Gender.FEMALE;
import static pogo.assistance.data.model.pokemon.PokedexEntry.Gender.MALE;

import com.google.common.base.Preconditions;
import lombok.experimental.UtilityClass;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokedexEntry.Form;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

/**
 * Example commands:
 *  - "?df weedle 44.5555,55.4444" -> candy
 *  - "?df [id] 44.5555,55.4444" -> candy
 */
@UtilityClass
public class VerifierBotUtils {

    /**
     * @return
     *      100iv post command for SuperBotP. Example command: "geodude cp445 m 44.5555,55.4444"
     */
    public static String toPerfectIvSpawnCommand(final PokemonSpawn pokemonSpawn) {
        Preconditions.checkArgument(pokemonSpawn.getIv().isPresent() && pokemonSpawn.getIv().get() == 100.0);
        Preconditions.checkArgument(pokemonSpawn.getCp().isPresent());
        return String.format("%s cp%d %s %f,%f n",
                getNameSegment(pokemonSpawn.getPokedexEntry()),
                pokemonSpawn.getCp().get(),
                getGenderSegment(pokemonSpawn.getPokedexEntry()),
                pokemonSpawn.getLatitude().toDegrees(),
                pokemonSpawn.getLongitude().toDegrees());
    }

    public static String toImperfectIvSpawnCommand(final PokemonSpawn pokemonSpawn) {
        Preconditions.checkArgument(!pokemonSpawn.getIv().isPresent() || pokemonSpawn.getIv().get() != 100.0);
        final StringBuilder stringBuilder = new StringBuilder("?df");

        /*
         * Prefer ID over name, since names can potentially be non-english in current implementation. But no easy way to
         * handle different forms (alolans, castform forms etc.) so prefer names in those cases.
         */
        if (pokemonSpawn.getPokedexEntry().getId() > 0
                && pokemonSpawn.getPokedexEntry().getForms().isEmpty()
                && !pokemonSpawn.getPokedexEntry().getName().toLowerCase().contains("unown")) {
            stringBuilder.append(" ").append(pokemonSpawn.getPokedexEntry().getId());
        } else {
            stringBuilder.append(" ").append(getNameSegment(pokemonSpawn.getPokedexEntry()));
        }

        pokemonSpawn.getIv().ifPresent(iv -> stringBuilder.append(" IV").append(iv.intValue()));
        pokemonSpawn.getCp().ifPresent(cp -> stringBuilder.append(" CP").append(cp));
        pokemonSpawn.getLevel().ifPresent(level -> stringBuilder.append(" L").append(level));
        if (pokemonSpawn.getPokedexEntry().getGender() == MALE || pokemonSpawn.getPokedexEntry().getGender() == FEMALE) {
            stringBuilder.append(" ").append(getGenderSegment(pokemonSpawn.getPokedexEntry()));
        }
        stringBuilder.append(" ").append(pokemonSpawn.getLatitude().toDegrees()).append(",")
                .append(pokemonSpawn.getLongitude().toDegrees());

        if (pokemonSpawn.getIv().orElse(-1.0) >= 90.0) {
            stringBuilder.append(" n"); // needed to mention verifier name for 90+ iv posts
        }

        return stringBuilder.toString();
    }

    /**
     * @return
     *      Pokemon name adapted to string that bot accepts.
     */
    private static String getNameSegment(final PokedexEntry pokedexEntry) {
        final String name = pokedexEntry.getName();
        if (name.equalsIgnoreCase("nidoran")) {
            switch (pokedexEntry.getGender()) {
                case MALE: return "nidoranm";
                case FEMALE: return "nidoranf";
                case NONE: case UNKNOWN: return "nidoran";
                default: throw new UnsupportedOperationException("Unsupported gender: " + pokedexEntry.getGender());
            }
        }

        if (name.equalsIgnoreCase("castform")) {
            if (pokedexEntry.getForms().contains(Form.CASTFORM_SUNNY)) {
                return name + "sunny";
            } else if (pokedexEntry.getForms().contains(Form.CASTFORM_RAINY)) {
                return name + "raniy";
            } else if (pokedexEntry.getForms().contains(Form.CASTFORM_SHOWY)) {
                return name + "snowy";
            } else if (pokedexEntry.getForms().contains(Form.CASTFORM_NORMAL)) {
                return name + "normal";
            } else {
                return name;
            }
        }

        if (name.toLowerCase().contains("unown")) {
            /*
             * Expected conversions:
             *   - "unown" -> "unown"
             *   - "Unown A" -> "UnownA"
             *   - "Unown - B" -> "UnownB"
             */
            return name.replaceAll("[\\s\\-]", "");
        }

        if (pokedexEntry.getForms().contains(ALOLAN)) {
            return "alolan " + name;
        }

        return name;
    }

    private static String getGenderSegment(final PokedexEntry pokedexEntry) {
        switch (pokedexEntry.getGender()) {
            case MALE: return "M";
            case FEMALE: return "F";
            case NONE: case UNKNOWN: return "";
            default: throw new UnsupportedOperationException("Unsupported gender: " + pokedexEntry.getGender());
        }
    }

}