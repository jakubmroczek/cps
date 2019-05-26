package cps.filtering;

public class BandpassFilter extends FIRFilter {
    @Override
    protected double modulate(int index)  {
        return 2.0 * Math.sin(Math.PI * index / 2.0);
    }

    @Override
    protected int getK(double sampleFrequency, double frequency) {
        return (int) ((4 * sampleFrequency) / (sampleFrequency - 4 * frequency));
    }
}
