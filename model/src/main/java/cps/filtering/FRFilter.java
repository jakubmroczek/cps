package cps.filtering;

import lombok.Builder;
import sun.misc.Signal;

import java.util.List;

public abstract class FRFilter {

    private int M;
    private double frequency;
    private FilterFunction filterFunction;
    private WindowFunction windowFunction;

    public List<Signal> filter() {
        return null;
    }

    //TODO: Jakie konkretne argumenty
    protected abstract double modulate(double sample);

    protected abstract double getK(final double sampleFrequency, final double frequency);
}
