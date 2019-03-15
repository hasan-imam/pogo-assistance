package pogo.assistance.bot.collector;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import java.util.Set;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import pogo.assistance.data.exchange.spawn.PokemonSpawnExchange;
import pogo.assistance.data.exchange.spawn.PokemonSpawnWebCrawler;
import pogo.assistance.data.extraction.source.web.pokemap.PokeMapSpawnDataExtractor;
import pogo.assistance.data.extraction.source.web.PokemonSpawnFetcher;
import pogo.assistance.data.extraction.source.web.radar.RadarSpawnDataExtractor;
import pogo.assistance.data.model.Region;

@Module
public class SpawnWebCrawlerModule {

    @Provides
    public static PokemonSpawnWebCrawler providePokemonSpawnWebCrawler(
            final Set<PokemonSpawnFetcher> pokemonSpawnFetchers,
            final PokemonSpawnExchange pokemonSpawnExchange) {
        return new PokemonSpawnWebCrawler(pokemonSpawnFetchers, pokemonSpawnExchange);
    }

    @Provides
    @ElementsIntoSet
    public static Set<PokemonSpawnFetcher> providePokeMapFetchers(
            final Gson gson,
            final CloseableHttpClient closeableHttpClient) {
        return ImmutableSet.of(
                new PokeMapSpawnDataExtractor(gson, closeableHttpClient, Region.NYC),
                new PokeMapSpawnDataExtractor(gson, closeableHttpClient, Region.SG),
                new RadarSpawnDataExtractor(gson, closeableHttpClient, Region.CL)
        );
    }

    @Provides
    public static CloseableHttpClient provideCloseableHttpClientForCrawler() {
        return HttpClients.custom()
                .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:60.0) Gecko/20100101 Firefox/60.0")
                .build();
    }

}
