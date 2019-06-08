package cps.model;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.abs;

public class SignalOperations {

    private static void validate(Signal lhs, Signal rhs) {
        //TODO: Change to exception
        assert lhs.getType().equals(rhs.getType());
        assert lhs.getDurationInNs().equals(rhs.getDurationInNs());
        assert lhs.getSamplingPeriod().equals(rhs.getSamplingPeriod());
        assert lhs.getSamples().size() == rhs.getSamples().size();
    }

    public static Signal<Double> add(Signal<Double> lhs, Signal<Double> rhs) {
        validate(lhs, rhs);

        int size = lhs.getSamples().size();
        List<Double> resultSamples = IntStream.range(0, size)
                                              .mapToObj(index -> lhs.getSamples().get(index) + rhs.getSamples().get(index))
                                              .collect(Collectors.toList());

        return new Signal<>(lhs.getType(), lhs.getDurationInNs(), lhs.getSamplingPeriod(), resultSamples);
    }

    public static Signal<Double> subtract(Signal<Double> lhs, Signal<Double> rhs)
    {
        validate(lhs, rhs);

        int size = lhs.getSamples().size();
        List<Double> resultSamples = IntStream.range(0, size)
                                              .mapToObj(index -> lhs.getSamples().get(index) - rhs.getSamples().get(index))
                                              .collect(Collectors.toList());

        return new Signal<>(lhs.getType(), lhs.getDurationInNs(), lhs.getSamplingPeriod(), resultSamples);
    }

    public static Signal<Double> multiply(Signal<Double> lhs, Signal<Double> rhs) {
        validate(lhs, rhs);

        int size = lhs.getSamples().size();
        List<Double> resultSamples = IntStream.range(0, size)
                                              .mapToObj(index -> lhs.getSamples().get(index) * rhs.getSamples().get(index))
                                              .collect(Collectors.toList());

        return new Signal<>(lhs.getType(), lhs.getDurationInNs(), lhs.getSamplingPeriod(), resultSamples);
    }

    public static Signal<Double> divide(Signal<Double> lhs, Signal<Double> rhs) {
        validate(lhs, rhs);
        Signal inversion = inverse(rhs);
        return multiply(lhs, inversion);
    }

    private static Signal<Double> inverse(Signal<Double> instance) {
        final double ZERO = 10e-9;
        List<Double> resultSamples = instance.getSamples().stream().map(x -> abs(x) < ZERO ? 0.0 : 1.0 / x).collect(Collectors.toList());
        return new Signal<>(instance.getType(), instance.getDurationInNs(), instance.getSamplingPeriod(), resultSamples);

    }

}
