package pogo.assistance.data.extraction.source.web.radar;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.inject.Inject;

import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import pogo.assistance.bot.responder.relay.pokedex100.CandySelector;
import pogo.assistance.data.extraction.source.SpawnSummaryStatistics;
import pogo.assistance.data.extraction.source.web.PokemonSpawnFetcher;
import pogo.assistance.data.model.Region;
import pogo.assistance.data.model.pokemon.ImmutablePokedexEntry;
import pogo.assistance.data.model.pokemon.ImmutablePokemonSpawn;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

@Slf4j
public class RadarSpawnDataExtractor implements Closeable, PokemonSpawnFetcher {

    private final AtomicReference<Instant> lastQueryTime = new AtomicReference<>(Instant.EPOCH);

    private final CloseableHttpClient closeableHttpClient;
    private final Gson gson;
    private final Region region;
    private final CookieStore cookieStore;

    @Inject
    public RadarSpawnDataExtractor(final Gson gson, final CloseableHttpClient closeableHttpClient, final Region region) {
        this.gson = gson;
        this.closeableHttpClient = closeableHttpClient;
        this.region = region;
        this.cookieStore = new BasicCookieStore();

        // Calling this populates the cookie store with the csrf token which is needed for the fetch
        RadarUtils.refreshCookies(this.cookieStore, closeableHttpClient, region);
        // TODO: check what happens are cookie expiration
        // TODO: add handling for expiration
    }

    @Override
    public List<PokemonSpawn> fetch() {
        final Instant prevQueryTime = lastQueryTime.get();
        final JsonArray spawnEntries = executeQuery()
                .map(fullPayload -> fullPayload.getAsJsonObject("data"))
                .map(dataObject -> dataObject.getAsJsonArray("pokemon"))
                .orElseGet(JsonArray::new);

        final AtomicInteger fetchedCount = new AtomicInteger(0);
        final AtomicInteger uniqueCount = new AtomicInteger(0);
        final SpawnSummaryStatistics statistics = new SpawnSummaryStatistics();
        final List<PokemonSpawn> pokemonSpawns = StreamSupport.stream(spawnEntries.spliterator(), false)
                .peek(__ -> fetchedCount.incrementAndGet())
                .map(jsonElement -> gson.fromJson(jsonElement, PokemonSpawnEntry.class))
                .map(PokemonSpawnEntry::asPokemonSpawn)
                .distinct()
                .peek(__ -> uniqueCount.incrementAndGet())
                .filter(pokemonSpawn -> !pokemonSpawn.getDespawnTime().isPresent()
                        || !pokemonSpawn.getDespawnTime().get().isBefore(Instant.now()))
                // Exclude dsp time since it might be introducing a lot of duplicates
                // TODO: investigate and re-enable
                .map(pokemonSpawn -> ImmutablePokemonSpawn.builder().from(pokemonSpawn).despawnTime(Optional.empty()).build())
                .peek(statistics)
                .collect(Collectors.toList());

        log.debug("Fetched spawns for {} at {}: total fetched: {}, without duplicate: {}, without de-spawned: {}",
                region, lastQueryTime.get(), fetchedCount, uniqueCount, pokemonSpawns.size());
        log.trace("{} spawn statistics between {} - {} ({}):\n{}",
                region, prevQueryTime, lastQueryTime.get(), Duration.between(prevQueryTime, lastQueryTime.get()), statistics.toString());

        return pokemonSpawns;
    }

    private Optional<JsonObject> executeQuery() {
        final Optional<JsonObject> fetched = RadarUtils.executeQuery(region, cookieStore, prepareRequest(), closeableHttpClient)
                .map(s -> gson.fromJson(s, JsonObject.class));
        fetched.ifPresent(this::updateLastQueryTime);
        return fetched;
    }

