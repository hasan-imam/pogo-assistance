package pogo.assistance.data.exchange.spawn;

import com.google.common.base.Verify;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

@Slf4j
class PokemonSpawnObservers implements Runnable {

    private final Set<PokemonSpawnObserver> observers;
    private final BlockingQueue<PokemonSpawn> spawnQueue;
    private final ExecutorService executorService;
    private final AtomicBoolean stopRunning = new AtomicBoolean(false);

    public PokemonSpawnObservers(
            final Collection<? extends PokemonSpawnObserver> observers,
            final BlockingQueue<PokemonSpawn> consumableSpawnQueue) {
        this.observers = Sets.newConcurrentHashSet(observers);
        this.spawnQueue = consumableSpawnQueue;
        this.executorService = Executors.newWorkStealingPool();
    }

    @Override
    public void run() {
        Verify.verify(!stopRunning.get());
        log.info("Observing pokemon spawn queue...");
        while (!stopRunning.get()) {
            try {
                final PokemonSpawn pokemonSpawn = spawnQueue.take();
                observers.forEach(observer -> executorService.submit(() -> observer.observe(pokemonSpawn)));
            } catch (final InterruptedException e) {
                log.warn("Pokemon spawn observers stopping on interruption.", e);
                log.warn("{} observe operations were awaiting execution", executorService.shutdownNow().size());
                Thread.currentThread().interrupt();
            }
        }
    }

    public void stopRunning() {
        stopRunning.set(true);
    }
}
