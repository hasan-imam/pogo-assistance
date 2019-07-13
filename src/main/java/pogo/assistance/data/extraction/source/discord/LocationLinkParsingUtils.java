package pogo.assistance.data.extraction.source.discord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Verify;
import io.jenetics.jpx.Point;
import io.jenetics.jpx.WayPoint;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LocationLinkParsingUtils {

    /**
     * Example map URLs:
     *  - http://maps.google.com/maps?q=37.4332914692569,-122.115651980398
     *  - https://www.google.com/maps/search/?api=1&query=37.5542702090763,-77.4791150614027
     */
    public static final Pattern GOOGLE_MAP_URL =
            Pattern.compile("(http.+google.+(q=|query=))(?<latitude>[-\\d\\.]+),(?<longitude>[-\\d\\.]+)");

    /**
     * Example map URLs:
     *  - http://maps.apple.com/maps?daddr=32.96542400382587,-117.09967962954777&z=10&t=s&dirflg=w
     *  - http://maps.apple.com/?ll=50.894967,4.341626
     */
    private static final Pattern APPLE_MAP_URL = Pattern.compile("http.+apple.+(daddr=|ll=|sll=)(?<latitude>[-\\d\\.]+),(?<longitude>[-\\d\\.]+)");

    /**
     * Example map URLs:
     *  - https://waze.com/ul?ll=32.96542400382587,-117.09967962954777
     */
    private static final Pattern WAZE_MAP_URL = Pattern.compile("http.+waze.+ll=(?<latitude>[-\\d\\.]+),(?<longitude>[-\\d\\.]+)");

    public static Point extractLocation(final String compiledText) {
        final AtomicInteger countPointsExtracted = new AtomicInteger(0);
        final Map<Point, Long> pointToOccurrence = Stream.of(GOOGLE_MAP_URL, APPLE_MAP_URL, WAZE_MAP_URL)
                .map(pattern -> extractPointsUsingPattern(compiledText, pattern))
                .flatMap(Collection::stream)
                .peek(point -> countPointsExtracted.incrementAndGet())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        Verify.verify(!pointToOccurrence.isEmpty(), "Didn't find any co-ordinate on text: %s", compiledText);
        final Map.Entry<Point, Long> mostFrequentEntry = pointToOccurrence.entrySet().stream()
                .max(Comparator.comparing(Map.Entry::getValue))
                .get();
        Verify.verify((countPointsExtracted.get() - mostFrequentEntry.getValue()) <= 1,
                "Confusing number of points that don't match with one another: %s",
                pointToOccurrence);
        return mostFrequentEntry.getKey();
    }

    private static List<Point> extractPointsUsingPattern(final String compiledText, final Pattern pattern) {
        final Matcher mapUrlMatcher = pattern.matcher(compiledText);
        final List<Point> points = new ArrayList<>();
        while (mapUrlMatcher.find()) {
            points.add(WayPoint.of(Double.parseDouble(mapUrlMatcher.group("latitude")), Double.parseDouble(mapUrlMatcher.group("longitude"))));
        }
        return points;
    }

}