    private void updateLastQueryTime(final JsonObject fullPayload) {
        final Instant lastUpdateTime = Optional.ofNullable(fullPayload.getAsJsonObject("data"))
                .map(dataObject -> dataObject.getAsJsonPrimitive("timestamp"))
                // Web UI always seems to subtract one from the actual quenry time when making the next call
                // Copy the same behavior here
                .map(lastUpdateTimeEpochSeconds -> lastUpdateTimeEpochSeconds.getAsLong() - 1)
                .map(lastUpdateTimeEpochSeconds -> lastUpdateTimeEpochSeconds * 1000)
                .map(Instant::ofEpochMilli)
                .orElseThrow(() -> new IllegalArgumentException("Could not parse timestamp from payload: " + fullPayload));
        log.trace("Updated last fetch time for {} from {} to {}", region, lastQueryTime.getAndSet(lastUpdateTime), lastUpdateTime);
    }

    private HttpUriRequest prepareRequest() {
        final String baseUrl = RadarUtils.BASE_URLS_OF_SOURCES.get(region).toString();
        final String uri = baseUrl + "/api/get_data";
        return RequestBuilder.post(uri)
                .setEntity(new UrlEncodedFormEntity((Iterable<? extends NameValuePair>) prepareFormParameters()))
                .setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=UTF-8")
                .setHeader(HttpHeaders.HOST, RadarUtils.BASE_URLS_OF_SOURCES.get(region).getHost()) // TODO check what this outputs
                .setHeader(HttpHeaders.REFERER, baseUrl)
                .setHeader(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.9")
                .build();
    }

    private List<NameValuePair> prepareFormParameters() {
        final List<NameValuePair> params = new ArrayList<>();

        params.add(new BasicNameValuePair("_", Long.toString(Instant.now().toEpochMilli()/1000)));

        // Roughly, the max zoom out
        switch (region) {
            case CL:
                params.add(new BasicNameValuePair("min_lat", "-44.083269355721626"));
                params.add(new BasicNameValuePair("max_lat", "-23.639020310618793"));
                params.add(new BasicNameValuePair("min_lon", "-90.66072463989259"));
                params.add(new BasicNameValuePair("max_lon", "-62.887287139892585"));
                break;
            case EXTON:
                params.add(new BasicNameValuePair("min_lat", "38.90684728656818"));
                params.add(new BasicNameValuePair("max_lat", "41.46619022337922"));
                params.add(new BasicNameValuePair("min_lon", "-76.9719958305359"));
                params.add(new BasicNameValuePair("max_lon", "-73.4398913383484"));
                break;
            default:
                throw new UnsupportedOperationException("Radar map doesn't cover region: " + region);
        }

        // All turned off, except for pokemons
        params.add(new BasicNameValuePair("show_gyms", "false"));
        params.add(new BasicNameValuePair("show_raids", "false"));
        params.add(new BasicNameValuePair("show_pokestops", "false"));
        params.add(new BasicNameValuePair("show_quests", "false"));
        params.add(new BasicNameValuePair("show_pokemon", "true"));
        params.add(new BasicNameValuePair("show_spawnpoints", "false"));
        params.add(new BasicNameValuePair("show_cells", "false"));

        // Filtering criteria
        // For pokemon - get all candies and 90iv+
        params.add(new BasicNameValuePair("pokemon_filter_exclude", CandySelector.NON_CANDY_POKEMON_IDS.toString()));
        params.add(new BasicNameValuePair("pokemon_filter_iv", "{\"or\":\"90-100\"}"));
        params.add(new BasicNameValuePair("quest_filter_exclude", "[]"));

        params.add(new BasicNameValuePair("last_update", Long.toString(lastQueryTime.get().toEpochMilli()/1000)));
        params.add(new BasicNameValuePair("_csrf", getCsrfToken()));

        return params;
    }

    @Override
    public void close() throws IOException {
        closeableHttpClient.close();
    }

    private String getCsrfToken() {
        return cookieStore.getCookies().stream().filter(cookie -> cookie.getName().equals("CSRF-TOKEN")).findAny().get().getValue();
    }
}