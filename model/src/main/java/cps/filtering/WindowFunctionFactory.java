package cps.filtering;

import static java.lang.Math.PI;
import static java.lang.Math.cos;

public class WindowFunctionFactory {

    public static final String RECTANGUIAR_WINDOW = "RECTANGULAR_WINDOW";
    public static final String HAMMING_WINDOW = "HAMMING_WINDOW";
    public static final String HANNING_WINDOW = "HANNING_WINDOW";
    public static final String BLACKMAN_WINDOW = "BLACKMAN_WINDOW";

    public static WindowFunction create(String windowFunction) {
        switch (windowFunction)
        {
            case RECTANGUIAR_WINDOW:
                return createRectangularWindow();

            case HAMMING_WINDOW:
                return createHammingWindow();

            case HANNING_WINDOW:
                return createHanningWindow();

            case BLACKMAN_WINDOW:
                return createBlackmanWindow();

            default:
                throw new IllegalArgumentException("Provided window function: " +
                        windowFunction + " does not exist");
        }

    }

    private static WindowFunction createRectangularWindow() {
        return (n, M) -> 1.0;
    }

    private static WindowFunction createHammingWindow() {
        return (n, M) -> 0.53836 - 0.46164 * cos( (2 * Math.PI * n) / M);
    }


    private static WindowFunction createHanningWindow() {
        return  (n, M) -> 0.5 - 0.5 * cos( (2 * Math.PI * n) / M);
    }

    private static WindowFunction createBlackmanWindow() {
        return  (n, M) -> 0.42 - 0.5 * cos ( (2 * PI * n) / M ) + 0.08 * cos((4 * PI * n) / M);
    }


}
