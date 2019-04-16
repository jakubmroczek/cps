package cps.conversion;

import cps.model.Signal;

import java.util.ArrayList;
import java.util.List;

public class Quantizer {
    public static Signal quantize(final Signal signal, final int bits) {

        List<Double> values = signal.getSamples();
        int levels = (int) (Math.pow(2, bits)) - 1;
        double minValue = values.get(0);
        for (double value : values) {
            if (value < minValue) {
                minValue = value;
            }
        }
        double maxValue = values.get(0);
        for (double value : values) {
            if (value > maxValue) {
                maxValue = value;
            }
        }
        double span = (maxValue - minValue) / levels;
        List<Double> newValues = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            newValues.add(minValue + Math.round((values.get(i) - minValue) / span) * span);
        }
        return new Signal(signal.getType(), signal.getDurationInNs(), signal.getSamplingPeriod(), newValues);
    }

}
