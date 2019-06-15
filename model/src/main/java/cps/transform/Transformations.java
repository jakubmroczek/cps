package cps.transform;

import org.apache.commons.math3.complex.Complex;
import cps.model.Signal;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Transformations {

    public static Signal<Complex> dft(Signal<Double> signal) {
        List<Complex> transformedSamples = new ArrayList<>();

        var N = signal.getSamples().size();

        for (int m = 0; m < N; m++) {
            double re = 0.0;
            double im = 0.0;

            for (int n = 0; n < N; n++) {
                double angle = -2 * Math.PI * n * m / N;

                re += signal.getSamples().get(n) * cos(angle);
                im += signal.getSamples().get(n) * sin(angle);
            }
            ;

            re /= N;
            im /= N;

            transformedSamples.add(new Complex(re, im));
            ;
        }

        return new Signal<>(signal.getType(),
                signal.getSamplingPeriod(),
                signal.getSamplingPeriod().dividedBy(N),
                transformedSamples);
    }

    public static Signal<Complex> fft(Signal<Double> signal) {
        throw new UnsupportedOperationException("not implemented");
    }

}