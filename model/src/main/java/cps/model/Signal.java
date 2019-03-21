package cps.model;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

//TODO: Zrobic interfejs funkcyjny
public class Signal  {

    public List<Double> signalSamples = new ArrayList<>();


    public Signal(Function<Duration, Double> function) {
        this.function = function;
    }

    //TODO: Adnotacje notnull
    public double calculate(Duration duration) {
        return function.apply(duration);
    }

    //TODO: To chyba nie jed odpowidzialnosc sygnalu, przeniesc gdzies indziej
    public SignalChart createChart(Duration duration, Duration probingPeriod) {
        Duration time = Duration.ofNanos(0);
        List<Double> samples = new ArrayList<>();

        while (time.compareTo(duration) <= 0) {
            double val = calculate(time);
            samples.add(val);
            signalSamples.add(val);
            time = time.plus(probingPeriod);
        }
        return new SignalChart(duration, probingPeriod, samples);
    }

    private Function<Duration, Double> function;
}
