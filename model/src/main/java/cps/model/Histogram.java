package cps.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.DoubleStream;

import static java.lang.Math.*;

@Data
public class Histogram {
    // Liczba przedziałów
    private final int bins;
    private final double min, max;
    private final List<Double> frequencyList;

    public Histogram(SignalChart chart, int bins) {
        this.bins = bins;

        //TODO: Efficiency lost, two loops, maybe chart should know it min and max value?
        //TODO: Change name from probes to samples
        List<Double> samples = chart.getProbes();

        //  TODO: NO SUCH ELEMENT EXCEPTION (WHEN SAMPLES IS EMPTY)
        min = Collections.min(samples);
        max = Collections.max(samples);

        frequencyList = new ArrayList<>();
        final double columnLength = (max - min) / bins;

        // List initialization
        for (int i = 0; i < bins; i++) {
            frequencyList.add(0.0);
        }

        for (double value : samples) {
            //Shows in which percent of the histogram length the sample is
            //TODO
            double percentage = (value - min) / (max - min);
            int index = (int) floor(percentage * bins);
            // In case when index is equal number of bins, (it can exceed the list size)
            index = min(index, bins - 1);
            double freq = frequencyList.get(index);
            frequencyList.set(index, freq + 1);
        }
    }

    private List<Double> countFrequencies(List<Double> samples) {
        final double columnLength = (max - min) / bins;

        //Nullptr exception
        final List<Double> results = new ArrayList<>(bins);
        // List initialization
        for (int i = 0; i < results.size(); i++) {
            results.set(i, new Double(0));
        }

        for (double value : samples) {
            //Shows in which percent of the histogram length the sample is
            double percentage = (value - min) / max;
            int index = (int) floor(percentage * bins);
            // In case when index is equal number of bins, (it can exceed the list size)
            index = min(index, bins - 1);
            double freq = results.get(index);
            results.add(index, freq + 1);
        }

        return results;
    }
}
