package cps.conversion;

import cps.model.Signal;

import java.util.ArrayList;
import java.util.List;

public class Quantizer {
    public static Signal<Double> quantize(final Signal<Double> signal, final int bits) {

        List<Double> values = signal.getSamples();
        int levels = (int) (Math.pow(2, bits)) - 1;

        double minValue = signal.getSamples().stream().min(Double::compareTo).orElse(signal.getSamples().get(0));
        double maxValue = signal.getSamples().stream().max(Double::compareTo).orElse(signal.getSamples().get(0));

        double span = (maxValue - minValue) / levels;
        List<Double> newValues = new ArrayList<>();
        for (Double value : values) {
            newValues.add(minValue + Math.round((value - minValue) / span) * span);
        }
        return new Signal<Double>(signal.getType(), signal.getDurationInNs(), signal.getSamplingPeriod(), newValues);
    }

}
