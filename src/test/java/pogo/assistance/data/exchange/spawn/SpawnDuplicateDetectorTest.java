package pogo.assistance.data.exchange.spawn;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import io.jenetics.jpx.WayPoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pogo.assistance.data.model.ImmutableSourceMetadata;
import pogo.assistance.data.model.pokemon.*;

class SpawnDuplicateDetectorTest {

    private static final Instant NOW = Instant.now();

    @Test
    void isUnique_WithDuplicate_DetectsDuplication() {
        final SpawnDuplicateDetector spawnDuplicateDetector = spy(new SpawnDuplicateDetector());
        final PokemonSpawn testSpawn1 = ImmutablePokemonSpawn.builder()
                .from(WayPoint.of(-1.0, -1.0))
                .pokedexEntry(Pokedex.getPokedexEntryFor(1, PokedexEntry.Gender.FEMALE).get())
                .cp(1)
                .iv(1.0)
                .level(1)
                .sourceMetadata(ImmutableSourceMetadata.builder().sourceName("test").build())
                .build();
        final PokemonSpawn testSpawn2 = ImmutablePokemonSpawn.copyOf(testSpawn1);
        assertTrue(spawnDuplicateDetector.isUnique(testSpawn1));
        assertFalse(spawnDuplicateDetector.isUnique(testSpawn2));
        assertFalse(spawnDuplicateDetector.isUnique(testSpawn1));
    }

    @Test
    void isUnique_BreachMapSizeThreshold_EvictsExpected() {
        final int evictionSizeThreshold = 10;
        final SpawnDuplicateDetector spawnDuplicateDetector = spy(new SpawnDuplicateDetector(
                evictionSizeThreshold, Duration.ZERO, Stopwatch.createStarted(), new ConcurrentHashMap<>()));
        // 5 in the past, 4 in the future, one at 'NOW' (which is also past at this point)
        IntStream.range(-evictionSizeThreshold/2, evictionSizeThreshold/2)
                .mapToObj(value -> NOW.plus(value, ChronoUnit.HOURS))
                .map(SpawnDuplicateDetectorTest::mockWithDespawnTime)
                .map(spawnDuplicateDetector::isUnique)
                .forEach(Assertions::assertTrue);
        assertEquals(evictionSizeThreshold, spawnDuplicateDetector.getSpawnToExpiration().size());

        final PokemonSpawn mockedSpawn = mockWithDespawnTime(NOW);
        spawnDuplicateDetector.isUnique(mockedSpawn);
        assertEquals(5, spawnDuplicateDetector.getSpawnToExpiration().size());
        assertThat(spawnDuplicateDetector.getSpawnToExpiration(), hasKey(mockedSpawn));
    }

    @Test
    void isUnique_BreachMapSizeThresholdWithoutReachingInterval_DoesNotEvict() {
        final int evictionSizeThreshold = 10;
        final Ticker ticker = mock(Ticker.class);
        final SpawnDuplicateDetector spawnDuplicateDetector = spy(new SpawnDuplicateDetector(
                evictionSizeThreshold, Duration.ZERO, Stopwatch.createStarted(ticker), new ConcurrentHashMap<>()));
        IntStream.rangeClosed(1, evictionSizeThreshold)
                .mapToObj(NOW::minusMillis)
                .map(SpawnDuplicateDetectorTest::mockWithDespawnTime)
                .map(spawnDuplicateDetector::isUnique)
                .forEach(Assertions::assertTrue);
        assertEquals(evictionSizeThreshold, spawnDuplicateDetector.getSpawnToExpiration().size());

        when(ticker.read()).thenReturn(0L);
        final PokemonSpawn mockedSpawn = mockWithDespawnTime(NOW);
        spawnDuplicateDetector.isUnique(mockedSpawn);
        assertEquals(11, spawnDuplicateDetector.getSpawnToExpiration().size());
    }

    private static PokemonSpawn mockWithDespawnTime(final Instant instant) {
        final PokemonSpawn mockedSpawn = mock(PokemonSpawn.class);
        when(mockedSpawn.getDespawnTime()).thenReturn(Optional.of(instant));
        return mockedSpawn;
    }
}