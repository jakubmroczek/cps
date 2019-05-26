package cps.filtering;

import cps.model.*;
import cps.util.Conversions;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

import static cps.util.Conversions.toFrequency;
import static java.lang.Math.sin;

public abstract class FRFilter {

    private int M;
    private double frequency;
    private WindowFunction windowFunction;

    public List<Signal> filter(final Signal signal) {
        double signalSamplingFrequency = toFrequency(signal.getSamplingPeriod());
        final int K = getK(signalSamplingFrequency, frequency);

        List<Double> newValues = new ArrayList<>();
        for (int i = 0; i < M; i++) {
            double newSample = 0.0;

            if (i == (M - 1) / 2) {
                newSample = 2.0 / K;
            } else {
                newSample = sin((2.0 * Math.PI * (i - (M - 1) / 2)) / K) / (Math.PI * (i - (M - 1) / 2));
            }
            newSample *= windowFunction.apply(i, M);
            newSample *= modulate(i - (M - 1) / 2);

            newValues.add(newSample);
        }

        Signal filterH = new Signal(Signal.Type.DISCRETE, signal.getSamplingPeriod().dividedBy(M), signal.getSamplingPeriod(),  newValues)
        Signal convolution = Filters.convolute(filterH, signal);

        List<Double> newConvolutionValues = new ArrayList<>();

        var size = convolution.getSamples().size() - M + 1;

        for (int i = 0; i < size; i++) {
            newConvolutionValues.add(convolution.getSamples().get(i + (M - 1) / 2);
        }

        ArrayList<Signal> res = new ArrayList<>();
        res.add(filterH);

        var duration  = signal.getDurationInNs().minus(convolution.getSamplingPeriod().multipliedBy(M - 1));
        var filteredSignal = new Signal(signal.getType(), duration, signal.getSamplingPeriod(), newConvolutionValues);

        res.add(filteredSignal);

        return res;
    }

    protected abstract double modulate(int index);

    protected abstract int getK(final double sampleFrequency, final double frequency);
}
