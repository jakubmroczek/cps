package cps.conversion;

import cps.model.*;

import java.lang.Math;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.Math.abs;
import static java.lang.Math.sin;

public class Reconstructor {

    public static Signal firstHoldInterpolation(final Signal signal, Duration samplingPeriodInNs) {
        //TODO: Allocatie proper number of memory to make it faster
        List<Double> samples = new ArrayList<>();
        List<Double> segmentInterpolation = new ArrayList<>();

        Duration elapsedTime = Duration.ZERO;
        final Duration duration = signal.getDurationInNs();

        int previous = 0;

        while (elapsedTime.compareTo(duration) < 0) {
            //TODO: array out of bounds
            if (elapsedTime.compareTo(signal.getSamplingPeriod().multipliedBy((previous + 1))) > 0) {
                samples.addAll(segmentInterpolation);
                segmentInterpolation.clear();
                previous++;
            }

            double a = (signal.getSamples().get(previous + 1) - signal.getSamples().get(previous)) / (signal.getSamplingPeriod().toNanos());
            double b = signal.getSamples().get(previous) - a * signal.getSamplingPeriod().multipliedBy(previous).toNanos();

            double interpolatedValue = a * elapsedTime.toNanos() + b;

            segmentInterpolation.add(interpolatedValue);
            elapsedTime = elapsedTime.plus(samplingPeriodInNs);
        }

        samples.add(signal.getSamples().get(signal.getSamples().size() - 1) );

        return new Signal(signal.getType(), signal.getDurationInNs(), samplingPeriodInNs, samples);
    }

    public static Signal reconstruct(Signal signal, Duration reconstructionFrequency, int maxProbes) {
        Duration elapsedTime = Duration.ZERO;
        Duration duration = signal.getDurationInNs();

        //TODO: Allocatie proper number of memory to make it faster
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

        for (int i =0; i < signal.getSamples().size(); i++) {
            sum += signal.getSamples().get(i) * sinc(ratio - i);
        }

        return sum;
    }


    public static Signal interpolate(Signal signal, Duration frequency) {
        double startTime = 0;
        double endTime = signal.getDurationInNs().toNanos();
        double oldTimeStep = signal.getSamplingPeriod().toNanos();
        double newTimeStep = frequency.toNanos();
        int n = (int) Math.ceil((endTime - startTime) / newTimeStep);
        List<Double> oldValues = signal.getSamples();
        List<Double> newValues = new ArrayList<>();
        double time = 0.0;
        for (int i = 0; i < n; i++) {
            int j = (int) (time / oldTimeStep);
            if (j == oldValues.size() - 1) {
                --j;
            }
            double a = (oldValues.get(j + 1) - oldValues.get(j)) / oldTimeStep;
            double b = oldValues.get(j + 1) - a * ((j + 1) * 1.0 * oldTimeStep);
            newValues.add(a * time + b);
            time += newTimeStep;
        }

        return new Signal(signal.getType(), signal.getDurationInNs(), frequency, newValues);
    }

    private static double sinc(double x) {
        return abs(x) < ZERO ? 1.0 : sin(Math.PI * x) / (Math.PI * x);
    }

    private static double ZERO = 0.00000001;
}
