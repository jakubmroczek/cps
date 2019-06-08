package cps.filtering;

import cps.model.*;
import cps.util.Conversions;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

import static cps.util.Conversions.toFrequency;
import static java.lang.Math.sin;

public abstract class FIRFilter {

    // TODO: The last three arguments are not elegant, maybe a DTO should be introduced
    public List<Signal<Double>> filter(final Signal<Double> signal, final int M, final double frequency, final WindowFunction windowFunction) {
        Signal<Double> filterImpulseResponse = createFilterImpulseResponse(signal, M, frequency, windowFunction);
        Signal<Double> filteredSignal = filter(signal, filterImpulseResponse, M);

        ArrayList<Signal<Double>> signals = new ArrayList<>();
        signals.add(filterImpulseResponse);

        signals.add(filteredSignal);

        return signals;
    }

    private Signal<Double> createFilterImpulseResponse(final Signal<Double> signal, final int M, final double frequency, final WindowFunction windowFunction) {
        double signalSamplingFrequency = toFrequency(signal.getSamplingPeriod());
        var K = getK(signalSamplingFrequency, frequency);

        List<Double> newValues = new ArrayList<>();
        for (int i = 0; i < M; i++) {
            double newSample;

            if (i == (M - 1) / 2) {
                newSample = 2.0 / K;
            } else {
                newSample = sin((2.0 * Math.PI * (i - (M - 1) / 2)) / K) / (Math.PI * (i - (M - 1) / 2));
            }
            newSample *= windowFunction.apply(i, M);
            var multiplicant = modulate(i - (M - 1) / 2);
            System.out.println(multiplicant);
             newSample *= multiplicant;


            newValues.add(newSample);
        }

        return new Signal<>(Signal.Type.DISCRETE, signal.getSamplingPeriod().multipliedBy(M), signal.getSamplingPeriod(),  newValues);
    }

    private Signal<Double> filter(final Signal<Double> signal, final Signal<Double> filterImpulseResponse, final int M) {
        Signal<Double> convolution = Filters.convolute(filterImpulseResponse, signal);


        List<Double> newConvolutionValues = new ArrayList<>();

        var size = convolution.getSamples().size() - M + 1;

        for (int i = 0; i < size; i++) {
            newConvolutionValues.add(convolution.getSamples().get(i + (M - 1) / 2));
        }

        var duration  = convolution.getDurationInNs().minus(convolution.getSamplingPeriod().multipliedBy(M - 1));
        return new Signal<>(signal.getType(), duration, signal.getSamplingPeriod(), newConvolutionValues);
    }

    protected abstract double modulate(int index);

    protected abstract double getK(final double sampleFrequency, final double frequency);
}
