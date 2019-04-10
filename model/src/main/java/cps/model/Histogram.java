package cps.model;

import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Math.copySign;
import static java.lang.Math.floor;
import static java.lang.Math.min;

@Getter
public class Histogram {
    private final int bins;
    private final double min, max;
    private final List<Double> frequencyList;

    public Histogram(Signal signal, int bins) {
        this.bins = bins;

        List<Double> samples = signal.getSamples();

        //TODO: Efficiency lost, two loops, maybe chart should know it min and max value?
        // TODO: NO SUCH ELEMENT EXCEPTION (WHEN SAMPLES IS EMPTY)
        min = Collections.min(samples);
        max = Collections.max(samples);

        frequencyList = new ArrayList<>();

        // List initialization
        for (int i = 0; i < bins; i++) {
            frequencyList.add(0.0);
        }

        for (double value : samples) {
            double percentage = (value - min) / (max - min);
            int index = (int) floor(percentage * bins);
            index = min(index, bins - 1);
            double freq = frequencyList.get(index);
            frequencyList.set(index, freq + 1);
        }
    }
}
