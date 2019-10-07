package pogo.assistance.data.extraction.source.discord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Closeables;
import com.google.common.net.HttpHeaders;
import io.jenetics.jpx.Point;
import io.jenetics.jpx.WayPoint;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.core.util.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import static java.net.HttpURLConnection.*;

@Slf4j
@UtilityClass
public class LocationLinkParsingUtils {

    /**
     * Verify online: https://regex101.com/r/5y0bcx/1
     */
    public static final Pattern MARKDOWN_LINK = Pattern.compile("\\[(?<text>[^\\]]*)\\]\\((?<link>[^\\)]*)\\)");

    /**
     * Example map URLs:
     *  - http://maps.google.com/maps?q=37.4332914692569,-122.115651980398
     *  - https://www.google.com/maps/search/?api=1&query=37.5542702090763,-77.4791150614027
     */
    public static final Pattern GOOGLE_MAP_URL =
            Pattern.compile("(http.+google.+(q=|query=|daddr=))(?<latitude>[-\\d\\.]+),(?<longitude>[-\\d\\.]+)");

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

    private static final Pattern SPINDA_MAP_URL = Pattern.compile("(?<url>http[s]?://link\\.spindamap\\.com/[/\\w\\d]+)");
    private static final Set<Pattern> REDIRECTING_URL_PATTERNS = ImmutableSet.of(SPINDA_MAP_URL);

