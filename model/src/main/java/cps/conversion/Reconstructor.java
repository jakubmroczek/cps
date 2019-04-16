package cps.conversion;

import cps.model.*;

import java.lang.Math;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.lang.Math.abs;
import static java.lang.Math.sin;

public class Reconstructor {

    public static Signal firstHoldInterpolation(final Signal signal, Duration samplingPeriodInNs) {
        //TODO: Allocatie proper number of memory to make it faster
        List<Double> samples = new ArrayList<>();

        Duration elapsedTime = Duration.ZERO;
        final Duration duration = signal.getDurationInNs();

        int previous = 0;

        while (elapsedTime.compareTo(duration) < 0) {
            //TODO: array out of bounds
            if (elapsedTime.compareTo(signal.getSamplingPeriod().multipliedBy((previous + 1))) > 0) {
                previous++;
            }

            double tangenns = (signal.getSamples().get(previous + 1) - signal.getSamples().get(previous)) / (signal.getSamplingPeriod().toNanos());
            double b = signal.getSamples().get(previous) - tangenns * signal.getSamplingPeriod().multipliedBy(previous).toNanos();

            double interpolatedValue = b * elapsedTime.toNanos() + b;

            samples.add(interpolatedValue);
            elapsedTime.plus(samplingPeriodInNs);
        }

        return new Signal(signal.getType(), signal.getDurationInNs(), samplingPeriodInNs, samples);
    }

    public Signal reconstruct(Signal signal, Duration reconstructionFrequency, int maxProbes) {
        Duration elapsedTime = Duration.ZERO;
        Duration duration = signal.getDurationInNs();

        //TODO: Allocatie proper number of memory to make it faster
        List<Double> samples = new ArrayList<>();

        while (elapsedTime.compareTo(duration) < 0) {
            double interpolatedValue = sincReconstruct(signal, elapsedTime, maxProbes);
            samples.add(interpolatedValue);
            elapsedTime.plus(reconstructionFrequency);
        }

        return new Signal(signal.getType(), signal.getDurationInNs(), reconstructionFrequency, samples);
    }

    private static double sincReconstruct(Signal signal, Duration x, int maxProbes) {
        assert !x.isNegative();

        double sum = 0.0;
        double ratio = (double) x.toNanos() / (double) signal.getSamplingPeriod().toNanos();

        for (int i =0; i < signal.getSamples().size(); i++) {
            sum += signal.getSamples().get(i) * sinc(ratio - i);
        }

        return sum;
    }

    private static double sinc(double x) {
        return abs(x) < ZERO ? 1.0 : sin(Math.PI * x) / (Math.PI * x);
    }

    private static double ZERO = 0.00000001;
}
