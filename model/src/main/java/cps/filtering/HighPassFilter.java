package cps.filtering;

public class HighPassFilter extends FIRFilter {
    @Override
    protected double modulate(int index)  {
        return Math.pow(-1, index);
    }

    @Override
    protected int getK(double sampleFrequency, double frequency) {
        return (int) (sampleFrequency / (sampleFrequency / 2.0 - frequency));

    }
}
