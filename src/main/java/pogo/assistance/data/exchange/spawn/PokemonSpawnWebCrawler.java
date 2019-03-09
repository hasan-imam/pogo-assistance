package pogo.assistance.data.exchange.spawn;

import com.google.common.util.concurrent.AbstractScheduledService;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import pogo.assistance.data.extraction.source.web.pokemap.spawn.PokemonSpawnFetcher;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

/**
 * TODO: dynamic/configurable fetch intervals
 * @implNote
 *      Why does this thing have a de-duplicator when the exchange already handles duplicates?
 *      We'll know more clearly where the duplicate spawns are coming from the logs of each de-duplicator
 */
@Slf4j
public class PokemonSpawnWebCrawler extends AbstractScheduledService {

    private final SpawnDuplicateDetector duplicateDetector = new SpawnDuplicateDetector();

    private final Set<PokemonSpawnFetcher> spawnFetchers;
    private final PokemonSpawnExchange spawnExchange;

    public PokemonSpawnWebCrawler(final Set<PokemonSpawnFetcher> spawnFetchers, final PokemonSpawnExchange spawnExchange) {
        this.spawnFetchers = spawnFetchers;
        this.spawnExchange = spawnExchange;
    }

    @Override
    protected void runOneIteration() throws Exception {
        final AtomicInteger fetchedCount = new AtomicInteger(0);
        final AtomicInteger offeredCount = new AtomicInteger(0);
        spawnFetchers.parallelStream()
                .map(PokemonSpawnWebCrawler::executeFetch)
                .flatMap(List::stream)
                .peek(__ -> fetchedCount.incrementAndGet())
                .filter(duplicateDetector::isUnique)
                .peek(__ -> offeredCount.incrementAndGet())
                .forEach(spawnExchange::offer);
        log.debug("Fetched {} spawns in total. Enqueued {} after de-duplication.",
                fetchedCount.get(), offeredCount.get());
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedRateSchedule(0, 1, TimeUnit.MINUTES);
    }

    @Override
    protected void shutDown() throws Exception {
        spawnFetchers.forEach(fetcher -> {
            try {
                fetcher.close();
            } catch (final IOException e) {
                log.error(String.format("Failed to close a fetcher of class %s", fetcher.getClass().getSimpleName()), e);
            }
        });
    }

    private static List<PokemonSpawn> executeFetch(final PokemonSpawnFetcher pokemonSpawnFetcher) {
        try {
            return pokemonSpawnFetcher.fetch();
        } catch (final Exception e) {
            log.error(String.format("%s failed to fetch", pokemonSpawnFetcher.getClass().getSimpleName()), e);
            return Collections.emptyList();
        }
    }
}
