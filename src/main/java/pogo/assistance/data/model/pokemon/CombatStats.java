package pogo.assistance.data.model.pokemon;

import java.math.BigDecimal;
import java.util.Optional;

import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Gson.TypeAdapters
@Value.Immutable
public interface CombatStats {

    Optional<Double> attackIv();
    Optional<Double> defenseIv();
    Optional<Double> staminaIv();

    @Value.Default
    default Optional<Double> combinedIv() {
        if (attackIv().isPresent() && defenseIv().isPresent() && staminaIv().isPresent()) {
            return Optional.of(100.0 * (attackIv().get() + defenseIv().get() + staminaIv().get()) / 45)
                    .map(iv -> new BigDecimal(iv).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        }
        return Optional.empty();
    }
}
