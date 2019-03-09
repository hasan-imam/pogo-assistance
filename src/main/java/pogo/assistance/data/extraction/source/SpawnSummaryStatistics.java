package pogo.assistance.data.extraction.source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import pogo.assistance.bot.responder.relay.pokedex100.CandySelector;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

/**
 * @implSpec
 *      Stats will not be consistent if {@link #toString()} and {@link #accept(PokemonSpawn)} happens concurrently.
 */
public class SpawnSummaryStatistics implements Consumer<PokemonSpawn> {

    private final List<PokemonSpawn> spawns = Collections.synchronizedList(new ArrayList<>());

    private long countTotal = 0;
    private long countHasIv = 0, countHasCp = 0, countHasLevel = 0;

    // Following counts are exclusive, e.g. if something is counted towards 90+ iv, it's not included in 80+ iv count
    private long countWithIv100 = 0, countWithIv90 = 0, countWithIv80 = 0, countWithIv50 = 0, countWithIv0 = 0;
    private long countWithCp3000 = 0, countWithCp2000 = 0;
    private long countWithLevel30 = 0;
    private long countTotalCandies = 0, countCandiesHasIv = 0;

    @Override
    public void accept(final PokemonSpawn pokemonSpawn) {
        spawns.add(pokemonSpawn);

        countTotal++;
        pokemonSpawn.getLevel().ifPresent(level -> {
            countHasLevel++;
            if (level >= 30) {
                countWithLevel30++;
            }
        });
        pokemonSpawn.getCp().ifPresent(cp -> {
            countHasCp++;
            if (cp >= 3000) {
                countWithCp3000++;
            } else if (cp >= 2000) {
                countWithCp2000++;
            }
        });
        pokemonSpawn.getIv().ifPresent(iv -> {
            countHasIv++;
            if (iv == 100) {
                countWithIv100++;
            } else if (iv >= 90) {
                countWithIv90++;
            } else if (iv >= 80) {
                countWithIv80++;
            } else if (iv >= 50) {
                countWithIv50++;
            } else if (iv == 0) {
                countWithIv0++;
            }
        });
        if (CandySelector.isCandy(pokemonSpawn.getPokedexEntry())) {
            countTotalCandies++;
            if (pokemonSpawn.getIv().isPresent()) {
                countCandiesHasIv++;
            }
        }
    }

    /**
     * @return
     *      Multi-line report of spawn statistics, with each line preceded by a 'tab' character.
     */
    public String toString() {
        final StringBuilder message = new StringBuilder("\t" + countTotal + " total spawns");

        message.append(System.lineSeparator())
                .append(String.format("\t%d had IV -> %d @ 100, %d 90+, %d 80+, %d 50+, %d @ 0",
                        countHasIv, countWithIv100, countWithIv90, countWithIv80, countWithIv50, countWithIv0));
        message.append(System.lineSeparator())
                .append(String.format("\t%d had CP -> %d 3000+, %d 2000+",
                        countHasCp, countWithCp3000, countWithCp2000));
        message.append(System.lineSeparator())
                .append(String.format("\t%d had level -> %d 30+",
                        countHasLevel, countWithLevel30));
        message.append(System.lineSeparator())
                .append(String.format("\t%d candies, %d of them have iv",
                countTotalCandies, countCandiesHasIv));

        final Map<String, Long> candyCounts = spawns.stream()
                .map(PokemonSpawn::getPokedexEntry)
                .filter(CandySelector::isCandy)
                .map(PokedexEntry::getName)
                .sorted()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        message.append(System.lineSeparator()).append(String.format("\tCandy count by pokemon: %s", candyCounts));

        return message.toString();
    }
}
