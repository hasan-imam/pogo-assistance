package pogo.assistance.bot.responder.relay.pokedex100;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import lombok.experimental.UtilityClass;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokedexEntry.Form;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static pogo.assistance.data.model.pokemon.PokedexEntry.Form.*;
import static pogo.assistance.data.model.pokemon.PokedexEntry.Gender.FEMALE;
import static pogo.assistance.data.model.pokemon.PokedexEntry.Gender.MALE;

/**
 * Example commands:
 *  - "?df alolan geodude iv50 44.5555,55.4444" -> candy
 *  - "?df [id] 44.5555,55.4444" -> candy
 */
@UtilityClass
public class VerifierBotUtils {

    /**
     * @return
     *      100iv post command for SuperBotP. Example command: "geodude cp445 m 44.5555,55.4444"
     */
    static String toPerfectIvSpawnCommand(final PokemonSpawn pokemonSpawn, final boolean mentionPoster, final boolean isForDonors) {
        Preconditions.checkArgument(pokemonSpawn.getIv().isPresent() && pokemonSpawn.getIv().get() == 100.0);
        Preconditions.checkArgument(pokemonSpawn.getCp().isPresent());
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("%s cp%d %s %f,%f",
                getNameSegment(pokemonSpawn.getPokedexEntry()),
                pokemonSpawn.getCp().get(),
                getGenderSegment(pokemonSpawn.getPokedexEntry()),
                pokemonSpawn.getLatitude().toDegrees(),
                pokemonSpawn.getLongitude().toDegrees()));
        if (mentionPoster) {
            stringBuilder.append(" n");
        }
        if (isForDonors) {
            stringBuilder.append(" d");
        }
        if (pokemonSpawn.getDespawnTime().isPresent()) {
            stringBuilder.append(" ").append(getDespawnTimeSegment(pokemonSpawn.getDespawnTime().get()));
        }
        return stringBuilder.toString();
    }

    static String toImperfectIvSpawnCommand(final PokemonSpawn pokemonSpawn, final boolean mentionPoster, final boolean isForDonors) {
        Preconditions.checkArgument(!pokemonSpawn.getIv().isPresent() || pokemonSpawn.getIv().get() < 100.0);
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

        if (!mentionPoster) {
            stringBuilder.append(" n"); // needed to NOT mention verifier name for 90+ iv posts
        }

        if (isForDonors) {
            stringBuilder.append(" d");
        }

        if (pokemonSpawn.getDespawnTime().isPresent()) {
            stringBuilder.append(" ").append(getDespawnTimeSegment(pokemonSpawn.getDespawnTime().get()));
        }

        return stringBuilder.toString();
    }

    /**
     * @return
     *      Pokemon name adapted to string that bot accepts.
     */
    private static String getNameSegment(final PokedexEntry pokedexEntry) {
        final String name = pokedexEntry.getName();
        if (pokedexEntry.getId() == 29 || pokedexEntry.getId() == 32) {
            switch (pokedexEntry.getGender()) {
                case MALE: return "nidoranm";
                case FEMALE: return "nidoranf";
                case UNKNOWN: return "nidoran";
                default: throw new UnsupportedOperationException("Unsupported Nidoran gender: " + pokedexEntry.getGender());
            }
        }

        if (name.equalsIgnoreCase("castform")) {
            if (pokedexEntry.getForms().contains(Form.CASTFORM_SUNNY)) {
                return name + "sunny";
            } else if (pokedexEntry.getForms().contains(Form.CASTFORM_RAINY)) {
                return name + "raniy";
            } else if (pokedexEntry.getForms().contains(Form.CASTFORM_SNOWY)) {
                return name + "snowy";
            } else if (pokedexEntry.getForms().contains(Form.CASTFORM_NORMAL)) {
                return name + "normal";
            } else {
                return name;
            }
        }

        if (name.equalsIgnoreCase("burmy") || name.equalsIgnoreCase("wormadam")) {
            if (pokedexEntry.getForms().contains(PLANT_CLOAK)) {
                return name + "plant";
            } else if (pokedexEntry.getForms().contains(SANDY_CLOAK)) {
                return name + "sand";
            } else if (pokedexEntry.getForms().contains(TRASH_CLOAK)) {
                return name + "trash";
            } else {
                return name;
            }
        }

        if (name.equalsIgnoreCase("cherrim")) {
            if (pokedexEntry.getForms().contains(SUNSHINE)) {
                return name + "sunshine";
            } else if (pokedexEntry.getForms().contains(OVERCAST)) {
                return name + "overcast";
            } else {
                return name;
            }
        }

        if (name.equalsIgnoreCase("shellos") || name.equalsIgnoreCase("gastrodon")) {
            if (pokedexEntry.getForms().contains(EAST_SEA)) {
                return name + "e";
            } else if (pokedexEntry.getForms().contains(WEST_SEA)) {
                return name + "w";
            } else {
                return name;
            }
        }

        if (name.toLowerCase().contains("unown")) {
            if (!pokedexEntry.getForms().isEmpty()) {
                // Form present - prepare name using that
                Verify.verify(pokedexEntry.getForms().size() == 1);
                final Form form = pokedexEntry.getForms().iterator().next();
                Verify.verify(form.name().contains("UNOWN"));
                final String character = form.name().replaceFirst("UNOWN_", "");
                if (character.length() == 1) {
                    return "Unown" + character;
                } else if (character.contains("EXCLAMATION")) {
                    return "Unown!";
                } else {
                    return "Unown?";
                }
            } else {
                /*
                 * Sometimes the form isn't present and the name is not exactly just "Unown" but with some characters added
                 * Expected conversions:
                 *   - "unown" -> "unown"
                 *   - "Unown A" -> "UnownA"
                 *   - "Unown - B" -> "UnownB"
                 */
                return name.replaceAll("[\\s\\-]", "");
            }
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

    private static String getDespawnTimeSegment(final Instant dspTime) {
        return String.format("dsp%dm", Instant.now().until(dspTime, ChronoUnit.MINUTES));
    }

}