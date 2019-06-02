package pogo.assistance.data.extraction.source.discord.sgv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import com.google.common.base.Verify;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class SGVMessageProcessorUtils {

    /**
     * Example map URLs:
     *  - https://ver.sx/u/4fq
     */
    private static final Pattern SGV_REDIRECT_URL = Pattern.compile("(?<versxurl>http[s]?://ver\\.sx[/\\w\\d]+)");

    public static String getGoogleMapUrl(final String compiledMessage) {
        final Matcher matcher = SGV_REDIRECT_URL.matcher(compiledMessage);
        Verify.verify(matcher.find());
        final String versxurl = matcher.group("versxurl");
        return getRedirectedUrlUsingUnfurlr(versxurl)
                .orElseGet(() -> getRedirectedUrlUsingCurl(versxurl)
                        .orElseThrow(() -> new IllegalArgumentException("Failed to expand versx URL to Google map URL")));
    }

    private static Optional<String> getRedirectedUrlUsingCurl(@NonNull final String originalUrl) {
        final String command = "curl -Ls -o /dev/null -w %{url_effective} " + originalUrl;
        try {
            final Process process = Runtime.getRuntime().exec(command);
            try (final InputStream inputStream = process.getInputStream()) {
                final String redirectedUrl = new BufferedReader(new InputStreamReader(inputStream)).readLine();
                log.error(String.format("%s ---redirected---> %s", originalUrl, redirectedUrl));
                return Optional.of(redirectedUrl);
            }
        } catch (final IOException e) {
            log.error(String.format("Failed to find final redirect URL of this source URL: %s", originalUrl));
            return Optional.empty();
        }
    }

    private static Optional<String> getRedirectedUrlUsingUnfurlr(final String originalUrl) {
        try {
            final Document document = Jsoup.connect("https://unfurlr.com")
                    .data("url", originalUrl)
                    .data("user_agent", "Random")
                    .post();
            return Optional.ofNullable(document.select("#content > div.results-content.clear > div > div.result.final-result > p"))
                    .map(Elements::html);
        } catch (final IOException e) {
            log.error(String.format("Failed to call Unfurlr to expand URL: %s", originalUrl));
            return Optional.empty();
        }
    }

}
