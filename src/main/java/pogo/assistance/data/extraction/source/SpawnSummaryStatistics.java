package pogo.assistance.data.extraction.source;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

import com.google.common.base.MoreObjects;
import pogo.assistance.bot.responder.relay.pokedex100.CandySelector;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

/**
 * @implSpec
 *      Stats will not be consistent if {@link #toString()} and {@link #accept(PokemonSpawn)} happens concurrently.
 */
public class SpawnSummaryStatistics implements Consumer<PokemonSpawn> {

    private long countTotal = 0;
    private long countHasIv = 0, countHasCp = 0, countHasLevel = 0;

    // Following counts are exclusive, e.g. if something is counted towards 90+ iv, it's not included in 80+ iv count
    private long countWithIv100 = 0, countWithIv90 = 0, countWithIv80 = 0, countWithIv50 = 0, countWithIv0 = 0;
    private long countWithCp3000 = 0, countWithCp2000 = 0;
    private long countWithLevel35 = 0, countWithLevel30 = 0;
    private long countTotalCandies = 0, countCandiesHasIv = 0;

    // Candy pokemon names mapped to the number of spawns for each of those candy pokemons
    private Map<String, Long> candyCounts = new TreeMap<>();

    @Override
    public void accept(final PokemonSpawn pokemonSpawn) {
        countTotal++;
        pokemonSpawn.getLevel().ifPresent(level -> {
            countHasLevel++;
            if (level == 35) {
                countWithLevel35++;
            } else if (level >= 30) {
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
            // Increment if present, add entry if absent
            candyCounts.computeIfPresent(pokemonSpawn.getPokedexEntry().getName(), (__, count) -> count + 1);
            candyCounts.putIfAbsent(pokemonSpawn.getPokedexEntry().getName(), 1L);
        }
    }

    public SpawnSummaryStatistics accumulate(final SpawnSummaryStatistics that) {
        final SpawnSummaryStatistics combined = new SpawnSummaryStatistics();
        SpawnSummaryStatistics.addStats(this, combined);
        SpawnSummaryStatistics.addStats(that, combined);
        return combined;
    }
    
    private static void addStats(final SpawnSummaryStatistics fromObject, final SpawnSummaryStatistics toObject) {
        toObject.countTotal += fromObject.countTotal;
        toObject.countHasIv += fromObject.countHasIv;
        toObject.countHasCp += fromObject.countHasCp;
        toObject.countHasLevel += fromObject.countHasLevel;

        toObject.countWithIv100 += fromObject.countWithIv100;
        toObject.countWithIv90 += fromObject.countWithIv90;
        toObject.countWithIv80 += fromObject.countWithIv80;
        toObject.countWithIv50 += fromObject.countWithIv50;
        toObject.countWithIv0 += fromObject.countWithIv0;

        toObject.countWithCp3000 += fromObject.countWithCp3000;
        toObject.countWithCp2000 += fromObject.countWithCp2000;
        toObject.countWithLevel35 += fromObject.countWithLevel35;
        toObject.countWithLevel30 += fromObject.countWithLevel30;
        toObject.countTotalCandies += fromObject.countTotalCandies;
        toObject.countCandiesHasIv += fromObject.countCandiesHasIv;

        fromObject.candyCounts.forEach((pokemonName, fromObjectCount) -> {
            toObject.candyCounts.compute(pokemonName, (__, toObjectCount) -> MoreObjects.firstNonNull(toObjectCount, 0L) + fromObjectCount);
        });
    }

    /**
     * @return
     *      Multi-line report of spawn statistics, with each line preceded by a 'tab' character.
     */
    public String toString() {
        final StringBuilder message = new StringBuilder("\t" + countTotal + " total spawns");

        message.append(System.lineSeparator())
                .append(String.format("\t%d had IV → %d @ 100%%, %d >= 90%%, %d >= 80%%, %d >= 50%%, %d @ 0%%",
                        countHasIv, countWithIv100, countWithIv90, countWithIv80, countWithIv50, countWithIv0));
        message.append(System.lineSeparator())
                .append(String.format("\t%d had CP → %d >= 3000, %d >= 2000",
                        countHasCp, countWithCp3000, countWithCp2000));
        message.append(System.lineSeparator())
                .append(String.format("\t%d had level → %d @ 35, %d >= 30",
                        countHasLevel, countWithLevel35, countWithLevel30));
        message.append(System.lineSeparator())
                .append(String.format("\t%d candies, %d of them had IV",
                countTotalCandies, countCandiesHasIv));

//        // Commenting out since this can result in too large of a string
//        // Currently this method is mainly used to send report in discord and candy details isn't too important
//        message.append(System.lineSeparator()).append(String.format("\tCandy count by pokemon: %s", candyCounts));

        return message.toString();
    }
}
