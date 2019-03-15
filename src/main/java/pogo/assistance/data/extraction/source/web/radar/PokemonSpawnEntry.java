package pogo.assistance.data.extraction.source.web.radar;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.immutables.gson.Gson;
import org.immutables.value.Value;
import org.slf4j.LoggerFactory;
import com.google.common.base.Verify;
import com.google.gson.annotations.SerializedName;
import io.jenetics.jpx.WayPoint;
import pogo.assistance.data.model.Weather;
import pogo.assistance.data.model.pokemon.ImmutablePokedexEntry;
import pogo.assistance.data.model.pokemon.ImmutablePokemonSpawn;
import pogo.assistance.data.model.pokemon.Pokedex;
import pogo.assistance.data.model.pokemon.PokedexEntry;
import pogo.assistance.data.model.pokemon.PokemonSpawn;

@Gson.TypeAdapters
@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PACKAGE)
interface PokemonSpawnEntry {

    @SerializedName("pokemon_id")
    int pokemonId();

    @SerializedName("lat")
    double latitude();

    @SerializedName("lon")
    double longitude();

    @SerializedName("expire_timestamp_true")
    boolean despawnTimeAccurate(); // TODO wtf is this

    @SerializedName("expire_timestamp")
    long despawnTimeEpochMilli();

    @SerializedName("first_seen_timestamp")
    long firstSeenTimeEpochMilli();

    @SerializedName("updated")
    long updatedTimeEpochMilli();

    @SerializedName("atk_iv")
    Optional<Integer> attack();

    @SerializedName("def_iv")
    Optional<Integer> defence();

    @SerializedName("sta_iv")
    Optional<Integer> stamina();

    @SerializedName("move_1")
    Optional<Integer> move1();

    @SerializedName("move_2")
    Optional<Integer> move2();

    @SerializedName("costume")
    int costume();

    @SerializedName("gender")
    int gender();

    @SerializedName("form")
    int form();

    @SerializedName("cp")
    Optional<Integer> cp();

    @SerializedName("level")
    Optional<Integer> level();

    @SerializedName("weather")
    Optional<Integer> weather();

    @SerializedName("weight")
    Optional<String> weight();

    @SerializedName("size")
    Optional<String> size();

    @SerializedName("spawn_id")
    Optional<String> spawnId();

    @SerializedName("id")
    String id();

    @SerializedName("pokestop_id")
    Optional<String> pokestopId(); // TODO: what is the meaning of this on a spawn data entry? nearest poke stop?

    @Value.Derived
    default Optional<Double> iv() {
        if (attack().isPresent() && defence().isPresent() && stamina().isPresent()) {
            // TODO: implement this properly - not sure if this is correct
            return Optional.of(100.0 * (attack().get() + defence().get() + stamina().get()) / 45);
        }
        return Optional.empty();
    }

    @Value.Derived
    default PokedexEntry.Gender genderEnum() {
        switch (gender()) {
            case 1: return PokedexEntry.Gender.MALE;
            case 2: return PokedexEntry.Gender.FEMALE;
            case 3: return PokedexEntry.Gender.NONE;
            default:
                throw new UnsupportedOperationException("Unhandled gender ID: " + gender());
        }
    }

    @Value.Derived
    default Set<PokedexEntry.Form> forms() {
        switch (pokemonId()) {
            case 351: // castform
                final Weather weather = weather().map(RadarUtils.WEATHER_ID_MAPPING::get).orElse(Weather.CLEAR);
                switch (form()) {
                    case 30: // normal
                        Verify.verify(weather == Weather.SUNNY || weather == Weather.CLEAR);
                        return Collections.singleton(PokedexEntry.Form.CASTFORM_NORMAL);
                    default:
                        LoggerFactory.getLogger(PokemonSpawnEntry.class).error("Unhandled castform form: {} with weather: {}", form(), weather());
                        return Collections.emptySet();
                }
            // TODO: implement handling
        }
        return Collections.emptySet();
    }

    @Value.Derived
    default PokemonSpawn asPokemonSpawn() {
        // TODO implement
        final ImmutablePokemonSpawn.Builder builder = ImmutablePokemonSpawn.builder();
        builder.from(WayPoint.of(latitude(), longitude()));
        builder.pokedexEntry(ImmutablePokedexEntry.builder().from(Pokedex.getPokedexEntryFor(pokemonId(), genderEnum()).get())
                .forms(forms())
                .build());
        level().ifPresent(builder::level);
        cp().ifPresent(builder::cp);
        iv().ifPresent(builder::iv);
        builder.despawnTime(Instant.ofEpochMilli(despawnTimeEpochMilli() * 1000));
        return builder.build();
    }

}
