package cps.conversion;
import cps.model.*;
import sun.awt.image.IntegerComponentRaster;

import java.lang.Math;
import java.util.stream.IntStream;

import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.lang.Math.pow;

public class Error {

    public static double mse(final Signal lhs, final Signal rhs) {
        assert lhs.getSamplingPeriodNs().equals(rhs.getSamplingPeriodNs());
        int size = min(lhs.getSamples().size(), rhs.getSamples().size());
        double sum = IntStream.range(0, size).mapToDouble(i -> pow(lhs.getSamples().get(i) - rhs.getSamples().get(i), 2)).sum();
        return sum / (size * 1.0);
    }

    public static double snr(final Signal lhs, final Signal rhs) {
        assert lhs.getSamplingPeriodNs().equals(rhs.getSamplingPeriodNs());

        int size = min(lhs.getSamples().size(), rhs.getSamples().size());

        double squaresSum = 0.0;
        double noiseSquaresSum = 0.0;

        for (int i = 0; i < size; i++) {
            squaresSum += lhs.getSamples().get(i) * rhs.getSamples().get(i);
            noiseSquaresSum += Math.pow(Math.abs(lhs.getSamples().get(i) - rhs.getSamples().get(i)), 2.0);
        }
        return 10.0 * Math.log10(squaresSum / noiseSquaresSum);
    }

    public double psnr(final Signal lhs, final Signal rhs) {
        assert lhs.getSamplingPeriodNs().equals(rhs.getSamplingPeriodNs());

        double maxLhs = lhs.getSamples().stream().max(Double::compareTo).orElseGet(() ->lhs.getSamples().get(0));
        return 10.0 * Math.log10(maxLhs / mse(lhs, rhs));
    }

    public double md(final Signal lhs, final Signal rhs) {
        assert lhs.getSamplingPeriodNs().equals(rhs.getSamplingPeriodNs());

        int size = min(lhs.getSamples().size(), rhs.getSamples().size());
        return IntStream.range(0, size).mapToObj(i -> abs(lhs.getSamples().get(i) - rhs.getSamples().get(i))).max(Double::compareTo).orElseGet(() -> abs(lhs.getSamples().get(0) - rhs.getSamples().get(0)));
    }

}
