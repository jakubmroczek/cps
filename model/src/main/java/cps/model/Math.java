package cps.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;

import static java.lang.Math.*;

public class Math {

    private final static int INTEGRATION_STEPS = 100;

    public static double averageValue(Function<Double, Double> function, double startInNs, double endInNs) {
        assert endInNs != startInNs;
        double duration = endInNs - startInNs;
        double integral = integrate(startInNs, endInNs, INTEGRATION_STEPS, function);
        return integral / duration;
    }

    public static double averageValue(Signal<Double> signal) {
        return signal.getSamples().stream().mapToDouble(Double::doubleValue).sum() / (double) signal.getSamples().size();
    }

    public static double averageAbsoluteValue(Function<Double, Double> function, double startInNs, double endInNs) {
        assert endInNs != startInNs;
        double duration = endInNs - startInNs;
        double integral = integrate(startInNs, endInNs, INTEGRATION_STEPS, x -> abs(function.apply(x)));
        return integral / duration;
    }

    public static double averageAbsoluteValue(Signal<Double> signal) {
        return signal.getSamples().stream().mapToDouble(Double::doubleValue).map(java.lang.Math::abs).sum() /
                (double) signal.getSamples().size();
    }

    public static double averagePower(Function<Double, Double> function, double startInNs, double endInNs) {
        assert endInNs != startInNs;
        double duration = endInNs - startInNs;
        double integral = integrate(startInNs, endInNs, INTEGRATION_STEPS, x -> pow(function.apply(x), 2));
        return integral / duration;
    }

    public static double averagePower(Signal<Double> signal) {
        return signal.getSamples().stream().mapToDouble(Double::doubleValue).map(i -> pow(i, 2)).sum() /
                (double) signal.getSamples().size();
    }

    public static double variance(Function<Double, Double> function, double startInNs, double endInNs) {
        assert endInNs != startInNs;
        double duration = endInNs - startInNs;
        double average = averageValue(function, startInNs, endInNs);
        double integral = integrate(startInNs, endInNs, INTEGRATION_STEPS, x -> pow(function.apply(x) - average, 2));
        return integral / duration;
    }

    public static double variance(Signal<Double> signal) {
        double average = averageValue(signal);
        return signal.getSamples().stream().mapToDouble(Double::doubleValue).map(i -> pow(i - average, 2)).sum() /
                (double) signal.getSamples().size();
    }

    public static double effectivePower(Function<Double, Double> function, double startInNs, double endInNs) {
        assert startInNs != endInNs;
        return sqrt(averagePower(function, startInNs, endInNs));
    }

    public static double effectivePower(Signal<Double> signal) {
        return sqrt(averagePower(signal));
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
        if (places < 0)
            throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
