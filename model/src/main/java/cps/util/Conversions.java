package cps.util;

import java.time.Duration;

public class Conversions {

    private final static int NANOSECONDS_TO_SECONDS = 1_000_000_000;

    public static double toFrequency(Duration period) {
        var periodInNano = period.getNano() + NANOSECONDS_TO_SECONDS * period.getSeconds();
        return NANOSECONDS_TO_SECONDS * (1.0 / periodInNano);
    }
}
