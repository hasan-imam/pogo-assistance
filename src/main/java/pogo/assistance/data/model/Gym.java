package pogo.assistance.data.model;

import java.util.Optional;

import org.immutables.gson.Gson;
import org.immutables.value.Value;
import io.jenetics.jpx.Point;

@Gson.TypeAdapters
@Value.Immutable
public interface Gym extends Point {

    Optional<String> getName();
    Optional<Boolean> isExRaidEligible();
    Optional<Boolean> isSponsored();
    Optional<Team> getDefendingTeam();

    enum Team {
        NONE,
        MYSTIC,
        VALOR,
        INSTINCT
    }

}
