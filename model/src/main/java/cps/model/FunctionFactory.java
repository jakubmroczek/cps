package cps.model;

import java.util.Random;
import java.util.function.Function;

import static java.lang.Math.*;

public class FunctionFactory {

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
    public static final String IMPULSE_NOISE = "S11";

    private FunctionFactory() {
    }

    //TODO: Ensure duration units!!!!!!
    public static Function<Double, Double> createFunction(String function, SignalArgs args) {
        switch (function) {
            case LINEARLY_DISTRIBUTED_NOISE:
                return createLinearlyDistributedNoise(args.getAmplitude());

            case GAUSSIAN_NOISE:
                return createGaussianNoise(args.getAmplitude());

            case SINUSOIDAL:
                return createSinusoidal(args.getAmplitude(), args.getPeriodInNs(), args.getInitialTimeInNs());

            case HALF_STRAIGHT_SINUSOIDAL:
                return createHalfStraightSinusoidal(args.getAmplitude(), args.getPeriodInNs(), args.getInitialTimeInNs());

            case FULL_STRAIGHT_SINUSOIDAL:
                return createFullStraightSinusoidal(args.getAmplitude(), args.getPeriodInNs(), args.getInitialTimeInNs());

            case RECTANGLE:
                return createRectangleFunction(args.getAmplitude(), args.getPeriodInNs(), args.getInitialTimeInNs(), args.getKw());

            case SYMETRIC_RECTANGLE:
                return createSymmetricRectangleFunction(args.getAmplitude(), args.getPeriodInNs(), args.getInitialTimeInNs(), args.getKw());

            case TRIANGLE:
                return createTriangleFunction(args.getAmplitude(), args.getPeriodInNs(), args.getInitialTimeInNs(), args.getKw());

            case UNIT_STEP:
                return createUnitStep(args.getAmplitude(), args.getInitialTimeInNs());

            case KRONECKER_DELTA:
                return createKroneckerDelta(args.getAmplitude(), args.getNs());

            case IMPULSE_NOISE:
                return createImpulseNoise(args.getAmplitude(), args.getProbability());
            default:
                throw new IllegalArgumentException(function + " unknown function");
        }
    }

    private static Function<Double, Double> createLinearlyDistributedNoise(double amplitude) {
        return x -> {
            Random random = new Random();
            return amplitude * (random.nextDouble() * 2.0 - 1.0);
        };
    }

    private static Function<Double, Double> createGaussianNoise(double amplitude) {
        return x -> {
            Random random = new Random();
            return amplitude * random.nextGaussian();
        };
    }

    private static Function<Double, Double> createSinusoidal(double amplitude, double period, double initialTime) {
        return x -> {
            double angleVelocity = 2.0 * PI / period;
            double argument = x - initialTime;
            return amplitude * sin(angleVelocity * argument);
        };
    }

    private static Function<Double, Double> createHalfStraightSinusoidal(double amplitude, double period, double initialTime) {
        return x -> {
            double angleVelocity = 2.0 * PI / period;
            double argument = x - initialTime;
            double left = sin(angleVelocity * argument);
            double right = abs(left);
            return 0.5 * amplitude * (left + right);
        };
    }

    private static Function<Double, Double> createFullStraightSinusoidal(double amplitude, double period, double initialTime) {
        return x -> {
            double angleVelocity = 2.0 * PI / period;
            double argument = x - initialTime;
            return amplitude * abs(sin(angleVelocity * argument));
        };
    }

    private static Function<Double, Double> createRectangleFunction(double amplitude, double period, double initialTime, double kw) {
        return x -> {

            double kMax = (x - initialTime) / period;
            double kMin = kMax - kw;

            double integer = ceil(kMin);

            return integer >= kMin && integer < kMax ? amplitude : 0;
        };
    }

    private static Function<Double, Double> createSymmetricRectangleFunction(double amplitude, double period, double initialTime,
            double kw) {
        return x -> {
            double kMax = (x - initialTime) / period;
            double kMin = kMax - kw;

            double integer = ceil(kMin);

            return integer >= kMin && integer < kMax ? amplitude : -amplitude;
        };
    }

    private static Function<Double, Double> createTriangleFunction(double amplitude, double period, double initialTime, double kw) {
        return x -> {
            double kMax = (x - initialTime) / period;
            double kMin = kMax - kw;

            double integer = ceil(kMin);

            if (integer >= kMin && integer < kMax) {
                return (amplitude / (kw * period)) * (x - integer * period - initialTime);
            } else {
                integer = floor(kMax - kw);
                return (-amplitude / (period * (1.0 - kw))) * (x - integer * period - initialTime) + (amplitude / (1.0 - kw));
            }
        };
    }

    private static Function<Double, Double> createUnitStep(double amplitude, double initialTime) {
        return x -> {
            if (x > initialTime) {
                return amplitude;
            } else if (x < initialTime) {
                return 0.0;
            } else {
                return 0.5 * amplitude;
            }
        };
    }

    private static Function<Double, Double> createKroneckerDelta(double amplitude, int Ns) {
        return n -> n - Ns == 0 ? amplitude : 0;
    }

    private static Function<Double, Double> createImpulseNoise(double amplitude, double probability) {
        return n -> {
            Random random = new Random();
            double threshold = random.nextDouble();
            if (threshold >= 0 && threshold <= probability) {
                return amplitude;
            } else {
                return 0.0;
            }
        };
    }

}
