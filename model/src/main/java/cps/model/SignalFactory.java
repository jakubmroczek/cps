package cps.model;

import com.sun.org.apache.regexp.internal.RE;

import java.time.Duration;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.lang.Math.*;
import static java.lang.Math.ceil;
import static java.lang.Math.floor;

public class SignalFactory {

    public static final String LINEARLY_DISTRIBUTED_NOISE = "S1";
    public static final String GAUSSIAN_NOISE = "S2";
    public static final String SINUSOIDAL = "S3";
    public static final String HALF_STRAIGHT_SINUSOIDAL = "S4";
    public static final String FULL_STRAIGHT_SINUSOIDAL = "S5";
    public static final String RECTANGLE = "S6";
    public static final String SYMETRIC_RECTANGLE = "S7";
    public static final String TRIANGLE = "S8";
    public static final String UNIT_STEP = "S9";
    public static final String KRONECKER_DELTA = "S10";

    //TODO: Ensure duration units!!!!!!
    public static Signal createSignal(String signal, SignalArgs args) {
        switch (signal)
        {
            case LINEARLY_DISTRIBUTED_NOISE:
                return getLinearlyDistibutedNoise(args.getAmplitude(), args.getInitialTime());

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

            case UNIT_STEP:
                return createUnitStep(args.getAmplitude(), args.getInitialTime());

            case KRONECKER_DELTA:
                return createKroneckerDelta(args.getAmplitude(), args.getNs(), args.getSamplingFrequency());

            default:
                throw new UnsupportedOperationException(signal + " unknown signal type");
        }
    }

    //TODO: Add inititialTime
    private static Signal getLinearlyDistibutedNoise(double amplitude, Duration initialTime) {
        Function<Duration, Double> function = duration -> {
            Random random = new Random();
            return sqrt(12.0) * (((random.nextInt() % 101) - 50.0) / 100.0) + amplitude;
        };
        return new Signal(Signal.Type.CONTINUOUS, function);
    }

    //TODO: Add inititialTime
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

    private static Signal createUnitStep(double amplitude, Duration initialTime) {
        Function<Duration, Double> function = duration -> {
            int compareToResult = duration.compareTo(initialTime);
            if (compareToResult > 0) {
                return amplitude;
            } else if(compareToResult < 0) {
                return 0.0;
            } else {
                return 0.5 * amplitude;
            }
        };

        return new Signal(Signal.Type.CONTINUOUS, function);
    }

    // Ns przesunieci numeru probki dla skoku jednostkowego
    //TODO: ?
    private static final Signal createKroneckerDelta(double amplitude, int Ns, Duration samplingFrequency) {
        Function<Integer, Double> kroneckerDelta = n -> n - Ns == 0 ? amplitude : 0;
        Function<Duration, Double> function = duration -> {
                        
        };
    }

    private SignalFactory() {
    }

}
