package pogo.assistance.data.model;

import java.time.Instant;
import java.util.Optional;

import org.immutables.gson.Gson;
import org.immutables.value.Value;
import pogo.assistance.data.model.pokemon.PokedexEntry;

@Gson.TypeAdapters
@Value.Immutable
public interface Raid {

    Optional<Instant> getSpawnTime();

    Instant getStartTime();
    Instant getEndTime();

    Gym getGym();
    int getLevel();

    Optional<Boolean> isExclusive();
    Optional<PokedexEntry> getPokedexEntry();
    Optional<Integer> getPokemonCp();

    Optional<Boolean> isBoosted();

}
