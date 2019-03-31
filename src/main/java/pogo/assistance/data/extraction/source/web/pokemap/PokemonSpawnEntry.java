package pogo.assistance.data.extraction.source.web.pokemap;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;

import org.immutables.gson.Gson;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;
import org.immutables.value.Value.Style.ImplementationVisibility;
import com.google.gson.annotations.SerializedName;
import io.jenetics.jpx.WayPoint;
import pogo.assistance.data.model.Region;
import pogo.assistance.data.model.SourceMetadata;
import pogo.assistance.data.model.pokemon.ImmutablePokedexEntry;
import pogo.assistance.data.model.pokemon.ImmutablePokemonSpawn;
import pogo.assistance.data.model.pokemon.Pokedex;
import pogo.assistance.data.model.pokemon.PokedexEntry.Form;
import pogo.assistance.data.model.pokemon.PokedexEntry.Gender;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

/**
 * Interface representing a single pokemon spawn entry in the JSON pokemap sends when we query it for spawns. Immutables
 * generates JSON adapter for it, making it easy to parse the JSON. Converting from this representation to the
 * general {@link PokemonSpawn} is also done {@link #asPokemonSpawn(Region)} here}.
 */
@Gson.TypeAdapters
@Value.Immutable
@Value.Style(visibility = ImplementationVisibility.PACKAGE)
interface PokemonSpawnEntry {

    @SerializedName("pokemon_id")
    int pokemonId();

    @SerializedName("lat")
    double latitude();

    @SerializedName("lng")
    double longitude();

    @SerializedName("despawn")
    long despawnTimeEpochMilli();

    @SerializedName("disguise")
    int isDisguised();

    @SerializedName("attack")
    int attack();

    @SerializedName("defence")
    int defence();

    @SerializedName("stamina")
    int stamina();

    @SerializedName("move1")
    int move1();

    @SerializedName("move2")
    int move2();

    @SerializedName("costume")
    int costume();

    @SerializedName("gender")
    int gender();

    @SerializedName("shiny")
    int shiny();

    @SerializedName("form")
    int form();

    @SerializedName("cp")
    int cp();

    @SerializedName("level")
    int level();

    @SerializedName("weather")
    int weather();

    @Derived
    default double iv() {
        // TODO: implement this properly - not sure if this is correct
        return (100.0 * (attack() + defence() + stamina()) / 45);
    }

    @Derived
    default Gender genderEnum() {
        switch (gender()) {
            case 1: return Gender.MALE;
            case 2: return Gender.FEMALE;
            case 3: return Gender.NONE;
            default:
                throw new UnsupportedOperationException("Unhandled gender ID: " + gender());
        }
    }

    @Derived
    default Set<Form> forms() {
        // TODO: implement handling
        // Sample: Grimer (id: 88) gets form 73 when alolan
        return Collections.emptySet();
    }

    @Derived
    default PokemonSpawn asPokemonSpawn(final SourceMetadata sourceMetadata) {
        final ImmutablePokemonSpawn.Builder builder = ImmutablePokemonSpawn.builder();
        builder.from(WayPoint.of(latitude(), longitude()));
        builder.sourceMetadata(sourceMetadata);
        builder.pokedexEntry(ImmutablePokedexEntry.builder()
                .from(Pokedex.getPokedexEntryFor(pokemonId(), genderEnum()).get())
                .forms(forms())
                .build());
        if (level() > 0) {
            builder.level(level());
        }
        if (cp() > 0) {
            builder.cp(cp());
        }
        if (iv() >= 0) {
            builder.iv(iv());
        }
        builder.despawnTime(Instant.ofEpochMilli(despawnTimeEpochMilli() * 1000));
        return builder.build();
    }

}
