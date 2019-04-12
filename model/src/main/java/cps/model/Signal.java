package cps.model;

import lombok.Getter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

//TODO: Zrobic interfejs funkcyjny
public class Signal {

<<<<<<< HEAD
    public enum Type {
        CONTINUOUS, DISCRETE
    }

    @Getter
    private Type type;

    //TODO: Push from mac and merge changes

    @Getter
    @Setter
    private Duration duraionInNs;

    @Getter
    @Setter
    private Duration samplingPeriodNs;

    @Getter
    private List<Double> samples;
=======
    @Getter private Type type;
    @Getter private Duration durationInNs;
    @Getter private Duration samplingPeriod;
    @Getter private List<Double> samples;
>>>>>>> 5b0c07f8caa829c006b655b04b5a825cf1fc5bcb

    public Signal(Type type, Duration durationInNs, Duration samplingPeriod, List<Double> samples) {
        this.type = type;
        this.durationInNs = durationInNs;
        this.samplingPeriod = samplingPeriod;
        this.samples = samples;
    }

    public static Signal create(Type type, Function<Double, Double> function, Duration durationInNs, Duration samplingPeriodInNs) {

        if (type == Type.CONTINUOUS) {
            return createContinousSignal(function, durationInNs, samplingPeriodInNs);
        } else {
            return createDiscreteSignal(function, durationInNs, samplingPeriodInNs);
        }

    }

    private static Signal createContinousSignal(Function<Double, Double> function, Duration durationInNs, Duration samplingPeriodInNs) {
        Duration time = Duration.ZERO;

        List<Double> samples = new ArrayList<>();

        while (time.compareTo(durationInNs) <= 0) {
            double timeInNs = time.toNanos();
            samples.add(function.apply(timeInNs));
            time = time.plus(samplingPeriodInNs);
        }

        return new Signal(Type.CONTINUOUS, durationInNs, samplingPeriodInNs, samples);
    }

    private static Signal createDiscreteSignal(Function<Double, Double> function, Duration durationInNs, Duration samplingPeriodInNs) {

        long size = durationInNs.toNanos() / samplingPeriodInNs.toNanos();
        List<Double> samples = LongStream.range(0, size).mapToObj(n -> function.apply((double) n)).collect(Collectors.toList());
        return new Signal(Type.DISCRETE, durationInNs, samplingPeriodInNs, samples);
    }

    public enum Type {
        CONTINUOUS, DISCRETE
    }

}
