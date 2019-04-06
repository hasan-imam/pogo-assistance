package pogo.assistance.ui;

import java.time.Duration;

public class RenderingUtils {

    public static String toString(final Duration duration) {
        return duration.toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
    }

}
