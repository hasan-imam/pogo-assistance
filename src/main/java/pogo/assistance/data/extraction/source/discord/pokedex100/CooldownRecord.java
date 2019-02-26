package pogo.assistance.data.extraction.source.discord.pokedex100;

import io.jenetics.jpx.Length;
import io.jenetics.jpx.Length.Unit;
import io.jenetics.jpx.Point;
import java.time.Duration;
import java.util.Optional;
import org.immutables.gson.Gson;
import org.immutables.value.Value;
import org.slf4j.LoggerFactory;
import pogo.assistance.route.CooldownCalculator;

@Gson.TypeAdapters
@Value.Immutable
public interface CooldownRecord {

    Point fromPoint();
    Point toPoint();

    Optional<Length> pokedex100Distance();
    Optional<Duration> pokedex100Cooldown();

    /**
     * Distance calculated by {@link CooldownCalculator#getDistance(Point, Point) our internal library}
     */
    default Optional<Length> calculatedDistance() {
        return Optional.of(CooldownCalculator.getDistance(fromPoint(), toPoint()));
    }

    /**
     * Cooldown duration calcualted by {@link CooldownCalculator#getCooldown(Point, Point) our internal library}
     */
    default Optional<Duration> calculatedCooldown() {
        return Optional.of(CooldownCalculator.getCooldown(fromPoint(), toPoint()));
    }

    @Value.Derived
    default Length wgs84Distance() {
        try {
            return fromPoint().distance(toPoint());
        } catch (final ArithmeticException e) {
            // Happens when we try to calculate distance between nearly antipodal points
            // See: Geoid#distance for details
            LoggerFactory.getLogger(CooldownRecord.class).trace(e.getMessage());
            return Length.of(-1, Unit.METER);
        }
    }

    /**
     * Hand measured cooldown duration
     */
    Optional<Duration> measuredCooldown();

}
