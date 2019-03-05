package pogo.assistance.bot.responder.relay.pokedex100;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import lombok.experimental.UtilityClass;
import pogo.assistance.data.model.pokemon.Pokedex;
import pogo.assistance.data.model.pokemon.PokedexEntry;

@UtilityClass
class CandySelector {

    private static final Set<Integer> CANDY_POKEMON_IDS = ImmutableSet.<Integer>builder()
            .add(108)
            .add(113).add(242) // chansey
            .add(143)
            .add(147).add(148).add(149)
            .add(179) // mareep
            .add(201) // unown
            .add(204).add(205) // pineco
            .add(246).add(247).add(248)
            .add(280).add(281).add(282).add(475) // ralts
            .add(287).add(288).add(289) // slakoth
            .add(328) // trapinch
            .add(337).add(338) // lunatone & solrock
            .add(349) // feebas
            .add(358) // chimecho
            .add(366) // clamperl
            .add(371).add(372).add(373) // bagon
            .add(374).add(375)
            .add(408).add(409) // cranidos
            .add(415).add(416)
            .add(436).add(437) // bronzor
            .add(456).add(457) // finneon
            .build();
    private static final Set<String> CANDY_POKEMON_NAMES = ImmutableSet.<String>builder()
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

    public static boolean isCandy(final PokedexEntry pokedexEntry) {
        return (pokedexEntry.getId() > 0 && CANDY_POKEMON_IDS.contains(pokedexEntry.getId()))
                || CANDY_POKEMON_NAMES.contains(pokedexEntry.getName().toLowerCase());
    }

}
