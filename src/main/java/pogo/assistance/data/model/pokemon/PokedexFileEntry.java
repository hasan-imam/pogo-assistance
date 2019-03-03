package pogo.assistance.data.model.pokemon;

import java.util.List;
import java.util.Map;
import org.immutables.gson.Gson;
import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

/**
 * Only to be used to read the pokedex.json file. Immutable generated type adapters just makes the JSON parsing easy.
 */
@Gson.TypeAdapters
@Value.Immutable
@Value.Style(visibility = ImplementationVisibility.PACKAGE)
interface PokedexFileEntry {
    int getId();
    Map<String, String> getName();
    List<String> getType();
}
