package cps.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        //Code duplication
        long startInMs = start.toMillis();
        long endInMs = end.toMillis();
        long duration = endInMs - startInMs;
        Function<Double, Double> adapter = timeInMs -> {
            //TODO: Maybe we should take floor
            Duration time = Duration.ofMillis(timeInMs.longValue());
            return java.lang.Math.abs(signal.calculate(time));
        };
        double integral = integrate(startInMs, endInMs, INTEGRATION_STEPS, adapter);
        return integral / duration;
    }

    public static double averagePower(Signal signal, Duration start, Duration end) {
        validate(start, end);
        long startInMs = start.toMillis();
        long endInMs = end.toMillis();
        long duration = endInMs - startInMs;
        Function<Double, Double> adapter = timeInMs -> {
            //TODO: Maybe we should take floor
            Duration time = Duration.ofMillis(timeInMs.longValue());
            return java.lang.Math.pow(signal.calculate(time),2);
        };
        double integral = integrate(startInMs, endInMs, INTEGRATION_STEPS, adapter);
        return integral / duration;
    }

    public static double variance(Signal signal, Duration start, Duration end) {
        validate(start, end);
        long startInMs = start.toMillis();
        long endInMs = end.toMillis();
        long duration = endInMs - startInMs;
        Function<Double, Double> adapter = timeInMs -> {
            //TODO: Maybe we should take floor
            Duration time = Duration.ofMillis(timeInMs.longValue());
            return java.lang.Math.pow(signal.calculate(time) - Math.averageValue(signal,start,end),2);
        };
        double integral = integrate(startInMs, endInMs, INTEGRATION_STEPS, adapter);
        return integral / duration;
    }

    public static double effectivePower(Signal signal, Duration start, Duration end) {
        validate(start, end);
        return java.lang.Math.sqrt(averagePower(signal,start,end));
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

    public static Double round(Double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}