package pogo.assistance.bot.collector;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import pogo.assistance.data.exchange.spawn.PokemonSpawnExchange;
import pogo.assistance.data.exchange.spawn.PokemonSpawnWebCrawler;
import pogo.assistance.data.extraction.source.web.PokemonSpawnFetcher;
import pogo.assistance.data.extraction.source.web.pokemap.PokeMapSpawnDataExtractor;
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
                new PokeMapSpawnDataExtractor(gson, closeableHttpClient, Region.YVR),
                new RadarSpawnDataExtractor(gson, closeableHttpClient, Region.CL)
//                new RadarSpawnDataExtractor(gson, closeableHttpClient, Region.FL)
//                new RadarSpawnDataExtractor(gson, closeableHttpClient, Region.EXTON)
        );
    }

    @Provides
    public static CloseableHttpClient provideCloseableHttpClientForCrawler() {
        return HttpClients.custom()
                .setSSLSocketFactory(provideLayeredConnectionSocketFactory())
                .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:60.0) Gecko/20100101 Firefox/60.0")
                .build();
    }

    /**
     * @return
     *      Socket factory with SSL context that tolerates self-signed certificates. This was a compromise made for the Exton map (https://extonpokemap.com).
     */
    private static LayeredConnectionSocketFactory provideLayeredConnectionSocketFactory() {
        try {
            return new SSLConnectionSocketFactory(SSLContextBuilder.create()
                    .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                    .build());
        } catch (final NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new RuntimeException("Failed to setup SSL context for connection", e);
        }
    }

}
