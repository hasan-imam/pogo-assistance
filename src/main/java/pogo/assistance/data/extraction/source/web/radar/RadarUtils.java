package pogo.assistance.data.extraction.source.web.radar;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.classic.methods.RequestBuilder;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieOrigin;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.cookie.MalformedCookieException;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.cookie.BasicPathHandler;
import org.apache.hc.client5.http.impl.cookie.BasicSecureHandler;
import org.apache.hc.client5.http.impl.cookie.LaxExpiresHandler;
import org.apache.hc.client5.http.impl.cookie.LaxMaxAgeHandler;
import org.apache.hc.client5.http.impl.cookie.RFC6265CookieSpec;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import pogo.assistance.data.model.Region;
import pogo.assistance.data.model.Weather;

@Slf4j
@UtilityClass
class RadarUtils {

    public static final Map<Integer, Weather> WEATHER_ID_MAPPING = ImmutableMap.<Integer, Weather>builder()
            // TODO: verify all weather types
            // TODO: setup some automatic test to catch issues with this mapping changing
            .put(1, Weather.SUNNY) // or clear - checked
            .build();

    public static final Map<Region, URI> BASE_URLS_OF_SOURCES = ImmutableMap.of(
            Region.CL, URI.create("http://radarpokemon.cl"),
            Region.PN, URI.create("http://extonpokemap.com")); // TODO fix PN

    private static final Set<String> NECESSARY_COOKIES = ImmutableSet.of("SESSION-TOKEN", "CSRF-TOKEN");

    public static Optional<String> executeQuery(
            final Region region,
            final CookieStore cookieStore,
            final ClassicHttpRequest httpGetRequest,
            final CloseableHttpClient closeableHttpClient) {
        final URI requestUri;
        try {
            requestUri = httpGetRequest.getUri();
        } catch (final URISyntaxException e) {
            log.error("Failed to get URI from request: {}", httpGetRequest);
            return Optional.empty();
        }

        // Set cookies, update if necessary
        final HttpClientContext localContext = HttpClientContext.create();
        refreshCookies(cookieStore, closeableHttpClient, region);
        localContext.setCookieStore(cookieStore);

        // Print request URI, instead of the request itself, for all error logs. Printing request leaves out the base URL.
        try {
            final CloseableHttpResponse response = closeableHttpClient.execute(httpGetRequest, localContext);
            if (response.getCode() == HttpStatus.SC_SUCCESS) {
                // Update cookies from response - most likely unnecessary
                parseCookiesFromSetCookieHeaders(BASE_URLS_OF_SOURCES.get(region), response.getHeaders("Set-Cookie"))
                        .forEach(cookieStore::addCookie);
                return Optional.ofNullable(EntityUtils.toString(response.getEntity(), UTF_8))
                        .filter(s -> !s.isEmpty());
            } else {
                log.error("Query unsuccessful..." + System.lineSeparator()
                        + "Request URI: " + requestUri + System.lineSeparator()
                        + "Response: " + response);
            }
        } catch (final IOException e) {
            if (e instanceof HttpResponseException && ((HttpResponseException) e).getStatusCode() == HttpStatus.SC_FORBIDDEN) {
                log.error(String.format("Your access to %s has been blocked.", requestUri), e);
            } else {
                log.error(String.format("Failed to execute HTTP request on URI: %s", requestUri), e);
            }
        } catch (final ParseException e) {
            log.error(String.format("Failed to parse response. Request URI: %s", requestUri), e);
        }

        return Optional.empty();
    }

    public void refreshCookies(final CookieStore cookieStore, final CloseableHttpClient closeableHttpClient, final Region region) {
        cookieStore.clearExpired(Date.from(Instant.now()));
        if (!hasNecessaryCookies(cookieStore)) {
            final List<Cookie> fetchedCookies = getRequiredCookies(closeableHttpClient, region);
            Verify.verify(hasNecessaryCookies(fetchedCookies));
            fetchedCookies.forEach(cookieStore::addCookie);
        }
    }

    private boolean hasNecessaryCookies(final CookieStore cookieStore) {
        return hasNecessaryCookies(cookieStore.getCookies());
    }

    private boolean hasNecessaryCookies(final List<Cookie> cookies) {
        final Set<String> existingCookies = cookies.stream().map(Cookie::getName).collect(Collectors.toSet());
        return existingCookies.containsAll(NECESSARY_COOKIES);
    }

    /**
     * @return
     *      Makes a get request to the website's landing page, parses the cookies set and returns them. Resetting these cookies in later request is necessary
     *      for those requests to work. See {@link #NECESSARY_COOKIES} to see specifically which cookies are of particular interest.
     */
    private static List<Cookie> getRequiredCookies(final CloseableHttpClient closeableHttpClient, final Region region) {
        final String baseUrl = RadarUtils.BASE_URLS_OF_SOURCES.get(region).toString();
        final ClassicHttpRequest httpGetRequest = RequestBuilder.get(baseUrl)
                .setHeader(HttpHeaders.CONNECTION, "keep-alive")
                .setHeader(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.9")
                .build();
        try {
            final CloseableHttpResponse response = closeableHttpClient.execute(httpGetRequest);
            if (response.getCode() == HttpStatus.SC_SUCCESS) {
                return parseCookiesFromSetCookieHeaders(BASE_URLS_OF_SOURCES.get(region), response.getHeaders("Set-Cookie"));
                //                return Stream.of(response.getHeaders("Set-Cookie"))
                //                        .map(NameValuePair::getValue)
                //                        .map(headerValue -> headerValue.split(";")[0].split("="))
                //                        .map(pair -> {
                //                            final BasicClientCookie cookie = new BasicClientCookie(pair[0], pair[1]);
                //                            cookie.setDomain(BASE_URLS_OF_SOURCES.get(region).getHost());
                //                            cookie.setPath("/");
                //                            return cookie;
                //                        }).collect(Collectors.toList());
            } else {
                log.error("Failed to do initial get request to the server: {}. Error response: {}", baseUrl, response);
            }
        } catch (final IOException e) {
            log.error("Failed to do initial get request to the server: " + baseUrl, e);
        }
        return Collections.emptyList();
    }

    private static List<Cookie> parseCookiesFromSetCookieHeaders(final URI uri, final Header[] cookieHeaders) {
        final CookieOrigin cookieOrigin = new CookieOrigin(uri.getHost(), uri.getScheme().equalsIgnoreCase("https") ? 443 : 80, uri.getPath(), false);
        return Stream.of(cookieHeaders)
                .map(header -> LaxCookieSpec.parseLaxed(header, cookieOrigin))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Exists to parse this server's cookies in more lenient way, specifically the 'domain' attribute, which is always empty.
     * Looks like this beta version of Apache HTTP client v5 doesn't allow lenient parsing out of box. We can replace this if we figure out a better
     * alternative.
     */
    private static class LaxCookieSpec extends RFC6265CookieSpec {
        private static LaxCookieSpec INSTANCE = new LaxCookieSpec();

        private LaxCookieSpec() {
            super(new BasicPathHandler(),
                    // no domain handler - this is the relaxed part
                    new LaxMaxAgeHandler(),
                    new BasicSecureHandler(),
                    new LaxExpiresHandler());
        }

        public static List<Cookie> parseLaxed(final Header header, final CookieOrigin cookieOrigin) {
            try {
                return INSTANCE.parse(header, cookieOrigin);
            } catch (final MalformedCookieException e) {
                throw new RuntimeException("Failed to parse cookie header", e);
            }
        }
    }

}
