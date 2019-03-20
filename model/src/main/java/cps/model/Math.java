package cps.model;

import java.time.Duration;
import java.util.function.Function;

public class Math {

    private final static int INTEGRATION_STEPS = 100;

    //!!!! Measures everything in millisceonds
    private static void validate(Duration start, Duration end) {
        assert start.toMillis() != 0 && end.toMillis() != 0;
    }

    //TODO: Find proper english names
    public static double averageValue(Signal signal, Duration start, Duration end) {
        validate(start, end);
        long startInMs = start.toMillis();
        long endInMs = end.toMillis();
        long duration = endInMs - startInMs;
        Function<Double, Double> adapter = timeInMs -> {
            //TODO: Maybe we should take floor
            Duration time = Duration.ofMillis(timeInMs.longValue());
            return signal.calculate(time);
        };
        double integral = integrate(startInMs, endInMs, INTEGRATION_STEPS, adapter);
        return integral / duration;
    }

    public static double averageAbsoluteValue(Signal signal, Duration start, Duration end) {
        validate(start, end);
        throw new UnsupportedOperationException("not implemented yet.");
    }

    public static double averagePower(Signal signal, Duration start, Duration end) {
        validate(start, end);
        throw new UnsupportedOperationException("not implemented yet.");
    }

    public static double variance(Signal signal, Duration start, Duration end) {
        validate(start, end);
        throw new UnsupportedOperationException("not implemented yet.");
    }

    public static double effectivePower(Signal signal, Duration start, Duration end) {
        validate(start, end);
        throw new UnsupportedOperationException("not implemented yet.");
    }

    private static double integrate(double a, double b, int N, Function<Double, Double> function) {
        double h = (b - a) / N;
        double sum = 0.5 * (function.apply(a) + function.apply(b));
        for (int i = 1; i < N; i++) {
            double x = a + h * i;
            sum += function.apply(x);
        }
        return sum * h;
    }
}