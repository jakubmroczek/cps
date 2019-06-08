package cps.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class Signal<T> {

    public enum Type {
        CONTINUOUS, DISCRETE
    }

    @Setter
    @Getter private Type type;
    @Getter private Duration durationInNs;
    @Getter private Duration samplingPeriod;
    @Getter private List<T> samples;

    //TODO: Investigate if can be protected
    public Signal() {
    }

    public Signal(Signal<T> other) {
        this.type = other.type;
        this.durationInNs = other.durationInNs;
        this.samplingPeriod = other.samplingPeriod;
        this.samples = new ArrayList<>();
        this.samples.addAll(other.samples);
    }

    public Signal(Type type, Duration durationInNs, Duration samplingPeriod, List<T> samples) {
        this.type = type;
        this.durationInNs = durationInNs;
        this.samplingPeriod = samplingPeriod;
        this.samples = samples;
    }

    public static<T>  Signal<T> create(Type type, Function<Double, T> function, Duration durationInNs, Duration samplingPeriodInNs) {

        if (type == Type.CONTINUOUS) {
            return createContinousSignal(function, durationInNs, samplingPeriodInNs);
        } else {
            return createDiscreteSignal(function, durationInNs, samplingPeriodInNs);
        }

    }

    public static<T> Signal<T> createContinousSignal(Function<Double, T> function, Duration durationInNs, Duration samplingPeriodInNs) {
        Duration time = Duration.ZERO;

        List<T> samples = new ArrayList<>();

        while (time.compareTo(durationInNs) <= 0) {
            double timeInNs = time.toNanos();
            samples.add(function.apply(timeInNs));
            time = time.plus(samplingPeriodInNs);
        }

        return new Signal<>(Type.CONTINUOUS, durationInNs, samplingPeriodInNs, samples);
    }

    public static<T> Signal<T> createDiscreteSignal(Function<Double, T> function, Duration durationInNs, Duration samplingPeriodInNs) {

        long size = durationInNs.toNanos() / samplingPeriodInNs.toNanos();
        var samples = LongStream.range(0, size).mapToObj(n -> function.apply((double) n)).collect(Collectors.toList());
        return new Signal<>(Type.DISCRETE, durationInNs, samplingPeriodInNs, samples);
    }

}