    /**
     * @param compiledText
     *      Text to extract latitude-longitude data from. This text is expected to contain various map URLs (Google map,
     *      Apple map, Waze map etc.) where the URLs query parameters would have parsable lat-long details.
     * @return
     * @throws com.google.common.base.VerifyException
     *      if it fails to find geo location info in the input
     */
    public static Point extractLocation(final String compiledText) {
        final AtomicInteger countPointsExtracted = new AtomicInteger(0);
        final Map<Point, Long> pointToOccurrence = Stream.of(GOOGLE_MAP_URL, APPLE_MAP_URL, WAZE_MAP_URL)
                .map(pattern -> extractPointsUsingPattern(removeInvisibleMarkdownLinks(compiledText), pattern))
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

    private static String removeInvisibleMarkdownLinks(final String text) {
        final StringBuffer filteredTextBuffer = new StringBuffer();
        final Matcher linkMatcher = MARKDOWN_LINK.matcher(text);
        while (linkMatcher.find()) {
            // Find markdown links whose text (click-able section of link) contains no letter/digits and remove them
            // This process removes links unreadable to humans but exists to throw off machines
            if (linkMatcher.group("text").replaceAll("[^\\d^\\w]", "").isEmpty()) {
                linkMatcher.appendReplacement(filteredTextBuffer, "");
            }
        }
        linkMatcher.appendTail(filteredTextBuffer);
        return filteredTextBuffer.toString();
    }

    @VisibleForTesting
    static String replaceMapRedirectingUrls(final String text) {
        String replacedText = text; // updated with new text every time replacement is performed
        for (final Pattern pattern : REDIRECTING_URL_PATTERNS) {
            final Matcher matcher = pattern.matcher(replacedText);
            final StringBuffer sb = new StringBuffer(replacedText.length());
            while (matcher.find()) {
                final String originalUrl = matcher.group("url");
                Verify.verify(!Strings.isNullOrEmpty(originalUrl), "Pattern thinks empty string is a URL");

                final String redirectedUrl = getRedirectedUrlUsingDirectConnection(originalUrl)
                        .orElseGet(() -> getRedirectedUrlUsingCurl(originalUrl)
                                .orElseGet(() -> getRedirectedUrlUsingUnfurlr(originalUrl)
                                        .orElse(null)));
                final boolean isValidMapUrl = GOOGLE_MAP_URL.matcher(redirectedUrl).find()
                        || APPLE_MAP_URL.matcher(redirectedUrl).find()
                        || SPINDA_MAP_URL.matcher(redirectedUrl).find();
                if (!isValidMapUrl) {
                    log.warn("'{}' redirects to '{}', which doesn't seem to be a valid map URL", originalUrl, redirectedUrl);
                }
                matcher.appendReplacement(sb, isValidMapUrl ? redirectedUrl : Matcher.quoteReplacement(originalUrl));
            }
            matcher.appendTail(sb);
            replacedText = sb.toString();
        }
        return replacedText;
    }

    // TODO limit visibility
    public static Optional<String> getRedirectedUrlUsingCurl(@NonNull final String originalUrl) {
        final String command = "curl -Ls -o /dev/null -w %{url_effective} " + originalUrl;
        try {
            final Process process = Runtime.getRuntime().exec(command);
            try (final InputStream inputStream = process.getInputStream()) {
                final String redirectedUrl = new BufferedReader(new InputStreamReader(inputStream)).readLine();
                log.info(String.format("%s ---redirected---> %s", originalUrl, redirectedUrl));
                return Optional.of(redirectedUrl);
            }
        } catch (final IOException e) {
            log.error(String.format("Failed to find final redirect URL of this source URL: %s", originalUrl));
            return Optional.empty();
        }
    }

    // TODO limit visibility
    public static Optional<String> getRedirectedUrlUsingUnfurlr(final String originalUrl) {
        try {
            final Document document = Jsoup.connect("https://unfurlr.com")
                    .data("url", originalUrl)
                    .data("user_agent", "Random")
                    .post();
            return Optional.ofNullable(document.select("#content > div.results-content.clear > div > div.result.final-result > p"))
                    .map(Elements::html);
        } catch (final IOException e) {
            log.error(String.format("Failed to call Unfurlr to expand URL: %s", originalUrl), e);
            return Optional.empty();
        }
    }

    /**
     * @param originalUrl
     *      URL whose redirection will be resolved.
     * @return
     *      Returns URL to which {@code originalUrl} redirects to or same as input URL if there's no redirection.
     *      Returns empty if there's any error (e.g. bad input URL, not connected to internet, URL not reachable etc.).
     */
    @VisibleForTesting
    static Optional<String> getRedirectedUrlUsingDirectConnection(final String originalUrl) {
        InputStream inputStream = null;
        try {
            final HttpURLConnection conn = (HttpURLConnection) new URL(originalUrl).openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            conn.addRequestProperty("User-Agent", "Mozilla");
            conn.addRequestProperty("Referer", "google.com");
            conn.connect();
            inputStream = conn.getInputStream();

            final int responseCode = conn.getResponseCode();
            if (responseCode == HTTP_MOVED_PERM || responseCode == HTTP_MOVED_TEMP || responseCode == HTTP_SEE_OTHER) {
                final String redirectedUrl = conn.getHeaderField(HttpHeaders.LOCATION);
                logRedirection(originalUrl, redirectedUrl);
                return getRedirectedUrlUsingDirectConnection(redirectedUrl);
            } else if (responseCode == HTTP_OK) {
                final String redirectedUrl = conn.getURL().toString();
                logRedirection(originalUrl, redirectedUrl);
                return Optional.ofNullable(redirectedUrl);
            } else {
                return Optional.empty();
            }
        } catch (final MalformedURLException e) {
            log.error(String.format("Source URL '%s' malformed. Cannot redirect.", originalUrl), e);
            return Optional.empty();
        } catch (final IOException e) {
            log.error(String.format("Failed to connect to URL '%s'. Cannot find redirection details.", originalUrl), e);
            return Optional.empty();
        } finally {
            // Close input stream instead of calling disconnect on the connection itself
            Closeables.closeQuietly(inputStream);
        }
    }

    private static void logRedirection(final String originalUrl, final String redirectedUrl) {
        try {
            final URL original = new URL(originalUrl);
            final URL redirected = new URL(redirectedUrl);
            final boolean didProtocolChange = !original.getProtocol().equals(redirected.getProtocol());
            final boolean didHostChange = !original.getHost().equals(redirected.getHost());
            log.info(String.format("Redirection: %s ==> %s%s%s",
                    originalUrl,
                    redirectedUrl,
                    didProtocolChange ? " [PROTOCOL CHANGED]" : "",
                    didHostChange? " [HOST CHANGED]" : ""));
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException("Original/redirected URL was malformed", e);
        }
    }
}
