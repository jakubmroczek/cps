package cps.model;

import lombok.Getter;
import lombok.Setter;
import org.jfree.ui.about.resources.AboutResources;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

//TODO: Zrobic interfejs funkcyjny
public class Signal {

    public enum Type {
        CONTINUOUS, DISCRETE
    }

    @Getter
    private Type type;

    @Getter
    private List<Double> samples;

    public Signal(Type type, List<Double> samples) {
        this.type = type;
        this.samples = samples;
    }

    public static Signal create(Type type,
                                Function<Double, Double> function,
                               Duration duration,
                               Duration samplingFrequency) {

        Duration time = Duration.ZERO;

        List<Double> samples = new ArrayList<>();

        while (time.compareTo(duration) <= 0) {
            double timeInNs = time.toNanos();
            samples.add(function.apply(timeInNs));
            time = time.plus(samplingFrequency);
        }

        return new Signal(type, samples);
    }
}
