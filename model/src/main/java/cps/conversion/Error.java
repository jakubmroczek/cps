package cps.conversion;
import cps.model.*;

import java.lang.Math;
import java.util.stream.IntStream;

import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.lang.Math.pow;

public class Error {

    public static double mse(final Signal<Double> lhs, final Signal<Double> rhs) {
        assert lhs.getSamplingPeriod().equals(rhs.getSamplingPeriod());
        int size = min(lhs.getSamples().size(), rhs.getSamples().size());
        double sum = IntStream.range(0, size).mapToDouble(i -> pow(lhs.getSamples().get(i) - rhs.getSamples().get(i), 2)).sum();
        return sum / (size * 1.0);
    }

    public static double snr(final Signal<Double> lhs, final Signal<Double> rhs) {
        assert lhs.getSamplingPeriod().equals(rhs.getSamplingPeriod());

        int size = min(lhs.getSamples().size(), rhs.getSamples().size());

        double squaresSum = 0.0;
        double noiseSquaresSum = 0.0;

        for (int i = 0; i < size; i++) {
            squaresSum += lhs.getSamples().get(i) * lhs.getSamples().get(i);
            noiseSquaresSum += Math.pow(lhs.getSamples().get(i) - rhs.getSamples().get(i), 2.0);
        }
        return 10.0 * Math.log10(squaresSum / noiseSquaresSum);
    }

    public static double psnr(final Signal<Double> lhs, final Signal<Double> rhs) {
        assert lhs.getSamplingPeriod().equals(rhs.getSamplingPeriod());

        double maxLhs = lhs.getSamples().stream().max(Double::compareTo).orElseGet(() ->lhs.getSamples().get(0));
        return 10.0 * Math.log10(maxLhs / mse(lhs, rhs));
    }

    public static double md(final Signal<Double> lhs, final Signal<Double> rhs) {
        assert lhs.getSamplingPeriod().equals(rhs.getSamplingPeriod());

        int size = min(lhs.getSamples().size(), rhs.getSamples().size());
        return IntStream.range(0, size).mapToObj(i -> abs(lhs.getSamples().get(i) - rhs.getSamples().get(i))).max(Double::compareTo).orElseGet(() -> abs(lhs.getSamples().get(0) - rhs.getSamples().get(0)));
    }

    public static double enob(final Signal lhs, final Signal rhs) {
        return (snr(lhs, rhs) - 1.76) / 6.02;
    }

}
