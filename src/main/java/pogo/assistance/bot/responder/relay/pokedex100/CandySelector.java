package pogo.assistance.bot.responder.relay.pokedex100;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import lombok.experimental.UtilityClass;
import pogo.assistance.data.model.pokemon.PokedexEntry;

@UtilityClass
class CandySelector {

    private static final Set<Integer> CANDY_POKEMON_IDS = ImmutableSet.<Integer>builder()
            .add(108)
            .add(113)
            .add(143)
            .add(147).add(148).add(149)
            .add(179) // mareep
            .add(201) // unown
            .add(242)
            .add(246).add(247).add(248)
            .add(287).add(288).add(289) // slakoth
            .add(358)
            .add(371).add(372).add(373) // bagon
            .add(374).add(375)
            .add(408).add(409) // cranidos
            .add(415).add(416)
            .build();
    private static final Set<String> CANDY_POKEMON_NAMES = ImmutableSet.<String>builder()
            .add("lickitung")
            .add("chansey")
            .add("snorlax")
            .add("dratini").add("dragonair").add("dragonite")
            .add("mareep")
            .add("unown")
            .add("blissey")
            .add("larvitar").add("pupitar").add("tyranitar")
            .add("slakoth").add("vigoroth").add("slaking")
            .add("chimecho")
            .add("bagon").add("shelgon").add("salamence")
            .add("beldum").add("metang")
            .add("cranidos").add("rampardos")
            .add("combee").add("vespiquen")
            .build();

    public static boolean isCandy(final PokedexEntry pokedexEntry) {
        return (pokedexEntry.getId() > 0 && CANDY_POKEMON_IDS.contains(pokedexEntry.getId()))
                || CANDY_POKEMON_NAMES.contains(pokedexEntry.getName().toLowerCase());
    }

}
