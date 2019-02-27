package pogo.assistance.bot.responder.relay.pokedex100;

import com.google.common.base.Preconditions;
import lombok.experimental.UtilityClass;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokedexEntry.Form;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

@UtilityClass
public class VerifierBotUtils {

    /**
     * @return
     *      100iv post command for SuperBotP. Example command: "geodude cp445 m 44.5555,55.4444"
     */
    public static String toVerifierBotCommand(final PokemonSpawn pokemonSpawn) {
        Preconditions.checkArgument(pokemonSpawn.getIv().isPresent());
        Preconditions.checkArgument(pokemonSpawn.getCp().isPresent());
        return String.format("%s cp%d %s %f,%f",
                getNameSegment(pokemonSpawn.getPokedexEntry()),
                pokemonSpawn.getCp().get(),
                getGenderSegment(pokemonSpawn.getPokedexEntry()),
                pokemonSpawn.getLatitude().toDegrees(),
                pokemonSpawn.getLongitude().toDegrees());
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

        if (pokedexEntry.getForms().contains(Form.ALOLAN)) {
            return "alolan " + name;
        }

        return name;
    }

    private static String getGenderSegment(final PokedexEntry pokedexEntry) {
        switch (pokedexEntry.getGender()) {
            case MALE: return "m";
            case FEMALE: return "f";
            case NONE: case UNKNOWN: return "";
            default: throw new UnsupportedOperationException("Unsupported gender: " + pokedexEntry.getGender());
        }
    }

}