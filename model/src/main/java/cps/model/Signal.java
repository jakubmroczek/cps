package cps.model;

import lombok.Getter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.w3c.dom.css.DOMImplementationCSS;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Function;

class DiscreteSignal extends Signal {

    public DiscreteSignal(Function<Duration, Double> function) {
        super(Type.DISCRETE, null);
    }


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
