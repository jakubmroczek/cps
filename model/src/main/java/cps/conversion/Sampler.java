package cps.conversion;

import cps.model.Signal;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Sampler {
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
