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

    public static Signal reconstruct(Signal signal, Duration newSamplingPeriod, int maxProbes) {
        double startTime = 0.0;
        double endTime = signal.getDurationInNs().toNanos();
        double oldTimeStep = signal.getSamplingPeriod().toNanos();
        double newTimeStep = newSamplingPeriod.toNanos();
        int n = (int) Math.ceil((endTime - startTime) / newTimeStep);
        List<Double> oldValues = signal.getSamples();
        List<Double> newValues = new ArrayList<>();
        double time = 0.0;
        maxProbes = (int) (Math.min(maxProbes, (endTime - startTime) * (1.0 / oldTimeStep)));
        for (int i = 0; i < n; i++) {
            double sum = 0.0;
            int k = (int) (Math.max((time / oldTimeStep) - maxProbes, startTime / oldTimeStep));
            k = (int) (Math.max(k, (time / oldTimeStep) - maxProbes / 2));
            k = (int) (Math.min(k, (endTime / oldTimeStep) - maxProbes));
            int maxk = k + maxProbes;
            while (k < maxk) {
                sum += oldValues.get(k) * sinc(time / oldTimeStep - k);
                k++;
            }
            newValues.add(sum);
            time += newTimeStep;
        }

        return new Signal(signal.getType(), signal.getDurationInNs(), newSamplingPeriod, newValues);
    }

    private static double sinc(double x) {
        return abs(x) < ZERO ? 1.0 : sin(Math.PI * x) / (Math.PI * x);
    }

    private static double ZERO = 0.00000001;
}
