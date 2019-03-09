package pogo.assistance.data.extraction.source.web.pokemap.spawn;

import java.io.Closeable;
import java.util.List;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

/**
 * General interface for stateful/stateless fetchers. Extends closeable since fetchers are web-based and may need to
 * release connection resources when we are "done".
 */
public interface PokemonSpawnFetcher extends Closeable {

    /**
     * @return
     *      List of spawns fetched from the source. Fetched spawns should...
     *        - not contain things that have already de-spawned
     *        - not contain duplicates within the list
     *        - may contain duplicate across calls, but should try to reduce that as much as possible
     */
    List<PokemonSpawn> fetch();

}
