package cps.model;

import lombok.Getter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

//TODO: Zrobic interfejs funkcyjny
public class Signal  {

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
        while (time.compareTo(duration) <= 0) {
            double val = calculate(time);
            samples.add(val);
            time = time.plus(probingPeriod);
        }
        return new SignalChart(duration, probingPeriod, samples);
    }

    @Getter
    private Function<Duration, Double> function;

    @Getter
    private Type type;
}
