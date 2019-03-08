package pogo.assistance.bot.responder.relay.pokedex100;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import pogo.assistance.data.model.pokemon.Pokedex;
import pogo.assistance.data.model.pokemon.PokedexEntry;

@UtilityClass
public class CandySelector {

    private static final Set<String> CANDY_POKEMON_NAMES = ImmutableSet.<String>builder()
            // max evolved
            .add("Charizard").add("Venusaur").add("Blastoise").add("Meganium").add("Typhlosion").add("Feraligatr")

            // non nesting
            .add("lapras")
//            .add("hitmonlee").add("hitmonchan")
            .add("lickitung")
            .add("chansey").add("blissey")
            .add("snorlax")
            .add("dratini").add("dragonair").add("dragonite")
            .add("mareep")
            .add("unown")
            .add("pineco").add("forretress")
            .add("larvitar").add("pupitar").add("tyranitar")
            .add("ralts").add("kirlia").add("gardevoir").add("gallade")
            .add("slakoth").add("vigoroth").add("slaking")
//            .add("hariyama")
            .add("trapinch")
            .add("lunatone").add("solrock")
            .add("feebas")
            .add("chimecho")
            .add("clamperl")
            .add("bagon").add("shelgon").add("salamence")
            .add("beldum").add("metang")
            .add("cranidos").add("rampardos")
            .add("combee").add("vespiquen")
            .add("bronzor").add("bronzong")
            .add("finneon").add("lumineon")
            .build();

    private static final Set<Integer> CANDY_POKEMON_IDS;

    static {
        CANDY_POKEMON_IDS = CANDY_POKEMON_NAMES.stream()
                .map(pokemonName -> Pokedex.getPokedexEntryFor(pokemonName, null))
                .map(pokedexEntry -> pokedexEntry.get().getId())
                .collect(Collectors.toSet());
    }

    public static boolean isCandy(final PokedexEntry pokedexEntry) {
        return (pokedexEntry.getId() > 0 && CANDY_POKEMON_IDS.contains(pokedexEntry.getId()))
                || CANDY_POKEMON_NAMES.contains(pokedexEntry.getName().toLowerCase());
    }

}
