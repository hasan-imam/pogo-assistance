package pogo.assistance.data.extraction.source.web.radar;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pogo.assistance.bot.collector.SpawnWebCrawlerModule;
import pogo.assistance.data.extraction.source.web.PokemonSpawnFetcher;
import pogo.assistance.data.model.Region;
import pogo.assistance.data.model.pokemon.PokemonSpawn;
import pogo.assistance.data.serde.SerDeModule;

public class PokeTrainerSpawnDataExtractorIntegrationTest {

    private static PokemonSpawnFetcher pokemonSpawnFetcher;

    @BeforeAll
    static void setUp() {
        pokemonSpawnFetcher = new RadarSpawnDataExtractor(
                SerDeModule.providesGson(Collections.emptyMap(), Collections.emptySet()),
                SpawnWebCrawlerModule.provideCloseableHttpClientForCrawler(),
                Region.FL);
    }

    @AfterAll
    static void tearDown() throws IOException {
        pokemonSpawnFetcher.close();
    }

    @Test
    void happyCase_FetchesSpawnData() {
        final List<PokemonSpawn> fetched = pokemonSpawnFetcher.fetch();
        assertFalse(fetched.isEmpty());
    }

    @Test
    void consecutiveFetches_SubsequentFetchGetsLessData() {
        final List<PokemonSpawn> fetchedFirst = pokemonSpawnFetcher.fetch();
        final List<PokemonSpawn> fetchedSecond = pokemonSpawnFetcher.fetch();
        assertFalse(fetchedFirst.isEmpty());
        assertTrue(fetchedSecond.isEmpty());
    }
}
