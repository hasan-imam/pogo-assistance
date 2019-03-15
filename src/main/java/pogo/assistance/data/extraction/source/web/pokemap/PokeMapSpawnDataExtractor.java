package pogo.assistance.data.extraction.source.web.pokemap;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;
import javax.inject.Inject;

import org.apache.hc.client5.http.classic.methods.RequestBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpHeaders;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import pogo.assistance.data.extraction.source.SpawnSummaryStatistics;
import pogo.assistance.data.extraction.source.web.PokemonSpawnFetcher;
import pogo.assistance.data.model.Region;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

@Slf4j
public class PokeMapSpawnDataExtractor implements Closeable, PokemonSpawnFetcher {

    private static final String QUERIED_POKEMON_ID_LIST = IntStream.rangeClosed(1, 493)
            .mapToObj(String::valueOf)
            .collect(Collectors.joining(","));

    private final AtomicReference<Instant> lastQueryTime = new AtomicReference<>(Instant.EPOCH);

    private final CloseableHttpClient closeableHttpClient;
    private final Gson gson;
    private final Region region;

    @Inject
    public PokeMapSpawnDataExtractor(
            final Gson gson,
            final CloseableHttpClient closeableHttpClient,
            final Region region) {
        Preconditions.checkArgument(PokeMapUtils.BASE_URLS_OF_SOURCES.containsKey(region));
        this.gson = gson;
        this.closeableHttpClient = closeableHttpClient;
        this.region = region;
    }

    @Override
    public synchronized List<PokemonSpawn> fetch() {
        final ClassicHttpRequest httpGetRequest = prepareRequest();
        final AtomicInteger fetchedCount = new AtomicInteger(0);
        final AtomicInteger uniqueCount = new AtomicInteger(0);
        final Instant prevQueryTime = lastQueryTime.get();
        final JsonArray spawnEntries = executeQuery(httpGetRequest)
                .map(jsonObject -> jsonObject.getAsJsonArray("pokemons"))
                .orElseGet(JsonArray::new);
        final SpawnSummaryStatistics statistics = new SpawnSummaryStatistics();
        final List<PokemonSpawn> pokemonSpawns = StreamSupport.stream(spawnEntries.spliterator(), false)
                .peek(__ -> fetchedCount.incrementAndGet())
                .distinct()
                .peek(__ -> uniqueCount.incrementAndGet())
                .map(jsonElement -> gson.fromJson(jsonElement, PokemonSpawnEntry.class))
                .map(PokemonSpawnEntry::asPokemonSpawn)
                .filter(pokemonSpawn -> !pokemonSpawn.getDespawnTime().isPresent()
                        || !pokemonSpawn.getDespawnTime().get().isBefore(Instant.now()))
                .peek(statistics)
                .collect(Collectors.toList());

        log.debug("Fetched spawns for {} at {}: total fetched: {}, without duplicate: {}, without de-spawned: {}",
                region, lastQueryTime.get(), fetchedCount, uniqueCount, pokemonSpawns.size());
        log.trace("{} spawn statistics between {} - {} ({}):\n{}",
                region, prevQueryTime, lastQueryTime.get(), Duration.between(prevQueryTime, lastQueryTime.get()), statistics.toString());

        return pokemonSpawns;
    }

    private Optional<JsonObject> executeQuery(final ClassicHttpRequest httpGetRequest) {
        final Optional<JsonObject> fetched = PokeMapUtils.executeQuery(closeableHttpClient, httpGetRequest)
                .map(s -> gson.fromJson(s, JsonObject.class));
        fetched.ifPresent(this::updateLastQueryTime);
        return fetched;
    }

    private void updateLastQueryTime(final JsonObject fullPayload) {
        final Instant inserted = Optional.ofNullable(fullPayload.getAsJsonObject("meta"))
                .map(meta -> meta.getAsJsonPrimitive("inserted"))
                .map(insertedEpochSeconds -> insertedEpochSeconds.getAsLong() * 1000)
                .map(Instant::ofEpochMilli)
                .orElseThrow(() -> new IllegalArgumentException("Could not parse timestamp from payload: " + fullPayload));
        // TODO: What if there's an error and we never update the last request time?
        // Would that result in staying stuck with that issue?
        log.trace("Updated last fetch time for {} from {} to {}", region, lastQueryTime.getAndSet(inserted), inserted);
    }

    private ClassicHttpRequest prepareRequest() {
        final String baseUrl = PokeMapUtils.BASE_URLS_OF_SOURCES.get(region).toString();
        final String uri = baseUrl + "/query2.php?" +
                "since=" + lastQueryTime.get().toEpochMilli()/1000 + // server expects seconds, not milliseconds
                "&mons=" + QUERIED_POKEMON_ID_LIST +
                "&minIV=0";
        return RequestBuilder.get(uri)
                .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .setHeader(HttpHeaders.REFERER, baseUrl + "/?forcerefresh")
                .setHeader(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.9")
                .build();
    }

    @Override
    public void close() throws IOException {
        closeableHttpClient.close();
    }
}
