package pogo.assistance.data.extraction.source.discord;

import com.google.common.base.Verify;
import io.jenetics.jpx.Point;
import io.jenetics.jpx.WayPoint;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import lombok.experimental.UtilityClass;
import pogo.assistance.data.model.pokemon.PokedexEntry.Gender;

@UtilityClass
public class SpawnMessageParsingUtils {

    /*
     * Example map URLs:
     *  - http://maps.google.com/maps?q=37.4332914692569,-122.115651980398
     *  - https://www.google.com/maps/search/?api=1&query=37.5542702090763,-77.4791150614027
     */
    private static final Pattern GOOGLE_MAP_QUERY_URL =
            Pattern.compile("(.+(q=|query=))(?<latitude>[-\\d\\.]+),(?<longitude>[-\\d\\.]+)(.*)");

    public static Point parseGoogleMapQueryLink(final String url) {
        final Matcher mapUrlMatcher = GOOGLE_MAP_QUERY_URL.matcher(url);
        Verify.verify(mapUrlMatcher.find());
        return WayPoint.of(Double.parseDouble(mapUrlMatcher.group("latitude")), Double.parseDouble(mapUrlMatcher.group("longitude")));
    }

    public static Gender parseGenderFromSign(@Nullable final String sign) {
        if (sign == null || sign.isEmpty()) {
            return Gender.NONE;
        }

        switch (sign) {
            case "♀":
                return Gender.FEMALE;
            case "♂":
                return Gender.MALE;
            case "⚲":
                return Gender.NONE;
            default:
                throw new IllegalArgumentException("Unrecognized/missing gender: " + sign);
        }
    }
}
