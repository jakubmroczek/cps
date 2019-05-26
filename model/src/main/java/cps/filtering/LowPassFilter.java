package cps.filtering;

public class LowPassFilter extends FIRFilter {
    @Override
    protected double modulate(int index) {
        return 1.0;
    }

    @Override
    protected int getK(double sampleFrequency, double frequency)  {
        return (int) (sampleFrequency / frequency);
    }
}
