package pogo.assistance.bot.responder.relay.pokedex100;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableSet;
import lombok.experimental.UtilityClass;
import pogo.assistance.data.model.pokemon.Pokedex;
import pogo.assistance.data.model.pokemon.PokedexEntry;

// TODO: Add unit tests to catch static configuration errors
@UtilityClass
public class CandySelector {

    // Case doesn't matter in the sets below

    private static final Set<String> MAX_EVOLVED = ImmutableSet.<String>builder()
            .add("Charizard").add("Venusaur").add("Blastoise").add("Meganium").add("Typhlosion").add("Feraligatr")
            .add("Hariyama")
            .build();

    private static final Set<String> NON_NESTING = ImmutableSet.<String>builder()
            .add("Togetic")
            .add("Aerodactyl")
            .add("Shieldon").add("Bastiodon")
            .add("lapras")
            .add("hitmonlee").add("hitmonchan")
            .add("lickitung")
            .add("Porygon")
            .add("chansey").add("blissey")
            .add("snorlax")
            .add("dratini").add("dragonair").add("dragonite")
            /*.add("mareep")*/
            /*.add("unown")*/
            .add("pineco").add("forretress")
            .add("larvitar").add("pupitar").add("tyranitar")
            /*.add("ralts")*/.add("kirlia").add("gardevoir").add("gallade")
            .add("slakoth").add("vigoroth").add("slaking")
            /*.add("trapinch")*/
            /*.add("lunatone").add("solrock")*/
            .add("feebas")
            .add("chimecho")
            /*.add("clamperl")*/
            .add("bagon").add("shelgon").add("salamence")
            .add("beldum").add("metang")
            .add("cranidos").add("rampardos")
            /*.add("combee")*/.add("vespiquen")
            .add("bronzor").add("bronzong")
            /*.add("finneon")*/.add("lumineon")
            .add("Uxie")
            .add("Azelf")
            .add("Mesprit")
            .add("Gible").add("Gabite").add("Garchomp")
            .add("Absol")
            .add("Mawile")
            .add("Ferroseed")
            .add("Deino")
            .add("Litwick")
            .add("Golett")
            .add("Durant")
            .add("Heatmor")
            .add("Zweilous")
            .add("Lampent")
            .add("Excadrill")
            .build();

    private static final Set<String> MISC_RARE = ImmutableSet.<String>builder()
            .add("Pachirisu")
            .build();

    private static final Set<String> CANDY_POKEMON_NAMES = ImmutableSet.<String>builder()
            // max evolved
            .addAll(MAX_EVOLVED)
            // rare regional
            .addAll(MISC_RARE)
            // non nesting
            .addAll(NON_NESTING)
            .build();

    public static final Set<Integer> CANDY_POKEMON_IDS;
    public static final Set<Integer> NON_CANDY_POKEMON_IDS;

    static {
        CANDY_POKEMON_IDS = CANDY_POKEMON_NAMES.stream()
                .map(pokemonName -> Pokedex.getPokedexEntryFor(pokemonName, null))
                .map(pokedexEntry -> pokedexEntry.get().getId())
                .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
        NON_CANDY_POKEMON_IDS = IntStream.rangeClosed(1, 493)
                .filter(id -> !CANDY_POKEMON_IDS.contains(id))
                .boxed()
                .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
    }

    public static boolean isCandy(final PokedexEntry pokedexEntry) {
        final int pokemonId = pokedexEntry.getId();
        return (pokemonId > 0 && CANDY_POKEMON_IDS.contains(pokemonId))
                || CANDY_POKEMON_NAMES.contains(pokedexEntry.getName().toLowerCase())
                || isCandyDueToEvent(pokedexEntry);
    }

    private static boolean isCandyDueToEvent(final PokedexEntry pokedexEntry) {
        final int pokemonId = pokedexEntry.getId();
        // New alolan launches
        if ((pokemonId == 27 || pokemonId == 37) && pokedexEntry.getForms().contains(PokedexEntry.Form.ALOLAN)) {
            return true;
        }

        return false;
    }

}
