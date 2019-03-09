package pogo.assistance.data.exchange.spawn;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class SpawnDuplicateDetector {

    /**
     * If a spawn doesn't have a de-spawn time on it, it's kept in memory for this duration. If it does have de-spawn
     * time then that time is used to decide when to forget about that spawn.
     */
    private static final Duration DEFAULT_SPAWN_TTL = Duration.ofHours(1);
    /**
     * Assumption: At any given time, exchange shouldn't have to keep references to this many unexpired spawn objects.
     */
    private static final int DEFAULT_EVICTION_SIZE_THRESHOLD = 20000;
    /**
     * Assumption: We can afford to wait this long before doing clean up, even if we have more than
     * {@link #DEFAULT_EVICTION_SIZE_THRESHOLD} entries in {@link #spawnToExpiration}.
     */
    private static final Duration DEFAULT_EVICTION_TIME_THRESHOLD = Duration.ofMinutes(5);

    private final int evictionSizeThreshold;
    private final Duration evictionInterval;
    private final Stopwatch stopwatch;
    /**
     * Map that holds references to offered {@link PokemonSpawn}s. This mapping helps de-dupe same spawn being offered
     * from different sources. The map value is used to evict expired spawn objects from the map, so we don't keep the
     * map growing forever.
     *
     * Notable that this de-duping is dependent on duplicate {@link PokemonSpawn}s being exactly the same. If at some
     * point they start containing fields ignorable for this purpose (e.g. some metadata, source info etc.) we'll need
     * to come up with a custom composite key.
     */
    @VisibleForTesting
    @Getter(AccessLevel.PACKAGE)
    private final Map<PokemonSpawn, Instant> spawnToExpiration;

    public SpawnDuplicateDetector() {
        evictionSizeThreshold = DEFAULT_EVICTION_SIZE_THRESHOLD;
        evictionInterval = DEFAULT_EVICTION_TIME_THRESHOLD;
        spawnToExpiration = new ConcurrentHashMap<>();
        stopwatch = Stopwatch.createStarted();
    }

    /**
     * Checks whether a {@code pokemonSpawn} is duplicate or not. Also memorizes the spawn so a subsequent call will
     * return false.
     *
     * @param pokemonSpawn
     *      Spawn to check for duplication
     * @return
     *      True if the spawn is unique - not the same as any other passed to this method
     * @implNote
     *      Idea here is to be able to do checking from one thread even if the {@link #evictStaleSpawns() eviction} is
     *      running on a separate thread.
     */
    public synchronized boolean isUnique(final PokemonSpawn pokemonSpawn) {
        evictStaleSpawns();
        return syncCheckUniqueAndMemorize(pokemonSpawn);
    }

    private synchronized boolean syncCheckUniqueAndMemorize(final PokemonSpawn pokemonSpawn) {
        final boolean isDuplicate = spawnToExpiration.containsKey(pokemonSpawn);
        spawnToExpiration.computeIfAbsent(pokemonSpawn, __ -> {
            // Set expiration to despawn time or a default TTL offset from current time
            return pokemonSpawn.getDespawnTime()
                    .orElseGet(() -> Instant.now().plus(DEFAULT_SPAWN_TTL));
        });
        return !isDuplicate;
    }

    private void evictStaleSpawns() {
        // To prevent the map from growing endlessly, do some clean up
        final int spawnMapSize = spawnToExpiration.size();
        if (spawnMapSize >= evictionSizeThreshold) {
            if (stopwatch.elapsed().compareTo(evictionInterval) > 0) {
                stopwatch.reset().start();
                final Instant now = Instant.now();
                spawnToExpiration.entrySet().removeIf(entry -> {
                    return entry.getValue().isBefore(now);
                });
                log.debug("Cleared spawn de-duping map. Size went from {} to {}.", spawnMapSize, spawnToExpiration.size());
            }
        }
    }

}
