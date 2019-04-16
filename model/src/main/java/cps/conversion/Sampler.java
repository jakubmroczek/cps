package cps.conversion;

import cps.model.Signal;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class Sampler {

    //TODO: Similar responsibility as in Signal, unify it
    public static Signal sample(Function<Double, Double> function,
            Duration startTimeInNs,
            Duration endTimeInNs,
            Duration samplingPeriodInNs) {

        List<Double> samples = new ArrayList<>();
        Duration counter = startTimeInNs;

        while (counter.compareTo(endTimeInNs) <= 0) {
            double sample = function.apply((double)counter.toNanos());
            samples.add(sample);
            counter = counter.plus(samplingPeriodInNs);
        }

        return new Signal(Signal.Type.DISCRETE,  endTimeInNs.minus(startTimeInNs), samplingPeriodInNs, samples);
    }

    @Deprecated
    public static Signal sample(Signal signal, Duration newSamplingValue) {

        double samplingFrequency = newSamplingValue.toNanos();
        List<Double> values = signal.getSamples();
        double probingFrequency = signal.getSamplingPeriod().toNanos();

        if (samplingFrequency % probingFrequency > 0.000001) {
            throw new IllegalArgumentException("Sampling frequency of base signal should be multiplication of probing frequency");
        }
        int k = (int) (samplingFrequency / probingFrequency);
        List<Double> newValues = new ArrayList<>();
        int length = (int) ((values.size() * 1.0) * probingFrequency / samplingFrequency) + 1;
        for (int i = 0; i < length; i++) {
            newValues.add(values.get(i*k));
        }

        return new Signal(Signal.Type.DISCRETE, signal.getDurationInNs(), newSamplingValue, newValues);
    }
}
