package cps.filtering;

public interface FilterFunction {

    double apply(int n);

    double getK(final double samplingFrequency, final double frequency);
}
