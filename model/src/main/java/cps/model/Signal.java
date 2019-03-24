package cps.model;

import lombok.Getter;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

class DiscreteSignal extends Signal {

    public DiscreteSignal(Function<Long, Double> function) {
        super(Type.DISCRETE, null);
        this.function = function;
    }

    @Override
    public double calculate(Duration duration) {
        throw new UnsupportedOperationException("Discrete signal does not support this method.");
    }

    @Override
    public SignalChart createChart(Duration duration, Duration probingPeriod) {
        //Floor the probing period
        long size = duration.dividedBy(probingPeriod);
        var samples = LongStream.range(0, size).mapToObj(n -> function.apply(n)).collect(Collectors.toList());
        return new SignalChart(duration, probingPeriod, samples);
    }

    private Function<Long, Double> function;
}

//TODO: Zrobic interfejs funkcyjny
public class Signal  {

    @Getter
    private Function<Duration, Double> function;

    @Getter
    private Type type;

    enum Type {
        CONTINUOUS,
        DISCRETE
    }

    public Signal(Type type, Function<Duration, Double> function) {
        this.type = type;
        this.function = function;
    }

    //TODO: Adnotacje notnull
    public double calculate(Duration duration) {
        return function.apply(duration);
    }

    //TODO: To chyba nie jed odpowidzialnosc sygnalu, przeniesc gdzies indziej
    public SignalChart createChart(Duration duration, Duration probingPeriod) {
        Duration time = Duration.ZERO;
        List<Double> samples = new ArrayList<>();

        int i=0;
        while (time.compareTo(duration) <= 0) {
            double val = calculate(time);
            samples.add(val);
            time = time.plus(probingPeriod);

            double x = i++ * probingPeriod.toMillis();
            double y = val;
        }
        return new SignalChart(duration, probingPeriod, samples);
    }

}
