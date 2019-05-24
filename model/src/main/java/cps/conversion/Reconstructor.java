package cps.conversion;

import cps.model.*;

import java.lang.Math;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.*;

public class Reconstructor {

    public static Signal firstHoldInterpolation(final Signal signal, Duration samplingPeriodInNs) {
        assert samplingPeriodInNs.toNanos() % signal.getSamplingPeriod().toNanos() == 0;

        var time = Duration.ZERO;
        var samples = new ArrayList<Double>();

        if (signal.getSamples().size() > 1) {
            for (int i = 0, j = 1; j < signal.getSamples().size(); i++, j++) {
                samples.add(signal.getSamples().get(i));
                time =  signal.getSamplingPeriod().multipliedBy(i);
                final int numberOfPoints = (int) signal.getSamplingPeriod().dividedBy(samplingPeriodInNs) - 1;
                int index = 0;
                while (index++ < numberOfPoints) {
                    double y0 = signal.getSamples().get(i);
                    double y1 = signal.getSamples().get(j);

                    double interpolatedValue =
                    y0* (1 - (samplingPeriodInNs.toNanos() * index) / ((double) signal.getSamplingPeriod().toNanos()))
                    + y1 * ((samplingPeriodInNs.toNanos() * index) / ((double) signal.getSamplingPeriod().toNanos()));

                    samples.add(interpolatedValue);
                    time = time.plus(samplingPeriodInNs);
                }
            }
        }

        if (!signal.getSamples().isEmpty()) {
            int last = signal.getSamples().size() - 1;
            samples.add(signal.getSamples().get(last));
        }
        return new Signal(signal.getType(), signal.getDurationInNs(), samplingPeriodInNs, samples);
    }

    public static Signal reconstruct(Signal signal, Duration reconstructionFrequency, int maxProbes) {
        Duration elapsedTime = Duration.ZERO;
        Duration duration = signal.getDurationInNs();

        List<Double> samples = new ArrayList<>();

        while (elapsedTime.compareTo(duration) < 0) {
            double interpolatedValue = sincReconstruct(signal, elapsedTime, maxProbes);
            samples.add(interpolatedValue);
            elapsedTime = elapsedTime.plus(reconstructionFrequency);
        }

        return new Signal(signal.getType(), signal.getDurationInNs(), reconstructionFrequency, samples);
    }

    private static double sincReconstruct(Signal signal, Duration x, int maxProbes) {
        assert !x.isNegative();

        double sum = 0.0;

        double ratio = (double) x.toNanos() / (double) signal.getSamplingPeriod().toNanos();
        int leftIndex = (int) floor(ratio);
        int rightIndex = (int) ceil(ratio);

        System.out.println("Przed mapowaniem");

        System.out.println("L = " + leftIndex);

        // Shifting samples
        leftIndex -= (maxProbes - 1);
        leftIndex = max(leftIndex, 0);
//
//        System.out.println("R = " + rightIndex);
//
        rightIndex += (maxProbes - 1);
        rightIndex = min(rightIndex, signal.getSamples().size() - 1);

        System.out.println("Po mapowaniu");

        System.out.println("L = " + leftIndex + "; R = " + rightIndex + "; Point = " + x + "; maxProbes = " + maxProbes);
        for (int i = leftIndex; i <= rightIndex; i++) {
            sum += signal.getSamples().get(i) * sinc(ratio - i);
        }

        return sum;
    }

    private static double sinc(double x) {
        return abs(x) < ZERO ? 1.0 : sin(Math.PI * x) / (Math.PI * x);
    }

    private static double ZERO = 0.000000000000001;
}
