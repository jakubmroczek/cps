package cps;

import org.apache.commons.math3.complex.Complex;
import cps.model.*;

import javax.management.RuntimeErrorException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tmp {

    public static Signal<Complex> getTestSignal() {
        var args = SignalArgs.builder().amplitude(1.0)
                .periodInNs(1_000_000_000.0).build();
        var function = FunctionFactory.createFunction(FunctionFactory.SINUSOIDAL, args);

        List<Complex> samples = new ArrayList<>();
        var step = Duration.ofMillis(1);
        var time = Duration.ZERO;

        while (time.compareTo(Duration.ofSeconds(1)) <= 0) {
            double re = function.apply((double) time.toNanos());
            double im = re;
            Complex complex = new Complex(re, im);
            samples.add(complex);
            time = time.plus(step);
        }
        Signal<Complex> signal = new Signal<>(Signal.Type.CONTINUOUS, Duration.ofSeconds(1), Duration.ofMillis(1), samples);
        return signal;
    }

    public static Signal<Double> getTestDoubleSignal() {
        List<Double> samples = Arrays.asList(
                1.0,
                2.0,
                3.0,
                4.0,
                5.0,
                6.0,
                7.0,
                8.0,
                9.0,
                10.0
//                0.0,
//                0.0,
//                0.0,
//                0.0,
//                0.0,
//                0.0,
//                0.0,
//                0.0,
//                0.0,
//                0.0,
//                0.0,
//                0.0,
//                0.0,
//                0.0,
//                0.0,
//                0.0,
//                0.0,
//                0.0,
//                0.0,
//                0.0,
//                0.0,
//                0.0,
//                0.0,
//                0.0,
//                0.0,
//                0.0,
//                0.0,
//                0.0,
//                0.0,
//                0.0
        );
        if (samples.size() != 10) {
            throw new RuntimeException("nie rowne 32");
        }
        return new Signal<>(Signal.Type.CONTINUOUS, Duration.ofSeconds(1), Duration.ofMillis(1), samples);
    }
}