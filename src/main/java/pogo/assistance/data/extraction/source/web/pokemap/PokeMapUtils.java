package pogo.assistance.data.extraction.source.web.pokemap;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import pogo.assistance.data.model.Gym;
import pogo.assistance.data.model.Region;
import pogo.assistance.data.model.Weather;

@Slf4j
@UtilityClass
class PokeMapUtils {

    public static final Map<Integer, Gym.Team> TEAM_ID_MAPPING = ImmutableMap.of(
            0, Gym.Team.NONE,
            1, Gym.Team.MYSTIC,
            2, Gym.Team.VALOR,
            3, Gym.Team.INSTINCT);

    public static final Map<Integer, Weather> WEATHER_ID_MAPPING = ImmutableMap.<Integer, Weather>builder()
//            .put(0, Weather.SUNNY) // TODO: What is weather 0? Unknown? In JS/HTML it says 'none'
            // TODO: verify all weather types
            // TODO: setup some automatic test to catch issues with this mapping changing
            .put(1, Weather.SUNNY)
            .put(2, Weather.RAIN)
            .put(3, Weather.PARTLY_CLOUDY) // checked
            .put(4, Weather.CLOUDY)
            .put(5, Weather.WINDY)
            .put(6, Weather.SNOW)
            .put(7, Weather.FOG)
            .build();

    public static final Map<Region, URI> BASE_URLS_OF_SOURCES = ImmutableMap.of(
            Region.NYC, URI.create("https://nycpokemap.com"),
            Region.SG, URI.create("https://sgpokemap.com"),
            Region.YVR, URI.create("https://vanpokemap.com"),
            Region.SYD, URI.create("https://sydneypogomap.com"));

    public static Optional<String> executeQuery(final CloseableHttpClient closeableHttpClient, final HttpUriRequest httpGetRequest) {
        final URI requestUri = httpGetRequest.getURI();

        // Print request URI, instead of the request itself, for all error logs. Printing request leaves out the base URL.
        try {
            final CloseableHttpResponse response = closeableHttpClient.execute(httpGetRequest);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
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

    public Map<Long, Weather> parseWeatherConditions(final JsonArray weathers) {
        return StreamSupport.stream(weathers.spliterator(), false)
                .map(JsonElement::getAsJsonObject)
                .collect(Collectors.toMap(
                        weather -> weather.get("cell_id").getAsLong(),
                        weather -> WEATHER_ID_MAPPING.get(weather.get("weather").getAsInt())));
    }

}
