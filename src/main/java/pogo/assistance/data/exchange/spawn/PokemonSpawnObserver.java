package pogo.assistance.data.exchange.spawn;

import pogo.assistance.data.model.pokemon.PokemonSpawn;

public interface PokemonSpawnObserver {

    void observe(final PokemonSpawn pokemonSpawn);

}
