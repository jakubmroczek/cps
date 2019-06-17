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

//    private static Complex[] FFT(Complex[] values) {
//        int N = values.length;
//        if (N == 1) {
//            return values;
//        }
//        int M = N / 2;
//        Complex[] even = new Complex[M];
//        Complex[] odd = new Complex[M];
//        for (int i = 0; i < M; i++) {
//            even[i] = values[2 * i];
//            odd[i] = values[2 * i + 1];
//        }
//        Complex[] Feven = FFT(even);
//        Complex[] Fodd = FFT(odd);
//
//        Complex[] res = new Complex[N];
//        for (int k = 0; k < N / 2; k++) {
//            double angle = -Math.PI * k * 2.0 / (N * 1.0);
//            Complex exp = new Complex(Math.cos(angle), Math.sin(angle)).multiply(Fodd[k]);
//            res[k] = Feven[k].add(exp);
//            res[k + N / 2] = Feven[k].subtract(exp);
//        }
//
//        return res;
//    }

    public static int bitReverse(int n, int bits) {
        int reversedN = n;
        int count = bits - 1;

        n >>= 1;
        while (n > 0) {
            reversedN = (reversedN << 1) | (n & 1);
            count--;
            n >>= 1;
        }

        return ((reversedN << count) & ((1 << bits) - 1));
    }

    static void fft(Complex[] buffer) {

        int bits = (int) (log(buffer.length) / log(2));
        for (int j = 1; j < buffer.length / 2; j++) {

            int swapPos = bitReverse(j, bits);
            Complex temp = buffer[j];
            buffer[j] = buffer[swapPos];
            buffer[swapPos] = temp;
        }

        for (int N = 2; N <= buffer.length; N <<= 1) {
            for (int i = 0; i < buffer.length; i += N) {
                for (int k = 0; k < N / 2; k++) {

                    int evenIndex = i + k;
                    int oddIndex = i + k + (N / 2);
                    Complex even = buffer[evenIndex];
                    Complex odd = buffer[oddIndex];

                    double term = (-2 * PI * k) / (double) N;
                    Complex exp = (new Complex(cos(term), sin(term)).multiply(odd));

                    buffer[evenIndex] = even.add(exp);
                    buffer[oddIndex] = even.subtract(exp);
                }
            }
        }
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

        fft(values);

//        for (int i = 0; i < length; i++) {
//            values[i] = values[i].divide(length * 1.0);
//        }

        List<Complex> transformationResults = Arrays.asList(values);

        return new Signal<>(signal.getType(),
                signal.getSamplingPeriod(),
                signal.getSamplingPeriod().multipliedBy(length),
                transformationResults);
    }

    public static Signal<Double> ifft(Signal<Complex> signal) {
        throw new UnsupportedOperationException("not implemented");
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

    public static Signal<Complex> fastDCT(Signal<Double> signal) {
        throw new UnsupportedOperationException("not implemented");
    }

    public static Signal<Double> fastIDCT(Signal<Complex> signal) {
        throw new UnsupportedOperationException("not implemented");
    }
}