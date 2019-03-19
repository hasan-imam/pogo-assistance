package pogo.assistance.data.model.pokemon;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import lombok.experimental.UtilityClass;
import pogo.assistance.data.model.pokemon.PokedexEntry.Gender;
import pogo.assistance.data.serde.FileIOUtils;
import pogo.assistance.data.serde.SerDeModule;

/**
 * TODO: implement a ditto disguise possibility lookup
 * TODO: need more flexible 'form' qualifier
 * TODO: implement a non-nesting pokemon lookup (need to be kept up to date with game changes)
 * TODO: use genderless index in lookup methods
 */
@UtilityClass
public class Pokedex {

    private static Map<Integer, PokedexFileEntry> ID_TO_ENTRY;
    /**
     * Entries keyed by lower case pokemon name.
     */
    private static Map<String, PokedexFileEntry> ENGLISH_NAME_TO_ENTRY;
    private static Set<Integer> GENDERLESS_POKEMON_IDS;
    private static Set<Integer> ALOLAN_POSSIBLE_POKEMON_IDS;

    static {
        loadEntriesToIndex();
        loadGenderlessPokemonIndex();
        loadAlolanPossiblePokemonIndex();
    }

    public static Optional<PokedexEntry> getPokedexEntryFor(final int pokemonId, @Nullable final Gender gender, final Set<PokedexEntry.Form> forms) {
        return Optional.ofNullable(ID_TO_ENTRY.get(pokemonId))
                .map(pokedexFileEntry -> getPokedexEntryFromFileEntry(pokedexFileEntry, gender, forms));
    }

    public static Optional<PokedexEntry> getPokedexEntryFor(final int pokemonId, @Nullable final Gender gender) {
        return Optional.ofNullable(ID_TO_ENTRY.get(pokemonId))
                .map(pokedexFileEntry -> getPokedexEntryFromFileEntry(pokedexFileEntry, gender, Collections.emptySet()));
    }

    public static Optional<PokedexEntry> getPokedexEntryFor(final String pokemonName, @Nullable final Gender gender) {
        final String lowerCaseName = pokemonName.toLowerCase();
        final Optional<PokedexEntry> lookedUp = Optional.ofNullable(ENGLISH_NAME_TO_ENTRY.get(lowerCaseName))
                .map(entry -> getPokedexEntryFromFileEntry(entry, gender, Collections.emptySet()));
        if (!lookedUp.isPresent()) {
            // Look-up isn't straightforward for some. Put some effort into matching those.
            if (lowerCaseName.contains("nidoran")) {
                if (gender != null && gender != Gender.UNKNOWN && gender != Gender.NONE) {
                    return getPokedexEntryFor(gender == Gender.FEMALE ? 29 : 32, gender);
                } else {
                    return Optional.empty();
                }
            }
            if (lowerCaseName.contains("unown")) {
                // TODO: implement some basic inference to figure out the 'unown' letter
                return getPokedexEntryFor(201, Gender.NONE);
            }
        }
        return lookedUp;
    }

    public boolean isGenderLess(final int pokemonId) {
        return GENDERLESS_POKEMON_IDS.contains(pokemonId);
    }

    public boolean canHaveAlolanForm(final int pokemonId) {
        return ALOLAN_POSSIBLE_POKEMON_IDS.contains(pokemonId);
    }

    private static void loadEntriesToIndex() {
        if (ID_TO_ENTRY != null && ENGLISH_NAME_TO_ENTRY != null) {
            return;
        }

        final Map<Integer, PokedexFileEntry> idToEntry = new ConcurrentHashMap<>();
        final Map<String, PokedexFileEntry> englishNameToEntry = new ConcurrentHashMap<>();

        final List<PokedexFileEntry> entries = readEntriesFromFile();
        entries.forEach(pokedexFileEntry -> {
            idToEntry.put(pokedexFileEntry.getId(), pokedexFileEntry);
            englishNameToEntry.put(pokedexFileEntry.getName().get("english").toLowerCase(), pokedexFileEntry);
        });

        ID_TO_ENTRY = Collections.unmodifiableMap(idToEntry);
        ENGLISH_NAME_TO_ENTRY = Collections.unmodifiableMap(englishNameToEntry);
    }

    private static void loadGenderlessPokemonIndex() {
        GENDERLESS_POKEMON_IDS = PokedexConstants.GENDERLESS_POKEMON_NAMES.stream()
                .map(String::toLowerCase)
                .map(lowerCaseName -> Objects.requireNonNull(ENGLISH_NAME_TO_ENTRY.get(lowerCaseName)))
                .map(PokedexFileEntry::getId)
                .collect(Collectors.toSet());
    }

    private static void loadAlolanPossiblePokemonIndex() {
        ALOLAN_POSSIBLE_POKEMON_IDS = PokedexConstants.ALOLAN_POSSIBLE_POKEMON_NAMES.stream()
                .map(String::toLowerCase)
                .map(lowerCaseName -> Objects.requireNonNull(ENGLISH_NAME_TO_ENTRY.get(lowerCaseName)))
                .map(PokedexFileEntry::getId)
                .collect(Collectors.toSet());
    }

    private static List<PokedexFileEntry> readEntriesFromFile() {
        try {
            return Arrays.asList(SerDeModule.providesGson(Collections.emptyMap(), Collections.emptySet()).fromJson(
                    new String(Files.readAllBytes(
                            FileIOUtils.resolvePackageLocalFilePath("pokedex.json", Pokedex.class)),
                            StandardCharsets.UTF_8),
                    PokedexFileEntry[].class));
        } catch (final IOException e) {
            throw new RuntimeException("Failed to read pokedex file: pokedex.json", e);
        }
    }

    private static PokedexEntry getPokedexEntryFromFileEntry(
            final PokedexFileEntry pokedexFileEntry,
            @Nullable final Gender gender,
            final Set<PokedexEntry.Form> forms) {
        return ImmutablePokedexEntry.builder()
                .id(pokedexFileEntry.getId())
                .name(pokedexFileEntry.getName().get("english"))
                .gender(isGenderLess(pokedexFileEntry.getId()) ? Gender.NONE
                        : (gender == null ? Gender.UNKNOWN : gender))
                .forms(forms)
                .build();
    }

}
