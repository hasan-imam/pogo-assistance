package pogo.assistance.data.model.pokemon;

import java.util.Set;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

/**
 * Represents a Pokedex entry.
 *
 * A single Pokemon can have multiple dex entries for its gender variants, different forms etc.
 * TODO: make use of this repo: https://github.com/fanzeyi/pokemon.json/blob/master/pokedex.json
 */
@Gson.TypeAdapters
@Value.Immutable(intern = true)
public interface PokedexEntry {

    /**
     * Dex entry ID. This matches the Pokemon's dex entry ID in the game.
     */
    int getId();

    /**
     * Pokemon name in English.
     */
    String getName();

    /**
     * Since this field isn't optional, but there are sources which do not have gender information, this can be filled
     * with {@link Gender#NONE} in such missing information cases. But there are also Pokemon which are genderless and
     * can have their gender rightfully set to {@link Gender#NONE}.
     */
    Gender getGender();

    /**
     * This is a set since a single pokemon can have multiple form qualified (e.g. both alolan and shiny).
     */
    Set<Form> getForms();

    enum Gender {
        MALE,
        FEMALE,
        NONE,
        UNKNOWN
    }

    /**
     * Qualifier, when present on an entry, can alter various attributes (e.g. move set, type, stats) of the pokemon.
     */
    enum Form {
        // General forms
        ALOLAN,
        SHINY,

        // Pokemon specific forms
        CASTFORM_SUNNY, CASTFORM_RAINY, CASTFORM_SHOWY, CASTFORM_NORMAL
    }

}
