package cps.model;

import lombok.Getter;

import java.time.Duration;
import java.util.List;

/**
 * This class contains extra information about occurrences of each sample
 */
public class InterpolatedSignal extends Signal<Double> {

//    @Getter
    private List<Duration> sampleTimePoints;

    public InterpolatedSignal(Signal signal, List<Duration> sampleTimePoints) {
        super(signal.getType(), signal.getDurationInNs(), signal.getSamplingPeriod(), signal.getSamples());
        this.sampleTimePoints = sampleTimePoints;
    }
}
