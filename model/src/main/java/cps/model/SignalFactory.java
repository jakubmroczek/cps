package cps.model;

import java.time.Duration;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.lang.Math.*;
import static java.lang.Math.ceil;
import static java.lang.Math.floor;

public class SignalFactory {

    public static final String GAUSSIAN_NOISE = "S2";
    public static final String SINUSOIDAL = "S3";
    public static final String HALF_STRAIGHT_SINUSOIDAL = "S4";
    public static final String FULL_STRAIGHT_SINUSOIDAL = "S5";
    public static final String RECTANGLE = "S6";
    public static final String SYMETRIC_RECTANGLE = "S7";
    public static final String TRIANGLE = "S8";

    //TODO: Ensure duration units!!!!!!
    public static Signal createSignal(String signal, SignalArgs args) {
        switch (signal)
        {
            case GAUSSIAN_NOISE:
                return getGaussianNoise(args.getAmplitude());

            case SINUSOIDAL:
                return getSinusoidal(args.getAmplitude(),
                                    args.getPeriod(),
                                    args.getInitialTime());

            case HALF_STRAIGHT_SINUSOIDAL:
                return getHalfStraightSinusoidal(args.getAmplitude(),
                                                args.getPeriod(),
                                                args.getInitialTime());

            case FULL_STRAIGHT_SINUSOIDAL:
                return getFullStraightSinusoidal(args.getAmplitude(),
                        args.getPeriod(),
                        args.getInitialTime());

            case RECTANGLE:
                return getRectangleSignal(args.getAmplitude(),
                        args.getPeriod(),
                        args.getInitialTime(),
                        args.getKw());

            case SYMETRIC_RECTANGLE:
                return getSymetricRectangleSignal(args.getAmplitude(),
                        args.getPeriod(),
                        args.getInitialTime(),
                        args.getKw());

            case TRIANGLE:
                return getTriangleSignal(args.getAmplitude(),
                        args.getPeriod(),
                        args.getInitialTime(),
                        args.getKw());

            default:
                throw new UnsupportedOperationException(signal + " unknown signal type");
        }
    }

    private static Signal getGaussianNoise(double amplitude) {
        //generator ze strony przedmotu z wikampa

        /**
         * \param e expected value
         * \param v variance
         */
        BiFunction<Double, Double, Double> linearGenerator = (e, v) -> {
            Random random = new Random();
            return sqrt(12.0 * v) * (((random.nextInt() % 101) - 50.0) / 100.0) + e;
        };

        Function<Duration, Double> fun = duration -> {
            int n = 10;
            double x = 0.0;
            for (int i = 0; i < n; i++) {
                x += linearGenerator.apply(0.0, 1.0);
            }
            return x * sqrt(1.0 / (double) n) + amplitude;
        };

        return new Signal(Signal.Type.CONTINUOUS, fun);
    }

    private static Signal getSinusoidal(double amplitude, Duration period, Duration initialTime) {
        Function<Duration, Double> fun = duration -> {
            assert duration != null;
            double angleVelocity = 2.0 * PI / period.toNanos();
            Duration argument = duration.minus(initialTime);
            return amplitude * sin(angleVelocity * argument.toNanos());
        };
        return new Signal(Signal.Type.CONTINUOUS, fun);
    }

    private static Signal getHalfStraightSinusoidal(double amplitude, Duration period, Duration initialTime) {
        Function<Duration, Double> fun = duration -> {
            double angleVelocity = 2.0 * PI / period.toNanos();
            Duration argument = duration.minus(initialTime);
            double left = sin(angleVelocity * argument.toNanos());
            double right = abs(left);
            return 0.5 * amplitude * (left + right);
        };
        return new Signal(Signal.Type.CONTINUOUS, fun);
    }

    private static Signal getFullStraightSinusoidal(double amplitude, Duration period, Duration initialTime) {
        Function<Duration, Double> fun = duration -> {
            assert duration != null;
            double angleVelocity = 2.0 * PI / period.toNanos();
            Duration argument = duration.minus(initialTime);
            return amplitude * abs(sin(angleVelocity * argument.toNanos()));
        };
        return new Signal(Signal.Type.CONTINUOUS, fun);
    }

    private static Signal getRectangleSignal(double amplitude, Duration period, Duration initialTime, double kw) {
        assert period != null && period.toNanos() != 0;

        Function<Duration, Double> fun = duration -> {
            //TODO: Better description
            double coefficient = (duration.toNanos() - initialTime.toNanos()) / (double) period.toNanos();

            double kMax = coefficient;
            double kMin = coefficient - kw;

            double integer = ceil(kMin);

            return integer >= kMin && integer < kMax ? amplitude : 0;
        };

        return new Signal(Signal.Type.CONTINUOUS, fun);
    }

    private static Signal getSymetricRectangleSignal(double amplitude, Duration period, Duration initialTime, double kw) {
        assert period != null && period.toNanos() != 0;

        Function<Duration, Double> fun = duration -> {
            //TODO: Better description
            double coefficient = (duration.toNanos() - initialTime.toNanos()) / (double) period.toNanos();

            double kMax = coefficient;
            double kMin = coefficient - kw;

            double integer = ceil(kMin);

            return integer >= kMin && integer < kMax ? amplitude : -amplitude;
        };

        return new Signal(Signal.Type.CONTINUOUS, fun);
    }

    private static Signal getTriangleSignal(double amplitude, Duration period, Duration initialTime, double kw) {
        assert period != null && period.toNanos() != 0;

        Function<Duration, Double> fun = duration -> {
            //TODO: Better description
            double coefficient = (duration.toNanos() - initialTime.toNanos()) / (double) period.toNanos();

            double kMax = coefficient;
            double kMin = coefficient - kw;

            double integer = ceil(kMin);

            if (integer >= kMin && integer < kMax) {
                return (amplitude / (kw * period.toNanos())) * (duration.toNanos() - integer * period.toNanos() - initialTime.toNanos());
            } else {
                integer = floor(coefficient - kw);
                return (-amplitude / (period.toNanos() * (1.0 - kw))) * (duration.toNanos() - integer * period.toNanos() - initialTime.toNanos()) + (amplitude / (1.0 - kw));
            }
        };

        return new Signal(Signal.Type.CONTINUOUS, fun);
    }

    private SignalFactory() {
    }

}
