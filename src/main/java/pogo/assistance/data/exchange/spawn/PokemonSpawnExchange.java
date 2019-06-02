package pogo.assistance.data.exchange.spawn;

import java.io.Closeable;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.inject.Singleton;

import com.google.common.base.Verify;
import com.google.common.util.concurrent.UncaughtExceptionHandlers;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

@Singleton
@Slf4j
@RequiredArgsConstructor
public class PokemonSpawnExchange implements Closeable {

    /**
     * Internal queue where offered {@link PokemonSpawn}s are put. The {@link #pokemonSpawnObservers} consumes this
     * queue by dispatching new spawn info to all spawn registered spawn observers.
     */
    private final BlockingQueue<PokemonSpawn> spawnQueue;

    private final SpawnDuplicateDetector spawnDuplicateDetector;

    private final PokemonSpawnObservers pokemonSpawnObservers;
    private final Thread observerThread;

    public PokemonSpawnExchange(final Set<PokemonSpawnObserver> observers) {
        spawnDuplicateDetector = new SpawnDuplicateDetector();
        spawnQueue = new LinkedBlockingQueue<>();
        pokemonSpawnObservers = new PokemonSpawnObservers(observers, this.spawnQueue);

        // Kick off the observer thread
        // TODO refactor this to use guava service?
        observerThread = new Thread(this.pokemonSpawnObservers);
        observerThread.setName("pokemon-spawn-observers");
        observerThread.setUncaughtExceptionHandler(UncaughtExceptionHandlers.systemExit());
        observerThread.start();
    }

    public void offer(@NonNull final PokemonSpawn pokemonSpawn) {
        if (!spawnDuplicateDetector.isUnique(pokemonSpawn)) {
            log.trace("Ignoring duplicate spawn: {}", pokemonSpawn);
            return;
        }
        if (pokemonSpawn.getDespawnTime().isPresent() && pokemonSpawn.getDespawnTime().get().isBefore(Instant.now())) {
            // It's not really a spawn if it has already despawned...
            log.trace("Ignoring spawn that has already expired: {}", pokemonSpawn);
            return;
        }

        // Add to notification queue
        spawnQueue.add(pokemonSpawn);

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
