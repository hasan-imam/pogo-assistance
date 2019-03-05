package pogo.assistance.data.model.pokemon;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import lombok.experimental.UtilityClass;
import pogo.assistance.data.model.pokemon.PokedexEntry.Gender;
import pogo.assistance.data.serde.FileIOUtils;
import pogo.assistance.data.serde.SerDeModule;

/**
 * TODO: implement a ditto disguise possibility lookup
 * TODO: need more flexible 'form' qualifier
 * TODO: implement a non-nesting pokemon lookup (need to be kept up to date with game changes)
 */
@UtilityClass
public class Pokedex {

    private static Map<Integer, PokedexFileEntry> ID_TO_ENTRY;
    private static Map<String, PokedexFileEntry> ENGLISH_NAME_TO_ENTRY;
    private static Map<String, PokedexFileEntry> CHINESE_NAME_TO_ENTRY;

    static {
        loadEntriesToIndex();
    }

    public static Optional<PokedexEntry> getPokedexEntryFor(final int pokemonId, @Nullable final Gender gender) {
        return Optional.ofNullable(ID_TO_ENTRY.get(pokemonId))
                .map(pokedexFileEntry -> getPokedexEntryFromFileEntry(pokedexFileEntry, gender));
    }

    public static Optional<PokedexEntry> getPokedexEntryFor(final String pokemonName, @Nullable final Gender gender) {
        final String lowerCaseName = pokemonName.toLowerCase();
        return Optional.ofNullable(Optional.ofNullable(ENGLISH_NAME_TO_ENTRY.get(lowerCaseName))
                .orElseGet(() -> CHINESE_NAME_TO_ENTRY.get(lowerCaseName)))
                .map(entry -> getPokedexEntryFromFileEntry(entry, gender));
    }

    private static void loadEntriesToIndex() {
        if (ID_TO_ENTRY != null && ENGLISH_NAME_TO_ENTRY != null && CHINESE_NAME_TO_ENTRY != null) {
            return;
        }

        final Map<Integer, PokedexFileEntry> idToEntry = new ConcurrentHashMap<>();
        final Map<String, PokedexFileEntry> englishNameToEntry = new ConcurrentHashMap<>();
        final Map<String, PokedexFileEntry> chineseNameToEntry = new ConcurrentHashMap<>();

        final List<PokedexFileEntry> entries = readEntriesFromFile();
        entries.forEach(pokedexFileEntry -> {
            idToEntry.put(pokedexFileEntry.getId(), pokedexFileEntry);
            englishNameToEntry.put(pokedexFileEntry.getName().get("english").toLowerCase(), pokedexFileEntry);
            chineseNameToEntry.put(pokedexFileEntry.getName().get("chinese"), pokedexFileEntry);
        });

        ID_TO_ENTRY = Collections.unmodifiableMap(idToEntry);
        ENGLISH_NAME_TO_ENTRY = Collections.unmodifiableMap(englishNameToEntry);
        CHINESE_NAME_TO_ENTRY = Collections.unmodifiableMap(chineseNameToEntry);
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
            @Nullable final Gender gender) {
        return ImmutablePokedexEntry.builder()
                .id(pokedexFileEntry.getId())
                .name(pokedexFileEntry.getName().get("english"))
                .gender(gender == null ? Gender.UNKNOWN : gender)
                .build();
    }

}
