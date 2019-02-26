package pogo.assistance.route;

import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import io.jenetics.jpx.Length;
import io.jenetics.jpx.Length.Unit;
import io.jenetics.jpx.Point;
import java.time.Duration;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

public class CooldownCalculator {

    private static final NavigableMap<Double, Double> KM_TO_SECOND_CD_TABLE;

    static {
        final NavigableMap<Double, Double> kmToSecondCd = new TreeMap<>();
        kmToSecondCd.put(0.5D,   0 * 60D);
        kmToSecondCd.put(1D,   0.1 * 60D);
        kmToSecondCd.put(1.48D,  1 * 60D);
        kmToSecondCd.put(2D,   1.5 * 60D);
        kmToSecondCd.put(3D,   2.5 * 60D);
        kmToSecondCd.put(4D,     3 * 60D);
        kmToSecondCd.put(5.5D,   4 * 60D);
        kmToSecondCd.put(6D,   4.5 * 60D);
        kmToSecondCd.put(7D,     5 * 60D);
        kmToSecondCd.put(10D,    6 * 60D);
        kmToSecondCd.put(11D,    7 * 60D);
        kmToSecondCd.put(12.7D,  8 * 60D);
        kmToSecondCd.put(15D,    9 * 60D);
        kmToSecondCd.put(18D,   10 * 60D);
        kmToSecondCd.put(20.5D, 11 * 60D);
        kmToSecondCd.put(22D,   13 * 60D);
        kmToSecondCd.put(24D,   14 * 60D);
        kmToSecondCd.put(26D,   15 * 60D);
        kmToSecondCd.put(28D,   16 * 60D);
        kmToSecondCd.put(30.5D, 17 * 60D);
        kmToSecondCd.put(40D,   18 * 60D);
//        kmToSecondCd.put(42D,   19 * 60D);
        kmToSecondCd.put(43.5D, 19 * 60D); // from testing on pokedex100 bot
        kmToSecondCd.put(53D,   21 * 60D);
        kmToSecondCd.put(73D,   22 * 60D);
        kmToSecondCd.put(78D,   23 * 60D);
        kmToSecondCd.put(88.5D, 24 * 60D); // ?dist 33.9500,131.0000 34.7500,131.0000 -> 88.96 km, 27 minutes?
        kmToSecondCd.put(93D,   26 * 60D);
        kmToSecondCd.put(105D,  27 * 60D);
//        kmToSecondCd.put(113D,  28 * 60D);
//        kmToSecondCd.put(133D,  31 * 60D);
        kmToSecondCd.put(110D,  31 * 60D); // from testing on pokedex100 bot
        kmToSecondCd.put(133D,  34 * 60D); // from testing on pokedex100 bot
        /*
         * Following numbers are odd because the original numbers had "less-than" condition. For example:
         *     Original mapping condition: "< 500 KM -> 62 mins cooldown"
         *     Map entry: key: 499, value: 62 * 60
         * This is necessary because we lookup cooldown get querying ceiling entry in this navigable map
         */
        kmToSecondCd.put(135D,  35 * 60D); // from testing on pokedex100 bot
        kmToSecondCd.put(160D,  38 * 60D); // from testing on pokedex100 bot
        kmToSecondCd.put(200D,  42 * 60D); // from testing on pokedex100 bot
        kmToSecondCd.put(250D,  46 * 60D); // from testing on pokedex100 bot
        kmToSecondCd.put(305D,  49 * 60D); // from testing on pokedex100 bot
        kmToSecondCd.put(400D,  55 * 60D); // from testing on pokedex100 bot
        kmToSecondCd.put(499D,  62 * 60D);
        kmToSecondCd.put(549D,  66 * 60D);
        kmToSecondCd.put(599D,  70 * 60D);
        kmToSecondCd.put(649D,  74 * 60D);
        kmToSecondCd.put(699D,  77 * 60D);
        kmToSecondCd.put(750D,  82 * 60D);
        kmToSecondCd.put(801D,  84 * 60D);
        kmToSecondCd.put(838D,  88 * 60D);
        kmToSecondCd.put(898D,  90 * 60D);
        kmToSecondCd.put(899D,  91 * 60D);
        kmToSecondCd.put(947D,  95 * 60D);
        kmToSecondCd.put(1006D, 98 * 60D);
        kmToSecondCd.put(1019D, 102 * 60D);
        kmToSecondCd.put(1099D, 104 * 60D);
        kmToSecondCd.put(1179D, 109 * 60D);
        kmToSecondCd.put(1199D, 111 * 60D);
        kmToSecondCd.put(1220D, 113 * 60D);
        kmToSecondCd.put(1299D, 117 * 60D);
        kmToSecondCd.put(1343D, 119 * 60D);
        kmToSecondCd.put(1500D, 2 * 60 * 60D);
        kmToSecondCd.put(Double.MAX_VALUE, 2 * 60 * 60D);
        KM_TO_SECOND_CD_TABLE = Collections.unmodifiableNavigableMap(kmToSecondCd);
    }

    public static Duration getCooldown(final Point a, final Point b) {
        return cooldown(getDistance(a, b));
    }

    private static Duration cooldown(final Length distance) {
        final Entry<Double, Double> entry = KM_TO_SECOND_CD_TABLE.ceilingEntry(distance.to(Unit.METER));
        // No fraction in second values at the moment so it's okay to round from double to long
        return (entry == null) ? Duration.ofSeconds(Math.round(KM_TO_SECOND_CD_TABLE.lastEntry().getValue()))
                : Duration.ofSeconds(Math.round(entry.getValue()));
    }

    public static Length getDistance(final Point a, final Point b) {
        return getDistance(
                a.getLatitude().doubleValue(), a.getLongitude().doubleValue(),
                b.getLatitude().doubleValue(), b.getLongitude().doubleValue());
    }

    /**
     * @return
     *      Distance in kilometers
     * @implSpec
     *      Source: https://www.geodatasource.com/developers/java
     */
    public static Length getDistance(
            final double aLat, final double aLon,
            final double bLat, final double bLon) {
        final double theta = aLon - bLon;
        double dist = sin(toRadians(aLat)) * sin(toRadians(bLat))
                + cos(toRadians(aLat)) * cos(toRadians(bLat)) * cos(toRadians(theta));
        dist = acos(dist);
        dist = toDegrees(dist);
        // TODO: Refine these factors using: https://en.wikipedia.org/wiki/Conversion_of_units#Length
        dist = dist * 60 * 1.1515 * 1.609344;
        return Length.of(dist, Unit.METER);
    }

}