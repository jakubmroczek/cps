package cps.transform;

import org.apache.commons.math3.complex.Complex;
import cps.model.Signal;
import org.jfree.xml.factory.objects.DateObjectDescription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

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

            re /= N;
            im /= N;

            transformedSamples.add(new Complex(re, im));
        }

        return new Signal<>(signal.getType(),
                signal.getSamplingPeriod(),
                signal.getSamplingPeriod().multipliedBy(N),
                transformedSamples);
    }

    public static Signal<Double> idft(Signal<Complex> signal) {
        List<Double> transformationResults = new ArrayList<>(signal.getSamples().size());
        var N = signal.getSamples().size();

        for (int m = 0; m < N; m++) {
            var resultSample = 0.0;
            for (int n = 0; n < N; n++) {
                double angle = 2 * Math.PI * n * m / N;
                resultSample +=
                        signal.getSamples().get(n).getReal() * cos(angle)
                                - signal.getSamples().get(n).getImaginary() * sin(angle);
            }
            transformationResults.add(resultSample);
        }

        var frequency = signal.getSamplingPeriod().dividedBy(N);

        return new Signal<>(signal.getType(),
                signal.getDurationInNs().multipliedBy(N),
                frequency,
                transformationResults);
    }

    private static Complex[] fft(Complex[] values) {
        int N = values.length;
        if (N == 1) {
            return values;
        }
        int M = N / 2;
        Complex[] even = new Complex[M];
        Complex[] odd = new Complex[M];
        for (int i = 0; i < M; i++) {
            even[i] = values[2 * i];
            odd[i] = values[2 * i + 1];
        }
        Complex[] Feven = fft(even);
        Complex[] Fodd = fft(odd);

        Complex[] res = new Complex[N];
        for (int k = 0; k < N / 2; k++) {
            double angle = -Math.PI * k * 2.0 / (N * 1.0);
            Complex exp = new Complex(Math.cos(angle), Math.sin(angle)).multiply(Fodd[k]);
            res[k] = Feven[k].add(exp);
            res[k + N / 2] = Feven[k].subtract(exp);
        }

        return res;
    }

    public static Signal<Complex> fft(Signal<Double> signal) {
//        assert samples.size() % 2 == 0;
//
//        int N = samples.size();
//        if (N == 1) {
//            return Arrays.asList(
//                    new Complex(samples.get(0), 0.0)
//            );
//        }
//        int M = N / 2;
//
//        List<Double> even = new ArrayList<>();
//        List<Double> odd = new ArrayList<>();
//        for (int i = 0; i < M; i++) {
//            even.add(samples.get(2 * i));
//            odd.add(samples.get(2 * i + 1));
//        }
//        var Feven = fft(even);
//        var Fodd = fft(odd);
//
//        List<Complex> res = new ArrayList<>();
//        for (int i = 0; i < N; i++) {
//            res.add(new Complex(0.0, 0.0));
//        }
//        for (int k = 0; k < N / 2; k++) {
//            double angle = -Math.PI * k * 2.0 / (N * 1.0);
//            Complex exp = new Complex(Math.cos(angle), Math.sin(angle)).multiply(Fodd.get(k));
//            res.set(k, Feven.get(k).add(exp));
//            res.set(k + N / 2, Feven.get(k).subtract(exp));
//        }
//
//        return res;

        int length = signal.getSamples().size();
        int bits = (int) Math.ceil((Math.log(length) / Math.log(2)));
        length = (int) Math.pow(2, (bits));
        Complex[] values = new Complex[length];
        for (int i = 0; i < signal.getSamples().size(); i++) {
            values[i] = new Complex(signal.getSamples().get(i), 0.0);
        }
        for (int i = signal.getSamples().size(); i < length; i++) {
            values[i] = new Complex(0.0, 0.0);
        }

        values = fft(values);

        double[] realValues = new double[length];
        double[] imaginaryValues = new double[length];
        for (int i = 0; i < length; i++) {
            realValues[i] = values[i].getReal() / (length * 1.0);
            imaginaryValues[i] = values[i].getImaginary() / (length * 1.0);
        }


        List<Complex> transformationResults = Arrays.asList(values);

        return new Signal<>(signal.getType(),
                signal.getSamplingPeriod(),
                signal.getSamplingPeriod().multipliedBy(length),
                transformationResults);
    }

    public static Signal<Double> ifft(Signal<Complex> signal) {
        throw new UnsupportedOperationException("not implemented");
    }

    public static Signal<Complex> dct(Signal<Double> signal)
    {
        List<Complex> transformedSamples = new ArrayList<>();
        var N = signal.getSamples().size();

        double c = Math.PI / (2.0 * N);
        double scale = Math.sqrt(2.0 / N);

        for (int m = 0; m < N; m++)
        {
            double re = 0;
            double im = 0;

            for (int n = 0; n < N; n++){
                re += signal.getSamples().get(n) * Math.cos((2.0 * n + 1.0) * m * c);
                im += signal.getSamples().get(n) * Math.sin((2.0 * n + 1.0) * m * c); // ?? to zle raczej jest, nie wiem jak powinno byc
            }
            transformedSamples.add(new Complex(scale * re, scale * im));
        }

        return new Signal<>(signal.getType(),
                signal.getSamplingPeriod(),
                signal.getSamplingPeriod().multipliedBy(N),
                transformedSamples);
    }

    public static Signal<Double> idct(Signal<Complex> signal)
    {
        List<Double> transformationResults = new ArrayList<>(signal.getSamples().size());
        var N = signal.getSamples().size();

        double c = Math.PI / (2.0 * N);
        double scale = Math.sqrt(2.0 / N);

        for (int k = 0; k < N; k++)
        {
            double sum = signal.getSamples().get(0).getReal() / Math.sqrt(2.0);
            for (int n = 1; n < N; n++)
                sum += signal.getSamples().get(n).getReal() * Math.cos((2 * k + 1) * n * c);
                        //- signal.getSamples().get(n).getImaginary() * Math.sin((2 * k + 1) * n * c); // ?? Im psuje inversje

            transformationResults.add(scale * sum);
        }

        var frequency = signal.getSamplingPeriod().dividedBy(N);

        return new Signal<>(signal.getType(),
                signal.getDurationInNs().multipliedBy(N),
                frequency,
                transformationResults);
    }

    public static Signal<Complex> fastDCT(Signal<Double> signal) {
        throw new UnsupportedOperationException("not implemented");
    }

    public static Signal<Double> fastIDCT(Signal<Complex> signal) {
        throw new UnsupportedOperationException("not implemented");
    }
}