package pogo.assistance.data.exchange.spawn;

import com.google.common.base.Stopwatch;
import com.google.common.base.Verify;
import com.google.common.util.concurrent.UncaughtExceptionHandlers;
import java.io.Closeable;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

@Singleton
@Slf4j
@RequiredArgsConstructor
public class PokemonSpawnExchange implements Closeable {

    /**
     * If an {@link #offer(PokemonSpawn) offered} {@link PokemonSpawn spawn} doesn't have
     * {@link PokemonSpawn#getDespawnTime() despawn time} set up, we assume it to despawn after this duration.
     */
    private static final Duration DEFAULT_SPAWN_TTL = Duration.ofHours(1);
    /**
     * Assumption: At any given time, exchange shouldn't have to keep references to this many unexpired spawn objects.
     */
    private static final int EVICTION_SIZE_THRESHOLD = 1000;
    /**
     * Assumption: We can afford to wait this long before doing clean up, even if we have more than
     * {@link #EVICTION_SIZE_THRESHOLD} entries in {@link #spawnToExpiration}.
     */
    private static final Duration EVICTION_TIME_THRESHOLD = Duration.ofMinutes(5);

    /**
     * Map that holds references to offered {@link PokemonSpawn}s. This mapping helps de-dupe same spawn being offered
     * from different sources. The map value is used to evict expired spawn objects from the map, so we don't keep the
     * map growing forever.
     *
     * Notable that this de-duping is dependent on duplicate {@link PokemonSpawn}s being exactly the same. If at some
     * point they start containing fields ignorable for this purpose (e.g. some metadata, source info etc.) we'll need
     * to come up with a custom composite key.
     */
    private final Map<PokemonSpawn, Instant> spawnToExpiration;

    /**
     * Internal queue where offered {@link PokemonSpawn}s are put. The {@link #pokemonSpawnObservers} consumes this
     * queue by dispatching new spawn info to all spawn registered spawn observers.
     */
    private final BlockingQueue<PokemonSpawn> spawnQueue;
    private final Stopwatch stopwatch;

    private final PokemonSpawnObservers pokemonSpawnObservers;
    private final Thread observerThread;

    public PokemonSpawnExchange(final Set<PokemonSpawnObserver> observers) {
        spawnToExpiration = new HashMap<>();
        spawnQueue = new LinkedBlockingQueue<>();
        pokemonSpawnObservers = new PokemonSpawnObservers(observers, this.spawnQueue);
        stopwatch = Stopwatch.createStarted();

        // Kick off the observer thread
        observerThread = new Thread(this.pokemonSpawnObservers);
        observerThread.setName("pokemon-spawn-observers");
        observerThread.setUncaughtExceptionHandler(UncaughtExceptionHandlers.systemExit());
        observerThread.start();
    }

    public synchronized void offer(final PokemonSpawn pokemonSpawn) {
        if (spawnToExpiration.containsKey(pokemonSpawn)) {
            log.trace("Ignoring duplicate spawn: {}", pokemonSpawn);
        }
        spawnToExpiration.computeIfAbsent(pokemonSpawn, __ -> {
            // Add to notification queue
            spawnQueue.add(pokemonSpawn);
            // Set expiration to despawn time or a default TTL offset from current time
            return pokemonSpawn.getDespawnTime()
                    .orElseGet(() -> Instant.now().plus(DEFAULT_SPAWN_TTL));
        });

        // To prevent the map from growing endlessly, do some clean up
        final int spawnMapSize = spawnToExpiration.size();
        if (spawnMapSize > EVICTION_SIZE_THRESHOLD) {
            if (stopwatch.elapsed().compareTo(EVICTION_TIME_THRESHOLD) > 0) {
                stopwatch.reset().start();
                final Instant now = Instant.now();
                spawnToExpiration.entrySet().removeIf(entry -> entry.getValue().compareTo(now) < 0);
                log.debug("Cleared spawn de-duping map. Size went from {} to {}.", spawnMapSize, spawnToExpiration.size());
            }
        }

        Verify.verify(observerThread.isAlive(),
                "Exchange getting new offering while observers are not listening.");
    }

    @Override
    public void close() {
        pokemonSpawnObservers.stopRunning();
        try {
            observerThread.join();
        } catch (final InterruptedException e) {
            observerThread.interrupt();
            log.error("Interrupted while waiting for observers to finish.", e);
        }
    }
}
