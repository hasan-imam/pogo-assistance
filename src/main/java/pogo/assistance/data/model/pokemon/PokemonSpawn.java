package pogo.assistance.data.model.pokemon;

import io.jenetics.jpx.Point;
import java.time.Instant;
import java.util.Optional;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

/**
 * Represents a Pokemon spawn. The mandatory fields here are just the dex entry for the Pokemon and the geo location.
 * All spawn data unit is expected to contain at least this information. Based on the source various other information
 * can be present about the spawn.
 */
@Gson.TypeAdapters
@Value.Immutable
public interface PokemonSpawn extends Point {

    PokedexEntry getPokedexEntry();
    Optional<Integer> getLevel();
    Optional<Integer> getCp();
    Optional<Double> getIv();
    Optional<Instant> getDespawnTime();
    Optional<String> getLocationDescription();

}
