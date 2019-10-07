package pogo.assistance.data.extraction.source.discord.sgv;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Verify;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import pogo.assistance.data.extraction.source.discord.LocationLinkParsingUtils;

@Slf4j
@UtilityClass
public class SGVMessageProcessorUtils {

    /**
     * Example map URLs:
     *  - https://ver.sx/u/4fq
     */
    private static final Pattern SGV_REDIRECT_URL = Pattern.compile("(?<versxurl>http[s]?://ver\\.sx[/\\w\\d]+)");

    public static String getGoogleMapUrl(final String compiledMessage) {
        // First try to extract a google map URL
        final Matcher googleMapUrlMatcher = LocationLinkParsingUtils.GOOGLE_MAP_URL.matcher(compiledMessage);
        if (googleMapUrlMatcher.find()) {
            return googleMapUrlMatcher.group();
        }

        // Since the message doesn't have a direct map URL, try to extract a versx server URL and resolve google map URL from that
        final Matcher matcher = SGV_REDIRECT_URL.matcher(compiledMessage);
        Verify.verify(matcher.find());
        final String versxurl = matcher.group("versxurl");
        return LocationLinkParsingUtils.getRedirectedUrlUsingUnfurlr(versxurl)
                .orElseGet(() -> LocationLinkParsingUtils.getRedirectedUrlUsingCurl(versxurl)
                        .orElseThrow(() -> new IllegalArgumentException("Failed to expand versx URL to Google map URL")));
    }

}
