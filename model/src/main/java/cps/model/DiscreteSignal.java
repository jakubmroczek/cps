package cps.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class DiscreteSignal extends Signal {

    @Getter
    @Setter
    private List<Double> samples;
    private Function<Long, Double> function;

    public DiscreteSignal(Function<Long, Double> function) {
        super(Type.DISCRETE, null);
        this.function = function;
    }

    @Override public double calculate(Duration duration) {
        throw new UnsupportedOperationException("Discrete signal does not support this method.");
    }

    @Override public SignalChart createChart(Duration duration, Duration probingPeriod) {
        //Floor -> takes only the full multiple of duration
        //TODO: Exception handling
        long size = duration.toNanos() / probingPeriod.toNanos();
        samples = LongStream.range(0, size).mapToObj(n -> function.apply(n)).collect(Collectors.toList());
        return new SignalChart(duration, probingPeriod, samples);
    }
}
