package cps.transform;

import cps.model.Signal;
import org.apache.commons.math3.complex.Complex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.*;

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
        int length = signal.getSamples().size();
        int bits = (int) Math.ceil((log(length) / log(2)));
        length = (int) Math.pow(2, (bits));
        Complex[] values = new Complex[length];
        for (int i = 0; i < signal.getSamples().size(); i++) {
            values[i] = new Complex(signal.getSamples().get(i), 0.0);
        }
        for (int i = signal.getSamples().size(); i < length; i++) {
            values[i] = new Complex(0.0, 0.0);
        }

        values = fft(values);

        for (int i = 0; i < length; i++) {
            values[i] = values[i].divide(length * 1.0);
        }

        List<Complex> transformationResults = Arrays.asList(values);

        return new Signal<>(signal.getType(),
                signal.getSamplingPeriod(),
                signal.getSamplingPeriod().multipliedBy(length),
                transformationResults);
    }
    private static Complex[] ifft(Complex[] values) {
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
        Complex[] Feven = ifft(even);
        Complex[] Fodd = ifft(odd);

        Complex[] res = new Complex[N];
        for (int k = 0; k < N / 2; k++) {
            double angle = Math.PI * k * 2.0 / (N * 1.0);
            Complex exp = new Complex(Math.cos(angle), Math.sin(angle)).multiply(Fodd[k]);
            res[k] = Feven[k].add(exp);
            res[k + N / 2] = Feven[k].subtract(exp);
        }

        return res;
    }

    public static Signal<Double> ifft(Signal<Complex> signal) {
        int length = signal.getSamples().size();
        int bits = (int) Math.ceil((Math.log(length) / Math.log(2)));
        length = (int) Math.pow(2, (bits));
        Complex[] values = new Complex[length];
        for (int i = 0; i < signal.getSamples().size(); i++) {
            values[i] = new Complex(signal.getSamples().get(i).getReal(), signal.getSamples().get(i).getImaginary());
        }
        for (int i = signal.getSamples().size(); i < length; i++) {
            values[i] = new Complex(0.0, 0.0);
        }

        values = ifft(values);

        double[] realValues = new double[length];
        for (int i = 0; i < length; i++) {
            realValues[i] = values[i].getReal();
        }

        var frequency = signal.getSamplingPeriod().dividedBy(length);
        List<Double> transformationResults = new ArrayList<>(length);
        for (var c : values) {
            transformationResults.add(c.getReal());
        }

        return new Signal<Double>(signal.getType(),
                signal.getDurationInNs().multipliedBy(length),
                frequency,
                transformationResults);
    }

    public static Signal<Double> dct(Signal<Double> signal) {
        List<Double> transformedSamples = new ArrayList<>();
        var N = signal.getSamples().size();

        for (int m = 0; m < N; m++) {
            double c;
            if (m != 0) {
                c = sqrt(2.0 / N);
            } else {
                c = sqrt(1.0 / N);
            }

            double result = 0.0;

            for (int n = 0; n < N; n++) {
                result += signal.getSamples().get(n) * cos((Math.PI * (2.0 * n + 1.0) * m) / (2.0 * N));
            }
            transformedSamples.add(c * result);
        }

        return new Signal<>(signal.getType(),
                signal.getDurationInNs(),
                signal.getSamplingPeriod(),
                transformedSamples);
    }

    public static Signal<Double> idct(Signal<Double> signal) {
        List<Double> transformedSamples = new ArrayList<>(signal.getSamples().size());
        var N = signal.getSamples().size();

        for (int n = 0; n < N; n++) {
            double result = 0.0;
            for (int m = 0; m < N; m++) {
                double c;
                if (m != 0) {
                    c = sqrt(2.0 / N);
                } else {
                    c = sqrt(1.0 / N);
                }
                result += c * signal.getSamples().get(m) * cos((Math.PI * (2.0 * n + 1.0) * m) / (2.0 * N));
            }
            transformedSamples.add(result);
        }

        return new Signal<>(signal.getType(),
                signal.getDurationInNs(),
                signal.getSamplingPeriod(),
                transformedSamples);
    }

    public static void transform(double[] vector) {
        final int len = vector.length;
        int halfLen = len / 2;
        double[] real = new double[len];
        for (int i = 0; i < halfLen; i++) {
            real[i] = vector[i * 2];
            real[len - 1 - i] = vector[i * 2 + 1];
        }
        if (len % 2 == 1)
            real[halfLen] = vector[len - 1];
        Arrays.fill(vector, 0.0);

        Complex[] complexes = new Complex[len];
        for (int i =0 ; i < len; i++) {
            complexes[i] = new Complex(real[i], 0.0);
        }

        Complex[] result = fft(complexes);

        for (int i = 0; i < len; i++) {
            double c;
            if (i != 0) {
                c = sqrt(2.0 / len);
            } else {
                c = sqrt(1.0 / len);
            }

            double temp = i * Math.PI / (len * 2);
            vector[i] = c * (result[i].getReal() * Math.cos(temp) + vector[i] * Math.sin(temp));
        }
    }

    public static Signal<Double> fastDCT(Signal<Double> signal) {
        int length = signal.getSamples().size();
        int bits = (int) Math.ceil((log(length) / log(2)));
        length = (int) Math.pow(2, (bits));
        double[] values = new double[length];
        for (int i = 0; i < signal.getSamples().size(); i++) {
            values[i] = signal.getSamples().get(i);
        }
        for (int i = signal.getSamples().size(); i < length; i++) {
            values[i] = 0.0;
        }

        transform(values);

        List<Double> transformResult = new ArrayList<>();
        for (var v : values) {
            transformResult.add(v);
        }

        return new Signal<>(signal.getType(),
                signal.getDurationInNs(),
                signal.getSamplingPeriod(),
                transformResult);
    }

    public static void inverseTransform(double[] vector) {
        int len = vector.length;
        if (len > 0)
            vector[0] /= 2;
        double[] real = new double[len];
        for (int i = 0; i < len; i++) {
            double temp = i * Math.PI / (len * 2);
            real[i] = vector[i] * Math.cos(temp);
            vector[i] *= -Math.sin(temp);
        }

        Complex[] complexes = new Complex[len];
        for (int i =0 ; i < len; i++) {
            complexes[i] = new Complex(real[i], 0.0);
        }

        Complex[] result = fft(complexes);

        int halfLen = len / 2;
        for (int i = 0; i < halfLen; i++) {
            vector[i * 2 + 0] = result[i].getReal();
            vector[i * 2 + 1] = result[len - 1 - i].getReal();
        }
        if (len % 2 == 1)
            vector[len - 1] = result[halfLen].getReal();
    }

    public static Signal<Double> fastIDCT(Signal<Double> signal) {
        int length = signal.getSamples().size();
        int bits = (int) Math.ceil((log(length) / log(2)));
        length = (int) Math.pow(2, (bits));
        double[] values = new double[length];
        for (int i = 0; i < signal.getSamples().size(); i++) {
            values[i] = signal.getSamples().get(i);
        }
        for (int i = signal.getSamples().size(); i < length; i++) {
            values[i] = 0.0;
        }

        inverseTransform(values);

        List<Double> transformResult = new ArrayList<>();
        for (var v : values) {
            transformResult.add(v);
        }

        return new Signal<>(signal.getType(),
                signal.getDurationInNs(),
                signal.getSamplingPeriod(),
                transformResult);
    }
}